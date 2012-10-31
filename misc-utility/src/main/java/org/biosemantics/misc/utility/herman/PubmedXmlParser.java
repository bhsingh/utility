package org.biosemantics.misc.utility.herman;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class PubmedXmlParser {

	public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(
				"/Users/bhsingh/Downloads/pubmed_result.xml"));
		PubmedDateData pubmedDateData = null;
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equalsIgnoreCase("PubmedArticle")) {
					pubmedDateData = new PubmedDateData();
				}
				if (startElement.getName().getLocalPart().equalsIgnoreCase("PMID")) {
					event = eventReader.nextEvent();
					pubmedDateData.setPmid(Integer.valueOf(event.asCharacters().getData()));
				}
				if (startElement.getName().getLocalPart().equalsIgnoreCase("PubDate")) {
					event = eventReader.nextEvent();
					event = eventReader.nextEvent();
					event = eventReader.nextEvent();
					pubmedDateData.setJournalYear(event.asCharacters().getData());
					event = eventReader.nextEvent();
					event = eventReader.nextEvent();
					event = eventReader.nextEvent();
					pubmedDateData.setJournalMonth(event.asCharacters().getData());
				}
				if (startElement.getName().getLocalPart().equalsIgnoreCase("PubMedPubDate")) {
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (attribute.getValue().equals("pubmed")) {
							event = eventReader.nextEvent();
							
							pubmedDateData.setPubYear(event.asCharacters().getData());
							event = eventReader.nextEvent();
							
							pubmedDateData.setPubMonth(event.asCharacters().getData());
						}
					}
				}

			}
			if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equalsIgnoreCase("PubmedArticle")) {
					System.out.println(pubmedDateData);
				}
			}
		}

	}
}

class PubmedDateData {
	private int pmid;
	private String pubMonth;
	private String pubYear;
	private String journalMonth;
	private String journalYear;

	@Override
	public String toString() {
		return "PubmedDateData [pmid=" + pmid + ", pubMonth=" + pubMonth + ", pubYear=" + pubYear + ", journalMonth="
				+ journalMonth + ", journalYear=" + journalYear + "]";
	}

	public void setPmid(int pmid) {
		this.pmid = pmid;
	}

	public void setPubMonth(String pubMonth) {
		this.pubMonth = pubMonth;
	}

	public void setPubYear(String pubYear) {
		this.pubYear = pubYear;
	}

	public void setJournalMonth(String journalMonth) {
		this.journalMonth = journalMonth;
	}

	public void setJournalYear(String journalYear) {
		this.journalYear = journalYear;
	}
}
