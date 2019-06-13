package com.moneytransfer.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moneytransfer.utils.MoneySerializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatementDto implements Serializable {
	private String fromDate;
	private String toDate;
	private List<TransactionDto> transactions;
	@JsonSerialize(using = MoneySerializer.class)
	private BigDecimal balance;
}
