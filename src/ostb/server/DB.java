package ostb.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import ostb.server.util.ConfigurationUtil;

public enum DB {
	// Account & other
	PLAYERS_ACCOUNTS("uuid VARCHAR(40), name VARCHAR(16), address VARCHAR(40), rank VARCHAR(20), join_time VARCHAR(10), PRIMARY KEY(uuid)"),
	PLAYERS_IP_ADDRESSES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), address VARCHAR(40), time VARCHAR(25), PRIMARY KEY(id)"),
	PLAYERS_LOCATIONS("uuid VARCHAR(40), location VARCHAR(100), PRIMARY KEY(uuid)"),
	PLAYERS_DISABLED_MESSAGES("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_CHAT_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), rank VARCHAR(20), server VARCHAR(25), time VARCHAR(50), message VARCHAR(250), PRIMARY KEY(id)"),
	PLAYERS_BLOCKED_MESSAGES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), server VARCHAR(25), message VARCHAR(250), PRIMARY KEY(id)"),
	PLAYERS_ARROW_TRAILS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_ACHIEVEMENTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game_name VARCHAR(25), achievement VARCHAR(100), PRIMARY KEY(id)"),
	PLAYERS_SETTINGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), setting VARCHAR(50), state INT, PRIMARY KEY(id)"),
	PLAYERS_WIN_EFFECTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_NOTIFICATIONS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), text VARCHAR(250), seen INT, PRIMARY KEY(id)"),
	PLAYERS_LEVELS("uuid VARCHAR(40), level INT, exp INT, PRIMARY KEY(uuid)"),
	PLAYERS_CHAT_COLOR("uuid VARCHAR(40), color VARCHAR(2), PRIMARY KEY(uuid)"),
	// Play time
	PLAYERS_LIFETIME_PLAYTIME("uuid VARCHAR(40), days INT, hours INT, minutes INT, seconds INT, PRIMARY KEY(uuid)"),
	PLAYERS_MONTHLY_PLAYTIME("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), days INT, hours INT, minutes INT, seconds INT, month INT,  PRIMARY KEY(id)"),
	PLAYERS_WEEKLY_PLAYTIME("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), days INT, hours INT, minutes INT, seconds INT, week INT, PRIMARY KEY(id)"),
	// Kits
	PLAYERS_KITS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), kit VARCHAR(50), PRIMARY KEY(id)"),
	PLAYERS_DEFAULT_KITS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game VARCHAR(25), kit VARCHAR(25), PRIMARY KEY(id)"),
	// Statistics
	PLAYERS_STAT_RESETS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	// Votes
	PLAYERS_LIFETIME_VOTES("uuid VARCHAR(40), amount INT, day INT, streak INT, highest_streak INT, PRIMARY KEY(uuid)"),
	PLAYERS_MONTHLY_VOTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, month INT, PRIMARY KEY(id)"),
	PLAYERS_WEEKLY_VOTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, week INT, PRIMARY KEY(id)"),
	// Coins
	PLAYERS_COINS_SKY_WARS("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	PLAYERS_COINS_HE("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	// Keys
	PLAYERS_KEY_FRAGMENTS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	
	NETWORK_PROXIES("server VARCHAR(25), PRIMARY KEY(server)"),
	NETWORK_POPULATIONS("server VARCHAR(25), population INT, PRIMARY KEY(server)"),
	NETWORK_COMMAND_DISPATCHER("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)"),
	NETWORK_SERVER_STATUS("id INT NOT NULL AUTO_INCREMENT, game_name VARCHAR(25), server_number INT, listed_priority INT, lore VARCHAR(100), players INT, max_players INT, PRIMARY KEY(id)"),
	NETWORK_SERVER_LIST("data_type VARCHAR(15), data_value VARCHAR(50), PRIMARY KEY(data_type)"),
	NETWORK_MINI_GAME_PERFORMANCE("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), map VARCHAR(250), maxPlayers INT, maxMemory DOUBLE, maxMemoryTime VARCHAR(50), lowestTPS DOUBLE, PRIMARY KEY(id)"),
	NETWORK_MAP_VOTES("id INT NOT NULL AUTO_INCREMENT, game_name VARCHAR(25), map VARCHAR(250), times_voted INT, PRIMARY KEY(id)"),
	NETWORK_MAP_RATINGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), map VARCHAR(250), rating INT, PRIMARY KEY(id)"),
	NETWORK_AUTO_ALERTS("text_id VARCHAR(25), how_often INT, text VARCHAR(100), PRIMARY KEY(text_id)"),
	NETWORK_BUKKIT_COMMAND_DISPATCHER("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)"),
	//NETWORK_BOOSTERS("id INT NOT NULL AUTO_INCREMENT, plugin VARCHAR(30), minute INT, PRIMARY KEY(id)"),
	NETWORK_ATTACK_DISTANCE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), distance DOUBLE, PRIMARY KEY(id)"),
	NETWORK_CPS_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), cps INT, PRIMARY KEY(id)"),
	NETWORK_DISTANCE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), distance DOUBLE, PRIMARY KEY(id)"),
	NETWORK_POWER_BOW_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), shots INT, PRIMARY KEY(id)"),
	
	// Perks
	HUB_ARMOR("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), type VARCHAR(15), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	HUB_SPINNING_BLOCKS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	HUB_HALO_PARTICLES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), name VARCHAR(25), active INT, amount_owned INT, unlocked_time VARCHAR(10), PRIMARY KEY(id)"),
	// Parkour
	HUB_PARKOUR("uuid VARCHAR(40), check_point VARCHAR(200), PRIMARY KEY(uuid)"),
	HUB_PARKOUR_FREE_CHECKPOINTS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_TIMES("uuid VARCHAR(40), seconds INT, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_ENDLESS_SCORES("uuid VARCHAR(40), best_score INT, PRIMARY KEY(uuid)"),
	// Crates
	HUB_CRATE_KEYS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, PRIMARY KEY(id)"),
	HUB_LIFETIME_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, PRIMARY KEY(id)"),
	HUB_MONTHLY_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, month INT, PRIMARY KEY(id)"),
	HUB_WEEKLY_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), amount INT, week INT, PRIMARY KEY(id)"),
	HUB_CRATE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), type VARCHAR(25), reward VARCHAR(100), time VARCHAR(25), PRIMARY KEY(id)"),
	// Sky Wars Crates
	HUB_SKY_WARS_CRATE_KEYS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_LIFETIME_SKY_WARS_CRATES_OPENED("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_MONTHLY_SKY_WARS_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, month INT, PRIMARY KEY(id)"),
	HUB_WEEKLY_SKY_WARS_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, week INT, PRIMARY KEY(id)"),
	HUB_SKY_WARS_CRATE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reward VARCHAR(100), PRIMARY KEY(id)"),
	// Hardcore Elimination Crates
	HUB_HE_CRATE_KEYS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_LIFETIME_HE_CRATES_OPENED("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_MONTHLY_HE_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, month INT, PRIMARY KEY(id)"),
	HUB_WEEKLY_HE_CRATES_OPENED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), amount INT, week INT, PRIMARY KEY(id)"),
	HUB_HE_CRATE_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reward VARCHAR(100), PRIMARY KEY(id)"),
	
	STAFF_ONLINE("uuid VARCHAR(40), server VARCHAR(100), PRIMARY KEY(uuid)"),
	STAFF_CHAT("id INT NOT NULL AUTO_INCREMENT, command VARCHAR(250), PRIMARY KEY(id)"),
	STAFF_MUTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), address VARCHAR(40), reason VARCHAR(100), proof VARCHAR(100), date VARCHAR(10), time VARCHAR(25), expires VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_BAN("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), attached_uuid VARCHAR(40), staff_uuid VARCHAR(40), who_unbanned VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), unban_date VARCHAR(10), unban_time VARCHAR(25), day INT, active INT, PRIMARY KEY(id)"),
	STAFF_BAN_PROOF("id INT NOT NULL AUTO_INCREMENT, ban_id INT, proof VARCHAR(100), PRIMARY KEY(id)"),
	STAFF_UNMUTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_REPORTS("id INT NOT NULL AUTO_INCREMENT, reporting VARCHAR(40), uuid VARCHAR(40), text VARCHAR(250), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_CLOSED_REPORTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date_closed VARCHAR(10), amount INT, PRIMARY KEY(id)"),
	STAFF_TICKETS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reported_uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), reason_closed VARCHAR(30), comments VARCHAR(100), playtime VARCHAR(40), proof VARCHAR(100), time_opened VARCHAR(25), date_closed VARCHAR(10), time_closed VARCHAR(25), opened BOOL, PRIMARY KEY(id)"),
	STAFF_TICKETS_CLOSED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date_closed VARCHAR(10), amount INT, PRIMARY KEY(id)"),
	STAFF_COMMANDS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), time VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)");
	
	private String table = null;
	private String keys = "";
	private Databases database = null;
	
	private DB(String query) {
		String databaseName = toString().split("_")[0];
		database = Databases.valueOf(databaseName);
		table = toString().replace(databaseName, "");
		table = table.substring(1, table.length()).toLowerCase();
		String [] declarations = query.split(", ");
		for(int a = 0; a < declarations.length - 1; ++a) {
			String declaration = declarations[a].split(" ")[0];
			if(!declaration.equals("id")) {
				keys += "`" + declaration + "`, ";
			}
		}
		keys = keys.substring(0, keys.length() - 2); 
		database.connect();
		try {
			if(database.getConnection() != null) {
				database.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (" + query + ")").execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return table;
	}
	
	public Connection getConnection() {
		return this.database.getConnection();
	}
	
	public boolean isKeySet(String key, String value) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(" + key + ") FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getInt(1) > 0;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public boolean isKeySet(String [] keys, String [] values) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT COUNT(" + keys[0] + ") FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query + " LIMIT 1");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getInt(1) > 0;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public boolean isUUIDSet(UUID uuid) {
		return isUUIDSet("uuid", uuid);
	}
	
	public boolean isUUIDSet(String key, UUID uuid) {
		return isKeySet(key, uuid.toString());
	}
	
	public int getInt(String key, String value, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getInt(String [] keys, String [] values, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public void updateInt(String set, int update, String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void updateInt(String set, int update, String [] keys, String [] values) {
		PreparedStatement statement = null;
		try {
			String query = "UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 0; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void updateBoolean(String set, boolean update, String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + (update ? "1" : "0") + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public String getString(String key, String value, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getString(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public String getString(String [] keys, String [] values, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query + " LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getString(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getAllStrings(String colum) {
		return getAllStrings(colum, "", "");
	}
	
	public List<String> getAllStrings(String colum, String key, String value) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + colum + " FROM " + getName();
			if(key != null && !key.equals("") && value != null && !value.equals("")) {
				query += " WHERE " + key + " = '" + value + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(colum));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return results;
	}
	
	public List<String> getAllStrings(String colum, String [] keys, String [] values) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + colum + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(colum));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return results;
	}
	
	public List<String> getAllStrings(String colum, String key, String value, String limit) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + colum + " FROM " + getName();
			if(key != null && value != null) {
				query += " WHERE " + key + " = '" + value + "'";
			}
			query += " LIMIT " + limit;
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(colum));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return results;
	}
	
	public void updateString(String set, String update, String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void updateString(String set, String update, String [] keys, String [] values) {
		PreparedStatement statement = null;
		try {
			String query = "UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public boolean getBoolean(String key, String value, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getBoolean(requested);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public int getSize() {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(*) FROM " + getName());
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getSize(String key, String value) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(" + key + ") FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getSize(String [] keys, String [] values) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT COUNT(" + keys[0] + ") FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public List<String> getOrdered(String orderBy, String requested, String key, String value, long limit) {
		return getOrdered(orderBy, requested, key, value, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, String key, String value, long limit, boolean descending) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit > 0 ? " LIMIT " + limit : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getOrdered(String orderBy, String requested, int limit) {
		return getOrdered(orderBy, requested, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, int limit, boolean descending) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit > 0 ? " LIMIT " + limit : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public void delete(String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("DELETE FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void delete(String [] keys, String [] values) {
		PreparedStatement statement = null;
		try {
			String query = "DELETE FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void deleteUUID(UUID uuid) {
		deleteUUID("uuid", uuid);
	}
	
	public void deleteUUID(String key, UUID uuid) {
		delete(key, uuid.toString());
	}
	
	public void deleteAll() {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("DELETE FROM " + getName());
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public boolean insert(String values) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("INSERT INTO " + getName() + " (" + keys + ") VALUES (" + values + ")");
			statement.execute();
			return true;
		} catch(SQLException e) {
			if(!e.getMessage().startsWith("Duplicate entry")) {
				e.printStackTrace();
			}
		} finally {
			close(statement);
		}
		return false;
	}
	
	public boolean execute(String sql) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
		return false;
	}
	
	public static void close(PreparedStatement statement, ResultSet resultSet) {
		close(statement);
		close(resultSet);
	}
	
	public static void close(PreparedStatement statement) {
		try {
			if(statement != null) {
				statement.close();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(ResultSet resultSet) {
		try {
			if(resultSet != null) {
				resultSet.close();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public enum Databases {
		PLAYERS, NETWORK, HUB, STAFF;
		
		private Connection connection = null;
		
		public void connect() {
			try {
				if(connection == null || connection.isClosed()) {
					ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/../db.yml");
					String address = config.getConfig().getString("address");
					String user = config.getConfig().getString("user");
					String password = config.getConfig().getString("password");
					connection = DriverManager.getConnection("jdbc:mysql://" + address + ":3306/" + toString().toLowerCase(), user, password);
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		public Connection getConnection() {
			return this.connection;
		}
		
		public void disconnect() {
			if(connection != null) {
				try {
					connection.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
