package org.biosemantics.misc.utility.herman;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.ncbi.eutils.PubmedRestClient;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class ESearchDateParser {

	private static final int START_YEAR = 1950;
	private static final int END_YEAR = 1979;
	private static final Logger logger = LoggerFactory.getLogger(ESearchDateParser.class);

	public static void main(String[] args) throws JAXBException, IOException {
		writeDate();
	}

	private static void writeDate() throws JAXBException, IOException {
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		pubmedRestClient.setBaseUrl("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		CSVReader csvReader = new CSVReader(new FileReader(new File("/Users/bhsingh/Desktop/herman/gene.txt")));
		List<String[]> lines = csvReader.readAll();
		csvReader.close();
		Map<String, Object> geneProtMap = new HashMap<String, Object>();
		for (String[] columns : lines) {
			geneProtMap.put(columns[0], null);
		}
		CSVWriter csvWriter = new CSVWriter(new FileWriter("/Users/bhsingh/Desktop/herman/" + START_YEAR + "-"
				+ END_YEAR + "-date.csv"));
		try {
			for (int year = START_YEAR; year <= END_YEAR; year++) {
				for (int month = 1; month <= 12; month++) {
					MultivaluedMapImpl params = new MultivaluedMapImpl();
					params.add("db", "pubmed");
					params.add("retMax", "4000000");
					params.add("term", year + "/" + month);
					params.add("field", "Date - Publication");// search by date
					ESearchResult esearchResult = pubmedRestClient.search(params);
					List<BigInteger> ids = esearchResult.getIdList().getId();
					logger.info("{} {} {}", new Object[] { month, year, ids.size() });
					for (BigInteger id : ids) {
						csvWriter.writeNext(new String[] { "" + id, "" + geneProtMap.containsKey(id.toString()),
								"" + month, "" + year });
					}
				}
			}
			csvWriter.flush();
		} finally {
			csvWriter.close();
		}
	}
}
