package com.moneytransfer.service;

import java.math.BigDecimal;

import com.moneytransfer.entity.Account;

public interface AccountService extends CrudService<Account> {

	void transfer(Account fromAccount, Account toAccount, BigDecimal amount);

	Account createNew(Account account);

}
