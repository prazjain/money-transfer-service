package com.moneytransfer.service;

import java.util.Date;
import java.util.List;

import com.moneytransfer.entity.Transaction;

public interface TransactionService extends CrudService<Transaction> {
	List<Transaction> findByAccountIdAndTransactionDateBetween(Long accountId, Date fromDate, Date toDate);
}
