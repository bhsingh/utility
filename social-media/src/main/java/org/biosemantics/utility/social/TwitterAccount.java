package org.biosemantics.utility.social;

public enum TwitterAccount {

	CONSUMER_KEY("gnky5UluLfPQwJix80b7tQ"), CONSUMER_SECRET("Rvl1iu2pjcValNn3fD5yVH51dvAWAyGMzbLycWIQy8"), TOKEN_KEY(
			"1499105834-I6RSjiYb4vfv0O5ckN214BKZkCPMaBBzGCwBdfT"), TOKEN_SECRET(
			"qJn3sQt4DvJrQYF5GWOlm6VTM2wOqe3OhJmygsDKT8"), APPLICATION_ID("MIEUR");

	private String value;

	private TwitterAccount(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
