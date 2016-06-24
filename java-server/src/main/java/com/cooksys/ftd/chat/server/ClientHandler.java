package com.cooksys.ftd.chat.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable, Closeable {

	Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket client;
	private PrintWriter writer;
	private BufferedReader reader;
	private BlockingDeque<String> queue;

	public ClientHandler(Socket client, BlockingDeque<String> bq) throws IOException {
		super();
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new PrintWriter(client.getOutputStream(), true);
		this.queue = bq;
	}

	@Override
	public void run() {
		try {
			log.info("handling client {}", this.client.getRemoteSocketAddress());
			String username = reader.readLine().trim();
			LocalDateTime timestamp = LocalDateTime.now();
			log.info("Welcome to the chat [{}]",username);
			String intro = timestamp + ": Welcome to the chat " + username;
			this.queue.put(intro);
			
			while (!this.client.isClosed()) {
				String echo = reader.readLine();
				this.queue.put(timestamp + " " +username + ": " + echo);
			}
			
			this.close();
		} catch (IOException | InterruptedException e) {
			log.error("Handler fail! oh noes :(", e);
		}
	}

	@Override
	public void close() throws IOException {
		log.info("closing connection to client {}", this.client.getRemoteSocketAddress());
		this.client.close();
	}
	
	public void setMessage (String msg) {
		writer.print(msg);
		writer.flush();
	}

}
