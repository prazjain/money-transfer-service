package com.moneytransfer.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.service.impl.TransactionServiceImpl;
import com.moneytransfer.utils.ComparableArgumentMatcher;
import com.moneytransfer.utils.IDateUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TransactionServiceImplTests {
	
	@InjectMocks
	private TransactionServiceImpl transactionService;
	
	@Mock
	private TransactionRepository transactionRepository;
	
	@Mock
	private IDateUtils dateUtils;
	
	@Before
	public void before() { 
		MockitoAnnotations.initMocks(this) ;		
	}
	
	@Test
	public void whenFindByIdHasNoAccountId_thenThrowIllegalArgumentException() { 
		//List<Transaction> findByAccountIdAndTransactionDateBetween(Long accountId, Date fromDate, Date toDate);

		
		Calendar cal = Calendar.getInstance();
		Date fromDate = cal.getTime();
		cal.add(Calendar.DATE, 1);
		Date datePlusOne = cal.getTime();
		cal.add(Calendar.DATE, 1);
		Date datePlusTwo = cal.getTime();
		
		Long fromAccountId = 1l;
		Account account = new Account();
		account.setId(fromAccountId);
		
		List<Transaction> transactionList = new ArrayList<Transaction>();
		Transaction tx = new Transaction();
		tx.setId(2l);
		tx.setAccount(account);
		account.setTransactions(transactionList);
		transactionList.add(tx);
		
		when(dateUtils.addDay
				(argThat(new ComparableArgumentMatcher<Date>(datePlusOne)), eq(1)))
			.thenReturn(datePlusTwo);
		
		when(transactionRepository.findAllByAccountIdAndTransactionDateBetween(
				argThat(new ComparableArgumentMatcher<Long>(fromAccountId))
				,argThat(new ComparableArgumentMatcher<Date>(fromDate))
				,argThat(new ComparableArgumentMatcher<Date>(datePlusTwo))))
			.thenReturn(transactionList);
		
		List<Transaction> actualList = transactionService.findByAccountIdAndTransactionDateBetween(fromAccountId, fromDate, datePlusOne);
		
		assertThat(actualList, sameInstance(transactionList));
		assertThat(actualList.size(), is(transactionList.size()));
		//as we know we have just one transaction here, we want to check that transaction object
		assertThat(actualList.get(0), is(transactionList.get(0)));
		
		verify(transactionRepository, times(1)).findAllByAccountIdAndTransactionDateBetween(				
				argThat(new ComparableArgumentMatcher<Long>(fromAccountId))
				,argThat(new ComparableArgumentMatcher<Date>(fromDate))
				,argThat(new ComparableArgumentMatcher<Date>(datePlusTwo)));

	}
	

}
