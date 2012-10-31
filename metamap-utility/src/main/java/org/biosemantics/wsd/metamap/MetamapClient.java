package org.biosemantics.wsd.metamap;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class MetamapClient {

	private MetaMapApi api;
	public static final String CUI_PATTERN = "C(\\d{7})";
	public static Pattern pattern = Pattern.compile(CUI_PATTERN);
	private static final Logger logger = LoggerFactory.getLogger(MetamapClient.class);

	private List<String> options;

	public void setOptions(List<String> options) {
		if (!CollectionUtils.isEmpty(options)) {
			this.options = options;
			api.setOptions(this.options);
		}
	}

	public MetamapClient() {
		api = new MetaMapApiImpl();
		// List<String> theOptions = new ArrayList<String>();
		// theOptions.add("-y"); // turn on Word Sense Disambiguation
		// theOptions.add("-K");// ignore stop phrases.
		// theOptions.add("-X");// truncate candidates mapping
		// if (theOptions.size() > 0) {
		// api.setOptions(theOptions);
		// }
	}

	public MetamapIndexingResult getCuis(String text) throws Exception {
		List<Result> resultList = api.processCitationsFromString(text);
		Collection<String> cuis = new ArrayList<String>();
		for (Result result : resultList) {
			List<Utterance> utterances = result.getUtteranceList();
			if (utterances.size() > 0) {
				for (Utterance utterance : result.getUtteranceList()) {
					List<PCM> pcms = utterance.getPCMList();
					if (pcms.size() > 0) {
						for (PCM pcm : pcms) {
							List<Mapping> maps = pcm.getMappingList();
							if (maps.size() > 0) {
								for (Mapping map : pcm.getMappingList()) {
									for (Ev mapEv : map.getEvList()) {
										cuis.add(mapEv.getConceptId());
										// matchedWords.addAll(mapEv.getMatchedWords());
									}
								}
							}
						}
					}
				}
			}
		}
		MetamapIndexingResult metamapIndexingResult = new MetamapIndexingResult(cuis);
		return metamapIndexingResult;
	}
	
	public List<Result> getResults(String text) throws Exception {
		List<Result> resultList = api.processCitationsFromString(text);
		return resultList;
	}
}
