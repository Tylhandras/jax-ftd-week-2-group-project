package com.cooksys.ftd.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {

	Logger log = LoggerFactory.getLogger(Server.class);

	int port;
	Map<ClientHandler, Thread> handlerThreads;
	BlockingDeque<String> queue;
	MessageHandler msg;

	public Server(int port, BlockingDeque<String> bq) {
		super();
		this.port = port;
		this.handlerThreads = new ConcurrentHashMap<>();
		this.queue = bq;
	}

	@Override
	public void run() {
		log.info("Server started on port {}", this.port);
		try (ServerSocket server = new ServerSocket(this.port)) {
			msg = new MessageHandler(queue, handlerThreads);
			new Thread(msg).start();
			while (true) {
				Socket client = server.accept();
				log.info("Client connected {}", client.getRemoteSocketAddress());
				ClientHandler clientHandler = new ClientHandler(client, this.queue);
				Thread clientHandlerThread = new Thread(clientHandler);
				this.handlerThreads.put(clientHandler, clientHandlerThread);
				clientHandlerThread.start();
			}
		} catch (IOException e) {
			log.error("Server fail! oh noes :(", e);
		} finally {
			for (ClientHandler clientHandler : this.handlerThreads.keySet()) {
				try {
					clientHandler.close();
					this.handlerThreads.get(clientHandler).join();
					this.handlerThreads.remove(clientHandler);
				} catch (IOException | InterruptedException e) {
					log.warn("Failed to close handler :/", e);
				}
			}
		}
	}

}
