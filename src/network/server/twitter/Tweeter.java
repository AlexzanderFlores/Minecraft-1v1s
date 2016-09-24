package network.server.twitter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Tweeter {
	private static String consumerKey = null;
	private static String consumerSecret = null;
	private static String accessToken = null;
	private static String accessSecret = null;
	private ConfigurationBuilder cb = null;
	private TwitterFactory factory = null;
	private Twitter twitter = null;
	
	public Tweeter(String key, String cSecret, String token, String aSecret) {
		consumerKey = key;
		consumerSecret = cSecret;
		accessToken = token;
		accessSecret = aSecret;
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret).setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessSecret);
		factory = new TwitterFactory(cb.build());
		twitter = factory.getInstance();
	}
	
	public static String getConsumerKey() {
		return consumerKey;
	}
	
	public static String getConsumerSecret() {
		return consumerSecret;
	}
	
	public static String getAccessToken() {
		return accessToken;
	}
	
	public static String getAccessSecret() {
		return accessSecret;
	}
	
	public Twitter getTwitter() {
		return twitter;
	}
	
	public boolean isRunning() {
		return cb != null && factory != null && twitter != null;
	}
	
	public void delete() {
		consumerKey = null;
		consumerSecret = null;
		accessToken = null;
		accessSecret = null;
		cb = null;
		factory = null;
		twitter = null;
	}
	
	public List<Status> search(String text) {
		return search(text, 100);
	}
	
	public List<Status> search(String text, int max) {
		List<Status> replies = new ArrayList<Status>();
		try {
			Query query = new Query(text);
			try {
				query.setCount(max);
			} catch(Throwable e) {
				query.setCount(30);
			}
			QueryResult result = twitter.search(query);
			for(Status status : result.getTweets()) {
				replies.add(status);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return replies;
	}
	
	public boolean like(long id) {
		try {
			Status status = twitter.showStatus(id);
			if(status.isFavorited()) {
				return false;
			}
			twitter.createFavorite(id);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean follow(User user) {
		try {
			if(twitter.showFriendship(twitter.getId(), user.getId()) == null) {
				return false;
			} else {
				twitter.createFriendship(user.getId());
				return true;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public List<Status> getReplies(long id) {
		Status status = null;
		try {
			status = twitter.showStatus(id);
		} catch(Exception e) {
			e.printStackTrace();
			return new ArrayList<Status>();
		}
		String screenName = status.getUser().getScreenName();
		return search("@" + screenName + " since_id:" + id);
	}

	public long tweet(String text) {
		return tweet(text, null, -1);
	}
	
	public long tweet(String text, String media) {
		return tweet(text, media, -1);
	}
	
	public long tweet(String text, long replyID) {
		return tweet(text, null, replyID);
	}
	
	public long tweet(String text, String media, long replyID) {
		long id = -1;
		if(consumerKey == null || consumerSecret == null || accessToken == null || accessSecret == null) {
			return -2;
		}
		try {
			StatusUpdate update = new StatusUpdate(text);
			if(replyID != -1) {
				update.setInReplyToStatusId(replyID);
			}
			if(media != null) {
				try {
					update.media(new File(media));
				} catch(Exception e) {
					e.printStackTrace();
					return -3;
				}
			}
			Status status = twitter.updateStatus(update);
			id = status.getId();
		} catch(Exception e) {
			e.printStackTrace();
			return -4;
		}
		return id;
	}
}
