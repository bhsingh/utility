package org.biosemantics.utility.peregrine;

import java.util.List;

import junit.framework.Assert;

import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.ontology.api.Ontology;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "peregrine-utility-context.xml" })
public class PeregrineRmiTest {

	@Autowired
	private Peregrine peregrine;

	@Autowired
	private Ontology ontology;
	
	@Test
	public void testIndexing(){
		List<IndexingResult> indexingResults = peregrine.index("Malaria", Language.DEFAULT);
		Assert.assertNotNull(indexingResults);
	}

}
