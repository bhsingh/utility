package org.biosemantics.common.utility.wytze;

import gov.nih.nlm.ncbi.eutils.PubmedRestClient;
import gov.nih.nlm.ncbi.eutils.generated.efetch.Chemical;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeading;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.QualifierName;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
import gov.nih.nlm.ncbi.eutils.generated.esearch.Id;
import gov.nih.nlm.ncbi.eutils.generated.esearch.IdList;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Joiner;

public class MigraineChemicalFinder {

	public static void main(String[] args) throws JAXBException, IOException {
		Map<String, Set<Integer>> chemicalMap = new HashMap<String, Set<Integer>>();
		Map<String, Set<Integer>> spinalMap = new HashMap<String, Set<Integer>>();
		CSVWriter chemWriter = new CSVWriter(new FileWriter("/Users/bhsingh/Desktop/chemicals.txt"));
		CSVWriter spinalWriter = new CSVWriter(new FileWriter("/Users/bhsingh/Desktop/spine.txt"));
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		ESearchResult eSearchResult = pubmedRestClient.searchPubmed("migraine disorders[MeSH]", 30000);
		List<Object> objectList = eSearchResult
				.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR();
		int ctr = 0;
		for (Object object : objectList) {
			if (object instanceof IdList) {
				logger.debug("found");
				IdList idList = (IdList) object;
				for (Id id : idList.getId()) {
					int pmid = Integer.valueOf(id.getvalue());
					PubmedArticle pubmedArticle = pubmedRestClient.fetchPubmedArticle(pmid);
					try {
						for (Chemical chemical : pubmedArticle.getMedlineCitation().getChemicalList().getChemical()) {
							String substanceName = chemical.getNameOfSubstance();
							Set<Integer> pmidSet = null;
							if (chemicalMap.containsKey(substanceName)) {
								pmidSet = chemicalMap.get(substanceName);
							} else {
								pmidSet = new HashSet<Integer>();
							}
							pmidSet.add(pmid);
							chemicalMap.put(substanceName, pmidSet);
						}
					} catch (Exception e) {
						// do nothing here if no chemicals found
					}
					try {
						for (MeshHeading meshHeading : pubmedArticle.getMedlineCitation().getMeshHeadingList()
								.getMeshHeading()) {
							boolean insert = false;
							for (QualifierName qualifierName : meshHeading.getQualifierName()) {
								if (qualifierName.getvalue().contains("cerebrospinal fluid")) {
									insert = true;
									break;
								}
							}
							if (insert) {
								String descName = meshHeading.getDescriptorName().getvalue();
								Set<Integer> pmidSet = null;
								if (spinalMap.containsKey(descName)) {
									pmidSet = spinalMap.get(descName);
								} else {
									pmidSet = new HashSet<Integer>();
								}
								pmidSet.add(pmid);
								spinalMap.put(descName, pmidSet);
							}
						}
					} catch (Exception e) {
						// do nothing here
					}
					logger.debug("{}", ++ctr);
				}// all parsed
				logger.debug("PARSED: {}", ctr);
				for (Entry<String, Set<Integer>> entry : chemicalMap.entrySet()) {
					chemWriter.writeNext(new String[] { entry.getKey(), "" + entry.getValue().size(),
							joiner.join(entry.getValue()) });
				}
				chemWriter.flush();
				chemWriter.close();

				for (Entry<String, Set<Integer>> entry : spinalMap.entrySet()) {
					spinalWriter.writeNext(new String[] { entry.getKey(), "" + entry.getValue().size(),
							joiner.join(entry.getValue()) });
				}
				spinalWriter.flush();
				spinalWriter.close();
			}
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(MigraineChemicalFinder.class);
	private static Joiner joiner = Joiner.on("|").skipNulls();
}
