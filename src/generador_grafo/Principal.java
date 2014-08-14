package generador_grafo;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JButton;

import com.datastax.driver.core.Row;

public class Principal {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Principal window = new Principal();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Principal() {
		initialize();
	}
	
	private GenerarClientes clientes;
	private gestorCassandra gestorCassandra;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);
		
		JMenuItem mntmArrancaCassandra = new JMenuItem("Arranca cassandra");
		mnArchivo.add(mntmArrancaCassandra);
		
		mntmArrancaCassandra.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    try {
			    File currDir = new File(".\\apache-cassandra-2.0.9\\bin\\cassandra.bat");
			    String path = currDir.getAbsolutePath();
			    path = path.substring(0, path.length());
			    String dir = path.substring(0, path.length()-13);
				Runtime.getRuntime().exec("cmd /c cd "+dir+" && start "+path);
				btnInsertarseccionesBD.setEnabled(true);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}		
			}
		});
		
		JMenuItem mntmPintagrafo = new JMenuItem("Pinta grafo");
		mnArchivo.add(mntmPintagrafo);
		
		JMenuItem mntmPintaClientes = new JMenuItem("Pinta clientes");
		mnArchivo.add(mntmPintaClientes);
		
		JMenuItem mntmRecreaBd = new JMenuItem("Recrea BD");
		mnArchivo.add(mntmRecreaBd);
		mntmRecreaBd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				  gestorCassandra = new gestorCassandra();
				  gestorCassandra.connect("127.0.0.1");
				  gestorCassandra.createSchema();
				  gestorCassandra.close();
			}
		});
		
		
		JMenu mnOpciones = new JMenu("Opciones");
		menuBar.add(mnOpciones);
		
		JCheckBoxMenuItem chckbxmntmDebug = new JCheckBoxMenuItem("Debug");
		mnOpciones.add(chckbxmntmDebug);
		chckbxmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		        
				Principal.this.setDebug(((AbstractButton) e.getSource()).getModel().isSelected());
			}
		});
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.WEST);
		
		Box verticalBox = Box.createVerticalBox();
		panel.add(verticalBox);
		
		JLabel lblGenerarSecciones = new JLabel("Generar Secciones");
		verticalBox.add(lblGenerarSecciones);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		JLabel lblMinsecciones = new JLabel("MinSecciones:");
		horizontalBox.add(lblMinsecciones);
		
		txtMinsecciones = new JTextField();
		horizontalBox.add(txtMinsecciones);
		txtMinsecciones.setText("6");
		txtMinsecciones.setColumns(10);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		JLabel lblMaxsecciones = new JLabel("MaxSecciones:");
		horizontalBox_1.add(lblMaxsecciones);
		
		txtMaxsecciones = new JTextField();
		horizontalBox_1.add(txtMaxsecciones);
		txtMaxsecciones.setText("10");
		txtMaxsecciones.setColumns(10);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_2);
		
		JLabel lblMinCruces = new JLabel("MinCruces:");
		horizontalBox_2.add(lblMinCruces);
		
		txtMincruces = new JTextField();
		txtMincruces.setText("1");
		txtMincruces.setColumns(10);
		horizontalBox_2.add(txtMincruces);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_3);
		
		JLabel lblMaxCruces = new JLabel("MaxCruces:");
		horizontalBox_3.add(lblMaxCruces);
		
		txtMaxcruces = new JTextField();
		horizontalBox_3.add(txtMaxcruces);
		txtMaxcruces.setText("3");
		txtMaxcruces.setColumns(10);
		
		JButton btnGenerarsecciones = new JButton("Generar Secciones");
		verticalBox.add(btnGenerarsecciones);
		btnGenerarsecciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		        
				 grafo = new GenerarGrafo();
				 grafo.setAlgoritmo(2);
				 grafo.setDebug(true);
				 grafo.generar();
			}
		});
		
		btnInsertarseccionesBD = new JButton("Insertar Secciones BD");
		btnInsertarseccionesBD.setEnabled(true);
		verticalBox.add(btnInsertarseccionesBD);
		btnInsertarseccionesBD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestorCassandra = new gestorCassandra();
				 gestorCassandra.connect("127.0.0.1");
				 gestorCassandra.insertDataFromAdjacentTable(grafo.getTablaGrafo());
				 gestorCassandra.pinta();
				 gestorCassandra.close();
			}
		});
		
		JTextArea textArea = new JTextArea();
		JScrollPane scroll = new JScrollPane (textArea);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	          scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

	    frame.getContentPane().add(scroll, BorderLayout.SOUTH);

		textArea.setRows(22);
		//frame.getContentPane().add(textArea, BorderLayout.SOUTH);
		JTextAreaOutputStream out = new JTextAreaOutputStream (textArea);
		
		Box verticalBox_1 = Box.createVerticalBox();
		frame.getContentPane().add(verticalBox_1, BorderLayout.EAST);
		
		JLabel lblGenerarClientes = new JLabel("Generar Clientes");
		verticalBox_1.add(lblGenerarClientes);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_4);
		
		JLabel lblMinclientes = new JLabel("MinClientes:");
		horizontalBox_4.add(lblMinclientes);
		
		textField = new JTextField();
		textField.setText("10");
		textField.setColumns(10);
		horizontalBox_4.add(textField);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_5);
		
		JLabel lblMaxclientes = new JLabel("MaxClientes:");
		horizontalBox_5.add(lblMaxclientes);
		
		textField_1 = new JTextField();
		textField_1.setText("20");
		textField_1.setColumns(10);
		horizontalBox_5.add(textField_1);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_6);
		
		JLabel lblMinconsumo = new JLabel("MinConsumo:");
		horizontalBox_6.add(lblMinconsumo);
		
		textField_2 = new JTextField();
		textField_2.setText("1100");
		textField_2.setColumns(10);
		horizontalBox_6.add(textField_2);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_7);
		
		JLabel lblMaxconsumo = new JLabel("MaxConsumo:");
		horizontalBox_7.add(lblMaxconsumo);
		
		textField_3 = new JTextField();
		textField_3.setText("3500");
		textField_3.setColumns(10);
		horizontalBox_7.add(textField_3);
		
		JButton btnGenerarClientes = new JButton("Generar Clientes");
		verticalBox_1.add(btnGenerarClientes);
		btnGenerarClientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		        
				 clientes = new GenerarClientes();
				clientes.setAlgoritmo(2);
				clientes.setDebug(true);
				clientes.generar();
			}
		});
		
		
		JButton btnInsertarClientesBd = new JButton("Insertar Clientes BD");
		verticalBox_1.add(btnInsertarClientesBd);
		btnInsertarClientesBd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestorCassandra = new gestorCassandra();
				 gestorCassandra.connect("127.0.0.1");
				 gestorCassandra.insertDataFromAdjacentTable(grafo.getTablaGrafo());
				 gestorCassandra.pinta();
				 gestorCassandra.close();
			}
		});
		
        System.setOut (new PrintStream (out));
        
	}
	private JButton btnInsertarseccionesBD;
	private boolean debug=false;
	private JTextField txtMinsecciones;
	private JTextField txtMaxsecciones;
	private JTextField txtMaxcruces;
	private JTextField txtMincruces;
	private GenerarGrafo grafo;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	public void setDebug(boolean debug){
		this.debug=debug;		
	}
	
	public class JTextAreaOutputStream extends OutputStream
	{
	    private final JTextArea destination;

	    public JTextAreaOutputStream (JTextArea destination)
	    {
	        if (destination == null)
	            throw new IllegalArgumentException ("Destination is null");

	        this.destination = destination;
	    }

	    @Override
	    public void write(byte[] buffer, int offset, int length) throws IOException
	    {
	        final String text = new String (buffer, offset, length);
	        SwingUtilities.invokeLater(new Runnable ()
	            {
	                @Override
	                public void run() 
	                {
	                    destination.append (text);
	                }
	            });
	    }

	    @Override
	    public void write(int b) throws IOException
	    {
	        write (new byte [] {(byte)b}, 0, 1);
	    }
	}

}
