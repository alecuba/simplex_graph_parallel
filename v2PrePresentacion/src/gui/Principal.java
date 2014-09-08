package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DefaultCaret;

import mpi.MPI;
import mpi.MPIException;
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
	private JButton btnEncuentraCaminosx10;
	private JCheckBoxMenuItem chckbxmntmCargaCopia;
	private JCheckBoxMenuItem chckbxmntmBDTest1;
	private JCheckBoxMenuItem chckbxmntmBDTest2;
	private JCheckBoxMenuItem chckbxmntmBDTest50;
	private JCheckBoxMenuItem chckbxmntmBDTest100;
	private JCheckBoxMenuItem chckbxmntmBDTest200;
	private JCheckBoxMenuItem chckbxmntmBDTest400;
	private int tablaNum = 50;
	private boolean muestraLog=false;
	private JTextArea textArea;
	private JScrollPane scroll;
	private DefaultCaret caret;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
			Principal window = new Principal();
			window.frame.setVisible(true);
	}
	
	/**
	 * Launch the application. MPJ
	
	public static void main(String[] args) {
		MPI.Init(args);
		int rank = 0;
		try {
			rank = MPI.COMM_WORLD.Rank();
			int size = MPI.COMM_WORLD.Size();
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(rank==0){
			Principal window = new Principal();
			window.frame.setVisible(true);
        }
	}*/

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
		frame.setBounds(100, 100, 640, 700);
		if(this.muestraLog) frame.setBounds(100, 100, 640, 700); else frame.setBounds(100, 100, 640, 250);
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
		
		JMenu mnBDTest = new JMenu("BD Test");
		menuBar.add(mnBDTest);
		
		JMenuItem mntmTest1 = new JMenuItem("Copia Test 1");
		mnBDTest.add(mntmTest1);
		mntmTest1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.copiaTabla("1",false);
			}
		});
		chckbxmntmBDTest1 = new JCheckBoxMenuItem("Usa Test 1");
		mnBDTest.add(chckbxmntmBDTest1);
		chckbxmntmBDTest1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Principal.this.chckbxmntmBDTest1.isSelected()){
					Principal.this.chckbxmntmBDTest50.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest200.setSelected(false);
					Principal.this.chckbxmntmBDTest400.setSelected(false);
					Principal.this.chckbxmntmBDTest2.setSelected(false);					
					Principal.this.tablaNum=1;
					Principal.this.gestor.setPrecargaTabla(tablaNum);
				}
			}
		});	
		
		JMenuItem mntmTest2 = new JMenuItem("Copia Test 2");
		mnBDTest.add(mntmTest2);
		mntmTest2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.copiaTabla("2",false);
			}
		});
		chckbxmntmBDTest2 = new JCheckBoxMenuItem("Usa Test 2");
		mnBDTest.add(chckbxmntmBDTest2);
		chckbxmntmBDTest2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Principal.this.chckbxmntmBDTest2.isSelected()){
					Principal.this.chckbxmntmBDTest1.setSelected(false);
					Principal.this.chckbxmntmBDTest50.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest200.setSelected(false);
					Principal.this.chckbxmntmBDTest400.setSelected(false);
					Principal.this.tablaNum=2;
					Principal.this.gestor.setPrecargaTabla(tablaNum);
				}
			}
		});	
		
		JMenuItem mntmCopia50 = new JMenuItem("Copia Test 50");
		mnBDTest.add(mntmCopia50);
		mntmCopia50.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.copiaTabla("50",false);
			}
		});
		chckbxmntmBDTest50 = new JCheckBoxMenuItem("Usa Test 50");
		mnBDTest.add(chckbxmntmBDTest50);
		chckbxmntmBDTest50.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Principal.this.chckbxmntmBDTest50.isSelected()){
					Principal.this.chckbxmntmBDTest1.setSelected(false);
					Principal.this.chckbxmntmBDTest2.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest200.setSelected(false);
					Principal.this.chckbxmntmBDTest400.setSelected(false);
					Principal.this.tablaNum=50;
					Principal.this.gestor.setPrecargaTabla(tablaNum);
				}
			}
		});	
		
		JMenuItem mntmCopia100 = new JMenuItem("Copia Test 100");
		mnBDTest.add(mntmCopia100);
		mntmCopia100.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.copiaTabla("100",false);
			}
		});
		
		chckbxmntmBDTest100 = new JCheckBoxMenuItem("Usa Test 100");
		mnBDTest.add(chckbxmntmBDTest100);
		chckbxmntmBDTest100.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Principal.this.chckbxmntmBDTest100.isSelected()){
					Principal.this.chckbxmntmBDTest1.setSelected(false);
					Principal.this.chckbxmntmBDTest2.setSelected(false);
					Principal.this.chckbxmntmBDTest50.setSelected(false);
					Principal.this.chckbxmntmBDTest200.setSelected(false);
					Principal.this.chckbxmntmBDTest400.setSelected(false);
					Principal.this.tablaNum=100;
					Principal.this.gestor.setPrecargaTabla(tablaNum);
				}
			}
		});	

		JMenuItem mntmCopia200 = new JMenuItem("Copia Test 200");
		mnBDTest.add(mntmCopia200);
		mntmCopia200.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.copiaTabla("200",false);
			}
		});
		chckbxmntmBDTest200 = new JCheckBoxMenuItem("Usa Test 200");
		mnBDTest.add(chckbxmntmBDTest200);
		chckbxmntmBDTest200.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Principal.this.chckbxmntmBDTest200.isSelected()){
					Principal.this.chckbxmntmBDTest1.setSelected(false);
					Principal.this.chckbxmntmBDTest2.setSelected(false);
					Principal.this.chckbxmntmBDTest50.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest400.setSelected(false);
					Principal.this.tablaNum=200;
					Principal.this.gestor.setPrecargaTabla(tablaNum);
				}
			}
		});	
		
		JMenuItem mntmCopia400 = new JMenuItem("Copia Test 400");
		mnBDTest.add(mntmCopia400);
		mntmCopia400.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.copiaTabla("400",false);
			}
		});
		chckbxmntmBDTest400 = new JCheckBoxMenuItem("Usa Test 400");
		mnBDTest.add(chckbxmntmBDTest400);
		chckbxmntmBDTest400.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Principal.this.chckbxmntmBDTest400.isSelected()){
					Principal.this.chckbxmntmBDTest1.setSelected(false);
					Principal.this.chckbxmntmBDTest2.setSelected(false);
					Principal.this.chckbxmntmBDTest50.setSelected(false);
					Principal.this.chckbxmntmBDTest100.setSelected(false);
					Principal.this.chckbxmntmBDTest200.setSelected(false);
					Principal.this.tablaNum=400;
					Principal.this.gestor.setPrecargaTabla(tablaNum);
				}
			}
		});	

		if(gestor.getPrecargaTabla()[1]==400){
			chckbxmntmBDTest50.setSelected(true);
			}else{
				chckbxmntmBDTest50.setSelected(false);
			}
		if(gestor.getPrecargaTabla()[1]==200){
			chckbxmntmBDTest50.setSelected(true);
			}else{
				chckbxmntmBDTest50.setSelected(false);
			}
		if(gestor.getPrecargaTabla()[1]==100){
			chckbxmntmBDTest50.setSelected(true);
			}else{
				chckbxmntmBDTest50.setSelected(false);
			}
		if(gestor.getPrecargaTabla()[1]==50){
			chckbxmntmBDTest50.setSelected(true);
			}else{
				chckbxmntmBDTest50.setSelected(false);
			}
		if(gestor.getPrecargaTabla()[1]==1){
			chckbxmntmBDTest1.setSelected(true);
			}else{
				chckbxmntmBDTest1.setSelected(false);
			}
		if(gestor.getPrecargaTabla()[1]==2){
			chckbxmntmBDTest2.setSelected(true);
			}else{
				chckbxmntmBDTest2.setSelected(false);
			}
		
		JCheckBoxMenuItem chckbxmntmDebug = new JCheckBoxMenuItem("Debug");
		if(gestor.getDebug()){
			chckbxmntmDebug.setSelected(true);
			}else{
				chckbxmntmDebug.setSelected(false);
			}
		mnOpciones.add(chckbxmntmDebug);
		chckbxmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.setDebug(((AbstractButton) e.getSource())
						.getModel().isSelected());
			}
		});
		
		JCheckBoxMenuItem chckbxmntmParalelo = new JCheckBoxMenuItem("Paralelo");
		if(gestor.getParalelo()){
			chckbxmntmParalelo.setSelected(true);
			}else{
				chckbxmntmParalelo.setSelected(false);
			}
		mnOpciones.add(chckbxmntmParalelo);
		chckbxmntmParalelo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Principal.this.gestor.setParalelo(((AbstractButton) e.getSource())
						.getModel().isSelected());
			}
		});
		
		JCheckBoxMenuItem chckbxmntmMuestraLog = new JCheckBoxMenuItem("Muestra Log");
		chckbxmntmMuestraLog.setSelected(this.muestraLog);
		mnOpciones.add(chckbxmntmMuestraLog);
		chckbxmntmMuestraLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(((AbstractButton) e.getSource()).getModel().isSelected()){
					Principal.this.muestraLog=true;
					Principal.this.frame.setBounds(100, 100, 640, 700);
					textArea.setVisible(true);
					caret.setVisible(true);
					scroll.setVisible(true);
					Principal.this.sysOut=System.out;
					JTextAreaOutputStream out = new JTextAreaOutputStream(textArea);
					System.setOut(new PrintStream(out));
					try {
					        System.setOut(new PrintStream(new File("C://history.txt")));
					    } catch (Exception err) {
					         err.printStackTrace();
					    }					 
				}else{
					Principal.this.muestraLog=true;
					Principal.this.frame.setBounds(100, 100, 640, 250);
					textArea.setVisible(false);
					caret.setVisible(false);
					scroll.setVisible(false);
					System.setOut(Principal.this.sysOut);
				}
			}
		});

		JCheckBoxMenuItem chckbxmntmAutoInserta = new JCheckBoxMenuItem(
				"Auto inserta");
		if(gestor.getAutoInserta()){
			chckbxmntmAutoInserta.setSelected(true);
			}else{
				chckbxmntmAutoInserta.setSelected(false);
			}
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
		if(gestor.getPrecargaTabla()[0]==1){
		chckbxmntmCargaCopia.setSelected(true);
		}else{
			chckbxmntmCargaCopia.setSelected(false);
		}
		mnOpciones.add(chckbxmntmCargaCopia);
		
		chckbxmntmCargaCopia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(((AbstractButton) e.getSource()).getModel().isSelected()){
					Principal.this.gestor.setPrecargaTabla(1,Principal.this.tablaNum);
				}else{
					Principal.this.gestor.setPrecargaTabla(0,Principal.this.tablaNum);
				}
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
				//Thread queryThread = new Thread() {
				 //     public void run() {
				Principal.this.gestor.encuentraCaminos(Integer.parseInt(txtMinsecciones.getText()), Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2,Integer.parseInt(txtMinclientes.getText()), Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));;						
				gestionBotones(true);
				//      }
				 //   };
				  //  queryThread.start();	
			}
		});
		
		btnEncuentraCaminosx10 = new JButton("Encuentra Caminos Benchmark x10");
		verticalBox_2.add(btnEncuentraCaminosx10);
		btnEncuentraCaminosx10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gestionBotones(false);
				//Thread queryThread = new Thread() {
				 //     public void run() {
				Principal.this.gestor.encuentraCaminosx10(Integer.parseInt(txtMinsecciones.getText()), Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2,Integer.parseInt(txtMinclientes.getText()), Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));;						
				gestionBotones(true);
				//      }
				 //   };
				  //  queryThread.start();	
			}
		});

		textArea = new JTextArea();
		textArea.setVisible(this.muestraLog);
		caret = (DefaultCaret) textArea.getCaret();
		caret.setVisible(this.muestraLog);
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll = new JScrollPane(textArea);
		scroll.setVisible(muestraLog);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(scroll, BorderLayout.SOUTH);
		frame.getContentPane().add(lblMemoriaUtilizada, BorderLayout.NORTH);
		textArea.setRows(30);
		// frame.getContentPane().add(textArea, BorderLayout.SOUTH);
		JTextAreaOutputStream out = new JTextAreaOutputStream(textArea);
		//System.setOut(new PrintStream(out));
		/* try {
		        System.setOut(new PrintStream(new File("C://history.txt")));
		    } catch (Exception e) {
		         e.printStackTrace();
		    }*/

	}
	
	private PrintStream sysOut;
	
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
