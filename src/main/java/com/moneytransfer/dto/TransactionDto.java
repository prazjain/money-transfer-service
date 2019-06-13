package com.moneytransfer.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moneytransfer.utils.MoneySerializer;

import lombok.Getter;
import lombok.Setter;

@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "id")
@Getter
@Setter
public class TransactionDto implements Serializable {
	private Long id;
    private String typeCode;
    private String transactionDate;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal transactionAmount;
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal balance;
    private String counterparty;
}
