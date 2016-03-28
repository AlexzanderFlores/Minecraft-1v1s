package ostb.server.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private ServerSocket serverSocket = null;
	private int port = 0;
	private static int attemptInverval = 15000;
	private List<Connection> connections = null;
	private List<String> clientsConnected = null; // all clients which have at some point connected (uses server name as an identifier)
	
	public Server(int port) {
		this.port = port;
		this.connections = new ArrayList<Connection>();
		this.clientsConnected = new ArrayList<String>();
	}
	
	/*
	 * Starts the server
	 * Server will start listening for incoming requests to connect
	 */
	public void start() {
		new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						serverSocket = new ServerSocket(port);
						listen();
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(attemptInverval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	private void listen() {
		final Server server = this;
		new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						Connection connection = new Connection(socket, server);
						connection.start();
						connections.add(connection);
					} catch (IOException e) {
						if(e.getMessage().equalsIgnoreCase("socket closed")) {
							return;
						} else {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}
	
	/*
	 * Stops the server
	 * Cuts off all connections
	 */
	public void shutdown() {
		shutdownAllClients();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Shuts down the connection to all clients.
	 */
	public void shutdownAllClients() {
		for(Connection connection : connections) {
			connection.shutdown(true, false);
		}
		connections.clear();
	}
	
	/*
	 * Sends an instruction to all clients.
	 */
	public void sendMessageToAllClients(Instruction inst) {
		for(Connection connection : connections) {
			connection.sendMessageToClient(inst);
		}
	}
	
	public void removeConnection(Connection connection) {
		connections.remove(connection);
	}
	
	public void addClientConnected(String serverName) {
		if(!clientsConnected.contains(serverName)) {
			clientsConnected.add(serverName);
		}
	}
	
	public boolean doesClientExist(String serverName) {
		for(Connection connection : connections) {
			if(connection.getServerName().equalsIgnoreCase(serverName)) {
				return true;
			}
		}
		return false;
	}
	
	public Connection getConnection(String serverName) {
		for(Connection connection : connections) {
			if(connection.getServerName().equalsIgnoreCase(serverName)) {
				return connection;
			}
		}
		return null;
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public int getPort() {
		return port;
	}
	
	public List<Connection> getConnectionsOfType(String group) {
		List<Connection> connsOfType = new ArrayList<Connection>();
		for(Connection connection : connections) {
			if(connection.isConnectionInGroup(group)) {
				connsOfType.add(connection);
			}
		}
		return connsOfType;
	}
	
	public List<Connection> getConnections() {
		return connections;
	}
	
	public List<String> getClientsConnected() {
		return clientsConnected;
	}
}