package com.moneytransfer.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.utils.IDateUtils;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionRepository repository;
	
	@Autowired
	private IDateUtils dateUtils;

	@Override
	public Transaction save(Transaction obj) {
		return repository.save(obj);
	}

	@Override
	public Optional<Transaction> findById(Long id) {
		return repository.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}
	
	@Override
	public List<Transaction> findByAccountIdAndTransactionDateBetween(Long accountId, Date fromDate, Date toDate) { 
		
		//for between query to work, we need to set toDate to toDate+1, then find all data including toDate's transaction
		toDate = dateUtils.addDay(toDate, 1);
		
		List<Transaction> results = repository.findAllByAccountIdAndTransactionDateBetween(accountId, fromDate, toDate);
		
		return results;
		
	}

}
