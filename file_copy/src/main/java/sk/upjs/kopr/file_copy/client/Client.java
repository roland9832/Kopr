package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;
import sk.upjs.kopr.file_copy.Searcher;
import sk.upjs.kopr.file_copy.server.Server;

public class Client {
	
	public static final File FINAL_DESTINATION = new File("Users/Roniko/Desktop/test_client");
	
	
	private static ConcurrentHashMap<String, Long> copiedMap;
	private static int numOfTCP;
	private static int fileCount;
	private static long fileSize;
	private static boolean serverRequest = true;
	private CountDownLatch latch;
	private ExecutorService executor;
	ObjectOutputStream oos;
	
	public Client(int numOfTCP, CountDownLatch latch) {
		this.numOfTCP = numOfTCP;
		this.latch = latch;
		this.executor = Executors.newFixedThreadPool(numOfTCP); 
		
	}
	
	protected Task<Boolean> createTask(){
		return new Task<Boolean>() {
			
			protected Boolean call(){
				try {
					Socket socket = new Socket("localhost", Server.SERVER_PORT);
					oos = new ObjectOutputStream(socket.getOutputStream());
					System.out.println("Napojil sa");
					
					searchniFinalDestination();
					
					oos.writeInt(numOfTCP);
					oos.flush();
					
					
					executor = Executors.newCachedThreadPool();
					for(int i = 0; i < numOfTCP; i++) {
						Socket clientSocket = new Socket("localhost", Server.SERVER_PORT);
						
						
						executor.execute();
					}
				
				
				}catch (Exception e) {
					System.out.println("Nenapojil sa");
				}
				
				
				
				return false;
			}
		};
	}
	
	private static void searchniFinalDestination() {
		Searcher searcher = new Searcher(FINAL_DESTINATION, copiedMap, false);
		try {
			long[] countSize = searcher.call();
			System.out.println("Searcher na serveri - prebehol som files to send!");
			fileSize = countSize[0];
			fileCount = Long.valueOf(countSize[1]).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}

	
