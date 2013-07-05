package com.iapplize.gcmtest.http.Parsing;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.iapplize.gcmtest.http.object.User;

public class UserHandler extends DefaultHandler{
	 
		List<User> messages;
		private User currentMessage;
		
		
		private StringBuilder builder;
		
		public List<User> getMessages(){
	        return this.messages;
	    }
		
		@Override
	    public void characters(char[] ch, int start, int length)
	            throws SAXException {
	        super.characters(ch, start, length);
	        builder.append(ch, start, length);
	    }
		
		@Override
	    public void endElement(String uri, String localName, String name)
	            throws SAXException {
	        super.endElement(uri, localName, name);
	        
	            if (localName.equalsIgnoreCase("RegId"))
	            {
	            	currentMessage.RegId = builder.toString().replace("\n", "");
	            }
	            else if (localName.equalsIgnoreCase("Email"))
	            {
	            	currentMessage.Email = builder.toString().replace("\n", "");
	            } 
	            else if (localName.equalsIgnoreCase("Updated_at"))
	            {
	            	currentMessage.Updated_at = builder.toString().replace("\n", "");
	            }
	            else if (localName.equalsIgnoreCase("MacAddress"))
	            {
	            	currentMessage.MacAddress = builder.toString().replace("\n", "");
	            }
	            else if (localName.equalsIgnoreCase("UserName"))
	            {
	            	currentMessage.UserName = builder.toString().replace("\n", "");
	            }
	            else if (localName.equalsIgnoreCase("row"))
	            {
	            	messages.add(currentMessage);
	            }
	            builder.setLength(0);    
	        
	    }

	    @Override
	    public void startDocument() throws SAXException {
	        super.startDocument();
	        messages = new ArrayList<User>();
	        builder = new StringBuilder();
	    }

	    @Override
	    public void startElement(String uri, String localName, String name,
	            Attributes attributes) throws SAXException {
	        super.startElement(uri, localName, name, attributes);
	        if (localName.equalsIgnoreCase("row")){
	            this.currentMessage = new User();
	        }
	    }
		
	}