package com.moneytransfer.utils;

import org.mockito.ArgumentMatcher;


public class ComparableArgumentMatcher<T extends Comparable> implements ArgumentMatcher<T> { 
	private T expectedValue;
	
	public ComparableArgumentMatcher(T object) { 
		expectedValue = object;
	}

	@Override
	public boolean matches(T argument) {
		return expectedValue.compareTo(argument)==0;
	}
}
