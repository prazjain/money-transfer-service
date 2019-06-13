package com.moneytransfer.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moneytransfer.utils.MoneySerializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountDto implements Serializable {
	private String name;
	@JsonSerialize(using = MoneySerializer.class)
	private BigDecimal balance;
	@Override
	public int hashCode() {
		return Objects.hash(balance, name);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CreateAccountDto other = (CreateAccountDto) obj;
		boolean balanceEqual = balance.compareTo(other.balance)==0;
		boolean nameEqual = Objects.equals(name, other.name);
		return balanceEqual && nameEqual;
	}
}
