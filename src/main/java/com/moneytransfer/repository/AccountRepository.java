package com.moneytransfer.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.moneytransfer.entity.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
	
}
