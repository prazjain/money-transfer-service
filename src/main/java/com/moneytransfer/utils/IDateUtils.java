package com.moneytransfer.utils;

import java.util.Date;

public interface IDateUtils {	
	Date getNow();
	Date addDay(Date date, int add);
}
