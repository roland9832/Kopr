package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.Searcher;

public class FileSendTask implements Runnable {

	private static final int BLOCK_SIZE = 16384; // 16 kB
	private File sendFile;
	private final Socket socket;
	private static ConcurrentHashMap<String, Long> coppiedMap;
	private static BlockingQueue<File> sendQueue;
	private static int numOfTCP;
	private long offset;
	private boolean isInterrupted;

	public FileSendTask(BlockingQueue<File> sendQueue, Socket socket, ConcurrentHashMap<String, Long> copiedMap,
			int numOfTCP) throws FileNotFoundException {
		this.sendQueue = sendQueue;
		this.socket = socket;
		this.coppiedMap = copiedMap;
		this.numOfTCP = numOfTCP;
	}

	@Override
	public void run() {
		call();
	}

	private void call() {
		System.out.println("sendTask");
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			try {
				sendFile = sendQueue.take();

				while (sendFile != Searcher.POISON_PILL) {

					String sendFileName = sendFile.getPath().substring(Server.FILE_TO_SHARE.lastIndexOf('\\'));
					long sendFileSize = sendFile.length();
					if (coppiedMap.isEmpty() || !coppiedMap.containsKey(sendFileName)) {
						offset = 0;
					} else {
						offset = coppiedMap.get(sendFileName);
						if (offset == sendFileSize) {
							sendFile = sendQueue.take();
							continue;
						}
					}

					oos.writeUTF(sendFileName);
					oos.writeLong(sendFileSize);
					oos.flush();

					byte[] sendBytes = new byte[BLOCK_SIZE];
					RandomAccessFile raf = new RandomAccessFile(sendFile, "r");
					raf.seek(offset);

					while (offset < sendFileSize) {
						if (sendFileSize - offset < sendBytes.length) {
							sendBytes = new byte[(int) (sendFileSize - offset)];
						}
						offset += raf.read(sendBytes);
						oos.write(sendBytes);
					}

					oos.flush();
					raf.close();

					sendFile = sendQueue.take();
				}
				oos.writeUTF("poison.pill");
				oos.flush();

			} catch (SocketException e) {
				System.out.println("Server nema dvojicku");

			} catch (InterruptedException e) {
				isInterrupted = true;
			} finally {
				oos.close();
				socket.close();
				Thread.currentThread().interrupt();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void sendData() {

	}

}
