package com.example.applocatoinrevgeocod;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Xmlparse {
	static int i = 0;
	String temp = "";
	static StringBuffer sb;
	static String str1, str2;
	int counter = 0;

	void execute(String lat, String lon) {
		try {

			URLConnection conn = new URL(
					"http://maps.googleapis.com/maps/api/geocode/xml?latlng="
							+ lat + "," + lon + "&sensor=true")
					.openConnection();
			InputStream is = conn.getInputStream();
			sb = new StringBuffer();

			DefaultHandler dh = new DefaultHandler() {

				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					temp = new String(ch, start, length);
					super.characters(ch, start, length);
				}

				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {

					if (qName.equals("formatted_address")) {
						sb.append(temp);
						if (counter == 0) {
							str1 = temp;
						}
						if (counter == 1) {
							str2 = temp;
						}
						counter++;
					}

					super.endElement(uri, localName, qName);
				}

				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {

					super.startElement(uri, localName, qName, attributes);
				}

			};

			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, dh);

		} catch (Exception e) {
		}

	}
}
