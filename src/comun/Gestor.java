package comun;
import generacion.jomp.GenerarClientes;
import generacion.jomp.GenerarGrafo;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.RejectedExecutionException;
import busqueda.Caminos;
import busqueda.jomp.RecorreGrafo;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class Gestor{
		private Cluster cluster = null;
		private Session session = null;
		private GenerarGrafo grafo;
		private GenerarClientes clientes;
		private boolean debug = true;
		private boolean autoinserta = false;
		private boolean precargaTabla = true;
		
		public void setDebug(boolean debug) {
			this.debug = debug;
		}

		public void setConsultaCopia(boolean consultaCopia) {
			preCargaTabla();
		}

		
		public void setPrecargaTabla(boolean precarga){
			this.precargaTabla=precarga;
		}
		
		public void generarSeccionesYClientes(int minSecciones, int maxSecciones, int minCruces,int maxCruces,int minEntradas,int maxEntradas,int minClientes, int maxClientes, int minConsumo,int maxConsumo){
			if (precargaTabla){
				this.preCargaTabla();
			}else{
				generarSecciones(minSecciones,maxSecciones,minCruces,maxCruces,minEntradas,maxEntradas);
				generaClientes(minClientes,maxClientes,minConsumo,maxConsumo);
			}
		}
		
		public void generarSecciones(int minSecciones, int maxSecciones, int minCruces,int maxCruces,int minEntradas,int maxEntradas){
			if (precargaTabla){
				this.preCargaTabla();
			}else{
			if (esperaConexionCassandra()) {
				if (grafo == null)	grafo = new GenerarGrafo(this);
				grafo.setDebug(debug);
				if (grafo.generar(minSecciones,maxSecciones,minCruces,maxCruces, minEntradas, maxEntradas)) {
					if (debug)
						grafo.pintaTabla();
					if (autoinserta && esperaConexionCassandra()) {
						grafo.insertaGrafoCQL(true);
					}
				}
			}		
			}
		}
		
		public void generaClientes(int minClientes, int maxClientes, int minConsumo,int maxConsumo){
			if (precargaTabla) {this.preCargaTabla();}else{
			if (esperaConexionCassandra()) {
				if (clientes == null) clientes = new GenerarClientes(grafo,this);
				clientes.setDebug(debug);
				clientes.generar(minClientes,maxClientes,minConsumo,maxConsumo);
				if (debug)
					clientes.pintaTabla();
				if (autoinserta) {
					if (esperaConexionCassandra()) {
						clientes.insertaGrafoCQL(true);
					}
				}
			}
			}
		}
		
		public void encuentraCaminos(){
			if (precargaTabla) this.preCargaTabla();
			if (esperaConexionCassandra()) {
				// limpiarBD();
				// grafo = new GenerarGrafo(Principal.this);
				// grafo.setDebug(debug);
				// if(grafo.generar(Integer.parseInt(txtMinsecciones.getText()),Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2)){
				// grafo.pintaTabla();
				// grafo.insertaGrafoCQL(true);
				// grafo.pintaBD();
				// clientes = new
				// GenerarClientes(grafo,Principal.this);
				// clientes.setDebug(debug);
				// clientes.generar(Integer.parseInt(txtMinclientes.getText()),Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));
				// clientes.conectarClientes(grafo.numeroSecciones());
				// clientes.insertaGrafoCQL(true);
				// clientes.pintaBD();
				RecorreGrafo recorre = new RecorreGrafo(this);
				recorre.setDebug(true);
				System.out.println("1\n");
				System.out.println("Numero vertices:"
						+ recorre.consultaNumeroVertices());
				Caminos caminos =recorre.encuentraCaminosTodosClientes();
				caminos.pintaCaminosGenerados();
				// }
			}
		}
		public void limpiarBD() {
			if (enciendeProcesoCassandra() && esperaConexionCassandra()) {
				if (debug)
					System.out.printf("Conectado al cluster: %s\n", this
							.getCassandraCluster().getMetadata().getClusterName());
				if (debug) {
					for (Host host : this.getCassandraCluster().getMetadata()
							.getAllHosts()) {
						System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
								host.getDatacenter(), host.getAddress(),
								host.getRack());
					}
				}
				Session session = cluster.connect();
				try {
					session.execute("USE Bd");
					session.execute("DROP KEYSPACE Bd;");
				} catch (InvalidQueryException e) {
				}
			}
		}

		public void pintaGrafoBD() {
			boolean vacia = false;
			if (esperaConexionCassandra()) {
				try {
					getCassandraSession().execute("USE Bd");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("Vacia o no existe BD\n");
				}
				if (!vacia) {
					// copiaTabla();
					ResultSet results = getCassandraSession().execute(
							"SELECT * FROM vertices");
					System.out.println(String.format("%-14s%-14s%-14s",
							"idseccion", "vertA", "vertB"));
					// Iterator iter=results.iterator();
					while (results.iterator().hasNext()) {
						Row row = results.iterator().next();
						System.out.println(String.format("%s",
								"---------------+---------------+---------------"));
						System.out.println(String.format("%-20s%-20s%-20s",
								row.getInt("idseccion"), row.getInt("vertA"),
								row.getInt("vertB")));
					}
					System.out.println();
					results = getCassandraSession().execute(
							"SELECT * FROM caracteristicasVertices");
					System.out.println(String.format("%-14s%-14s%-5s", "idseccion",
							"consumoMax", "coste"));
					// iter=results.iterator();
					while (results.iterator().hasNext()) {
						Row row = (Row) results.iterator().next();
						System.out.println(String.format("%s",
								"---------------+---------------+---------------"));
						System.out.println(String.format("%-20s%-20s%-3.1f",
								row.getInt("idseccion"), row.getInt("consumoMax"),
								row.getFloat("coste")));
					}
					System.out.println();
				}
			}
		}

		public void pintaClienteBD() {
			boolean vacia = false;
			if (esperaConexionCassandra()) {
				try {
					getCassandraSession().execute("USE BD");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("Vacia o no existe BD\n");
				}
				if (!vacia) {
					ResultSet results = getCassandraSession().execute(
							"SELECT * FROM clientes");
					System.out.println(String.format("%-12s%-12s%-12s", "id",
							"idseccion", "consumoActual"));
					while (results.iterator().hasNext()) {
						Row row = results.iterator().next();
						System.out.println(String.format("%s",
								"--------+---------+-------------------"));

						System.out.println(String.format("%-20d%-20d%-20d",
								row.getInt("id"), row.getInt("idseccion"),
								row.getInt("consumoActual")));
					}
					System.out.println();
				}
			}
		}

		public void preCargaTabla() {
			if (esperaConexionCassandra()) {
				try {
					getCassandraSession().execute("USE BD");
				} catch (InvalidQueryException e) {
					System.out.println("Vacia o no existe BD\n");
					getCassandraSession()
							.execute(
									"CREATE KEYSPACE IF NOT EXISTS Bd WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
					getCassandraSession().execute("USE Bd");
				}

				try {
					getCassandraSession().execute(
							"SELECT idseccion FROM Bd.vertices LIMIT 1");
					getCassandraSession().execute("TRUNCATE Bd.vertices");
				} catch (InvalidQueryException e) {
					System.out.println("No habia tabla vertices Recreando\n");
					getCassandraSession()
							.execute(
									"CREATE TABLE Bd.vertices (vertA int, vertB int, idseccion int PRIMARY KEY)");
					getCassandraSession().execute(
							"CREATE INDEX vertices_vertA ON Bd.vertices (vertA)");
					getCassandraSession().execute(
							"CREATE INDEX vertices_vertB ON Bd.vertices (vertB)");
				}

				try {
					getCassandraSession()
							.execute(
									"SELECT idseccion FROM Bd.caracteristicasVertices LIMIT 1");
					getCassandraSession().execute(
							"TRUNCATE Bd.caracteristicasVertices");
				} catch (InvalidQueryException e) {
					System.out
							.println("No habia tabla caracteristicasVertices Recreando\n");
					getCassandraSession()
							.execute(
									"CREATE TABLE Bd.caracteristicasVertices (idseccion int PRIMARY KEY, consumoMax int, coste float)");
				}

				try {
					getCassandraSession()
							.execute("SELECT id FROM clientes LIMIT 1");
					getCassandraSession().execute("TRUNCATE Bd.clientes");
				} catch (InvalidQueryException e) {
					getCassandraSession()
							.execute(
									"CREATE TABLE clientes (id int PRIMARY KEY, idseccion int,consumoActual int)");
				}

				int[][] vertices = { { 0, 0, 2 }, { 1, 1, 6 }, { 2, 2, 6 },
						{ 3, 3, 6 }, { 4, 4, 6 }, { 5, 5, 6 }, { 6, 0, 6 },
						{ 7, 5, 4 }, { 8, 3, 4 }, { 9, 1, 4 }, { 10, 4, 2 },
						{ 11, 1, 3 }, { 12, 3, 5 } };
				float[][] verticesCaracteristicas = { { 0, 13438, (float) 0.5 },
						{ 1, 15040, (float) 0.7 }, { 2, 11273, (float) 0.9 },
						{ 3, 16086, (float) 1.5 }, { 4, 14202, (float) 1.2 },
						{ 5, 8478, (float) 0.6 }, { 6, 10555, (float) 0.65 },
						{ 7, 9562, (float) 1.11 }, { 8, 15320, (float) 0.72 },
						{ 9, 11210, (float) 0.91 }, { 10, 7721, (float) 1.25 },
						{ 11, 9811, (float) 0.58 }, { 12, 13811, (float) 1.58 } };
				int[][] clientes = { { 1, 4, 2039 }, { 0, 5, 1973 }, { 2, 2, 1587 } };
				BatchStatement batch = new BatchStatement();
				PreparedStatement psExtremos = getCassandraSession()
						.prepare(
								"INSERT INTO Bd.vertices (idseccion,vertA,vertB) VALUES (?, ?, ?)");
				PreparedStatement psCaracteristicas = getCassandraSession()
						.prepare(
								"INSERT INTO Bd.caracteristicasVertices (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
				PreparedStatement psClientes = getCassandraSession()
						.prepare(
								"INSERT INTO Bd.clientes (id, idseccion,consumoActual) VALUES (?, ?, ?)");
				int i;
				for (i = 0; i < vertices.length; i++) {
					batch.add(psExtremos.bind(vertices[i][0], vertices[i][1],
							vertices[i][2]));
				}
				for (i = 0; i < verticesCaracteristicas.length; i++) {
					batch.add(psCaracteristicas.bind(
							(int) verticesCaracteristicas[i][0],
							(int) verticesCaracteristicas[i][1],
							verticesCaracteristicas[i][2]));
				}
				for (i = 0; i < clientes.length; i++) {
					batch.add(psClientes.bind((int) clientes[i][0],
							(int) clientes[i][1], clientes[i][2]));

				}
				getCassandraSession().execute(batch);
			}
		}

		public void copiaTabla() {
			boolean vacia = false;
			if (esperaConexionCassandra()) {
				try {
					getCassandraSession().execute("USE BD");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("Vacia o no existe BD\n");
				}
				if (!vacia) {
					/*
					// getCassandraSession().execute("DROP TABLE Bd.verticesCP");
					// getCassandraSession().execute("DROP TABLE Bd.clientesCP");
					// getCassandraSession().execute("DROP TABLE Bd.caracteristicasVerticesCP");
					// getCassandraSession().execute("DROP INDEX Bd.vertices_vertACP");
					// getCassandraSession().execute("DROP INDEX Bd.vertices_vertBCP");
					// getCassandraSession().execute("CREATE TABLE Bd.verticesCP (vertA int, vertB int, idseccion int PRIMARY KEY)");
					// getCassandraSession().execute("CREATE INDEX vertices_vertACP ON Bd.verticesCP (vertA)");
					// getCassandraSession().execute("CREATE INDEX vertices_vertBCP ON Bd.verticesCP (vertB)");
					// getCassandraSession().execute("CREATE TABLE Bd.caracteristicasVerticesCP (idseccion int PRIMARY KEY, consumoMax int, coste float)");
					// getCassandraSession().execute("CREATE TABLE Bd.clientesCP (id int PRIMARY KEY, idseccion int,consumoActual int)");
					//BatchStatement batch = new BatchStatement();
					PreparedStatement psExtremos = getCassandraSession()
							.prepare(
									"INSERT INTO Bd.verticesCP (vertA,vertB,idseccion) VALUES (?, ?, ?)");
					PreparedStatement psCaracteristicas = getCassandraSession()
							.prepare(
									"INSERT INTO Bd.caracteristicasVerticesCP (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
					PreparedStatement psClientes = getCassandraSession()
							.prepare(
									"INSERT INTO Bd.clientesCP (id, idseccion,consumoActual) VALUES (?, ?, ?)");
					ResultSet results;
					/*
					 * ResultSet results =
					 * getCassandraSession().execute("SELECT * FROM Bd.vertices");
					 * while(results.iterator().hasNext()){ Row row =
					 * results.iterator().next();
					 * batch.add(psExtremos.bind(row.getInt
					 * ("idseccion"),row.getInt("vertA"),row.getInt("vertB"))); }
					 * results = getCassandraSession().execute(
					 * "SELECT * FROM Bd.caracteristicasVertices");
					 * while(results.iterator().hasNext()){ Row row =
					 * results.iterator().next();
					 * batch.add(psCaracteristicas.bind(row
					 * .getInt("idseccion"),row.getInt
					 * ("consumoMax"),row.getFloat("coste"))); }
					 
					results = getCassandraSession().execute(
							"SELECT * FROM Bd.clientes");
					while (results.iterator().hasNext()) {
						Row row = results.iterator().next();
						batch.add(psClientes.bind(row.getInt("id"),
								row.getInt("idseccion"),
								row.getInt("consumoActual")));
					}
					getCassandraSession().execute(batch);
					*/
				}

				System.out.println();
			}
		}
		public void setAutoInserta(boolean insertar) {
			this.autoinserta = insertar;
		}

		private Cluster getCassandraCluster() {
			return cluster;
		}

		public Session getCassandraSession() {
			if(session==null) esperaConexionCassandra();
			return session;
		}
		
		private boolean esperaConexionCassandra() {
			boolean encendido = false;
			int tiempo = 0;
			int espera = 3;
			int timeout = 15;
			encendido = false;
			if (enciendeProcesoCassandra()) {
				while (!encendido && tiempo < (timeout / espera)) {
					try {
						cluster = Cluster.builder().addContactPoint("127.0.0.1")
								.build();
						Metadata metadata = cluster.getMetadata();
						if (debug)
							System.out.printf("Conectado al cluster: %s\n",
									metadata.getClusterName());
						if (debug) {
							for (Host host : metadata.getAllHosts()) {
								System.out.printf(
										"Datatacenter: %s; Host: %s; Rack: %s\n",
										host.getDatacenter(), host.getAddress(),
										host.getRack());
							}
						}
						session = cluster.connect();

						encendido = true;
					} catch (NoHostAvailableException e) {
						try {
							System.out.println("Intentando reconectar en " + espera
									+ "s");
							Thread.sleep(espera * 1000);
							timeout++;
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (RejectedExecutionException e2) {
						try {
							System.out.println("Intentando reconectar en " + espera
									+ "s");
							Thread.sleep(espera * 1000);
							timeout++;
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
			if (tiempo == timeout / espera) {
				System.out.println("Dio timeout");
			}
			return encendido;
		}
		public boolean enciendeProcesoCassandra() {
			boolean encendido = false;
			encendido = compruebaProcesoCassandra();
			if (!encendido) {
				File currDir = new File(
						".\\apache-cassandra-2.0.9\\bin\\cassandra.bat");
				String path = currDir.getAbsolutePath();
				path = path.substring(0, path.length());
				String dir = path.substring(0, path.length() - 13);
				try {
					Runtime.getRuntime().exec(
							"cmd /c cd \"" + dir + "\" && start cassandra.bat");
					Thread.sleep(2000);
					encendido = compruebaProcesoCassandra();
				} catch (IOException e) {
					encendido = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					encendido = false;
				}
			}
			System.out.println("Resultado enciendeProcesoCassandra:" + encendido);
			return encendido;
		}

		private boolean compruebaProcesoCassandra() {
			boolean encendido = false;
			try {
				String line = "cmd /c tasklist.exe /v  | find " + "\""
						+ "CassandraMaster" + "\"";
				Process p = Runtime.getRuntime().exec(line);
				BufferedReader input = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				while ((line = input.readLine()) != null && !encendido) {
					if (!line.trim().equals("")) {
						encendido = true;
					}

				}
				input.close();
			} catch (Exception err) {
				encendido = false;
			}
			System.out.println("Resultado compruebaProcesoCassandra:" + encendido);
			return encendido;
		}

		public void apagaCassandra() {
			/*
			 * try { session.close(); String
			 * line="cmd /c tasklist.exe /v  | find "+"\""+"Cassandra"+"\""; Process
			 * p = Runtime.getRuntime().exec(line); BufferedReader input = new
			 * BufferedReader (new InputStreamReader(p.getInputStream())); while
			 * ((line = input.readLine()) != null) { if (!line.trim().equals("")) {
			 * // keep only the process name
			 * System.out.println(line.split("\\s{1,}")[1]); p =
			 * Runtime.getRuntime().exec("TASKKILL /PID "+line.split("\\s{1,}")[1]);
			 * }
			 * 
			 * } input.close(); } catch (Exception err) { err.printStackTrace(); }
			 */
		}

}
