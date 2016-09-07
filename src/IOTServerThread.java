import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class IOTServerThread extends Thread {

	public class Network {
		public int redstate;
		public int greenstate;
		public ArrayList<InetAddress> peers;

		public Network(int state) {
			this.redstate = state;
			this.greenstate = 1;
			peers = new ArrayList<>();
		}
	}

	protected DatagramSocket socket = null;
	protected BufferedReader in = null;
	protected boolean cnt = true;

	public ConcurrentHashMap<String, Network> dudos;

	public IOTServerThread() throws IOException {
		this("IOTServerThread");
	}

	public IOTServerThread(String name) throws IOException {
		super(name);
		socket = new DatagramSocket(3333);

	}

	public void run() {

		dudos = new ConcurrentHashMap<>();
		System.out.println("running");
		while (cnt) {
			try {
				byte[] buf = new byte[256];

				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				String rsp = new String(packet.getData(), 0, packet.getLength());
				String[] message = rsp.split(":");
				if (message.length != 2) {
					System.out.println("Bad messagem, check your code.");
					continue;
				}
				if (!dudos.containsKey(message[0]))
					dudos.put(message[0], new Network(Integer.parseInt(message[1])));
				else {
					if (dudos.get(message[0]).redstate == 0)
						dudos.get(message[0]).redstate = 1;
					else
						dudos.get(message[0]).redstate = 0;
				}
				if (!dudos.get(message[0]).peers.contains(packet.getAddress()))
					dudos.get(message[0]).peers.add(packet.getAddress());
				for (InetAddress peer : dudos.get(message[0]).peers) {
					byte[] buf2 = ("" + dudos.get(message[0]).redstate).getBytes();
					DatagramPacket out = new DatagramPacket(buf2, buf2.length, peer, 4000);
					socket.send(out);
				}
			} catch (IOException e) {
				e.printStackTrace();
				cnt = false;
			}
		}
		socket.close();
	}

}
