package busqueda;

import java.util.List;
import comun.Gestor;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {

	private Gestor gestor;
	private Caminos caminos;
	public int[][] resultados;
	
	public RecorreGrafo(Gestor gestor,Caminos caminos) {
		this.caminos=caminos;
		this.gestor = gestor;
	}

	public void generaCaminos(){
		boolean arrancado=false;
		int i=0;
		int posUltimoCamino;
		ResultSet resultsA;
		ResultSet resultsB;
		int ultimoVertice;
		while(!caminos.isFinalizados()){
			posUltimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
			if(posUltimoCamino!=-1){
				ultimoVertice = caminos.getUltimoVerticeDelCamino(posUltimoCamino);
				if(ultimoVertice!=-1){
						try {
							resultsA = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ultimoVertice);
							resultsB = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ultimoVertice);
							List rowsA = (List)resultsA.all();
							List rowsB = (List)resultsB.all();
							resultados = new int[rowsA.size() + rowsB.size()][2];
							for(i=0;i<rowsA.size();i++){
								resultados[i][0] = ((Row) rowsA.get(i)).getInt("vertB");
								resultados[i][1] = ((Row) rowsA.get(i)).getInt("idseccion");
							}
							for(i=i;i<rowsA.size()+rowsB.size();i++){
								resultados[i][1] = ((Row) rowsB.get(i-rowsA.size())).getInt("idseccion");
								resultados[i][0] = ((Row) rowsB.get(i-rowsA.size())).getInt("vertA");
							}
							BubbleSortInt2d sort2d = new BubbleSortInt2d();
							resultados=sort2d.sort(resultados);
							//omp parallel for private(i)
							for (i = 0; i < resultados.length; i++) {				
								if(!caminos.isVerticeVisitadoDelCamino(posUltimoCamino,resultados[i][0])){
									 caminos.agregaNuevoCaminoCopiando(posUltimoCamino, resultados[i][1], resultados[i][0]);
								}
							}
					    } catch (InvalidQueryException e) {
							if (debug)
								System.out.println("Error al obtener ultima seccion");
							e.printStackTrace();
						} catch (java.lang.ArrayIndexOutOfBoundsException e){
							if (debug)
								System.out.println("Error al obtener ultima seccion");
							e.printStackTrace();
						}
				    caminos.borraCamino(posUltimoCamino);
				}
				if (posUltimoCamino==0 && arrancado){posUltimoCamino=-1;}else{arrancado=true;}
			}
		}
	}
	
	public Caminos encuentraCaminosTodosClientes() {
		if (debug)
			System.out.printf("encuentraCaminosTodosClientes\n");
		try {
			gestor.getCassandraSession().execute("USE Bd");
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorbd\n");
		}
		try {
			ResultSet clientes = gestor.getCassandraSession().execute(
					"SELECT * FROM Bd.clientes");
			List rows = (List)clientes.all();
			long start = System.currentTimeMillis();
			for (int i=0;i<rows.size();i++) {
				System.out.print("Buscando camino para el cliente("
						+ ((Row)rows.get(i)).getInt("id") + ") conectado a la Seccion("
						+ ((Row)rows.get(i)).getInt("idseccion") + ")\n");
				caminos.agregaNuevoCamino(((Row)rows.get(i)).getInt("id"),-1,-1,((Row)rows.get(i)).getInt("idseccion"));
				generaCaminos();
			}
			long elapsedTimeMillis = System.currentTimeMillis()-start;
			System.out.println("\nZona paralela:"+jomp.runtime.OMP.inParallel()+" tiempo("+(elapsedTimeMillis/1000F)+")");
			// limpiaCaminosSinSalida();
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorconsulta\n");
		}
		System.out.println("Terminado encuentraCaminosTodosClientes");
		return caminos;
	}

	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public double consultaNumeroVertices() {
		double nVertices = 0;
		if (debug)
			System.out.printf("consultaNumeroVertices:");
		try {
			gestor.getCassandraSession().execute("USE Bd");
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorbd\n");
		}
		try {
			nVertices = gestor.getCassandraSession()
					.execute("SELECT COUNT(1) FROM Bd.vertices;").all().get(0)
					.getLong("count");
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorconsulta\n");
		}
		if (debug)
			System.out.printf(nVertices + "\n");
		return nVertices;
	}
	

	void sigueCamino(int idcamino, int camino, int vertActual) {
		/*
		 * caminos[idcliente][idcamino][camino]=vertActual; camino++; Select
		 * vertA,vertB where A,B = vertActual for each row
		 * if(!compruebaVerticeVisitado
		 * (caminos[idcliente][idcamino],row.getInt(vertA)) {
		 * sigueCamino(idcliente,idcamino,camino,row.getInt(vert)); idcamino++;
		 * 
		 * }
		 */

	}}

