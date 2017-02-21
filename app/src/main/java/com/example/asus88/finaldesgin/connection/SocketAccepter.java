package com.example.asus88.finaldesgin.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;



public abstract class SocketAccepter {

	public static final int LISTEN_PORT = 12451;
	private volatile int port;
	private volatile Thread listenThread;
	private volatile ServerSocketChannel socket;
	private volatile SocketChannel msgChannel;
	private volatile SocketChannel dataChannel;

	public SocketAccepter(int port) {
		this.port = port;
	}

	public boolean start() {
		if (listenThread != null)
			return false;

		try {
			socket = ServerSocketChannel.open();
			socket.socket().bind(new InetSocketAddress(port));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		listenThread = new Thread() {
			public void run() {
				try {
					do {
						if (msgChannel == null) {
							msgChannel = socket.accept();
						} else {
							dataChannel = socket.accept();
						}
						if (msgChannel != null && dataChannel != null) {
							onAcceptedSocket(msgChannel, dataChannel);
							msgChannel = null;
							dataChannel = null;
						}
					} while (true);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					listenThread = null;
				}
			}
		};
		listenThread.start();
		return true;
	}

	public void stop() {
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected abstract void onAcceptedSocket(SocketChannel msgChannel, SocketChannel dataChannel);
}
