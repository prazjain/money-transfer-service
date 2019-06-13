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
public class CreateTransferDto implements Serializable {
	private Long toAccountId;
	@JsonSerialize(using = MoneySerializer.class)
	private BigDecimal amount;
	@Override
	public int hashCode() {
		return Objects.hash(amount, toAccountId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CreateTransferDto other = (CreateTransferDto) obj;
		return amount.compareTo(other.amount)==0 && Objects.equals(toAccountId, other.toAccountId);
	}
}
