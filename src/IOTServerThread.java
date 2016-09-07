import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class IOTServerThread extends Thread {

	protected DatagramSocket socket = null;
	protected BufferedReader in = null;
	protected boolean cnt = true;

	public IOTServerThread() throws IOException {
		this("IOTServerThread");
	}

	public IOTServerThread(String name) throws IOException {
		super(name);
		socket = new DatagramSocket(3333);

		
	}

	public void run() {

		
		System.out.println("running");
		while (cnt) {
			try {
				System.out.println("1");
				byte[] buf = new byte[256];

				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				System.out.println("2");
				String rsp = new String(packet.getData(), 0, packet.getLength());
				System.out.println("A:" + rsp);
				
				byte[] buf2 = ("This client status").getBytes();
			      DatagramPacket out = new DatagramPacket(buf2, buf2.length, packet.getAddress(), packet.getPort());
			      socket.send(out);
			} catch (IOException e) {
				e.printStackTrace();
				cnt = false;
			}
		}
		socket.close();
	}


}
