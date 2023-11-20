package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
	
	public static final File FINAL_DESTINATION = new File("Users/Roniko/Desktop/test_client");
	
	private static ConcurrentHashMap<String, Long> copiedMap;
	private static BlockingQueue<File> sendQueue = new LinkedBlockingQueue<File>();
	private static int numOfTCP;
	private static int fileCount;
	private static long fileSize;
	private static boolean serverRequest = true;
	private CountDownLatch latch;
	private ExecutorService executor;
	
	public Client(int numOfTCP, CountDownLatch latch) {
		this.numOfTCP = numOfTCP;
		this.latch = latch;
	}

}
