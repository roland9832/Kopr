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

	private long offset;
	private boolean isInterrupted;

	public FileSendTask(BlockingQueue<File> sendQueue, Socket socket, ConcurrentHashMap<String, Long> copiedMap)
			throws FileNotFoundException {
		this.sendQueue = sendQueue;
		this.socket = socket;
		this.coppiedMap = copiedMap;
	}

	@Override
	public void run() {
		runMethod();
	}

	private void runMethod() {
		System.out.println("Som vo FileSendTasku!");
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			try {
				sendFile = sendQueue.take();

				while (sendFile != Searcher.POISON_PILL) {

					String fileToSendName = sendFile.getPath().substring(Server.FILE_TO_SHARE.getPath().lastIndexOf('/') + 1);
					long fileToSendSize = sendFile.length();

//					System.out.println("------- FILE TO SEND NAME: " + fileToSendName);

					if (coppiedMap == null || coppiedMap.isEmpty() || !coppiedMap.containsKey(fileToSendName)) {
						offset = 0;

//						System.out.println("Posielam subor: " + fileToSend.getName() + " , velkost: " + fileToSendSize
//								+ " stiahnuty offset: " + offset + " cez vlakno: " + Thread.currentThread().getName());

					} else { // inak si vezmem offset z mapy stiahnutych
						offset = coppiedMap.get(fileToSendName);

//						System.out.println("Posielam ZVYSNE BAJTY zo suboru: " + fileToSend.getName() + " , velkost: "
//								+ fileToSendSize + " stiahnuty offset: " + offset + " cez vlakno: "
//								+ Thread.currentThread().getName());

						if (offset == fileToSendSize) {
//							System.out.println("!!! TENTO SUBOR NEPOSIELAM, LEBO HO UZ MAM: " + fileToSend.getName());
							sendFile = sendQueue.take();
							continue;
						}
					}

					oos.writeUTF(fileToSendName);
					oos.writeLong(fileToSendSize);
					oos.flush();

					byte[] buffer = new byte[BLOCK_SIZE];
					RandomAccessFile raf = new RandomAccessFile(sendFile, "r");
					raf.seek(offset);

					while (offset < fileToSendSize) {
						if (fileToSendSize - offset < buffer.length) {
							buffer = new byte[(int) (fileToSendSize - offset)];
						}
						offset += raf.read(buffer);
						oos.write(buffer);
//						System.out.println("******** BUFFER: " + Arrays.toString(buffer));
					}

					oos.flush();
					raf.close();

					sendFile = sendQueue.take();
				}
				oos.writeUTF("poison.pill");
				oos.flush();

			} catch (SocketException e) {
				System.out.println("Server nema partaka !!!");

			} catch (InterruptedException e) {
				isInterrupted = true;
			} finally {
				if (oos != null)
					oos.close();
				if (socket != null && socket.isConnected())
					socket.close();
				if (isInterrupted)
					Thread.currentThread().interrupt();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
