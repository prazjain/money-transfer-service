package com.moneytransfer.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moneytransfer.utils.MoneySerializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto implements Serializable {
	private Long id;	
	private String name;
	private String openDate;
	private String closeDate;
	@JsonSerialize(using = MoneySerializer.class)
	private BigDecimal balance;
	private Long version;
}
