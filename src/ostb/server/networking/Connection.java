package ostb.server.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import ostb.server.networking.Instruction.Inst;
import ostb.server.servers.slave.PlayerLogger;
import ostb.server.servers.slave.Slave;

/*
 * Represents a connection to a client.
 */
public class Connection extends Thread {
	private Socket socket = null;
	private Server server = null;
	private boolean run = true;
	private String serverName = null;
	private String [] groups = null;
	
	public Connection(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		new Thread() {
			@Override
			public void run() {
				run = true;
				while(run) {
					try {
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
						Object object = in.readObject();
						if(object instanceof Instruction) {
							Instruction inst = (Instruction) object;
							// If true, the client connection to the server is shutting down.
							if(inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_SHUTDOWN.toString())) {
								shutdown(false, true);
								break;
							} else {
								process(inst);
							}
						}
					} catch(IOException e) {
						e.printStackTrace();
						shutdown(false, true);
					} catch(ClassNotFoundException e) {
						e.printStackTrace();
						shutdown(false, true);
					}
				}
			}
		}.start();
	}
	
	/*
	 * Terminates the connection from the server to the client
	 */
	public void shutdown(boolean sendMessage, boolean removeFromList) {
		run = false;
		if(isSocketValid()) {
			if(sendMessage) {
				sendMessageToClient(new Instruction(new String [] {Inst.SERVER_SHUTDOWN.toString()}));
			}
			try {
				socket.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		socket = null;
		if(removeFromList) {
			server.getConnections().remove(this);
		}
	}
	
	public void process(Instruction inst) {
		if(inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_INIT.toString())) {
			serverName = inst.getData()[1];
			server.addClientConnected(serverName);
			if(inst.getData().length > 2) {
				groups = new String[inst.getData().length - 2];
				for(int i = 2; i < inst.getData().length; i++) {
					groups[i - 2] = inst.getData()[i];
				}
			}
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_SEND_TO_ALL.toString())) { // SERVER_INST, CLIENT_INST, EXTRA_DATA
			if(inst.getData().length == 3) {
				try {
					Inst i = Inst.valueOf(inst.getData()[1].toUpperCase());
					server.sendMessageToAllClients(new Instruction(new String[]{i.toString(), inst.getData()[2]}));
				} catch(IllegalArgumentException e) {
					// Do nothing
				}
			}
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_SEND_TO_CLIENT.toString())) { // SERVER_INST, CLIENT_INST, CLIENT, EXTRA_DATA
			if(inst.getData().length == 4) {
				String serverName = inst.getData()[2];
				if(server.getClientsConnected().contains(serverName) && !server.doesClientExist(serverName)) {
					//CommandDispatcher.dispatch(serverName, inst.getData()[3]);
				} else {
					Inst i = null;
					try {
						i = Inst.valueOf(inst.getData()[1].toUpperCase());
					} catch(IllegalArgumentException e) {
						return;
					}
					if(Slave.getServer().doesClientExist(serverName)) {
						Connection connection = Slave.getServer().getConnection(serverName);
						connection.sendMessageToClient(new Instruction(new String[]{i.toString(), inst.getData()[3]}));
					}
				}
			}
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_SEND_TO_GROUP.toString())) { // SERVER_INST, CLIENT_INST, CLIENT_TYPE, EXTRA_DATA
			if(inst.getData().length == 4) {
				Inst i = null;
				try {
					i = Inst.valueOf(inst.getData()[1].toUpperCase());
				} catch(IllegalArgumentException e) {
					return;
				}
				List<Connection> conns = Slave.getServer().getConnectionsOfType(inst.getData()[2]);
				for(Connection connection : conns) {
					connection.sendMessageToClient(new Instruction(new String [] {i.toString(), inst.getData()[3]}));
				}
			}
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_LOG_PLAYER.toString())) { // SERVER_INST, PLAYER_UUID, SERVER_CONNECTED_TO
			UUID uuid = UUID.fromString(inst.getData()[1]);
			String name = inst.getData()[2];
			PlayerLogger.log(uuid, name);
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_PLAYER_DISCONNECT.toString())) { // SERVER_INST, PLAYER_UUID
			UUID uuid = UUID.fromString(inst.getData()[1]);
			PlayerLogger.remove(uuid);
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_GET_PLAYER_LOCATION.toString())) { // SERVER_INST, CLIENT_NAME, PLAYER_UUID
			String client = inst.getData()[1];
			UUID uuid = UUID.fromString(inst.getData()[2]);
			String server = PlayerLogger.getSever(uuid);
			sendMessageToClient(new Instruction(new String [] {Inst.CLIENT_RECEIEVE_PLAYER_LOCATION.toString(), Inst.CLIENT_COMMAND.toString(), client, server, uuid.toString()}));
		}
	}
	
	public void sendMessageToClient(Instruction inst) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(inst);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSocketValid() {
		try {
			return socket != null && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public boolean isConnectionInGroup(String group) {
		if(groups == null) {
			return false;
		}
		for(String agroup : groups) {
			if(agroup.equalsIgnoreCase(group)) {
				return true;
			}
		}
		return false;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public Server getServer() {
		return server;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String[] getGroups() {
		return groups;
	}
}