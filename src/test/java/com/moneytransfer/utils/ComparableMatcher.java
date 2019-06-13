package com.moneytransfer.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ComparableMatcher<T extends Comparable> extends BaseMatcher<T>  {
	private T expectedValue;
	
	public ComparableMatcher(T object) { 
		expectedValue = object;
	}
	
	@Override
	public boolean matches(Object item) {
		if (item instanceof Comparable) { 
			return expectedValue.compareTo(item)==0;
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("expected value was :" + expectedValue.toString());		
	}

}
