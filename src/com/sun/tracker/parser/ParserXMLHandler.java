package com.sun.tracker.parser;


import java.text.DecimalFormat;
import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ParserXMLHandler extends DefaultHandler {

	// names of the XML tags
	static public final String SOLCITIES = "SolCities";
	static public final String TOPCITIES = "TopCities";
	static public final String SOLME = "SolMe";
	static private final String CITY = "city";

	// names of the XML attributes of city tag
	static private final String NAME = "CityName";
	static private final String COUNTRY = "CityCountry";
	static private final String LATITUDE = "CityLat";
	static private final String LONGITUDE = "CityLon";
	static private final String TEMP = "CityTemp";
	static private final String CODE = "CityCode";
	static private final String CONTINENT = "CityContinent";
	static private final String YAHOO_CODE = "CityYahooCode";
	static private final String DISTANCE = "CityDistance";

	// Array list de feeds
	private ArrayList solcities;
	// Boolean permettant de savoir si nous sommes � l'int�rieur d'un item
	private boolean inSolCities;
	private boolean inSolMe;
	private boolean inTopCities;
	// Feed courant
	private City currentCity;
	// Buffer permettant de contenir les donn�es d'un tag XML
	private StringBuffer buffer;
	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		super.processingInstruction(target, data);
	}
	public ParserXMLHandler() {
		super();
	}
	// * Cette m�thode est appel�e par le parser une et une seule
	// * fois au d�marrage de l'analyse de votre flux xml.
	// * Elle est appel�e avant toutes les autres m�thodes de l'interface,
	// * � l'exception unique, �videmment, de la m�thode setDocumentLocator.
	// * Cet �v�nement devrait vous permettre d'initialiser tout ce qui doit
	// * l'�tre avant led�but du parcours du document.
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		solcities = new ArrayList();
		currentCity = new City();

		inSolCities = false;
		inSolMe = false;
		inTopCities = false;
	}
	/*
	 * Fonction �tant d�clench�e lorsque le parser trouve un tag XML
	 * C'est cette m�thode que nous allons utiliser pour instancier un nouveau feed
	 */
	@Override
	public void startElement(String uri, String localName, String name,	Attributes attributes) throws SAXException {

		try{

			// Nous r�initialisons le buffer a chaque fois qu'il rencontre un item
			buffer = new StringBuffer();
			// Ci dessous, localName contient le nom du tag rencontr�
			// Nous avons rencontr� un tag ITEM, il faut donc instancier un nouveau feed
			if (localName.equalsIgnoreCase(CITY)){
				currentCity.reset();

				// get all attributes
				String attrValue = attributes.getValue(NAME);
				currentCity.name = attrValue;
				attrValue = attributes.getValue(COUNTRY);
				currentCity.country = attrValue;
				attrValue = attributes.getValue(LATITUDE);
				currentCity.latitude = attrValue;
				attrValue = attributes.getValue(LONGITUDE);
				currentCity.longitude = attrValue;
				attrValue = attributes.getValue(TEMP);
				currentCity.temp = Integer.parseInt(attrValue);
				attrValue = attributes.getValue(CODE);
				currentCity.code = Integer.parseInt(attrValue);

				if(inSolMe){
					attrValue = attributes.getValue(YAHOO_CODE);
					currentCity.yahoo_code = Integer.parseInt(attrValue);
				}
				else
					currentCity.yahoo_code = -1;

				if(inSolCities){
					attrValue = attributes.getValue(DISTANCE);
					currentCity.distance = roundTwoDecimals(Double.parseDouble(attrValue));
				}
				else
					currentCity.distance = -1;	

				if(inTopCities){
					attributes.getValue(CONTINENT);
					currentCity.continent = attributes.getValue(CONTINENT);
				}
				else
					currentCity.continent = "";	
			}
			// Vous pouvez d�finir des actions � effectuer pour chaque item rencontr�
			if (localName.equalsIgnoreCase(SOLCITIES))
				inSolCities = true;

			if (localName.equalsIgnoreCase(SOLME))
				inSolMe = true;

			if (localName.equalsIgnoreCase(TOPCITIES))
				inTopCities = true;
		}
		catch(Exception e)
		{
			// catch to avoid a exception out scope
			return;
		}
	}
	// * Fonction �tant d�clench�e lorsque le parser � pars�
	// * l'int�rieur de la balise XML La m�thode characters
	// * a donc fait son ouvrage et tous les caract�re inclus
	// * dans la balise en cours sont copi�s dans le buffer
	// * On peut donc tranquillement les r�cup�rer pour compl�ter
	// * notre objet currentFeed
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {

		if (localName.equalsIgnoreCase(CITY))			
			solcities.add(currentCity.copy());
	}
	// * Tout ce qui est dans l'arborescence mais n'est pas partie
	// * int�grante d'un tag, d�clenche la lev�e de cet �v�nement.
	// * En g�n�ral, cet �v�nement est donc lev� tout simplement
	// * par la pr�sence de texte entre la balise d'ouverture et
	// * la balise de fermeture
	public void characters(char[] ch,int start, int length)	throws SAXException{
		String lecture = new String(ch,start,length);
		if(buffer != null) buffer.append(lecture);
	}

	private double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}


	// cette m�thode nous permettra de r�cup�rer les donn�es
	public ArrayList getData(){
		return solcities;
	}

}