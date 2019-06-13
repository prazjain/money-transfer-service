package com.moneytransfer.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moneytransfer.entity.Transaction;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

	List<Transaction> findAllByAccountIdAndTransactionDateBetween(Long accountId, Date fromDate, Date toDate);
	/*	
	@Query("select tx from com.moneytransfer.entity.Transaction tx where tx.account.id = :accountId and tx.transactionDate >= :fromDate and tx.transactionDate <= :toDate")
	List<Transaction> findAllByAccountAndTransactionDateFilter(@Param("accountId")Long accountId
			,@Param("fromDate") Date fromDate
			,@Param("toDate") Date toDate);

	
	@Query("select tx from com.moneytransfer.entity.Transaction tx where tx.account.id = ?1 and tx.transactionDate >= ?2 and tx.transactionDate <= ?3")
	List<Transaction> findAllByAccountAndTransactionDateFilter(Long accountId,Date fromDate, Date toDate);
	*/	
}
