package network.server.servers.slave;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import network.server.DB;
import network.server.tasks.AsyncDelayedTask;

public class Server {
	private static boolean running = true;
	private Socket connection = null;
	private DataOutputStream response = null;
	
    public Server() {
        new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ServerSocket socket = null;
		        InputStreamReader inputStream = null;
		        BufferedReader input = null;
		        try {
		            socket = new ServerSocket(25599);
		            System.out.println("Starting socket server on port " + socket.getLocalPort());
		            while(running) {
		                connection = socket.accept();
		                inputStream = new InputStreamReader(connection.getInputStream());
		                input = new BufferedReader(inputStream);
		                String command = input.readLine().toLowerCase();
		                System.out.println("Socket server input: \"" + command + "\"");
		                String reply = runCommand(command);
		                System.out.println("Socket server ouput: \"" + reply + "\"");
		                reply(reply);
		            }
		        } catch(Exception e) {
		            e.printStackTrace();
		        } finally {
		        	if(socket != null) {
		        		try {
							socket.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
		        	}
		        	if(connection != null) {
		        		try {
							connection.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
		        	}
		        	if(inputStream != null) {
		        		try {
							inputStream.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
		        	}
		        	if(input != null) {
		        		try {
							input.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
		        	}
		        	if(response != null) {
		        		try {
							response.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
		        	}
		        }
			}
		});
    }
    
    public static boolean isRunning() {
    	return running;
    }
    
    public static void setRunning(boolean running) {
    	Server.running = running;
    }
    
    public String runCommand(String command) {
    	if(command.startsWith("gettwitterurl:")) {
    		String address = command.replace("gettwitterurl:", "");
    		String result = DB.PLAYERS_TWITTER_AUTH_URLS.getString("address", address, "url");
    		return result == null ? "null" : result;
    	}
        return "";
    }
    
    private void reply(String reply) {
    	try {
    		response = new DataOutputStream(connection.getOutputStream());
        	response.writeUTF(reply);
            response.flush();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
}