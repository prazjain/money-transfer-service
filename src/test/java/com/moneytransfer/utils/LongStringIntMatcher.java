package com.moneytransfer.utils;

import java.math.BigDecimal;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * This is need to compare long which is return from json format, and sometimes comes back as int!
 * @author Prashant
 *
 */
public class LongStringIntMatcher extends BaseMatcher<Long> {
	
	private Long expectedValue;
	
	public LongStringIntMatcher(Long object) { 
		expectedValue = object;
	}

	@Override
	public boolean matches(Object item) {
		
		if ((item instanceof Long) || (item instanceof String) || (item instanceof Integer)) { 
			Long other = 0l;
			if (item instanceof String) { 
				String itemString = (String) item;
				try {
					other = Long.parseLong(itemString);
				} catch(NumberFormatException nfe) { 
					return false;
				}
			} else if (item instanceof Integer) {
				other = ((Integer) item).longValue();
			} else {
				other = (Long) item;
			}
			return expectedValue.compareTo(other)==0;
		}
		return false;

	}

	@Override
	public void describeTo(Description description) {
		description.appendText("expected value was :" + expectedValue.toString());
	}
}
