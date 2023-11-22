package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	public static final String FILE_TO_SHARE = "C:\\Users\\Roniko\\Desktop\\test_server\\";

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
		System.out.println("Server sa spustil");
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
			ExecutorService executor = Executors.newCachedThreadPool();
			while (true) {
				Socket socket = serverSocket.accept();

				ObjectInputStream osi = new ObjectInputStream(socket.getInputStream());
				numOfTCP = osi.readInt();
				System.out.println("Server prijal: " + numOfTCP);
				copiedMap = (ConcurrentHashMap<String, Long>) osi.readObject();
				searchniFileToShare();

				for (int i = 0; i < numOfTCP; i++) {
					Socket socketSender = serverSocket.accept();
					FileSendTask sendTask = new FileSendTask(sendQueue, socketSender, copiedMap, numOfTCP);
					executor.execute(sendTask);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();

		}
	}

	private static void searchniFileToShare() {
		try {
			Searcher searcher = new Searcher(new File(FILE_TO_SHARE), sendQueue, numOfTCP, serverRequest);
			long[] countSize = searcher.call();
			fileSize = countSize[0];
			fileCount = Long.valueOf(countSize[1]).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
