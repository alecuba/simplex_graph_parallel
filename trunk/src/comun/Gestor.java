package comun;

import generacion.GenerarClientes;
import generacion.GenerarGrafo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.RejectedExecutionException;

import linealizacion.PreparaLineal;
import busqueda.Caminos;
import busqueda.RecorreGrafo;
import busqueda.Secciones;

import com.codahale.metrics.Gauge;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
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
		private int usaTablaPrecargada = 1;
		private int tablaNum = 2;
		private int maxSimultaneousRequest;
		private int maxConnectionsPerHost;
		
		public void setDebug(boolean debug) {
			this.debug = debug;
		}
		
		public boolean getDebug(){
			return debug;
		}
		
		public void setPrecargaTabla(int precarga,int num){
			this.usaTablaPrecargada=precarga;
			this.tablaNum=num;
		}
		
		public void setPrecargaTabla(int num){
			this.tablaNum=num;
		}
		
		public int[] getPrecargaTabla(){
			int[] datos={usaTablaPrecargada,tablaNum};
			return datos;
		}
		
		public boolean getAutoInserta(){
			return autoinserta;
		}
		
		
		private void preCargaTabla(){
           switch(tablaNum){
           case 1: preCargaTabla2();System.out.println("Utilizando tabla test1");break;
           case 2: preCargaTabla3();System.out.println("Utilizando tabla test2");break;
           case 50: copiaTabla("50",true);System.out.println("Utilizando tabla test50");break;
           case 100:copiaTabla("100",true);System.out.println("Utilizando tabla test100");break;
           case 200:copiaTabla("200",true);System.out.println("Utilizando tabla test200");break;
           case 400:copiaTabla("400",true);System.out.println("Utilizando tabla test400");break;
           }
		}
		
		public boolean generarSeccionesYClientes(int minSecciones, int maxSecciones, int minCruces,int maxCruces,int minEntradas,int maxEntradas,int minClientes, int maxClientes, int minConsumo,int maxConsumo){
			boolean generado=false;
			if (usaTablaPrecargada==1){
				this.preCargaTabla();
			}else{
				if(generarSecciones(minSecciones,maxSecciones,minCruces,maxCruces,minEntradas,maxEntradas)){
					generado=generaClientes(minClientes,maxClientes,minConsumo,maxConsumo);
				}
			}
			return generado;
		}
		
		public boolean generarSecciones(int minSecciones, int maxSecciones, int minCruces,int maxCruces,int minEntradas,int maxEntradas){
			boolean generado=false;
			if (usaTablaPrecargada==1){
				this.preCargaTabla();
			}else{
			if (esperaConexionCassandra()) {
				if (grafo == null)	grafo = new GenerarGrafo(this);
				grafo.setDebug(debug);
				if (grafo.generar(minSecciones,maxSecciones,minCruces,maxCruces, minEntradas, maxEntradas)) {
					generado=true;
					if (debug)
						grafo.pintaTabla();
					if (autoinserta && esperaConexionCassandra()) {
						grafo.insertaGrafoCQL(true);
					}
				}
			}		
			}
			if(!generado) System.out.println("No se pudo generar los grafos");
			return generado;
		}
		
		public boolean generaClientes(int minClientes, int maxClientes, int minConsumo,int maxConsumo){
			boolean generado=false;
			if (usaTablaPrecargada==1) {this.preCargaTabla();}else{
			if (esperaConexionCassandra()) {
				if (clientes == null) clientes = new GenerarClientes(grafo,this);
				clientes.setDebug(debug);
				clientes.generar(minClientes,maxClientes,minConsumo,maxConsumo);
				if(grafo!=null) clientes.conectarClientes(grafo);
				generado=true;
				if (debug)
					clientes.pintaTabla();
				if (autoinserta) {
					if (esperaConexionCassandra()) {
						clientes.insertaGrafoCQL(true);
					}
				}
			}
			}
			if(!generado) System.out.println("No se pudo generar a los clientes");
			return generado;
		}
		
		public void encuentraCaminos(int minSecciones, int maxSecciones, int minCruces,int maxCruces,int minEntradas,int maxEntradas,int minClientes, int maxClientes, int minConsumo,int maxConsumo){
			boolean generado=true;
			if (usaTablaPrecargada==1){
				this.preCargaTabla();
			}
			if((grafo==null) && checkGrafoVacioBD()) generado=generarSecciones( minSecciones,  maxSecciones,  minCruces, maxCruces, minEntradas, maxEntradas);
			if((clientes==null)&&checkClienteVacioBD()) generado=generaClientes(minClientes,  maxClientes,  minConsumo, maxConsumo);
			if(generado){
						Secciones secciones=new Secciones();
						Caminos caminos = new Caminos(secciones);
						RecorreGrafo recorre = new RecorreGrafo(this,caminos);
						recorre.setDebug(true);
						long tiempo=recorre.encuentraCaminosTodosClientes();
						caminos.pintaCaminosGenerados();
						System.out.println("\nNumero Threads utilizados:"+jomp.runtime.OMP.getMaxThreads()+" tiempo("+(tiempo/1000F)+")");
						if(!secciones.rellenaCaracteristicas(this)){
							//Linealizar
							PreparaLineal lineal = new PreparaLineal(this);
							lineal.creaTablaPreSimplex(caminos,secciones);
							lineal.pintaTablaPreSimplex();
							//simplex
						}
						}else{
							if(!generado) System.out.println("No se pudo encontrar caminos porque no se genero");
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
		
		public boolean checkGrafoVacioBD() {
			boolean vacia = false;
			ResultSet results = null;
			if (esperaConexionCassandra()) {
				try {
					getCassandraSession().execute("USE Bd");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("No existe BD\n");
				}
				if(!vacia){
				try {
					results = getCassandraSession().execute("SELECT * FROM vertices LIMIT 1");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("No existe vertices\n");
				}
				}
				if(!vacia && results != null){
					if(results.all().isEmpty()) vacia=true;
				}
			}
			return vacia;
		}
		
		public boolean checkClienteVacioBD() {
			boolean vacia = false;
			ResultSet results = null;
			if (esperaConexionCassandra()) {
				try {
					getCassandraSession().execute("USE Bd");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("No existe BD\n");
				}
				if(!vacia){
				try {
					results = getCassandraSession().execute("SELECT * FROM clientes LIMIT 1");
				} catch (InvalidQueryException e) {
					vacia = true;
					System.out.println("No existe clientes\n");
				}
				}
				if(!vacia && results != null){
					if(results.all().isEmpty()) vacia=true;
				}
			}
			return vacia;
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
		
		public void preCargaTabla2() {
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

				int[][] vertices = { { 0, 0, 2 }, { 1, 0, 6 }, { 2, 1, 3 },
						{ 3, 1, 4 }, { 4, 1, 6 }, { 5, 2, 4 }, { 6, 2, 6 },
						{ 7, 3, 4 }, { 8, 3, 5 }, { 9, 3, 6 }, { 10, 4, 5 },
						{ 11, 4, 6 }, { 12, 5, 6 } };
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
		
		public void preCargaTabla3() {
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

				int[][] vertices = { { 0, 0, 2 }, { 1, 0, 1 },{ 2, 1, 2 }};
				float[][] verticesCaracteristicas = { { 0, 13438, (float) 0.5 },
						{ 1, 15040, (float) 0.7 }, { 2, 11273, (float) 0.9 }};
				int[][] clientes = { { 0, 1, 2039 },{ 1, 2, 2109 }};
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

		public void copiaTabla(String num,boolean restaura) {
			System.out.println("restaura:"+restaura+" para num:"+num);
			if (esperaConexionCassandra()) {
			 if(!restaura){
				
				try {
					getCassandraSession().execute("USE Bd");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE KEYSPACE IF NOT EXISTS Bd WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
			    	getCassandraSession().execute("USE Bd");
				}

				try {
					getCassandraSession().execute("SELECT * FROM Bd.vertices"+num+" LIMIT 1");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE TABLE Bd.vertices"+num+" (vertA int, vertB int, idseccion int PRIMARY KEY)");
					getCassandraSession().execute("CREATE INDEX vertices_vertA"+num+" ON Bd.vertices"+num+" (vertA)");
					getCassandraSession().execute("CREATE INDEX vertices_vertB"+num+" ON Bd.vertices"+num+" (vertB)");
				}
				try {
					getCassandraSession().execute("SELECT * FROM Bd.caracteristicasVertices"+num+" LIMIT 1");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE TABLE Bd.caracteristicasVertices"+num+" (idseccion int PRIMARY KEY, consumoMax int, coste float)");
				}
				try {
					getCassandraSession().execute("SELECT * FROM Bd.clientes"+num+" LIMIT 1");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE TABLE Bd.clientes"+num+" (id int PRIMARY KEY, idseccion int,consumoActual int)");
				}
			 
				BatchStatement batch = new BatchStatement();
				PreparedStatement psExtremos = getCassandraSession().prepare("INSERT INTO Bd.vertices"+num+" (vertA,vertB,idseccion) VALUES (?, ?, ?)");
				PreparedStatement psCaracteristicas = getCassandraSession().prepare("INSERT INTO Bd.caracteristicasVertices"+num+" (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
				PreparedStatement psClientes = getCassandraSession().prepare("INSERT INTO Bd.clientes"+num+" (id, idseccion,consumoActual) VALUES (?, ?, ?)");
				ResultSet results =	getCassandraSession().execute("SELECT * FROM Bd.vertices");
				while(results.iterator().hasNext()){ 
						Row row = results.iterator().next();
						batch.add(psExtremos.bind(row.getInt("idseccion"),row.getInt("vertA"),row.getInt("vertB"))); }
						results = getCassandraSession().execute("SELECT * FROM Bd.caracteristicasVertices");
				while(results.iterator().hasNext()){
						Row row = results.iterator().next();
						batch.add(psCaracteristicas.bind(row.getInt("idseccion"),row.getInt("consumoMax"),row.getFloat("coste"))); }
					 	results = getCassandraSession().execute("SELECT * FROM Bd.clientes");
				while (results.iterator().hasNext()) {
						Row row = results.iterator().next();
						batch.add(psClientes.bind(row.getInt("id"),row.getInt("idseccion"),row.getInt("consumoActual")));
					}
					getCassandraSession().execute(batch);
			}else{
				try {
					getCassandraSession().execute("USE Bd");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE KEYSPACE IF NOT EXISTS Bd WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
			    	getCassandraSession().execute("USE Bd");
				}

				try {
					getCassandraSession().execute("SELECT * FROM Bd.vertices LIMIT 1");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE TABLE Bd.vertices (vertA int, vertB int, idseccion int PRIMARY KEY)");
					getCassandraSession().execute("CREATE INDEX vertices_vertA ON Bd.vertices (vertA)");
					getCassandraSession().execute("CREATE INDEX vertices_vertB ON Bd.vertices (vertB)");
				}
				try {
					getCassandraSession().execute("SELECT * FROM Bd.caracteristicasVertices LIMIT 1");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE TABLE Bd.caracteristicasVertices (idseccion int PRIMARY KEY, consumoMax int, coste float)");
				}
				try {
					getCassandraSession().execute("SELECT * FROM Bd.clientes LIMIT 1");
				} catch (InvalidQueryException e) {
					getCassandraSession().execute("CREATE TABLE Bd.clientes (id int PRIMARY KEY, idseccion int,consumoActual int)");
				}
				BatchStatement batch = new BatchStatement();
				PreparedStatement psExtremos = getCassandraSession().prepare("INSERT INTO Bd.vertices (vertA,vertB,idseccion) VALUES (?, ?, ?)");
				PreparedStatement psCaracteristicas = getCassandraSession().prepare("INSERT INTO Bd.caracteristicasVertices (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
				PreparedStatement psClientes = getCassandraSession().prepare("INSERT INTO Bd.clientes (id, idseccion,consumoActual) VALUES (?, ?, ?)");
				ResultSet results =	getCassandraSession().execute("SELECT * FROM Bd.vertices"+num);
				System.out.println("Se van a insertar "+results.all().size()+" vertices");
				while(results.iterator().hasNext()){ 
						Row row = results.iterator().next();
						batch.add(psExtremos.bind(row.getInt("idseccion"),row.getInt("vertA"),row.getInt("vertB"))); }
				results = getCassandraSession().execute("SELECT * FROM Bd.caracteristicasVertices"+num);
				while(results.iterator().hasNext()){
						Row row = results.iterator().next();
						batch.add(psCaracteristicas.bind(row.getInt("idseccion"),row.getInt("consumoMax"),row.getFloat("coste"))); }
				results = getCassandraSession().execute("SELECT * FROM Bd.clientes"+num);
				while (results.iterator().hasNext()) {
						Row row = results.iterator().next();
						batch.add(psClientes.bind(row.getInt("id"),row.getInt("idseccion"),row.getInt("consumoActual")));
					}
					getCassandraSession().execute(batch);
			}
		   }
		}
		
		public void setAutoInserta(boolean insertar) {
			this.autoinserta = insertar;
		}

		private Cluster getCassandraCluster() {
			return cluster;
		}

		public Session getCassandraSession() {
			if(session==null){
				esperaConexionCassandra();
			} else{
				
				//Gauge<Integer> gauge = session.getCluster().getMetrics().getOpenConnections();
				//session.getCluster().getMetrics().getOpenConnections()
				//Integer numberOfHosts = session.getCluster().getMetrics().getOpenConnections().getValue();
			return session;
			}
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
						Thread.sleep(espera * 1000);
					} catch (InterruptedException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					try {
						cluster = Cluster.builder().addContactPoint("127.0.0.1").withProtocolVersion(2).build();
						Metadata metadata = cluster.getMetadata();
						this.maxSimultaneousRequest=cluster.getConfiguration().getPoolingOptions().getMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL);
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
					} catch (OutOfMemoryError e){
						encendido = false;
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
