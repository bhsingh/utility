package org.biosemantics.utility.social;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import au.com.bytecode.opencsv.CSVWriter;

public class TestTwitterJ {

	public static void main(String[] args) throws TwitterException, IOException {
		// search();
		testStreaming();
	}

	private static void testStreaming() throws IOException {
		final CSVWriter csvWriter = new CSVWriter(new FileWriter(""));

		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.setOAuthConsumer(TwitterAccount.CONSUMER_KEY.getValue(),
				TwitterAccount.CONSUMER_SECRET.getValue());
		twitterStream.setOAuthAccessToken(loadAccessToken());

		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {
				List<String> output = new ArrayList<String>();
				try {

					output.add("" + status.isRetweet());
					output.add("" + status.getRetweetCount());
					output.add("" + status.isTruncated());
					output.add("" + status.isFavorited());
					if (status.getCreatedAt() != null) {
						output.add(status.getCreatedAt().toGMTString());
					} else {
						output.add("NA");
					}
					if (StringUtils.isBlank(status.getInReplyToScreenName())) {
						output.add("NA");
					} else {
						output.add(status.getInReplyToScreenName());
					}
					if (status.getGeoLocation() != null) {
						output.add("" + status.getGeoLocation().getLatitude());
						output.add("" + status.getGeoLocation().getLongitude());
					} else {
						output.add("NA");
						output.add("NA");
					}
					output.add("" + status.getText());
					if (status.getHashtagEntities() != null) {
						for (HashtagEntity hashtagEntity : status.getHashtagEntities()) {
							output.add(hashtagEntity.getText());
						}
					}
					csvWriter.writeNext(output.toArray(new String[output.size()]));
					csvWriter.flush();
				} catch (Exception e) {
					logger.error("exception parsing status: ", e);
				}
				System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				logger.error("Got a status deletion notice id: {}", statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				logger.error("Got track limitation notice: {}", numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				logger.error("Got scrub_geo event userId:{} upToStatusId:{}", new Object[] { userId, upToStatusId });
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				logger.error("Got stall warning: {}", warning);
			}

			@Override
			public void onException(Exception ex) {
				logger.error("", ex);
			}
		};

		twitterStream.addListener(listener);
		FilterQuery filterQuery = new FilterQuery(0, null,
				new String[] { "Actos,Pioglitazone,Glustin,Glizone,Pioz,Zactos" });
		twitterStream.filter(filterQuery);
	}

	private static void search() throws TwitterException {
		// The factory instance is re-useable and thread safe.
		TwitterFactory factory = new TwitterFactory();
		AccessToken accessToken = loadAccessToken();
		Twitter twitter = factory.getInstance();
		twitter.setOAuthConsumer(TwitterAccount.CONSUMER_KEY.getValue(), TwitterAccount.CONSUMER_SECRET.getValue());
		twitter.setOAuthAccessToken(accessToken);
		Query query = new Query("#Actos");
		query.setLang("en");
		query.setCount(100);
		QueryResult result = twitter.search(query);
		for (Status status : result.getTweets()) {
			System.out.println(status.getCreatedAt() + " " + status.isRetweet() + " " + "@"
					+ status.getUser().getScreenName() + ":" + status.getText());
		}
	}

	private static AccessToken loadAccessToken() {
		String token = TwitterAccount.TOKEN_KEY.getValue();
		String tokenSecret = TwitterAccount.TOKEN_SECRET.getValue();
		return new AccessToken(token, tokenSecret);
	}

	private static final Logger logger = LoggerFactory.getLogger(TestTwitterJ.class);
}
