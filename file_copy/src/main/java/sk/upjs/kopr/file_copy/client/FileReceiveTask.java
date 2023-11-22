package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import sk.upjs.kopr.file_copy.Searcher;

public class FileReceiveTask implements Runnable {
	private static final int BUFFER_SIZE = 16384;
	private long offset;
	private long length; // length of data to be received
	private Socket socket;
	private CountDownLatch latch;
	ConcurrentHashMap<String, Long> copiedMap;
	private boolean inf;

	public FileReceiveTask(Socket socket, ConcurrentHashMap<String, Long> copiedMap, CountDownLatch latch)
			throws IOException {
		this.socket = socket;
		this.copiedMap = copiedMap;
		this.latch = latch;
//		this.inf = inf;
	}

	@Override
	public void run() {
		call();
	}

	private void call() {
		try {
			ObjectInputStream osi = new ObjectInputStream(socket.getInputStream());
			while (true) {
				String mapKey = osi.toString();
				if (mapKey.equals(Searcher.POISON_PILL.getName())) {
					break;
				}
				File saveFile = new File(Client.FINAL_DESTINATION.getAbsolutePath() + '/' + mapKey);
				System.out.println("SaveFile path: "+ Client.FINAL_DESTINATION.getAbsolutePath() + '/' + mapKey);
				File parentOfFile = saveFile.getParentFile();
				if(!parentOfFile.exists()) {
					parentOfFile.mkdirs();
				}
				
				if(copiedMap.containsKey(mapKey)) {
					offset = copiedMap.get(mapKey);
				}else {
					offset = 0;
				}
				
				long fileSize = osi.readLong();
			
				RandomAccessFile raf = new RandomAccessFile(saveFile, "rw");
				raf.setLength(fileSize);
				byte[] recievedBytes = new byte[BUFFER_SIZE];
				raf.seek(offset);
				int veReadBytes = 0;
				while(offset < fileSize) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					if(fileSize - offset < recievedBytes.length) {
						veReadBytes = osi.read(recievedBytes, 0, (int) (fileSize - offset));
					}else {
						veReadBytes = osi.read(recievedBytes, 0, recievedBytes.length);
					}
					
					raf.seek(offset);
					raf.write(recievedBytes, 0, veReadBytes);
					offset = offset + veReadBytes;
					
				}
				raf.close();
				if(offset < fileSize) {
					copiedMap.put(mapKey, offset);
				}else {
					copiedMap.put(mapKey, fileSize); //treba to vobec? offset a filesize by sa tu mal rovnat
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			Thread.currentThread().interrupt();
		}
	}

}
