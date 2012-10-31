package gov.nih.nlm.ncbi.eutils;

import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeadingList;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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

	public void setBaseUrl(String baseUrl) throws JAXBException {
		this.baseUrl = baseUrl;
		client = Client.create();
		eSearchResource = client.resource(this.baseUrl + ESEARCH);
		eFetchResource = client.resource(this.baseUrl + EFETCH);
		jcSearch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.esearch");
		searchUnmarshaller = jcSearch.createUnmarshaller();
		jcFetch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.efetch");
		fetchUnmarshaller = jcFetch.createUnmarshaller();
	}

	public ESearchResult search(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making esearch query with params {}", queryParams.toString());
		InputStream is = eSearchResource.queryParams(queryParams).get(InputStream.class);
		ESearchResult searchResult = (ESearchResult) searchUnmarshaller.unmarshal(is);
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		logger.debug("results count {}", searchResult.getCount().intValue());
		return searchResult;
	}

	// http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=11850928,11482001&format=xml
	public PubmedArticleSet fetch(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making efetch query with params {}", queryParams.toString());
		InputStream is = eFetchResource.queryParams(queryParams).post(InputStream.class);
		PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) fetchUnmarshaller.unmarshal(is);
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		logger.debug("results count {}", pubmedArticleSet.getPubmedArticle().size());
		return pubmedArticleSet;
	}

	public MeshHeadingList getMeshHeadingList(String pmid) throws JAXBException {
		logger.debug("fetchMeshHeadings query with params {}", pmid);
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("db", "pubmed");
		params.add("retmode", "xml");
		params.add("id", pmid);
		PubmedArticleSet pubmedArticleSet = fetch(params);
		List<PubmedArticle> pubmedArticles = pubmedArticleSet.getPubmedArticle();
		PubmedArticle pubmedArticle = pubmedArticles.get(0);
		return pubmedArticle.getMedlineCitation().getMeshHeadingList();

	}

	public void destroy() {
	}

	public static void main(String[] args) throws JAXBException, IOException {
		PubmedRestClient pubmedRestClient = new PubmedRestClient();
		pubmedRestClient.setBaseUrl("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/");
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("db", "pubmed");
		params.add("term", "malaria");
		params.add("retmax", "10");
		ESearchResult eSearchResult = pubmedRestClient.search(params);
		List<BigInteger> ids = eSearchResult.getIdList().getId();
		for (BigInteger bigInteger : ids) {
			System.out.println(bigInteger.intValue());
		}
	}
}
