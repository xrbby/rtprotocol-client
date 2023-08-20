package me.xrbby.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RTClient implements Runnable {

	private static final Map<URI, EndpointListener> endpoints = new HashMap<>();

	private final Thread thread;
	private final Socket socket;
	private final BufferedReader reader;
	private final PrintWriter writer;
	private boolean isRunning;

	public RTClient(String serverAddress, int serverPort) throws IOException {

		this.thread = new Thread(this);
		this.socket = new Socket(serverAddress, serverPort);
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(socket.getOutputStream(), true);
	}

	public void open() {

		if(isRunning)
			return;

		isRunning = true;
		thread.start();
	}

	public static boolean registerEndpointListener(URI uri, EndpointListener endpointListener) {

		if(endpoints.containsKey(uri))
			return false;

		endpoints.put(uri, endpointListener);
		return true;
	}

	private EndpointListener getEndpointListener(URI uri) {

		if(endpoints.containsKey(uri))
			return endpoints.get(uri);

		return null;
	}

	@Override
	public void run() {

		while(isRunning)
			try {
				String receivedData = reader.readLine();

				if(receivedData != null) {
					String[] splitData = receivedData.split(" ", 2);

					URI uri = URI.create(splitData[0]);

					EndpointListener endpointListener = getEndpointListener(uri);

					if(endpointListener != null)
						endpointListener.onDataReceive(splitData[1]);
				}
			} catch(IOException exception) { exception.printStackTrace(); close(); }
	}

	public void send(URI uri, String data) {

		String packedData = uri.getPath() + " " + data;

		writer.println(packedData);
	}

	public void close() {

		try { reader.close(); }
		catch(IOException exception) { exception.printStackTrace(); }

		writer.close();

		isRunning = false;

		try { socket.close(); }
		catch(IOException exception) { exception.printStackTrace(); }

		thread.interrupt();
	}
}