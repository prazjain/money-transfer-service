package com.moneytransfer.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytransfer.controller.AccountController;
import com.moneytransfer.dto.AccountDto;
import com.moneytransfer.dto.CreateAccountDto;
import com.moneytransfer.dto.CreateTransferDto;
import com.moneytransfer.dto.StatementDto;
import com.moneytransfer.dto.TransactionDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.Transaction.TransactionTypeCode;
import com.moneytransfer.service.impl.AccountServiceImpl;
import com.moneytransfer.service.impl.TransactionServiceImpl;
import com.moneytransfer.utils.ComparableArgumentMatcher;
import com.moneytransfer.utils.ComparableMatcher;
import com.moneytransfer.utils.DateFormats;
import com.moneytransfer.utils.LongStringIntMatcher;
import com.moneytransfer.utils.TestUtil;
import com.moneytransfer.utils.ValidationMessages;

@ExtendWith(SpringExtension.class)
//@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class AccountControllerTest {
	
	@Mock
	private AccountServiceImpl accountService;	
	
	@Mock
	private TransactionServiceImpl transactionService;
	
	@InjectMocks
	private AccountController controller;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Mock
	private ModelMapper modelMapper;
	
	
	@Before
	public void before() { 
		MockitoAnnotations.initMocks(this) ;
		this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}
	private String getBigDecimalToString(BigDecimal bigDecimal) { 
		return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
	}
	
	@Test
	@DisplayName("POST /accounts with 0 Balance")
	public void whenCreateAccountBalanceIs0_thenCreateAccountReturnsBadRequest() throws Exception {
				
		CreateAccountDto createAccountDto = new CreateAccountDto();
		createAccountDto.setName("Prashant");
		createAccountDto.setBalance(new BigDecimal(0));
		
		 mockMvc.perform(post("/accounts")
				 .contentType(MediaType.APPLICATION_JSON)
				 .content(TestUtil.convertObjectToJson(createAccountDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.ACCOUNT_SHOULD_HAVE_A_POSITIVE_STARTING_BALANCE));
	}
	
	@Test
	@DisplayName("POST /accounts with Balance Null")
	public void whenCreateAccountBalanceNull_thenCreateAccountReturnsBadRequest() throws Exception {
		
		//create mock data
		CreateAccountDto createAccountDto = new CreateAccountDto();
		createAccountDto.setName("Prashant");
		
		mockMvc.perform(post("/accounts")
				 .contentType(MediaType.APPLICATION_JSON)
				 .content(TestUtil.convertObjectToJson(createAccountDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.ALL_FIELDS_NAME_BALANCE_ARE_MANDATORY));
	}
	
	@Test
	@DisplayName("POST /accounts with Valid Input")
	public void whenCreateAccountDtoIsValid_thenItCreatesAccount() throws Exception {
		
		//create mock data
		CreateAccountDto createAccountDto = new CreateAccountDto();
		createAccountDto.setName("Prashant");
		createAccountDto.setBalance(new BigDecimal(1000l));
		
		//create mock data for account
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		Date openDate = cal.getTime();	
		
		Account mockUnsavedAccount = new Account();
		mockUnsavedAccount.setName(createAccountDto.getName());
		mockUnsavedAccount.setOpenDate(openDate);
		mockUnsavedAccount.setCloseDate(null);
		mockUnsavedAccount.setBalance(createAccountDto.getBalance());
		
		Account mockSavedAccount = new Account();
		mockSavedAccount.setId(1l);
		mockSavedAccount.setName(mockUnsavedAccount.getName());
		mockSavedAccount.setOpenDate(mockUnsavedAccount.getOpenDate());
		mockSavedAccount.setCloseDate(mockUnsavedAccount.getCloseDate());
		mockSavedAccount.setBalance(mockUnsavedAccount.getBalance());
		mockSavedAccount.setVersion(1l);
		
		AccountDto mockAccountDto = new AccountDto() ;		
		mockAccountDto.setId(mockSavedAccount.getId());
		mockAccountDto.setName(mockSavedAccount.getName());
		mockAccountDto.setOpenDate(mockSavedAccount.getOpenDate() ==null ? null : (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(mockSavedAccount.getOpenDate()));
		mockAccountDto.setCloseDate(mockSavedAccount.getCloseDate() == null ? null : (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(mockSavedAccount.getCloseDate()));
		mockAccountDto.setBalance(mockSavedAccount.getBalance());
		mockAccountDto.setVersion(mockSavedAccount.getVersion());
				
		when(modelMapper.map(eq(createAccountDto), eq(Account.class))).thenReturn(mockUnsavedAccount);
		when(accountService.createNew(mockUnsavedAccount)).thenReturn(mockSavedAccount);
		when(modelMapper.map(eq(mockSavedAccount), eq(AccountDto.class))).thenReturn(mockAccountDto);
				
		mockMvc.perform(post("/accounts")
				 .contentType(MediaType.APPLICATION_JSON)
				 .content(TestUtil.convertObjectToJson(createAccountDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				//validate the headers
				.andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
				.andExpect(jsonPath("$.id", is(mockAccountDto.getId().intValue())))
				.andExpect(jsonPath("$.name", is(mockAccountDto.getName())))
				.andExpect(jsonPath("$.openDate", is(mockAccountDto.getOpenDate())))
				.andExpect(jsonPath("$.closeDate", is(mockAccountDto.getCloseDate())))
				.andExpect(jsonPath("$.balance", is(getBigDecimalToString(mockAccountDto.getBalance()))))
				//.andExpect(jsonPath("$.version", is(new LongMatcher(2l))));
		.andExpect(jsonPath("$.version", is(new LongStringIntMatcher(mockAccountDto.getVersion()))));
		
	}
	
	@Test
	@DisplayName("POST /accounts with Blank Name")
	public void whenCreateAccountNameIsBlank_thenCreateAccountReturnsBadRequest() throws Exception {
		
		//create mock data
		CreateAccountDto createAccountDto = new CreateAccountDto();
		createAccountDto.setName("");
		createAccountDto.setBalance(new BigDecimal(1000));
		
		 mockMvc.perform(post("/accounts")
				 .contentType(MediaType.APPLICATION_JSON)
				 .content(TestUtil.convertObjectToJson(createAccountDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.ALL_FIELDS_NAME_BALANCE_ARE_MANDATORY));
	}
		
	@Test
	@DisplayName("GET /account/1 - Not Found")
	public void whenGetAccountIdDoesNotExist_thenReturnNotFoundStatus() throws Exception {
		//setup mocked service
		when(accountService.findById(1l)).thenReturn(Optional.empty());		
		
		mockMvc.perform(get("/account/{id}", 1))
				//validate response code
				.andExpect(status().isNotFound());
			
	}
	
	@Test
	@DisplayName("GET /account/1 - Found")
	public void whenGetAccountIdExists_thenReturnAccount() throws Exception {
		
		//create mock data for account
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		Date openDate = cal.getTime();	
		
		Account mockAccount = new Account();
		mockAccount.setId(1l);
		mockAccount.setName("Prashant");
		mockAccount.setOpenDate(openDate);
		mockAccount.setCloseDate(null);
		mockAccount.setBalance(new BigDecimal(100));
		mockAccount.setVersion(1l);
		
		AccountDto mockAccountDto = new AccountDto() ;		
		mockAccountDto.setId(mockAccount.getId());
		mockAccountDto.setName(mockAccount.getName());
		mockAccountDto.setOpenDate(mockAccount.getOpenDate() ==null ? null : (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(mockAccount.getOpenDate()));
		mockAccountDto.setCloseDate(mockAccount.getCloseDate() == null ? null : (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(mockAccount.getCloseDate()));
		mockAccountDto.setBalance(mockAccount.getBalance());
		mockAccountDto.setVersion(mockAccount.getVersion());
				
		//setup mocked service
		when(accountService.findById(mockAccount.getId())).thenReturn(Optional.of(mockAccount));
		when(modelMapper.map(mockAccount, AccountDto.class)).thenReturn(mockAccountDto);
		
		
		mockMvc.perform(get("/account/{id}", 1))
				//validate response code and content type
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				//validate the headers
				.andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
				
				//validate the returned fields
				
				//we need to safely convert long to int, because mockito cannot compare int value in json vs long value in object
				//this is a known issue, and so we need to safely convert long to int to compare values.
				.andExpect(jsonPath("$.id", is(mockAccountDto.getId().intValue())))
				.andExpect(jsonPath("$.name", is(mockAccountDto.getName())))
				.andExpect(jsonPath("$.openDate", is(mockAccountDto.getOpenDate())))
				.andExpect(jsonPath("$.closeDate", is(mockAccountDto.getCloseDate())))
				.andExpect(jsonPath("$.balance", is(getBigDecimalToString(mockAccountDto.getBalance()))))
				.andExpect(jsonPath("$.version", is(mockAccountDto.getVersion().intValue())));
				
	}
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - amount missing")
	public void whenTransferringAndAmountMissing_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.TO_TRANSFER_BOTH_TO_ACCOUNT_ID_AND_BALANCE_ARE_NEEDED));
	}
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - amount not positive")
	public void whenTransferringAndAmountNotPositive_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(0));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers",fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0));

	}
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - fromAccountId does not exist")
	public void whenTransferringAndFromAccountIdDoesNotExist_thenReturnNotFound() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(new Account()));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.empty());
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isNotFound());	
	}	
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - from And to accounts are same")
	public void whenTransferringAndFromAndToAccountsAreSame_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(2l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.FROM_ACCOUNT_ID_AND_TO_ACCOUNT_ID_CANNOT_BE_SAME));
	}	
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - Insufficient balance")
	public void whenTransferringAndInsufficientBalanceInAccount_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		Account fromAccount = new Account();
		fromAccount.setId(fromAccountId);
		fromAccount.setVersion(1l);
		fromAccount.setBalance(new BigDecimal(100));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(new Account()));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.FROM_ACCOUNT_ID_DOES_NOT_HAVE_SUFFICIENT_FUNDS));
	}	
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - If-Match Version Not same as Account version")
	public void whenTransferringAndIsMatchNotSameAsAccountVersion_thenReturnPreconditionFailed() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		Account fromAccount = new Account();
		fromAccount.setId(fromAccountId);
		fromAccount.setVersion(2l);
		fromAccount.setBalance(new BigDecimal(1000));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(new Account()));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isPreconditionFailed());	
	}	
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - If-Match RequestHeader is blank")
	public void whenTransferringAndIsMatchRequestHeaderIsBlank_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(new Account()));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(new Account()));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", " ")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest());	
	}	

	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - If-Match RequestHeader is not Long")
	public void whenTransferringAndIsMatchRequestHeaderIsNotLong_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(new Account()));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(new Account()));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "abc")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest());	
	}
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - Optimistic Exception")
	public void whenTransferringAndOptimisticLockingException_thenReturnBadRequest() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		Account toAccount = new Account();
		toAccount.setId(toAccountId);
		Account fromAccount = new Account();
		fromAccount.setId(fromAccountId);
		fromAccount.setVersion(1l);
		fromAccount.setBalance(new BigDecimal(1000));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(toAccount));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
		doThrow(OptimisticLockingFailureException.class).when(accountService)
				.transfer(eq(fromAccount), eq(toAccount), argThat(new ComparableArgumentMatcher<BigDecimal>(createTransferDto.getAmount())));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.MONEY_TRANSFER_TRANSACTION_FAILED_PLEASE_TRY_AGAIN));
	}	
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - toAccountId does not exist")
	public void whenTransferringAndToAccountIdDoesNotExist_thenReturnNotFound() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.empty());
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(new Account()));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isNotFound());	
	}
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - toAccountId missing")
	public void whenTransferringAndToAccountIdMissing_thenReturnBadRequest() throws IOException, Exception {
		//create mock data
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setAmount(new BigDecimal(500));
		
		 mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(ValidationMessages.TO_TRANSFER_BOTH_TO_ACCOUNT_ID_AND_BALANCE_ARE_NEEDED));

	}
	
	@Test
	@DisplayName("POST /account/{fromAccountId}/transfers - Transfer Successful")
	public void whenTransferringAndValidInput_thenReturnOk() throws IOException, Exception { 
		//create mock data
		Long toAccountId = new Long(2l);
		Long fromAccountId = new Long(1l);
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccountId);
		createTransferDto.setAmount(new BigDecimal(500));
		
		Account fromAccount = new Account();
		fromAccount.setId(fromAccountId);
		fromAccount.setVersion(1l);
		fromAccount.setBalance(new BigDecimal(1000));
		
		when(accountService.findById(eq(toAccountId))).thenReturn(Optional.of(new Account()));
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
		
		mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "1")
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isOk())
				.andExpect(content().string(ValidationMessages.TRANSFER_SUCCESSFUL));
	}

	@Test
	public void whenGenerateStatmentAndMissingFromDate_thenReturnBadRequest() throws Exception {
		
		Date now = Calendar.getInstance().getTime();
		
		Long fromAccountId = 1l;
		
		mockMvc.perform(get("/account/{id}/statement", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("toDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(now))
				 )
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void whenGenerateStatmentAndMissingToDate_thenReturnBadRequest() throws Exception { 
		Date now = Calendar.getInstance().getTime();
		
		Long fromAccountId = 1l;
		
		mockMvc.perform(get("/account/{id}/statement", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("fromDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(now))
				 )
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void whenGenerateStatmentAndNotExistingAccountId_thenReturnNotFound() throws Exception { 
		Date now = Calendar.getInstance().getTime();
		
		Long fromAccountId = 1l;
		
		mockMvc.perform(get("/account/{id}/statement", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("fromDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(now))
				 .param("toDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(now))
				 )
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void whenGenerateStatmentAndValidInput_thenReturnStatementWithNoTransaction() throws Exception { 
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		cal.add(Calendar.DATE, -2);
		Date dayBeforeYesterday = cal.getTime();		
		
		Long fromAccountId = 1l;
		Account fromAccount = new Account();
		fromAccount.setId(fromAccountId);
		fromAccount.setVersion(1l);
		fromAccount.setOpenDate(dayBeforeYesterday);
		fromAccount.setBalance(new BigDecimal(1000));
		
		
		
		List<Transaction> transactionList = new ArrayList<Transaction>();
		
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
		when(transactionService.findByAccountIdAndTransactionDateBetween(
				argThat(new ComparableArgumentMatcher<Long>(fromAccountId))
				, argThat(new ComparableArgumentMatcher<Date>(now))
				, argThat(new ComparableArgumentMatcher<Date>(now))))
			.thenReturn(transactionList);
		
		MvcResult mvcResult = mockMvc.perform(get("/account/{id}/statement", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("fromDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(now))
				 .param("toDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(now))
				 )
				.andExpect(status().isOk())
				.andReturn();
		
		String statementJson = mvcResult.getResponse().getContentAsString();
		StatementDto statementDto = (new ObjectMapper()).readValue(statementJson, StatementDto.class);
		
		assertThat(statementDto.getBalance(), nullValue());
		assertThat(statementDto.getTransactions(), notNullValue());
		assertThat(statementDto.getTransactions().size(), is(0));
	}
	
	@Test
	public void whenGenerateStatmentAndValidInput_thenReturnStatementWithTransactions() throws Exception { 
		Calendar cal = Calendar.getInstance();
		
		Date now = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date filterDate = cal.getTime();
		
		Long fromAccountId = 1l;
		Account fromAccount = new Account();
		fromAccount.setId(fromAccountId);
		fromAccount.setVersion(1l);
		fromAccount.setOpenDate(now);
		fromAccount.setBalance(new BigDecimal(1000));
		
		List<Transaction> transactionList = new ArrayList<Transaction>();
		Transaction tx = new Transaction();
		tx.setId(2l);
		tx.setCounterparty("INITIAL DEPOSIT");
		tx.setBalance(fromAccount.getBalance());
		tx.setTransactionAmount(fromAccount.getBalance());
		tx.setTypeCode(TransactionTypeCode.IN);
		
		transactionList.add(tx);
		
		TransactionDto txDto = new TransactionDto();
		txDto.setId(tx.getId());
		txDto.setCounterparty(tx.getCounterparty());
		txDto.setBalance(tx.getBalance());
		txDto.setTransactionAmount(tx.getTransactionAmount());
		txDto.setTypeCode(tx.getTypeCode().name());
		
		when(modelMapper.map(eq(tx), eq(TransactionDto.class))).thenReturn(txDto);		
		when(accountService.findById(eq(fromAccountId))).thenReturn(Optional.of(fromAccount));
		when(transactionService.findByAccountIdAndTransactionDateBetween(
				argThat(new ComparableArgumentMatcher<Long>(fromAccountId))
				, argThat(new ComparableArgumentMatcher<Date>(filterDate))
				, argThat(new ComparableArgumentMatcher<Date>(filterDate))))
			.thenReturn(transactionList);
		
		MvcResult mvcResult = mockMvc.perform(get("/account/{id}/statement", fromAccountId)
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("fromDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(filterDate))
				 .param("toDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(filterDate))
				 )
				.andExpect(status().isOk())
				.andReturn();
		
		String statementJson = mvcResult.getResponse().getContentAsString();
		StatementDto statementDto = (new ObjectMapper()).readValue(statementJson, StatementDto.class);
		
		assertThat(statementDto.getBalance(), is(new ComparableMatcher<BigDecimal>(fromAccount.getBalance())));
		assertThat(statementDto.getTransactions(), notNullValue());
		assertThat(statementDto.getTransactions().size(), is(1));
		assertThat(statementDto.getTransactions().get(0).getCounterparty(), is("INITIAL DEPOSIT"));
		assertThat(statementDto.getTransactions().get(0).getTypeCode(), is(TransactionTypeCode.IN.name()));
		
	}
	
}
