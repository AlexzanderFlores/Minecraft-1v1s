package ostb.server.networking;

import java.io.Serializable;

public class Instruction implements Serializable {
	// Set instructions.
	public static enum Inst {
		CLIENT_SHUTDOWN, // message from client to server saying that the client is shutting down
		CLIENT_FORCE_SHUTDOWN, // message from server to client to force client to shutdown
		SERVER_SHUTDOWN, // message from server to all clients saying that the server is shutting down
		CLIENT_INIT, // message from client to server initializing the server details
		SERVER_SEND_TO_ALL, // message from client to server to send an instruction to all clients (including self)
		CLIENT_COMMAND, // message from server to client to dispatch a command
		SERVER_SEND_TO_CLIENT, // message from client to server to send an instruction to a specific client (server specified)
		SERVER_SEND_TO_GROUP, // message from client to server to send an instruction to all clients of a specific group (example: SG)
		SERVER_LOG_PLAYER, // message from client to server to store where a player logged in at
		SERVER_PLAYER_DISCONNECT, // message from client to server to remove a player's stored location
		SERVER_GET_PLAYER_LOCATION, // message from client to server to send an instruction back to the client of where a player is
		CLIENT_RECEIEVE_PLAYER_LOCATION // message from server to client to tell the client where the requested player's location is
	}

	private static final long serialVersionUID = -785604691741278616L;
	private String [] data = null;
	
	public Instruction(String [] data) {
		this.data = data;
	}
	
	public String [] getData() {
		return data;
	}
}