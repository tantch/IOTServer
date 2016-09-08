import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class Server {
	public static IOTServerThread thread;

	public Server() throws IOException {

		thread = new IOTServerThread();
		thread.start();

		JFrame frame = new JFrame("JButtonTable Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		DefaultTableModel dm = new DefaultTableModel(0, 0);
		String[] columnNames = { "Private Network Name", "Number of Users", "Red State", "Green State", "Switch" };

		dm.setColumnIdentifiers(columnNames);

		JTable table = new JTable(dm);
		table.getColumn("Switch").setCellRenderer(new ButtonRenderer());
		table.getColumn("Switch").setCellEditor(new ButtonEditor(new JCheckBox()));

		JScrollPane scroll = new JScrollPane(table);

		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		frame.add(scroll);

		frame.pack();
		frame.setVisible(true);

		TableWatcher tw = new TableWatcher(dm, thread);
		tw.start();
	}

	public class TableWatcher extends Thread {
		DefaultTableModel d;
		IOTServerThread i;

		public TableWatcher(DefaultTableModel d, IOTServerThread i) {
			this.d = d;
			this.i = i;
		}

		public void run() {
			while (true) {

				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while (d.getRowCount() > 0)
					d.removeRow(0);
				for (String network : i.dudos.keySet()) {
					d.addRow(new Object[] { network, i.dudos.get(network).peers.size(), i.dudos.get(network).redstate,
							i.dudos.get(network).greenstate, "Switch network " + network });
				}

			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Server();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}
}

class ButtonRenderer extends JButton implements TableCellRenderer {

	public ButtonRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(UIManager.getColor("Button.background"));
		}
		setText((value == null) ? "" : value.toString());
		return this;
	}
}

class ButtonEditor extends DefaultCellEditor {

	protected JButton button;
	private String label;
	private boolean isPushed;

	public ButtonEditor(JCheckBox checkBox) {
		super(checkBox);
		button = new JButton();
		button.setOpaque(true);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (isSelected) {
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		} else {
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		label = (value == null) ? "" : value.toString();
		button.setText(label);
		isPushed = true;
		return button;
	}

	@Override
	public Object getCellEditorValue() {
		if (isPushed) {
			try {
				DatagramSocket socket = new DatagramSocket(3334);
				// label.split("network ")[1];
				ConcurrentHashMap<String, IOTServerThread.Network> dudos = Server.thread.dudos;
				if(dudos.get(label.split("network ")[1]).greenstate==0)
					dudos.get(label.split("network ")[1]).greenstate=new Integer(1);
				else{
					dudos.get(label.split("network ")[1]).greenstate=new Integer(0);
					
				}
				for (InetAddress peer : dudos.get(label.split("network ")[1]).peers) {
					int n = dudos.get(label.split("network ")[1]).greenstate + 2;
					byte[] buf2 = ("" + n).getBytes();
					DatagramPacket out = new DatagramPacket(buf2, buf2.length, peer, 4000);
					socket.send(out);
				}
				socket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		isPushed = false;
		return label;
	}

	@Override
	public boolean stopCellEditing() {
		isPushed = false;
		return super.stopCellEditing();
	}

	@Override
	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}