package org.biosemantics.utility.social;

public enum TwitterAccount {

	CONSUMER_KEY(""), CONSUMER_SECRET(""), TOKEN_KEY(
			"1499105834-"), TOKEN_SECRET(
			""), APPLICATION_ID("");

	private String value;

	private TwitterAccount(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
