package com.moneytransfer.utils;

import java.util.Calendar;
import java.util.Date;

/*
 * This class is encapsulating static methods to get dates. Hiding static method here to their values can be mocked while testing
 */
public class DateUtilsImpl implements IDateUtils {
	@Override
	public Date getNow() {
		return Calendar.getInstance().getTime();
	}

	/**
	 * Increments date, and resets hours, minutes, seconds to 0.
	 */
	@Override
	public Date addDay(Date date, int add) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, add);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
