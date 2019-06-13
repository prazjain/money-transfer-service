package com.moneytransfer.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moneytransfer.dto.AccountDto;
import com.moneytransfer.dto.CreateAccountDto;
import com.moneytransfer.dto.CreateTransferDto;
import com.moneytransfer.dto.StatementDto;
import com.moneytransfer.dto.TransactionDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.service.AccountService;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.utils.DateFormats;
import com.moneytransfer.utils.ValidationMessages;

@RestController
public class AccountController {

	@Autowired
	private AccountService accountService;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private TransactionService transactionService;
	
	/**
	 * This method is only for testing purposes, to use when application is running and enquire the data in database.
	 * This is not needed for original spec in test.
	 * @param id
	 * @return
	 */
	@GetMapping(path="/account/{id}")
	public ResponseEntity<?> getAccount(final @PathVariable Long id) { 
		Optional<Account> accountOpt = accountService.findById(id);
		if (!accountOpt.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		AccountDto dto = modelMapper.map(accountOpt.get(), AccountDto.class);
		
		return ResponseEntity.ok().eTag("\"" + dto.getVersion() + "\"").body(dto);
	}
	
	/**
	 * This method is only for testing purposes, to use when application is running and enquire the data in database.
	 * This is not needed for original spec in test.
	 * @param dto
	 * @return
	 */
	@PostMapping(path="/accounts")
	public ResponseEntity<?> postAccount(@RequestBody CreateAccountDto createDto) {		
		if (StringUtils.isAnyBlank(createDto.getName()) || createDto.getBalance()==null) {
			return ResponseEntity.badRequest().body(ValidationMessages.ALL_FIELDS_NAME_BALANCE_ARE_MANDATORY);
		} 
		if (createDto.getBalance()!=null && createDto.getBalance().compareTo(new BigDecimal(0.0)) < 1) {
			return ResponseEntity.badRequest().body(ValidationMessages.ACCOUNT_SHOULD_HAVE_A_POSITIVE_STARTING_BALANCE);
		}
		
		Account account = modelMapper.map(createDto, Account.class);
		account = accountService.createNew(account);
		AccountDto dto = modelMapper.map(account, AccountDto.class);
		return ResponseEntity.ok().eTag("\"" + dto.getVersion() + "\"").body(dto);				
	}
	
	@PostMapping(path="/account/{fromAccountId}/transfers")
	public ResponseEntity<?> transferAmount(final @PathVariable Long fromAccountId,final @RequestBody CreateTransferDto transferDto
			,final @RequestHeader("If-Match") String ifMatchValue) { 
		//validation checks
		if (transferDto==null || transferDto.getToAccountId()==null || transferDto.getAmount()==null) {
			return ResponseEntity.badRequest().body(ValidationMessages.TO_TRANSFER_BOTH_TO_ACCOUNT_ID_AND_BALANCE_ARE_NEEDED);
		}
		//check balance is positive
		if (transferDto.getAmount().compareTo(new BigDecimal(0.0)) < 1) { 
			return ResponseEntity.badRequest().body(ValidationMessages.TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0);
		}
		//check transferTo account is not the same as from account
		if (fromAccountId.equals(transferDto.getToAccountId())) {
			return ResponseEntity.badRequest().body(ValidationMessages.FROM_ACCOUNT_ID_AND_TO_ACCOUNT_ID_CANNOT_BE_SAME); 
		}
		
		//check transferTo account id exist
		Optional<Account> toAccountOpt = accountService.findById(transferDto.getToAccountId());
		if (!toAccountOpt.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Account toAccount = toAccountOpt.get();
		
		//check fromAccountId exists
		Optional<Account> fromAccountOpt = accountService.findById(fromAccountId);
		if (!fromAccountOpt.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Account fromAccount = fromAccountOpt.get();
		
		if (Strings.isBlank(ifMatchValue)) {
			return ResponseEntity.badRequest().build();
		}
		Long ifMatch = 0l;
		try {
			ifMatch = Long.parseLong(ifMatchValue);
		} catch(NumberFormatException ex) { 
			return ResponseEntity.badRequest().build();
		}
		if (!(ifMatch.compareTo(fromAccount.getVersion()) == 0 )) // ifMatchValue.equals("\"" + fromAccount.getVersion() + "\"")) {
		{
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		
		//check from account has that much balance
		if (fromAccount.getBalance().compareTo(transferDto.getAmount()) < 0) {
			return ResponseEntity.badRequest().body(ValidationMessages.FROM_ACCOUNT_ID_DOES_NOT_HAVE_SUFFICIENT_FUNDS); 
		}

		try { 
			accountService.transfer(fromAccount, toAccount, transferDto.getAmount());
		} catch(OptimisticLockingFailureException ex) { 
			// this will happen when a race condition happens (eg user has balance 100 pounds, and initiates 2 transfers of 100 each from different devices at same time
			//in that case transfer from one of the device will fail, and transaction failure will lead to a OptimisticLockingFailureException, this will be caught here and approprite error
			// message is sent back to user.
			return ResponseEntity.badRequest().body(ValidationMessages.MONEY_TRANSFER_TRANSACTION_FAILED_PLEASE_TRY_AGAIN);
		}
		return ResponseEntity.ok().body(ValidationMessages.TRANSFER_SUCCESSFUL);
	}
	
	//"/account/{id}/statement?fromDate={fromDateStr}&toDate={toDateStr}"
	@GetMapping(path="/account/{id}/statement")
	public ResponseEntity<?> generateStatement(final @PathVariable Long id, final @RequestParam("fromDate") String fromDateStr
			, final @RequestParam("toDate") String toDateStr ) {
		
		if (Strings.isBlank(fromDateStr) || Strings.isBlank(toDateStr)) {
			return ResponseEntity.badRequest().body(ValidationMessages.FROM_DATE_AND_TO_DATE_BOTH_SHOULD_BE_PRESENT);
		}
		
		SimpleDateFormat sdFormat = new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY);
		Date fromDate;
		try {
			fromDate = sdFormat.parse(fromDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(ValidationMessages.FROM_DATE_SHOULD_BE_IN_DD_M_MYYYY_FORMAT_AS_QUERY_STRING);
		}
		Date toDate;
		try {
			toDate = sdFormat.parse(toDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(ValidationMessages.TO_DATE_SHOULD_BE_IN_DD_M_MYYYY_FORMAT_AS_QUERY_STRING);
		}
				
		Optional<Account> accountOpt = accountService.findById(id);
		if (!accountOpt.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		
		List<Transaction> transactions = transactionService.findByAccountIdAndTransactionDateBetween(id, fromDate, toDate);
		
		SimpleDateFormat format = new SimpleDateFormat(DateFormats.DATE_TO_JSON_DD_MM_YYYY);
		
		StatementDto statementDto = new StatementDto();
		statementDto.setFromDate(format.format(fromDate));
		statementDto.setToDate(format.format(toDate));
				
		List<TransactionDto> transactionDtos = transactions.stream().sorted(Comparator.comparing(Transaction::getTransactionDate)).map(transaction -> modelMapper.map(transaction, TransactionDto.class)).collect(Collectors.toList());
		
		statementDto.setTransactions(transactionDtos);
		//find balance from last transaction in this batch, that will be the statement's balance
		if (transactionDtos.size() > 0) { 
			statementDto.setBalance(transactionDtos.get(transactionDtos.size() - 1).getBalance());
		}

		return ResponseEntity.ok(statementDto);
	}
}
