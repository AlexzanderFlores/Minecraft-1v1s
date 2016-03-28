package ostb.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Tweeter {
	//TODO: Make an enum with keys in this one file
	private static String consumerKey = null;
	private static String consumerSecret = null;
	private static String accessToken = null;
	private static String accessSecret = null;
	private static ConfigurationBuilder cb = null;
	private static TwitterFactory factory = null;
	private static Twitter twitter = null;
	private static Status status = null;
	
	public Tweeter(String key, String cSecret, String token, String aSecret, int a) {
		consumerKey = key;
		consumerSecret = cSecret;
		accessToken = token;
		accessSecret = aSecret;
	}
	
	public static List<Status> getReplies() {
		if(status == null) {
			return new ArrayList<Status>();
		}
		if(cb == null) {
			 cb = new ConfigurationBuilder();
			 cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
			 .setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessSecret);
			 factory = new TwitterFactory(cb.build());
			 twitter = factory.getInstance();
		 }
		List<Status> replies = new ArrayList<Status>();
		List<Status> all = null;
		try {
			long id = status.getId();
			String screenName = status.getUser().getScreenName();
			Query query = new Query("@" + screenName + " since_id:" + id);
			try {
				query.setCount(100);
			} catch(Throwable e) {
				query.setCount(30);
			}
			QueryResult result = twitter.search(query);
			all = new ArrayList<Status>();
			do {
				List<Status> tweets = result.getTweets();
				for(Status tweet : tweets) {
					if(tweet.getInReplyToStatusId() == id) {
						all.add(tweet);
					}
				}
				if(all.size() > 0) {
					for(int a = all.size() - 1; a >= 0; --a) {
						replies.add(all.get(a));
					}
					all.clear();
				}
				query = result.nextQuery();
				if(query != null) {
					result = twitter.search(query);
				}
			} while(query != null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return replies;
	}
	
	/*public static long getID() {
		return status == null ? -1 : status.getId();
	}*/
	
	public static long tweet(String text) {
		return tweet(text, null);
	}
	
	public static long tweet(String text, String media) {
		long id = -1;
		if(consumerKey == null || consumerSecret == null || accessToken == null || accessSecret == null) {
			return -1;
		}
		 if(cb == null) {
			 cb = new ConfigurationBuilder();
			 cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
			 .setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessSecret);
			 factory = new TwitterFactory(cb.build());
			 twitter = factory.getInstance();
		 }
		 try {
			 text = ChatColor.stripColor(text);
			 StatusUpdate update = new StatusUpdate(text);
			 if(media != null) {
				 try {
					 update.media(new File(Bukkit.getWorldContainer().getPath() + "/../resources/" + media));
				 } catch(Exception e) {
					 e.printStackTrace();
					 return -1;
				 }
			 }
			 status = twitter.updateStatus(update);
			 id = status.getId();
		 } catch(TwitterException e) {
			 e.printStackTrace();
			 return -1;
		 }
		 return id;
	}
}
