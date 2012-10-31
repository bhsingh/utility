package org.biosemantics.misc.utility.katia;

import gov.nih.nlm.ncbi.eutils.PubmedRestClient;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeading;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.efetch.QualifierName;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Joiner;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TitleParserOsumeke {
	private static final Logger logger = LoggerFactory.getLogger("TitleParser");
	private static final Joiner joiner = Joiner.on(",").skipNulls();
	private static final Joiner pipeJoiner = Joiner.on(" | ").skipNulls();
	private static final String IN_FOLDER = "/Users/bhsingh/Desktop/katia/Drug_Event_txtfiles";
	private static final String OUT_FILE = "/Users/bhsingh/Desktop/katia/katia-task2.txt";

	public static void main(String[] args) throws IOException, JAXBException {

		File folder = new File(IN_FOLDER);
		Set<Integer> pubmedIds = new HashSet<Integer>();
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		pubmedRestClient.setBaseUrl("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(OUT_FILE)));
		File[] titleFiles = folder.listFiles();
		for (File file : titleFiles) {
			logger.info("{}", file.getName());
			List<String> lines = FileUtils.readLines(file);
			Set<String> titles = new HashSet<String>();
			for (String line : lines) {
				String title = line;
				title = title.replaceAll("\\[\\d+\\]", "");
				if (!title.endsWith("?") || !title.endsWith(".") || !title.endsWith("!")) {
					title = title + ".";
				}
				titles.add(title);
			}
			for (String title : titles) {
				MultivaluedMapImpl params = new MultivaluedMapImpl();
				params.add("db", "pubmed");
				params.add("retMax", "10");
				params.add("term", title);
				params.add("field", "title");// search by title
				try {
					ESearchResult esearchResult = pubmedRestClient.search(params);
					List<BigInteger> pmids = esearchResult.getIdList().getId();
					if (pmids.size() == 1) {
						for (BigInteger pmid : pmids) {
							pubmedIds.add(pmid.intValue());
						}
					} else if (pmids.size() == 0) {
						logger.info("file: {},  0 results for title: {}", new Object[] { file.getName(), title });
					} else {
						List<Integer> multipleIds = new ArrayList<Integer>();
						for (BigInteger pmid : pmids) {
							multipleIds.add(pmid.intValue());
						}
						String strPmids = joiner.join(multipleIds);
						MultivaluedMapImpl parameters = new MultivaluedMapImpl();
						parameters.add("db", "pubmed");
						parameters.add("id", strPmids);
						parameters.add("retmode", "xml");
						PubmedArticleSet pubmedArticleSet = pubmedRestClient.fetch(parameters);
						for (PubmedArticle pubmedArticle : pubmedArticleSet.getPubmedArticle()) {
							String pubmedTitle = null;
							try {
								pubmedTitle = pubmedArticle.getMedlineCitation().getArticle().getArticleTitle();
							} catch (Exception e) {
								// TODO: handle exception
							}
							if (pubmedTitle != null && title.equalsIgnoreCase(pubmedTitle)) {
								csvWriter.writeNext(extractData(file, pubmedArticle));
								logger.info("appox. match found for title:{}", title);
								break;
							}
						}
					}
				} catch (Exception e) {
					logger.error("", e);
				}

			}
			// get mesh headings for unique titles
			logger.info("total {} unique ids for file {} {}", new Object[] { pubmedIds.size(), file.getName() });
			String strPmids = joiner.join(pubmedIds);
			MultivaluedMapImpl params = new MultivaluedMapImpl();
			params.add("db", "pubmed");
			params.add("id", strPmids);
			params.add("retmode", "xml");
			try {
				PubmedArticleSet pubmedArticleSet = pubmedRestClient.fetch(params);
				List<PubmedArticle> articles = pubmedArticleSet.getPubmedArticle();
				for (PubmedArticle pubmedArticle : articles) {
					String[] data = extractData(file, pubmedArticle);
					csvWriter.writeNext(data);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		csvWriter.flush();
		csvWriter.close();
	}

	private static String[] extractData(File file, PubmedArticle pubmedArticle) {
		String[] data = new String[4];
		int pmid = pubmedArticle.getMedlineCitation().getPMID().getValue().intValue();
		data[0] = file.getName();
		data[1] = "" + pmid;
		try {
			List<String> headings = new ArrayList<String>();
			List<MeshHeading> meshHeadings = pubmedArticle.getMedlineCitation().getMeshHeadingList().getMeshHeading();
			for (MeshHeading meshHeading : meshHeadings) {
				if (meshHeading.getQualifierName() != null) {
					List<QualifierName> qualifierNames = meshHeading.getQualifierName();
					for (QualifierName qualifierName : qualifierNames) {
						headings.add(meshHeading.getDescriptorName().getContent() + "/" + qualifierName.getContent());
					}
				} else {
					headings.add(meshHeading.getDescriptorName().getContent());
				}
			}
			String title = pubmedArticle.getMedlineCitation().getArticle().getArticleTitle();
			data[2] = title;
			String strHeadings = pipeJoiner.join(headings);
			data[3] = strHeadings;
		} catch (Exception e) {
			logger.error("{}", pmid);
		}
		return data;
	}

}
