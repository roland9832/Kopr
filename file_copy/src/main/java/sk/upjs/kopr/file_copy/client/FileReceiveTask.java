package sk.upjs.kopr.file_copy.client;

import java.awt.AlphaComposite;
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
				String mapKey = osi.readUTF();
				System.out.println("Save File Name: " + mapKey + Thread.currentThread().getName() );
				if (mapKey.equals(Searcher.POISON_PILL.getName())) {
					break;
				}
				File saveFile = new File(Client.FINAL_DESTINATION + '\\' + mapKey);
				File parentOfFile = saveFile.getParentFile();
				parentOfFile.mkdirs();
				RandomAccessFile raf = new RandomAccessFile(saveFile, "rw");

				if (copiedMap.containsKey(mapKey)) {
					offset = copiedMap.get(mapKey);
				} else {
					offset = 0;
				}

				long fileSize = osi.readLong();

				raf.setLength(fileSize);
				byte[] recievedBytes = new byte[BUFFER_SIZE];
				raf.seek(offset);
				int veReadBytes = 0;
				boolean running = true;
				while (offset < fileSize && running) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					running = writeData(fileSize, recievedBytes, veReadBytes, raf, osi);
				}
				raf.close();
				if (offset < fileSize) {
					copiedMap.put(mapKey, offset);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			
			Thread.currentThread().interrupt();
		}
	}

	private boolean writeData(long fileSize, byte[] recievedBytes, int veReadBytes, RandomAccessFile raf,
			ObjectInputStream osi) {
		try {
			if (fileSize - offset < recievedBytes.length) {
				veReadBytes = osi.read(recievedBytes, 0, (int) (fileSize - offset));
			} else {
				veReadBytes = osi.read(recievedBytes, 0, recievedBytes.length);
			}

			raf.seek(offset);
			raf.write(recievedBytes, 0, veReadBytes);
			offset = offset + veReadBytes;
			return true;
		} catch (Exception e) {
			System.out.println("Zapis zlihal");
			return false;
		}
	}

}
