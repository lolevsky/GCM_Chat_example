package com.iapplize.gcmtest.http.Parsing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.iapplize.gcmtest.http.object.User;

public class xmlParser {


	public List<User> parseUserData(String content) {

		List<User> result = new ArrayList<User>();

		UserHandler handler = new UserHandler();
		XMLReader xr;

		SAXParserFactory spf = SAXParserFactory.newInstance();

		try {
			SAXParser sp = spf.newSAXParser();

			xr = sp.getXMLReader();

			xr.setContentHandler(handler);
			InputSource is = new InputSource(new StringReader(content));
			xr.parse(is);

			result = handler.getMessages();
		} catch (SAXException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return result;
	}


}
