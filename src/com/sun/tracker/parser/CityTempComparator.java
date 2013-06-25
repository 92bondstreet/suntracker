package com.sun.tracker.parser;

import java.util.Comparator;

public class CityTempComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		City r1 = (City) o1;
		City r2 = (City) o2;
		
		final int BEFORE = -1;
		final int EQUAL = 0;
		    final int AFTER = 1;
		    
		int r1Temp = r1.temp;
		int r2Temp = r2.temp;
		
		if(r1Temp<r2Temp)
			return AFTER;
		else if(r1Temp>r2Temp)
			return BEFORE;
			
		return EQUAL;
	}
}