package org.biosemantics.wsd.metamap;

import java.io.Serializable;
import java.util.Collection;

public class MetamapIndexingResult implements Serializable {

	private static final long serialVersionUID = 5360003558580774924L;
	Collection<String> cuis;

	public MetamapIndexingResult(Collection<String> cuis) {
		this.cuis = cuis;
	}

	public Collection<String> getCuis() {
		return cuis;
	}

}
