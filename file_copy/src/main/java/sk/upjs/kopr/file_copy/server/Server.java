package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import sk.upjs.kopr.file_copy.Searcher;

public class Server {

	public static final int SERVER_PORT = 5000;
	public static final File FILE_TO_SHARE = new File("C:\\Users\\Roniko\\Desktop\\test_server");

	private static ConcurrentHashMap<String, Long> copiedMap;
	private static BlockingQueue<File> sendQueue = new LinkedBlockingQueue<File>();
	private static int numOfTCP;
	private static int fileCount;
	private static long fileSize;
	private static boolean serverRequest = true;

	public static void main(String[] args) {
		Server server = new Server();
		server.runServerMethod();
	}


	public void runServerMethod() {
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

			while (true) {
				System.out.println("Cakam na ulohu...");
				Socket socket = serverSocket.accept();

				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

				numOfTCP = ois.readInt();
				System.out.println("server prijal: " + numOfTCP);
				copiedMap = (ConcurrentHashMap<String, Long>) ois.readObject();
				searchniFileToShare();
				

				ExecutorService executor = Executors.newCachedThreadPool();
				for (int i = 0; i < numOfTCP; i++) {
					Socket socketSender = serverSocket.accept();
					FileSendTask sendTask = new FileSendTask(sendQueue, socketSender, copiedMap);
					executor.execute(sendTask);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}

	private static void searchniFileToShare() {
		try {
			Searcher searcher = new Searcher(FILE_TO_SHARE, sendQueue, numOfTCP, serverRequest);
			long[] countSize = searcher.call();
			System.out.println("Searcher na serveri - prebehol som files to send!");
			fileSize = countSize[0];
			fileCount = Long.valueOf(countSize[1]).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
