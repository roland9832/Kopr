package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import sk.upjs.kopr.file_copy.FileRequest;

public class FileReceiveTask implements Runnable{
	private static final int BUFFER_SIZE = 16384;
	private long offset;
	private long length; // length of data to be received
	private Socket socket;
	private CountDownLatch latch;
	ConcurrentHashMap<String, Long> copiedMap;
	
	
	public FileReceiveTask(Socket socket, ConcurrentHashMap<String, Long> copiedMap, CountDownLatch latch) throws IOException {
		this.socket = socket;
		this.copiedMap = copiedMap;
		this.latch = latch;
	}
	
	@Override
	public void run() {
		call();
	}


	private void call(){
		try(Socket socket = new Socket(inetAddress, serverPort)) {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		
	}

}
