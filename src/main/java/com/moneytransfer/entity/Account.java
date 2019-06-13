package com.moneytransfer.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "id")
public class Account {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String name;
	@Basic
	@Temporal(TemporalType.DATE)
	private Date openDate;
	@Basic
	@Temporal(TemporalType.DATE)
	private Date closeDate;
	private BigDecimal balance;
	
	@Version
	@NotNull
	private Long version = 0L;
	
	@OneToMany(cascade=CascadeType.ALL)
	@JsonManagedReference
	private List<Transaction> transactions = new ArrayList<Transaction>();
	
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
		Account other = (Account) obj;
		return Objects.equals(id, other.id);
	}	
}
