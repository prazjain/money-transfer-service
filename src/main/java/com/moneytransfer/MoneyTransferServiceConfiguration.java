package com.moneytransfer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import com.moneytransfer.dto.AccountDto;
import com.moneytransfer.dto.TransactionDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.Transaction.TransactionTypeCode;
import com.moneytransfer.service.AccountService;
import com.moneytransfer.service.TransactionService;
import com.moneytransfer.service.impl.AccountServiceImpl;
import com.moneytransfer.service.impl.TransactionServiceImpl;
import com.moneytransfer.utils.DateFormats;
import com.moneytransfer.utils.DateUtilsImpl;
import com.moneytransfer.utils.IDateUtils;


@Configuration
public class MoneyTransferServiceConfiguration {
	
	@Bean(name="modelMapper")
	public ModelMapper getModelMapper() { 
		ModelMapper modelMapper = new ModelMapper();
		
		Converter<Date,String> dateToStringConverter = date -> {
			if (date.getSource()==null)
				return "";
			
			SimpleDateFormat format = new SimpleDateFormat(DateFormats.DATE_TO_JSON_DD_MM_YYYY);
			String strDate = format.format(date.getSource());
			return strDate;			
		};
		
		Converter<String,Date> stringToDateConverter = string -> {
			SimpleDateFormat format = new SimpleDateFormat(DateFormats.DATE_TO_JSON_DD_MM_YYYY);
			Date date = null;
			try {
				date = format.parse(string.getSource());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return date;
		};
		
		modelMapper.createTypeMap(Account.class, AccountDto.class)
			.addMappings(mapper -> {
				mapper.using(dateToStringConverter).map(Account::getOpenDate, AccountDto::setOpenDate);
				mapper.using(dateToStringConverter).map(Account::getCloseDate, AccountDto::setCloseDate);
			});
		
		modelMapper.createTypeMap(AccountDto.class, Account.class)
			.addMappings(mapper -> {
				mapper.using(stringToDateConverter).map(AccountDto::getOpenDate, Account::setOpenDate);
				mapper.using(stringToDateConverter).map(AccountDto::getCloseDate, Account::setCloseDate);
			});
		
		Converter<TransactionTypeCode, String> typeCodeToStringConverter = typeCode  -> typeCode.getSource().name();
		
		modelMapper.createTypeMap(Transaction.class, TransactionDto.class)
		.addMappings(mapper -> {
			mapper.using(typeCodeToStringConverter).map(Transaction::getTypeCode, TransactionDto::setTypeCode);
		});
		
		return modelMapper;
	}
	
	@Bean
	@Primary
	public AccountService getAccountService() { 
		return new AccountServiceImpl();
	}
	
	@Bean
	@Primary
	public TransactionService getTransactionService() { 
		return new TransactionServiceImpl();
	}
	
	@Bean
	@Primary
	public IDateUtils getDateUtils() { 
		return new DateUtilsImpl();
	}
	
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
