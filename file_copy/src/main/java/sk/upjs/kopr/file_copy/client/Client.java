package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sk.upjs.kopr.file_copy.Searcher;
import sk.upjs.kopr.file_copy.server.Server;

public class Client extends Service<Boolean> {

	public static final String FINAL_DESTINATION = "C:\\Users\\Roniko\\Desktop\\test_client\\";

	private static ConcurrentHashMap<String, Long> copiedMap;
	private static int numOfTCP;
	private static int fileCount;
	private static long fileSize;
	private CountDownLatch latch;
	private ExecutorService executor;
	ObjectOutputStream oos;

	public Client(int numOfTCP, CountDownLatch latch) {
		this.numOfTCP = numOfTCP;
		this.latch = latch;
		

	}

	protected Task<Boolean> createTask() {
		return new Task<Boolean>() {
			protected Boolean call() {
				try {
					Socket socket = new Socket("localhost", Server.SERVER_PORT);
					oos = new ObjectOutputStream(socket.getOutputStream());
					System.out.println("Napojil sa");
					
					copiedMap = new ConcurrentHashMap<String, Long>();
					searchniFinalDestination();
					
					
					oos.writeInt(numOfTCP);
					oos.writeObject(copiedMap);
					oos.flush();

					executor = Executors.newCachedThreadPool();
					for (int i = 0; i < numOfTCP; i++) {
						Socket clientSocket = new Socket("localhost", Server.SERVER_PORT);
						FileReceiveTask recieveTask = new FileReceiveTask(clientSocket, copiedMap, latch);
						executor.execute(recieveTask);
					}

					try {
						latch.await();
					} catch (InterruptedException e) {
						System.out.println("Client Executor shutdown");
						executor.shutdownNow();
					}
					executor.close();
					socket.close();

				} catch (Exception e) {
					System.out.println("Nenapojil sa");
				}
				return false;
			}
		};
	}

	private static void searchniFinalDestination() {
		Searcher searcher = new Searcher(new File(FINAL_DESTINATION), copiedMap, false);
		try {
			long[] countSize = searcher.call();
			System.out.println("Final destination:" + 
			FINAL_DESTINATION);
			fileSize = countSize[0];
			fileCount = Long.valueOf(countSize[1]).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
