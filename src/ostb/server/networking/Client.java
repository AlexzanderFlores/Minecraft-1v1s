package ostb.server.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.PlayerLocationEvent;
import ostb.server.networking.Instruction.Inst;

public class Client {
	private Client instance = null;
	private String ip = null;
	private int port = 0;
	private int timeout = 5000; // connection timeout in milliseconds
	private static int attemptInterval = 15000;
	private Socket socket = null;
	
	public Client(String ip, int port) {
		this.ip = ip;
		this.port = port;
		instance = this;
	}
	
	public Client(String ip, int port, int timeout) {
		this(ip, port);
		this.timeout = timeout;
	}
	
	/*
	 * Starts the client connection to the server
	 * Will attempt to connect to the server
	 */
	public void start() {
		new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						socket = new Socket();
						socket.connect(new InetSocketAddress(ip, port), timeout);
						Bukkit.getLogger().info("Connected to the server!");
						List<String> groups = ProPlugin.getGroups();
						if(groups == null || groups.isEmpty()) {
							sendMessageToServer(new Instruction(new String [] {Inst.CLIENT_INIT.toString(), OSTB.getServerName()}));
						} else {
							int size = groups.size() + 2;
							String [] inst = new String [size];
							inst[0] = Inst.CLIENT_INIT.toString();
							inst[1] = OSTB.getServerName();
							int counter = 2;
							for(String group : groups) {
								inst[counter++] = group;
							}
							sendMessageToServer(new Instruction(inst));
						}
						listen();
						break;
					} catch(IOException e) {
						
					}
					try {
						Thread.sleep(attemptInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	private void listen() {
		new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
						Object object = in.readObject();
						if(object instanceof Instruction) {
							Instruction inst = (Instruction) object;
							// If true, the connection to the server will be terminated.
							if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_SHUTDOWN.toString()) || inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_FORCE_SHUTDOWN.toString())) {
								break;
							} else {
								process(inst);
							}
						}
					} catch(Exception e) {
						shutdown(false);
						instance.start();
					}
				}
			}
		}.start();
	}
	
	public void shutdown(boolean sendMessage) {
		if(isSocketValid()) {
			if(sendMessage) {
				sendMessageToServer(new Instruction(new String [] {Inst.CLIENT_SHUTDOWN.toString()}));
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket = null;
	}
	
	public void process(Instruction inst) {
		if(inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_COMMAND.toString())) {
			if(inst.getData().length == 2) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), inst.getData()[1]);
			}
		} else if(inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_RECEIEVE_PLAYER_LOCATION.toString())) {
			String server = inst.getData()[3];
			UUID uuid = UUID.fromString(inst.getData()[4]);
			Bukkit.getPluginManager().callEvent(new PlayerLocationEvent(uuid, server));
		}
	}
	
	public void sendMessageToServer(Instruction inst) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(inst);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean isSocketValid() {
		try {
			return socket != null && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public String getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public Socket getSocket() {
		return socket;
	}
}