package com.sun.tracker.parser;

import java.util.Comparator;

public class CityDistanceComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		City r1 = (City) o1;
		City r2 = (City) o2;
		
		final int BEFORE = -1;
		final int EQUAL = 0;
		    final int AFTER = 1;
		    
		double r1Distance = r1.distance;
		double r2Distance = r2.distance;
		
		if(r1Distance<r2Distance)
			return BEFORE;
		else if(r1Distance>r2Distance)
			return AFTER;
			
		return EQUAL;
	}
}