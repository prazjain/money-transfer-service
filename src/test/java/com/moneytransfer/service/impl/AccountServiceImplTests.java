package com.moneytransfer.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ibm.icu.util.Calendar;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.Transaction.TransactionTypeCode;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.service.impl.AccountServiceImpl;
import com.moneytransfer.utils.ComparableMatcher;
import com.moneytransfer.utils.IDateUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AccountServiceImplTests {

	@InjectMocks
	private AccountServiceImpl accountService;
	
	@Mock
	private AccountRepository repository;
	
	@Mock
	private TransactionService transactionService;
	
	@Mock
	private IDateUtils dateUtils;
	
	@Before
	public void before() { 
		MockitoAnnotations.initMocks(this) ;		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenCreateNewAccountNull_thenThrowIllegalArgumentException() { 
		accountService.createNew(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenCreateNewAccountWithAccountId_thenThrowIllegalArgumentException() { 
		Account account = new Account();
		account.setId(1l);
		account.setBalance(new BigDecimal(1000));
				
		account = accountService.createNew(account);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenCreateNewAccountNonPositiveBalance_thenThrowIllegalArgumentException() { 
		Account account = new Account();
		account.setId(1l);
		account.setBalance(new BigDecimal(0));
				
		account = accountService.createNew(account);
	}
	
	@Test
	public void whenCreateNewAccountValidArgs_thenReturnNewAccount() {
		
		Account account = new Account();
		account.setBalance(new BigDecimal(1000));
		
		Date now = Calendar.getInstance().getTime();
		when(dateUtils.getNow()).thenReturn(now);
		
		when(repository.save(isA(Account.class))).thenReturn(account);
		
		account = accountService.createNew(account);
		
		assertThat(account.getTransactions(), notNullValue());
		assertThat(account.getTransactions().size(), is(1));
		Transaction tx = account.getTransactions().get(0);
		assertThat(tx.getAccount(), notNullValue());
		assertThat(tx.getBalance(), is(new ComparableMatcher<BigDecimal>(account.getBalance())));
		assertThat(tx.getTransactionDate(), is(new ComparableMatcher<Date>(now)));
		assertThat(tx.getTypeCode(), is(TransactionTypeCode.IN));
		assertThat(tx.getCounterparty(), is("INITIAL DEPOSIT"));
		verify(transactionService).save(isA(Transaction.class));
		verify(repository).save(isA(Account.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenTransferingWithoutFromAccount_thenThrowsException() { 
		accountService.transfer(null, new Account(), new BigDecimal(100));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenTransferingWithoutToAccount_thenThrowsException() { 
		accountService.transfer(new Account(), null, new BigDecimal(100));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenTransferingWithNonPositiveBalance_thenThrowsException() {
		accountService.transfer(new Account(), new Account(), new BigDecimal(0));
	}
	
	@Test(expected = IllegalArgumentException.class)	
	public void whenTransferingWithInsufficientBalance_thenThrowsException() { 
		Account fromAccount = new Account();
		fromAccount.setBalance(new BigDecimal(50));
		
		accountService.transfer(fromAccount, new Account(), new BigDecimal(100));
	}
	
	@Test
	public void whenTransferingWithValidInput_thenFinishesSuccessfully() {
		
		BigDecimal fromAccountInitialBalance = new BigDecimal(1000);
		Account fromAccount = new Account();
		fromAccount.setId(1l);
		fromAccount.setBalance(fromAccountInitialBalance);
		
		BigDecimal toAccountInitialBalance = new BigDecimal(1000);
		Account toAccount = new Account();
		toAccount.setId(2l);
		toAccount.setBalance(toAccountInitialBalance);
		
		BigDecimal amount = new BigDecimal(500);
		
		Date now = Calendar.getInstance().getTime();
		when(dateUtils.getNow()).thenReturn(now);
		
		accountService.transfer(fromAccount, toAccount, amount);
		
		assertThat(fromAccount.getBalance(), is(new ComparableMatcher<BigDecimal>(fromAccountInitialBalance.subtract(amount))));
		assertThat(fromAccount.getTransactions(), notNullValue());
		assertThat(fromAccount.getTransactions().size(), is(1));
		Transaction tx = fromAccount.getTransactions().get(0);
		assertThat(tx.getAccount(), is(fromAccount));
		assertThat(tx.getTransactionAmount(), is(new ComparableMatcher<BigDecimal>(amount)));
		assertThat(tx.getBalance(), is(new ComparableMatcher<BigDecimal>(fromAccount.getBalance())));
		assertThat(tx.getTransactionDate(), is (new ComparableMatcher<Date>(now)));
		assertThat(tx.getTypeCode(), is(TransactionTypeCode.OUT));
		assertThat(tx.getCounterparty(), is(toAccount.getId().toString()));
		
		assertThat(toAccount.getBalance(), is(new ComparableMatcher<BigDecimal>(fromAccountInitialBalance.add(amount))));
		assertThat(toAccount.getTransactions(), notNullValue());
		assertThat(toAccount.getTransactions().size(), is(1));
		Transaction toTx = toAccount.getTransactions().get(0);
		assertThat(toTx.getAccount(), is(toAccount));
		assertThat(toTx.getTransactionAmount(), is(new ComparableMatcher<BigDecimal>(amount)));
		assertThat(toTx.getBalance(), is(new ComparableMatcher<BigDecimal>(toAccount.getBalance())));
		assertThat(toTx.getTransactionDate(), is (new ComparableMatcher<Date>(now)));
		assertThat(toTx.getTypeCode(), is(TransactionTypeCode.IN));
		assertThat(toTx.getCounterparty(), is(fromAccount.getId().toString()));
		
		verify(repository).save(eq(fromAccount));
		verify(repository).save(eq(toAccount));
		verify(transactionService,times(2)).save(isA(Transaction.class));
		
	}
	

}
