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
		List<String> theOptions = new ArrayList<String>();
		theOptions.add("-y"); // turn on Word Sense Disambiguation
		theOptions.add("-K");// ignore stop phrases.
		theOptions.add("-X");// truncate candidates mapping
		if (theOptions.size() > 0) {
			api.setOptions(theOptions);
		}
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
										System.out.println(mapEv.getMatchedWords());
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

	public List<Ev> parse(String text) throws Exception {
		List<Result> resultList = api.processCitationsFromString(text);
		List<Ev> evList = new ArrayList<Ev>();
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
									evList.addAll(map.getEvList());
								}
							}
						}
					}
				}
			}
		}
		return evList;
	}

	public List<Result> getResults(String text) throws Exception {
		List<Result> resultList = api.processCitationsFromString(text);
		return resultList;
	}

	public static void main(String[] args) throws Exception {
		String[] topics = new String[] {
				"Patients with hearing loss.",
				"Patients with complicated GERD who receive endoscopy.",
				"Hospitalized patients treated for methicillin-resistant Staphylococcus aureus (MRSA) endocarditis.",
				"Patients diagnosed with localized prostate cancer and treated with robotic surgery.",
				"Patients with dementia.",
				"Patients who had positron emission tomography (PET), magnetic resonance imaging (MRI), or computed tomography (CT) for staging or monitoring of cancer.",
				"Patients with ductal carcinoma in situ (DCIS).",
				"Patients treated for vascular claudication surgically.",
				"Women with osteopenia.",
				"Patients being discharged from the hospital on hemodialysis.",
				"Patients with chronic back pain who receive an intraspinal pain-medicine pump.",
				"Female patients with breast cancer with mastectomies during admission.",
				"Adult patients who received colonoscopies during admission which revealed adenocarcinoma.",
				"Adult patients discharged home with palliative care / home hospice.",
				"Adult patients who are admitted with an asthma exacerbation.",
				"Patients who received methotrexate for cancer treatment while in the hospital.",
				"Patients with Post-traumatic Stress Disorder.",
				"Adults who received a coronary stent during an admission.",
				"Adult patients who presented to the emergency room with with anion gap acidosis secondary to insulin dependent diabetes.",
				"Patients admitted for treatment of CHF exacerbation.",
				"Patients with CAD who presented to the Emergency Department with Acute Coronary Syndrome and were given Plavix.",
				"Patients who received total parenteral nutrition while in the hospital.",
				"Diabetic patients who received diabetic education in the hospital.",
				"Patients who present to the hospital with episodes of acute loss of vision secondary to glaucoma.",
				"Patients co-infected with Hepatitis C and HIV.",
				"Patients admitted with a diagnosis of multiple sclerosis.",
				"Patients admitted with morbid obesity and secondary diseases of diabetes and or hypertension.",
				"Patients admitted for hip or knee surgery who were treated with anti-coagulant medications post-op.",
				"Patients admitted with chest pain and assessed with CT angiography.",
				"Children admitted with cerebral palsy who received physical therapy.",
				"Patients who underwent minimally invasive abdominal surgery.",
				"Patients admitted for surgery of the cervical spine for fusion or discectomy.",
				"Patients admitted for care who take herbal products for osteoarthritis.",
				"Patients admitted with chronic seizure disorder to control seizure activity.",
				"Cancer patients with liver metastasis treated in the hospital who underwent a procedure." };

		MetamapClient client = new MetamapClient();
		for (String topic : topics) {
			System.out.println("topic = " + "\t" + topic);
			List<Ev> evs = client.parse(topic);
			for (Ev ev : evs) {
				System.out.println(ev.getConceptId() + "\t" + ev.getConceptName() + "\t" + ev.getPreferredName() + "\t"
						+ ev.getMatchedWords());
			}
			System.out.println("----------------------");
		}

	}
}
