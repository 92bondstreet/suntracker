package com.sun.tracker.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.tracker.parser.ParserXMLHandler;

import android.content.Context;

public class ContainerData {

	static public Context context;
	public ContainerData() {
	}
	
	public static ArrayList getSolcities(InputStream myxml) throws IOException{
		
		// On passe par une classe factory pour obtenir une instance de sax
		SAXParserFactory fabrique = SAXParserFactory.newInstance();
		SAXParser parseur = null;
		ArrayList entries = null;
		try {
			// On "fabrique" une instance de SAXParser
			parseur = fabrique.newSAXParser();
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}

		/*
		 * Le handler sera gestionnaire du fichier XML c'est à dire que c'est lui qui sera chargé
		 * des opérations de parsing. On vera cette classe en détails ci après.
		*/
		DefaultHandler handler = new ParserXMLHandler();
		try {
			if(myxml==null)
				return null;
			else{
				parseur.parse(myxml, handler);
				// On récupère directement la liste des feeds
				entries = ((ParserXMLHandler) handler).getData();
			}
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		 catch (Exception e) {
			 return null;
			}
		// On la retourne l'array list
		 // Closing the input stream will trigger connection release
		 myxml.close();
		return entries;
	}
}