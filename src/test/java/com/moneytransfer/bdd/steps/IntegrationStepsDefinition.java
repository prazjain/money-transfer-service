package com.moneytransfer.bdd.steps;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytransfer.bdd.IntegrationTest;
import com.moneytransfer.dto.AccountDto;
import com.moneytransfer.dto.CreateAccountDto;
import com.moneytransfer.dto.CreateTransferDto;
import com.moneytransfer.dto.StatementDto;
import com.moneytransfer.dto.TransactionDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction.TransactionTypeCode;
import com.moneytransfer.utils.ComparableMatcher;
import com.moneytransfer.utils.DateFormats;
import com.moneytransfer.utils.TestUtil;
import com.moneytransfer.utils.ValidationMessages;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class IntegrationStepsDefinition extends IntegrationTest {

	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders
						.webAppContextSetup(this.wac)
						.build();
	}
	
	private AccountDto accountOne ;
	private StatementDto statementOne;
	
	private AccountDto accountTwo ;
	private StatementDto statementTwo;
	
	/****************** Steps on User one ******************/
	
	@Given("User one account {string} is created with starting balance of {string}")
	public void user_one_account_is_created_with_starting_balance_of(String name, String balance) throws IOException, Exception {
		accountOne = createAccount(name, new BigDecimal(balance));
	}

	@When("I get statement for user one")
	public void i_get_statement_for_user_one() throws Exception {
		Calendar cal = Calendar.getInstance();		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);		
		Date date = cal.getTime();
		statementOne = generateStatementRequest(accountOne, date);
	}	
	
	@Then("I see transaction count for user one is {string}")
	public void i_see_transaction_count_for_user_one_is(String count) {
		assertThat(statementOne.getTransactions().size(), is(Integer.parseInt(count)));
	}

	@Then("I see last transaction amount for user one is {string}")
	public void i_see_last_transaction_amount_for_user_one_is(String amount) {
		List<TransactionDto> transactionList = statementOne.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getTransactionAmount(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(amount))));
	}

	@Then("I see last transaction type code for user one is {string}")
	public void i_see_last_transaction_type_code_for_user_one_is(String typeCode) {
		List<TransactionDto> transactionList = statementOne.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getTypeCode(), is(typeCode));
	}

	@Then("I see last transaction balance for user one is {string}")
	public void i_see_last_transaction_balance_for_user_one_is(String balance) {
		List<TransactionDto> transactionList = statementOne.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getBalance(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(balance))));
	}

	@Then("I see last transaction counterparty for user one is {string}")
	public void i_see_last_transaction_counterparty_for_user_one_is(String counterparty) {
		List<TransactionDto> transactionList = statementOne.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getCounterparty(), is(counterparty));
	}

	@Then("I see statement balance  for user one is {string}")
	public void i_see_statement_balance_for_user_one_is(String expectedBalance) {
		assertThat(statementOne.getBalance(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(expectedBalance))));
	}

	@Then("I see account balance  for user one is {string}")
	public void i_see_account_balance_for_user_one_is(String expectedBalance) {
		assertThat(accountOne.getBalance(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(expectedBalance))));
	}

	/****************** Steps on User two ******************/
	
	@Given("User two account {string} is created with starting balance of {string}")
	public void user_two_account_is_created_with_starting_balance_of(String name, String balance) throws IOException, Exception {
		accountTwo = createAccount(name, new BigDecimal(balance));
	}

	@When("I get statement for user two")
	public void i_get_statement_for_user_two() throws Exception {
		Calendar cal = Calendar.getInstance();		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);		
		Date date = cal.getTime();
		statementTwo = generateStatementRequest(accountTwo, date);
	}
	
	@Then("I see transaction count for user two is {string}")
	public void i_see_transaction_count_for_user_two_is(String count) {
		assertThat(statementTwo.getTransactions().size(), is(Integer.parseInt(count)));
	}

	@Then("I see last transaction amount for user two is {string}")
	public void i_see_last_transaction_amount_for_user_two_is(String amount) {
		List<TransactionDto> transactionList = statementTwo.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getTransactionAmount(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(amount))));
	}

	@Then("I see last transaction type code for user two is {string}")
	public void i_see_last_transaction_type_code_for_user_two_is(String typeCode) {
		List<TransactionDto> transactionList = statementTwo.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getTypeCode(), is(typeCode));
	}

	@Then("I see last transaction balance for user two is {string}")
	public void i_see_last_transaction_balance_for_user_two_is(String balance) {
		List<TransactionDto> transactionList = statementTwo.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getBalance(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(balance))));
	}

	@Then("I see last transaction counterparty for user two is {string}")
	public void i_see_last_transaction_counterparty_for_user_two_is(String counterparty) {
		List<TransactionDto> transactionList = statementTwo.getTransactions();
		TransactionDto lastTransaction = transactionList.get(transactionList.size() - 1);
		
		assertThat(lastTransaction.getCounterparty(), is(counterparty));
	}

	@Then("I see statement balance  for user two is {string}")
	public void i_see_statement_balance_for_user_two_is(String expectedBalance) {
		assertThat(statementTwo.getBalance(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(expectedBalance))));
	}

	@Then("I see account balance  for user two is {string}")
	public void i_see_account_balance_for_user_two_is(String expectedBalance) {
		assertThat(accountTwo.getBalance(), is(new ComparableMatcher<BigDecimal>(new BigDecimal(expectedBalance))));
	}
	
	/****************** Common steps to both users *******************/
	

	@When("User one transfers {string} to user two account")
	public void user_one_transfers_to_user_two_account(String amount) throws IOException, Exception {
	    
		transferAmount(accountOne, accountTwo, new BigDecimal(amount));
		accountOne = getAccount(accountOne.getId());
		accountTwo = getAccount(accountTwo.getId());
	}

	
	@When("User two transfers {string} to user one account")
	public void user_two_transfers_to_user_one_account(String amount) throws IOException, Exception {
		transferAmount(accountTwo, accountOne, new BigDecimal(amount));
		accountOne = getAccount(accountOne.getId());
		accountTwo = getAccount(accountTwo.getId());
	}
	
	/****************** Private methods below **********************************/
	
	private AccountDto createAccount(String name, BigDecimal balance) throws IOException, Exception { 
		CreateAccountDto createAccountDto = new CreateAccountDto();
		createAccountDto.setName(name);
		createAccountDto.setBalance(balance);
		MvcResult mvcResult = mockMvc.perform(post("/accounts")
				 .contentType(MediaType.APPLICATION_JSON)
				 .content(TestUtil.convertObjectToJson(createAccountDto)))
				.andExpect(status().isOk())
				.andReturn();
		
		String json = mvcResult.getResponse().getContentAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		objectMapper.setDateFormat(sdf);
		AccountDto account = objectMapper.readValue(json, AccountDto.class);
		return account;
	}
	
	private AccountDto getAccount(Long accountId) throws Exception { 
		MvcResult mvcResult = mockMvc.perform(get("/account/{id}", accountId)
				 .contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		String json = mvcResult.getResponse().getContentAsString();
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		objectMapper.setDateFormat(sdf);
		AccountDto account = objectMapper.readValue(json, AccountDto.class);
		return account;
	}
	
	private StatementDto generateStatementRequest(AccountDto account, Date filterDate) throws Exception { 
		
		MvcResult mvcResult = mockMvc.perform(get("/account/{id}/statement", account.getId())
				 .contentType(MediaType.APPLICATION_JSON)
				 .param("fromDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(filterDate))
				 .param("toDate", (new SimpleDateFormat(DateFormats.REQUEST_PARAM_GEN_STATEMENT_DD_MM_YYYY)).format(filterDate))
				 )
				.andExpect(status().isOk())
				.andReturn();
		
		String statementJson = mvcResult.getResponse().getContentAsString();
		StatementDto statementDto = (new ObjectMapper()).readValue(statementJson, StatementDto.class);
		return statementDto;
	}
	
	private void transferAmount(AccountDto fromAccount, AccountDto toAccount, BigDecimal amount) throws IOException, Exception { 
		
		CreateTransferDto createTransferDto = new CreateTransferDto();
		createTransferDto.setToAccountId(toAccount.getId());
		createTransferDto.setAmount(amount);
		
		mockMvc.perform(post("/account/{fromAccountId}/transfers", fromAccount.getId())
				 .contentType(MediaType.APPLICATION_JSON)
				 .header("If-Match", "" + fromAccount.getVersion().toString())
				 .content(TestUtil.convertObjectToJson(createTransferDto)))
				.andExpect(status().isOk())
				.andExpect(content().string(ValidationMessages.TRANSFER_SUCCESSFUL));
	}
	
}
