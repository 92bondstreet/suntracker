package com.sun.tracker.parser;

import java.util.Comparator;

public class CityCountryComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		City r1 = (City) o1;
		City r2 = (City) o2;
		
		return r1.country.compareTo(r2.country);
	}
}