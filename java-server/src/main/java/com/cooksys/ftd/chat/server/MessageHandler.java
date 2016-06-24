package com.cooksys.ftd.chat.server;

import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandler implements Runnable {
	
	Logger log = LoggerFactory.getLogger(MessageHandler.class);
	
	BlockingDeque<String> queue;
	Map<ClientHandler, Thread> handlerThreads;
	
	public MessageHandler(BlockingDeque<String> bq, Map<ClientHandler, Thread> map) {
		super();
		this.queue = bq;
		this.handlerThreads = map;
	}

	@Override
	public void run() {
		try {
			while (true) {
				if (queue.size() > 0) {
					String temp = queue.takeFirst();
					for (ClientHandler c : handlerThreads.keySet()) {
						c.setMessage(temp);
					}
				}
			}
		} catch (InterruptedException e) {
			log.error("Message failed to send", e);
		}
	}
}
