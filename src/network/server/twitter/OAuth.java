package network.server.twitter;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import network.player.MessageHandler;
import network.server.DB;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class OAuth {
	private static RequestToken requestToken = null;
	
	public static String getURL() {
		try {
			Twitter twitter = new TwitterFactory().getInstance();
			requestToken = twitter.getOAuthRequestToken();
			return requestToken.getAuthorizationURL();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String setPin(UUID uuid, String pin) {
		try {
    		AccessToken accessToken = TwitterFactory.getSingleton().getOAuthAccessToken(requestToken, pin);
			long id = accessToken.getUserId();
			String token = accessToken.getToken();
			String secret = accessToken.getTokenSecret();
			String key = "uuid";
			String value = uuid.toString();
			if(DB.PLAYERS_TWITTER_API_KEYS.isKeySet(key, value)) {
				DB.PLAYERS_TWITTER_API_KEYS.updateString("access_token", token, key, value);
				DB.PLAYERS_TWITTER_API_KEYS.updateString("access_secret", secret, key, value);
			} else {
				DB.PLAYERS_TWITTER_API_KEYS.insert("'" + uuid + "', '" + accessToken.getToken() + "', '" + accessToken.getTokenSecret() + "'");
			}
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				MessageHandler.sendMessage(player, "&a&lYour Twitter is now linked with 1v1s!");
			}
			return String.valueOf(id);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "-1";
	}
}
