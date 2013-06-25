package com.sun.tracker.parser;

public class CityHashMap{
	private int CodeImg;
	private int CityTemp;
	private String CityName;
	private String CityCountry;
	private String CityDistance;
	
	
	
	public CityHashMap(int codeImg, int cityTemp, String cityName,
			String cityCountry, String cityDistance) {
		super();
		CodeImg = codeImg;
		CityTemp = cityTemp;
		CityName = cityName;
		CityCountry = cityCountry;
		CityDistance = cityDistance;
	}
}