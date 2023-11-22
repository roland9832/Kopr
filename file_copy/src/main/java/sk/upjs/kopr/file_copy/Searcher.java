package sk.upjs.kopr.file_copy;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.client.Client;

public class Searcher implements Callable<long[]> {

	public static final File POISON_PILL = new File("poison.pill");
	private File main;
	private BlockingQueue<File> sendQueue;
	private ConcurrentHashMap<String, Long> copiedMap;
	private static int numOfTCP;
	private static boolean serverRequest;
	private int fileCount;
	private long fileSize;

	public Searcher(File main, BlockingQueue<File> sendQue, int numOfTCP, boolean serverRequest) {
		this.main = main;
		this.numOfTCP = numOfTCP;
		this.sendQueue = sendQue;
		this.serverRequest = serverRequest;
	}

	public Searcher(File main, ConcurrentHashMap<String, Long> copiedMap, boolean serverRequest) {
		this.main = main;
		this.copiedMap = copiedMap;
		this.serverRequest = serverRequest;
	}

	// pill?
	@Override
	public long[] call() throws Exception {
		if (main.exists() && main.listFiles().length > 0) {
			search(main.listFiles());
			for(int i = 0; i < numOfTCP; i++) {
				sendQueue.offer(Searcher.POISON_PILL);
			}
		}

		return new long[] { fileSize, fileCount };
	}

	// ~ cviko 8 dirCounter
	// pill?
	private void search(File[] files) {
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && serverRequest) {
				sendQueue.offer(files[i]);
				fileCount++;
				fileSize = fileSize + files[i].length();
			}
			else if (files[i].isFile() && !serverRequest) {
				String file = files[i].getPath().substring(Client.FINAL_DESTINATION.getPath().lastIndexOf('/') + 1);
				copiedMap.put(file, files[i].length());
				fileCount++;
				fileSize = fileSize + files[i].length();
			}
			else if (files[i].isDirectory()) {
				search(files[i].listFiles());
			}
		}
	}

}
