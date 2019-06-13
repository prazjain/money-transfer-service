package com.moneytransfer.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.Transaction.TransactionTypeCode;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.service.AccountService;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.utils.IDateUtils;
import com.moneytransfer.utils.ValidationMessages;

@Service
public class AccountServiceImpl implements AccountService {
	
	@Autowired
	private AccountRepository repository;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private IDateUtils dateUtils;

	@Override
	public Account save(Account obj) {
		return repository.save(obj);
	}

	@Override
	public Optional<Account> findById(Long id) {		
		return repository.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}
	
	@Transactional
	@Override
	public Account createNew(Account account) {
		if (account==null || account.getId()!=null || account.getBalance().compareTo(new BigDecimal(0)) <= 0) {
			throw new IllegalArgumentException(ValidationMessages.ACCOUNT_OBJECT_IS_INVALID);
		}
		
		Date openDate = dateUtils.getNow();
		account.setOpenDate(openDate);
		
		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setTransactionAmount(account.getBalance());
		transaction.setBalance(account.getBalance());
		transaction.setTransactionDate(openDate);
		transaction.setTypeCode(TransactionTypeCode.IN);
		//when account opens for first time, we give a standard name for counterparty
		transaction.setCounterparty("INITIAL DEPOSIT");
		
		account.getTransactions().add(transaction);	
		
		transactionService.save(transaction);
		return save(account);
	}

	@Transactional
	@Override
	public void transfer(Account fromAccount, Account toAccount, BigDecimal amount) {
		//preconditions
		if (fromAccount==null || toAccount==null || amount.compareTo(new BigDecimal(0)) <= 0) {
			throw new IllegalArgumentException(ValidationMessages.FROM_ACCOUNT_AND_TO_ACCOUNT_SHOULD_NOT_BE_NULL_AND_TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0);
		}
		if (fromAccount.getBalance().compareTo(amount) < 0) {
			throw new IllegalArgumentException(ValidationMessages.FROM_ACCOUNT_DOES_NOT_CONTAIN_SUFFICIENT_FUNDS);
		}
		
		//date now
		Date txDate = dateUtils.getNow();
		//update accounts and add a new transaction
		BigDecimal fromAccountBalance = fromAccount.getBalance().subtract(amount);
		fromAccount.setBalance(fromAccountBalance);
		BigDecimal toAccountBalance = toAccount.getBalance().add(amount);
		toAccount.setBalance(toAccountBalance);
		
		//create transaction for fromAccount
		Transaction fromAccountTransaction = new Transaction();
		fromAccountTransaction.setAccount(fromAccount);
		fromAccountTransaction.setTransactionAmount(amount);
		fromAccountTransaction.setBalance(fromAccountBalance);
		fromAccountTransaction.setTransactionDate(txDate);
		fromAccountTransaction.setTypeCode(TransactionTypeCode.OUT);
		fromAccountTransaction.setCounterparty(toAccount.getId().toString());
				
		//create transaction for toAccount
		Transaction toAccountTransaction = new Transaction();
		toAccountTransaction.setAccount(toAccount);
		toAccountTransaction.setTransactionAmount(amount);
		toAccountTransaction.setBalance(toAccountBalance);
		toAccountTransaction.setTransactionDate(txDate);
		toAccountTransaction.setTypeCode(TransactionTypeCode.IN);
		toAccountTransaction.setCounterparty(fromAccount.getId().toString());
		
		fromAccount.getTransactions().add(fromAccountTransaction);
		toAccount.getTransactions().add(toAccountTransaction);
		
		transactionService.save(fromAccountTransaction);
		transactionService.save(toAccountTransaction);
		
		save(fromAccount);
		save(toAccount);
	}

}
