package gov.nih.nlm.ncbi.eutils;

import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeading;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeadingList;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.efetch.QualifierName;
import gov.nih.nlm.ncbi.eutils.generated.esearch.Count;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/*
 * http://www.ncbi.nlm.nih.gov/books/NBK25500/
 */
public class PubmedRestClient {

	private static final String DEFAULT_BASE_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private Client client;
	private WebResource eSearchResource;
	private WebResource eFetchResource;
	private JAXBContext jcSearch;
	private JAXBContext jcFetch;
	private Unmarshaller searchUnmarshaller;
	private Unmarshaller fetchUnmarshaller;
	private String baseUrl;
	private static final Logger logger = LoggerFactory.getLogger(PubmedRestClient.class);
	private static final String ESEARCH = "esearch.fcgi";
	private static final String EFETCH = "efetch.fcgi";

	public PubmedRestClient() throws JAXBException {
		this(DEFAULT_BASE_URL);

	}

	// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
	public PubmedRestClient(String baseUrl) throws JAXBException {
		this.baseUrl = baseUrl;
		client = Client.create();
		eSearchResource = client.resource(this.baseUrl + ESEARCH);
		eFetchResource = client.resource(this.baseUrl + EFETCH);
		jcSearch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.esearch");
		searchUnmarshaller = jcSearch.createUnmarshaller();
		jcFetch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.efetch");
		fetchUnmarshaller = jcFetch.createUnmarshaller();
	}

	public ESearchResult searchInPubmed(String searchTerm) throws JAXBException {
		MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();
		searchParams.add("db", "pubmed");
		searchParams.add("term", searchTerm);
		return search(searchParams);
	}

	public ESearchResult searchInPubmedForTitle(String title) throws JAXBException {
		MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();
		searchParams.add("db", "pubmed");
		searchParams.add("field", "title");
		searchParams.add("term", title);
		return search(searchParams);
	}

	public PubmedArticle fetchPubmedArticleForPmid(long pmid) throws JAXBException {
		MultivaluedMap<String, String> fetchParams = new MultivaluedMapImpl();
		fetchParams.add("db", "pubmed");
		fetchParams.add("id", String.valueOf(pmid));
		fetchParams.add("format", "xml");
		PubmedArticleSet pubmedArticleSet = fetch(fetchParams);
		if (pubmedArticleSet != null) {
			List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
			if (objects.size() == 1) {
				if (objects.get(0) instanceof PubmedArticle) {
					PubmedArticle pubmedArticle = (PubmedArticle) objects.get(0);
					return pubmedArticle;
				}
			}
		}
		throw new IllegalStateException();
	}

	/*
	 * Pubmed central:
	 * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?
	 * db=pmc&field=title
	 * &term=Accuracy%20of%20single%20progesterone%20test%20to%20predict%
	 * 20early%20pregnancy%20outcome%20in%20women%20with%20pain%20or%20bleeding:%20meta-analysis%20of%20cohort%20studies.
	 */
	public ESearchResult search(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making esearch query with params {}", queryParams.toString());
		InputStream is = eSearchResource.queryParams(queryParams).get(InputStream.class);
		ESearchResult searchResult = (ESearchResult) searchUnmarshaller.unmarshal(is);
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		List<Object> objects = searchResult
				.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR();
		for (Object object : objects) {
			if (object instanceof Count) {
				Count count = (Count) object;
				logger.debug("results count {}", count.getvalue());
				break;
			}
		}
		return searchResult;
	}

	// http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=11850928,11482001&format=xml
	/*
	 * Pubmed central:
	 * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db
	 * =pmc&id=3460254
	 */
	public PubmedArticleSet fetch(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making efetch query with params {}", queryParams.toString());
		InputStream is = eFetchResource.queryParams(queryParams).post(InputStream.class);
		Object obj = fetchUnmarshaller.unmarshal(is);
		PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) obj;
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		logger.debug("results count {}", pubmedArticleSet.getPubmedArticleOrPubmedBookArticle().size());
		return pubmedArticleSet;
	}

	public MeshHeadingList getMeshHeadingListForPubmedArticle(long pmid) throws JAXBException {
		logger.debug("fetchMeshHeadings query with params {}", pmid);
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("db", "pubmed");
		params.add("retmode", "xml");
		params.add("id", String.valueOf(pmid));
		PubmedArticleSet pubmedArticleSet = fetch(params);
		List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
		if (objects.size() == 1) {
			PubmedArticle pubmedArticle = (PubmedArticle) objects.get(0);
			return pubmedArticle.getMedlineCitation().getMeshHeadingList();
		}
		throw new IllegalStateException();

	}

	public static void main(String[] args) throws JAXBException {
		PubmedRestClient restClient = new PubmedRestClient();
		restClient.searchInPubmed("Malaria");
		restClient
				.searchInPubmedForTitle("Anaesthetic influences on brain haemodynamics in the rat and their significance to biochemical, neuropharmacological and drug disposition studies.");
		PubmedArticle pubmedArticle = restClient.fetchPubmedArticleForPmid(2764997L);
		logger.info("{}", pubmedArticle.getMedlineCitation().getPMID().getvalue());
		MeshHeadingList mesHeadingList = restClient.getMeshHeadingListForPubmedArticle(2764997L);
		for (MeshHeading meshHeading : mesHeadingList.getMeshHeading()) {
			for (QualifierName qualifierName : meshHeading.getQualifierName()) {
				logger.info("{} ({})/{} ({})", new Object[]{meshHeading.getDescriptorName().getvalue(),meshHeading.getDescriptorName().getMajorTopicYN(), qualifierName.getvalue(), qualifierName.getMajorTopicYN()});	
			}
		}
	}

}
