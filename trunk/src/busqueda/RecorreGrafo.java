package busqueda;

import java.util.List;

import comun.Gestor;
import busqueda.BubbleSortInt2d;
import busqueda.Caminos.Camino;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {


	private Gestor gestor;
	private Caminos caminos;
	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public RecorreGrafo(Gestor gestor,Caminos caminos) {
		this.caminos=caminos;
		this.gestor = gestor;
	}

	public void generaCaminos(int idcliente,int verticeInicial){
		boolean iniciado=false;
		while(!caminos.isFinalizados()||!iniciado){
			/*//omp parallel shared(caminos,iniciado)*/ 
			//{
			Camino ultimoCamino=null;
		    if(iniciado){
		    	/*//omp critical getultimocamino*/
		    	//{
		    		ultimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
		    	//}
			}
			if(ultimoCamino!=null||!iniciado){
				boolean error=false;
				int ultimoVertice;
				if(iniciado&&ultimoCamino!=null){
				ultimoVertice = caminos.getUltimoVerticeDelCamino(ultimoCamino);
				}else{
					ultimoVertice=verticeInicial;
					/*//omp critical getultimocamino*/
			    	//{
			    		iniciado=true;
			    	//}
				}
				if(ultimoVertice!=-1){
					Statement  consulta1 = new SimpleStatement("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ultimoVertice);
					Statement  consulta2 = new SimpleStatement("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ultimoVertice);
					ResultSet  resultsA=null;
					ResultSet  resultsB=null;
						try {
							resultsA = gestor.getCassandraSession().execute(consulta1);
							resultsB = gestor.getCassandraSession().execute(consulta2);
							error=false;
					    } catch (InvalidQueryException e) {
							if (debug)
								System.out.println("Error al obtener ultima seccion");
							e.printStackTrace();
							error=true;
					    }
					if(!error){
						if(resultsA!=null&&resultsB!=null){
							List rowsA = (List)resultsA.all();
							List rowsB = (List)resultsB.all();
							if(rowsA.size()>0||rowsB.size()>0){
									int[][] resultados = new int[rowsA.size() + rowsB.size()][2];
									int i=0;
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
									/*//omp for schedule(dynamic,1)*/
									for (int j = 0; j < resultados.length; j++) {
										//if(debug) System.out.println("Busqueda Camino soy tread"+jomp.runtime.OMP.getThreadNum());
										/*//omp critical caminos*/
										//{
										if(!caminos.isVerticeVisitadoDelCamino(ultimoCamino,resultados[j][0])){
											if(ultimoVertice!=verticeInicial){
											 caminos.agregaNuevoCamino(ultimoCamino, resultados[j][1], resultados[j][0]);
											}else{
												 caminos.agregaNuevoCamino(idcliente,verticeInicial, resultados[j][1], resultados[j][0]);
											}											
										}
										//}
									}
								}
						}
						if(ultimoVertice!=verticeInicial){
						/*//omp critical caminos*/
						//{
						caminos.borraCamino(ultimoCamino);
						//}
						}
					}else{
							System.out.println("ResultsA o B a es null");
						}
				}
			}
			/*//omp barrier	*/
		  //}		
		}
	}
	
	public long encuentraCaminosTodosClientes() {
		long elapsedTimeMillis=0;
		boolean error=false;
		if (debug)
			System.out.printf("encuentraCaminosTodosClientes\n");
		try {
			gestor.getCassandraSession().execute("USE Bd");
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorbd\n");
		}
		ResultSet clientes=null;
		try {
			clientes = gestor.getCassandraSession().execute(
					"SELECT * FROM Bd.clientes");
			error=false;
	    } catch (InvalidQueryException e) {
			if (debug)
				System.out.println("Error al obtener clientes");
			e.printStackTrace();
			error=true;
	    }
	if(!error){
		if(clientes!=null){
			List rows = (List)clientes.all();
			if(rows.size()>0){
				long start = System.currentTimeMillis();
				for (int i=0;i<rows.size();i++) {
					System.out.print("Buscando camino para el cliente("
							+ ((Row)rows.get(i)).getInt("id") + ") conectado a la Seccion("
							+ ((Row)rows.get(i)).getInt("idseccion") + ")\n");
					generaCaminos(((Row)rows.get(i)).getInt("id"),((Row)rows.get(i)).getInt("idseccion"));
				}
				elapsedTimeMillis = System.currentTimeMillis()-start;
			}
			}
	}
		System.out.println("Terminado encuentraCaminosTodosClientes");
		return elapsedTimeMillis;
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

