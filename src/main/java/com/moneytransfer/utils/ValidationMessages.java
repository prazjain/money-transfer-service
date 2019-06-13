package com.moneytransfer.utils;

public class ValidationMessages {

	public static final String ALL_FIELDS_NAME_BALANCE_ARE_MANDATORY = "All fields (name, balance) are mandatory";
	public static final String ACCOUNT_SHOULD_HAVE_A_POSITIVE_STARTING_BALANCE = "Account should have a positive starting balance";
	public static final String TO_DATE_SHOULD_BE_IN_DD_M_MYYYY_FORMAT_AS_QUERY_STRING = "To date should be in ddMMyyyy format as query string";
	public static final String FROM_DATE_SHOULD_BE_IN_DD_M_MYYYY_FORMAT_AS_QUERY_STRING = "From date should be in ddMMyyyy format as query string";
	public static final String FROM_DATE_AND_TO_DATE_BOTH_SHOULD_BE_PRESENT = "fromDate and toDate both should be present";
	public static final String TRANSFER_SUCCESSFUL = "Transfer successful";
	public static final String MONEY_TRANSFER_TRANSACTION_FAILED_PLEASE_TRY_AGAIN = "Money transfer transaction failed, please try again";
	public static final String FROM_ACCOUNT_ID_DOES_NOT_HAVE_SUFFICIENT_FUNDS = "fromAccountId does not have sufficient funds";
	public static final String FROM_ACCOUNT_ID_AND_TO_ACCOUNT_ID_CANNOT_BE_SAME = "fromAccountId and toAccountId cannot be same";
	public static final String TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0 = "Transfer amount should be greater than 0";
	public static final String TO_TRANSFER_BOTH_TO_ACCOUNT_ID_AND_BALANCE_ARE_NEEDED = "To transfer both toAccountId and balance are needed";
	public static final String ACCOUNT_OBJECT_IS_INVALID = "Account object is invalid";
	public static final String FROM_ACCOUNT_DOES_NOT_CONTAIN_SUFFICIENT_FUNDS = "fromAccount does not contain sufficient funds";
	public static final String FROM_ACCOUNT_AND_TO_ACCOUNT_SHOULD_NOT_BE_NULL_AND_TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0 = "fromAccount and toAccount should not be null, and transfer amount should be greater than 0";
}
