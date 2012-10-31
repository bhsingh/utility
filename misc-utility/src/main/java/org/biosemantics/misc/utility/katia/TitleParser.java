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
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Joiner;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TitleParser {
	private static final Logger logger = LoggerFactory.getLogger("TitleParser");
	private static final Joiner joiner = Joiner.on(",").skipNulls();
	private static final Joiner pipeJoiner = Joiner.on(" | ").skipNulls();
	private static final String IN_FILE = "/Users/bhsingh/code/git/utility/misc-utility/src/main/resources/erythromycin_acuterenalfailure_08082012.txt";
	private static final String OUT_FILE = "/Users/bhsingh/code/git/utility/misc-utility/src/main/resources/erythromycin_acuterenalfailure_08082012_out.txt";

	public static void main(String[] args) throws IOException, JAXBException {
		List<String> lines = FileUtils.readLines(new File(IN_FILE));
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		pubmedRestClient.setBaseUrl("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		List<Integer> pubmedIds = new ArrayList<Integer>();
		for (String line : lines) {
			String[] columns = line.split("\\.");
			if (columns != null && columns.length > 2) {
				String title = columns[2].trim();
				title = title.replaceAll("\\[\\d+\\]", "");
				if (!title.endsWith(".")) {
					title = title + ".";
				}
				MultivaluedMapImpl params = new MultivaluedMapImpl();
				params.add("db", "pubmed");
				params.add("retMax", "100");
				params.add("term", title);
				params.add("field", "title");// search by title
				ESearchResult esearchResult = pubmedRestClient.search(params);
				List<BigInteger> pmids = esearchResult.getIdList().getId();
				if (pmids.size() == 1) {
					for (BigInteger pmid : pmids) {
						pubmedIds.add(pmid.intValue());
					}
				} else {
					logger.info("{} pubmed ids found for title: {}", new Object[] { pmids.size(), title });
				}

			}
		}
		// get mesh headings
		String strPmids = joiner.join(pubmedIds);
		MultivaluedMapImpl params = new MultivaluedMapImpl();
		params.add("db", "pubmed");
		params.add("id", strPmids);
		params.add("retmode", "xml");
		PubmedArticleSet pubmedArticleSet = pubmedRestClient.fetch(params);
		List<PubmedArticle> articles = pubmedArticleSet.getPubmedArticle();
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(OUT_FILE)));
		for (PubmedArticle pubmedArticle : articles) {
			int pmid = pubmedArticle.getMedlineCitation().getPMID().getValue().intValue();
			try {
				List<String> headings = new ArrayList<String>();
				List<MeshHeading> meshHeadings = pubmedArticle.getMedlineCitation().getMeshHeadingList()
						.getMeshHeading();
				for (MeshHeading meshHeading : meshHeadings) {
					if (meshHeading.getQualifierName() != null) {
						List<QualifierName> qualifierNames = meshHeading.getQualifierName();
						for (QualifierName qualifierName : qualifierNames) {
							headings.add(meshHeading.getDescriptorName().getContent() + "/"
									+ qualifierName.getContent());
						}
					} else {
						headings.add(meshHeading.getDescriptorName().getContent());
					}
				}
				String title = pubmedArticle.getMedlineCitation().getArticle().getArticleTitle();
				String strHeadings = pipeJoiner.join(headings);
				csvWriter.writeNext(new String[] { "" + pmid, title, strHeadings });
			} catch (Exception e) {
				logger.error("{}", pmid);
			}
		}
		csvWriter.flush();
		csvWriter.close();
	}

}
