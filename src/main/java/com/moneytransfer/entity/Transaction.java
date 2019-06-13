package com.moneytransfer.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "id")
public class Transaction implements Serializable {
	
	/*
	public enum TransactionTypeCode { 
		IN ("IN"),
		OUT("OUT");		
		private String value;
		TransactionTypeCode(String val) { value = val; }
		public String getValue() { return value; }		
	}
	*/
	public enum TransactionTypeCode {  IN , OUT	 }
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
    private TransactionTypeCode typeCode;
	
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
	
    private BigDecimal transactionAmount;
    private BigDecimal balance;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JsonBackReference
    private Account account;
    
    /**
     * This store string description of other party involved in the transaction, eg it can be their name
     * (like we see in our bank transactions (its shows a name of bank/person/company/cash machine name etc)
     */
    private String counterparty;
    
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		return Objects.equals(id, other.id);
	}
	
}
