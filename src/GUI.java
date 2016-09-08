import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class GUI {

	private JFrame frame;
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 */
	public GUI() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		IOTServerThread thread = new IOTServerThread();
		thread.start();
		Object[][] data;

		String[] columnNames = { "Private Network Name", "Number of Users", "Red State", "Green State", "Switch" };

		DefaultTableModel dtm = new DefaultTableModel(0, 0);
		dtm.setColumnIdentifiers(columnNames);

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 342);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		table = new JTable(dtm);
		scrollPane.setViewportView(table);

		
		TableWatcher tw = new TableWatcher(dtm, thread);
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
					JButton switcher;
					switcher = new JButton("Switch on/off red light");
					switcher.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {

						}
					});
					d.addRow(new Object[] { network, i.dudos.get(network).peers.size(), i.dudos.get(network).redstate,
							i.dudos.get(network).greenstate, switcher });
				}

			}
		}
	}

}
