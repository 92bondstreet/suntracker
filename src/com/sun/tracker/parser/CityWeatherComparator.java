package com.sun.tracker.parser;

import java.util.Comparator;

public class CityWeatherComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		City r1 = (City) o1;
		City r2 = (City) o2;
		
		final int BEFORE = -1;
		final int EQUAL = 0;
		    final int AFTER = 1;
		    
		int r1Code = r1.code;
		int r2Code = r2.code;
		
		if(r1Code<r2Code)
			return BEFORE;
		else if(r1Code>r2Code)
			return AFTER;
			
		return EQUAL;
	}
}