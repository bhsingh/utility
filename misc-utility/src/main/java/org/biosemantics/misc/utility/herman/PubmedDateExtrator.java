package org.biosemantics.misc.utility.herman;

import gov.nih.nlm.ncbi.eutils.PubmedRestClient;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubMedPubDate;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Joiner;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class PubmedDateExtrator {

	
	private static final String INPUT_FILE = "/Users/bhsingh/Desktop/herman/input/pmidag";
	private static final String OUT_FILE = "/Users/bhsingh/Desktop/herman/output/pmidag";
	private static final Logger logger = LoggerFactory.getLogger(PubmedDateExtrator.class);
	private static final Joiner joiner = Joiner.on(",").skipNulls();

	public static void writeGeneProtein() throws JAXBException, IOException {
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		pubmedRestClient.setBaseUrl("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("db", "pubmed");
		params.add("term", "gene and proteins");
		params.add("retmax", "2000000");
		ESearchResult eSearchResult = pubmedRestClient.search(params);
		pubmedRestClient.search(params);
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File("/Users/bhsingh/Desktop/herman/gene.txt")));
		List<BigInteger> ids = eSearchResult.getIdList().getId();
		logger.info("{}", ids.size());
		for (BigInteger id : ids) {
			csvWriter.writeNext(new String[] { id.toString() });
		}
		csvWriter.flush();
		csvWriter.close();
	}

	public static void writeDate() throws JAXBException, IOException {
		CSVReader csvReader = new CSVReader(new FileReader(new File("/Users/bhsingh/Desktop/herman/gene.txt")));
		List<String[]> lines = csvReader.readAll();
		csvReader.close();
		Map<String, Object> geneProtMap = new HashMap<String, Object>();
		for (String[] columns : lines) {
			geneProtMap.put(columns[0], null);
		}
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		pubmedRestClient.setBaseUrl("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");

		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		List<String> pmids = FileUtils.readLines(new File(INPUT_FILE));
		int size = pmids.size();
		logger.info("{}", size);
		List<String> fetchPmids = new ArrayList<String>();
		int ctr = 0;
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(OUT_FILE)));
		for (String pmid : pmids) {
			ctr++;
			fetchPmids.add(pmid);
			if (ctr % 1000 == 0 || ctr >= size) {
				logger.info("ctr:{}", ctr);
				params.clear();
				params.add("db", "pubmed");
				params.add("retmode", "xml");
				params.add("id", joiner.join(fetchPmids));
				try {
					PubmedArticleSet pubmedArticleSet = pubmedRestClient.fetch(params);
					for (PubmedArticle pubmedArticle : pubmedArticleSet.getPubmedArticle()) {
						String[] out = new String[6];
						out[0] = pubmedArticle.getMedlineCitation().getPMID().getValue().toString();
						out[1] = String.valueOf(geneProtMap.containsKey(pmid));
						try {
							out[2] = pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue()
									.getPubDate().getMonth();
						} catch (Exception e) {
						}
						try {
							out[3] = String.valueOf(pubmedArticle.getMedlineCitation().getArticle().getJournal()
									.getJournalIssue().getPubDate().getYear());
						} catch (Exception e) {
						}
						try {
							List<PubMedPubDate> dates = pubmedArticle.getPubmedData().getHistory().getPubMedPubDate();
							for (PubMedPubDate pubMedPubDate : dates) {
								if (pubMedPubDate.getPubStatus().trim().equalsIgnoreCase("pubmed")) {
									try {
										out[4] = pubMedPubDate.getMonth();
									} catch (Exception e) {
									}
									try {
										out[5] = String.valueOf(pubMedPubDate.getYear());
									} catch (Exception e) {
									}
									break;
								}
							}
						} catch (Exception e) {
						}
						csvWriter.writeNext(out);

					}
				} catch (Exception e) {
					logger.error("error", e);
					logger.error("file : {}", INPUT_FILE);
				}
				fetchPmids.clear();
				csvWriter.flush();
			}

		}
		csvWriter.flush();
		csvWriter.close();
	}

	public static void main(String[] args) throws JAXBException, IOException {
		// writeGeneProtein();
		writeDate();
	}

}
