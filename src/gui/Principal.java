package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.text.DefaultCaret;
import comun.Gestor;

public class Principal {

	private JFrame frame;
	private JTextField txtMinsecciones;
	private JTextField txtMaxsecciones;
	private JTextField txtMaxcruces;
	private JTextField txtMincruces;
	private JTextField txtMinclientes;
	private JTextField txtMaxclientes;
	private JTextField txtMinconsumo;
	private JTextField txtMaxconsumo;
	private JTextField txtThreads;
	private JLabel lblMemoriaUtilizada = new JLabel("0 MB 0%");
	private Gestor gestor=new Gestor();
	private JButton btnGenerarsecciones;
	private JButton btnGenerarClientes;
	private JButton btnGrafoyCliente;
	private JButton btnEncuentraCaminos;
	public JCheckBoxMenuItem chckbxmntmCargaCopia;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
			Principal window = new Principal();
			window.frame.setVisible(true);
	}

	/**
	 * Create the application.
	 */
	public Principal() {
		jomp.runtime.OMP.setNumThreads(4);
		initialize();
		ActualizaMemoria.start();
	}

	Thread ActualizaMemoria = new Thread() {
		public void run() {
			long mb = 1024 * 1024;
			long memorialibre = 0;
			Runtime runtime = Runtime.getRuntime();
			while (true) {
				try {			
					memorialibre = (runtime.totalMemory() - runtime
							.freeMemory());
					lblMemoriaUtilizada.setText("Memoria Maxima("
							+ runtime.totalMemory() / mb + " MB) Actual:"
							+ memorialibre / mb + " MB "
							+ ((memorialibre * 100) / runtime.totalMemory())
							+ "%");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 640, 600);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) {
				Principal.this.gestor.apagaCassandra();
				System.exit(0);
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}
		});

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);

		JMenuItem mntmArrancaCassandra = new JMenuItem("Arranca cassandra");
		mnArchivo.add(mntmArrancaCassandra);

		mntmArrancaCassandra.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.enciendeProcesoCassandra();
			}
		});

		JMenuItem mntmPintagrafo = new JMenuItem("Pinta grafo BD");
		mntmPintagrafo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.pintaGrafoBD();
			}
		});
		mnArchivo.add(mntmPintagrafo);

		JMenuItem mntmPintaClientes = new JMenuItem("Pinta clientes BD");
		mntmPintaClientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.pintaClienteBD();
			}
		});
		mnArchivo.add(mntmPintaClientes);

		JMenuItem mntmRecreaBd = new JMenuItem("Recrea BD");
		mnArchivo.add(mntmRecreaBd);
		mntmRecreaBd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.limpiarBD();
			}
		});

		JMenu mnOpciones = new JMenu("Opciones");
		menuBar.add(mnOpciones);

		JCheckBoxMenuItem chckbxmntmDebug = new JCheckBoxMenuItem("Debug");
		chckbxmntmDebug.setSelected(true);
		mnOpciones.add(chckbxmntmDebug);
		chckbxmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.setDebug(((AbstractButton) e.getSource())
						.getModel().isSelected());
			}
		});

		JCheckBoxMenuItem chckbxmntmAutoInserta = new JCheckBoxMenuItem(
				"Auto inserta");
		chckbxmntmAutoInserta.setSelected(false);
		mnOpciones.add(chckbxmntmAutoInserta);
		chckbxmntmAutoInserta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.setAutoInserta(((AbstractButton) e.getSource())
						.getModel().isSelected());
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
		txtMinsecciones.setText("20");
		txtMinsecciones.setColumns(10);

		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);

		JLabel lblMaxsecciones = new JLabel("MaxSecciones:");
		horizontalBox_1.add(lblMaxsecciones);

		txtMaxsecciones = new JTextField();
		horizontalBox_1.add(txtMaxsecciones);
		txtMaxsecciones.setText("50");
		txtMaxsecciones.setColumns(10);

		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_2);

		JLabel lblMinCruces = new JLabel("MinCruces:");
		horizontalBox_2.add(lblMinCruces);

		txtMincruces = new JTextField();
		txtMincruces.setText("3");
		txtMincruces.setColumns(10);
		horizontalBox_2.add(txtMincruces);

		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_3);

		JLabel lblMaxCruces = new JLabel("MaxCruces:");
		horizontalBox_3.add(lblMaxCruces);

		txtMaxcruces = new JTextField();
		horizontalBox_3.add(txtMaxcruces);
		txtMaxcruces.setText("4");
		txtMaxcruces.setColumns(10);

		Box verticalBox_1 = Box.createVerticalBox();
		panel.add(verticalBox_1);

		JLabel lblGenerarClientes = new JLabel("Generar Clientes");
		verticalBox_1.add(lblGenerarClientes);

		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_4);

		JLabel lblMinclientes = new JLabel("MinClientes:");
		horizontalBox_4.add(lblMinclientes);

		txtMinclientes = new JTextField();
		txtMinclientes.setText("3");
		txtMinclientes.setColumns(10);
		horizontalBox_4.add(txtMinclientes);

		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_5);

		JLabel lblMaxclientes = new JLabel("MaxClientes:");
		horizontalBox_5.add(lblMaxclientes);

		txtMaxclientes = new JTextField();
		txtMaxclientes.setText("4");
		txtMaxclientes.setColumns(10);
		horizontalBox_5.add(txtMaxclientes);

		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_6);

		JLabel lblMinconsumo = new JLabel("MinConsumo:");
		horizontalBox_6.add(lblMinconsumo);

		txtMinconsumo = new JTextField();
		txtMinconsumo.setText("1100");
		txtMinconsumo.setColumns(10);
		horizontalBox_6.add(txtMinconsumo);

		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_7);

		JLabel lblMaxconsumo = new JLabel("MaxConsumo:");
		horizontalBox_7.add(lblMaxconsumo);
				
		txtMaxconsumo = new JTextField();
		txtMaxconsumo.setText("3500");
		txtMaxconsumo.setColumns(10);
		horizontalBox_7.add(txtMaxconsumo);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_8);
		JLabel lblThreads = new JLabel("Threads:");
		horizontalBox_8.add(lblThreads);
		txtThreads = new JTextField();
		txtThreads.setText(Integer.toString(jomp.runtime.OMP.getMaxThreads()));
		txtThreads.addKeyListener(new KeyListener() {

		    @Override
		    public void keyTyped(KeyEvent e) {
		        char c = e.getKeyChar();
		        if (!((c >= '1') && (c <= (char)(((int)'0')+Runtime.getRuntime().availableProcessors())) ||
		           (c == KeyEvent.VK_BACK_SPACE) ||
		           (c == KeyEvent.VK_DELETE))) {
		        	txtThreads.setText(Integer.toString(Runtime.getRuntime().availableProcessors()));
		          e.consume();
		        }else{
		        	if(txtThreads.getText().length()>=1){txtThreads.setText("");
		        	}
		        }
		      }

		    @Override
		    public void keyReleased(KeyEvent arg0) {
		        // TODO Auto-generated method stub
		    	try{
			    	jomp.runtime.OMP.setNumThreads(Integer.parseInt(Principal.this.txtThreads.getText()));
			    	System.out.println("Ha cambiado el numero de threads a "+jomp.runtime.OMP.getMaxThreads());
		    	} catch (NumberFormatException e){
		    	}
		    }

		    @Override
	        public void keyPressed(KeyEvent e) {
	        };
		});
		horizontalBox_8.add(txtThreads);
		
		Box verticalBox_2 = Box.createVerticalBox();
		panel.add(verticalBox_2);

		chckbxmntmCargaCopia = new JCheckBoxMenuItem("CargaCopia");
		chckbxmntmCargaCopia.setSelected(true);
		mnOpciones.add(chckbxmntmCargaCopia);
		chckbxmntmCargaCopia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.setPrecargaTabla(((AbstractButton) e.getSource())
						.getModel().isSelected());
			}
		});

		btnGenerarsecciones = new JButton("Generar Secciones");
		verticalBox_2.add(btnGenerarsecciones);
		btnGenerarsecciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestionBotones(false);
				//Thread queryThread = new Thread() {
				 //    public void run() {
				    	  Principal.this.gestor.generarSecciones(Integer.parseInt(txtMinsecciones.getText()),Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2);
				    	  gestionBotones(true);
				   //   }
				   // };
				   //queryThread.start();
							
			}
		});

		btnGenerarClientes = new JButton("Generar Clientes");
		verticalBox_2.add(btnGenerarClientes);
		btnGenerarClientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestionBotones(false);
				//Thread queryThread = new Thread() {
				//     public void run() {
				Principal.this.gestor.generaClientes(Integer.parseInt(txtMinclientes.getText()),Integer.parseInt(txtMaxclientes.getText()), Integer.parseInt(txtMinconsumo.getText()), Integer.parseInt(txtMaxconsumo.getText()));
				gestionBotones(true);
				//      }
				//    };
				//    queryThread.start();
			}
		});

		btnGrafoyCliente = new JButton("Genera Grafos y Clientes");
		verticalBox_2.add(btnGrafoyCliente);
		btnGrafoyCliente.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestionBotones(false);
				//Thread queryThread = new Thread() {
				//      public void run() {
				Principal.this.gestor.generarSeccionesYClientes(Integer.parseInt(txtMinsecciones.getText()), Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2,Integer.parseInt(txtMinclientes.getText()), Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));
				gestionBotones(true);
				  //    }
				  //   };
				  //  queryThread.start();
			}
		});

		btnEncuentraCaminos = new JButton("Encuentra Caminos");
		verticalBox_2.add(btnEncuentraCaminos);
		btnEncuentraCaminos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestionBotones(false);
				Thread queryThread = new Thread() {
				      public void run() {
				Principal.this.gestor.encuentraCaminos(Integer.parseInt(txtMinsecciones.getText()), Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2,Integer.parseInt(txtMinclientes.getText()), Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));;						
				gestionBotones(true);
				      }
				    };
				    queryThread.start();	
			}
		});

		final JTextArea textArea = new JTextArea();
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(scroll, BorderLayout.SOUTH);
		frame.getContentPane().add(lblMemoriaUtilizada, BorderLayout.NORTH);
		textArea.setRows(22);
		// frame.getContentPane().add(textArea, BorderLayout.SOUTH);
		JTextAreaOutputStream out = new JTextAreaOutputStream(textArea);
		System.setOut(new PrintStream(out));

	}
	
	private void gestionBotones(boolean estado){
		if(estado){
			Principal.this.btnEncuentraCaminos.setEnabled(true);
			Principal.this.btnGrafoyCliente.setEnabled(true);
			Principal.this.btnGenerarClientes.setEnabled(true);
			Principal.this.btnGenerarsecciones.setEnabled(true);
		}else{
			Principal.this.btnEncuentraCaminos.setEnabled(false);
			Principal.this.btnGrafoyCliente.setEnabled(false);
			Principal.this.btnGenerarClientes.setEnabled(false);
			Principal.this.btnGenerarsecciones.setEnabled(false);
		}
	}

	public class JTextAreaOutputStream extends OutputStream {
		private final JTextArea destination;

		public JTextAreaOutputStream(JTextArea destination) {
			if (destination == null)
				throw new IllegalArgumentException("Destination is null");

			this.destination = destination;
		}

		@Override
		public void write(byte[] buffer, int offset, int length)
				throws IOException {
			final String text = new String(buffer, offset, length);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					destination.append(text);
				}
			});
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}
	}

}
