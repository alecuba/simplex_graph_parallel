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

	public void generaCaminos(){		
		while(!caminos.isFinalizados()){

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.caminos = caminos;
  __omp_Object0.gestor = gestor;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object0);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  caminos = __omp_Object0.caminos;
  gestor = __omp_Object0.gestor;
}
// OMP PARALLEL BLOCK ENDS
	
			
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
					caminos.agregaNuevoCamino(((Row)rows.get(i)).getInt("id"),-1,-1,((Row)rows.get(i)).getInt("idseccion"));
					generaCaminos();
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

	}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  Caminos caminos;
  Gestor gestor;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

			{
			Camino ultimoCamino;
                         // OMP CRITICAL BLOCK BEGINS
                         synchronized (jomp.runtime.OMP.getLockByName("getultimocamino")) {
                         // OMP USER CODE BEGINS

			{
			 ultimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
		    }
                         // OMP USER CODE ENDS
                         }
                         // OMP CRITICAL BLOCK ENDS

			if(ultimoCamino!=null){
				boolean error=false;
				int ultimoVertice = caminos.getUltimoVerticeDelCamino(ultimoCamino);
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
                                                                                 // OMP CRITICAL BLOCK BEGINS
                                                                                 synchronized (jomp.runtime.OMP.getLockByName("caminos")) {
                                                                                 // OMP USER CODE BEGINS

										{
										if(!caminos.isVerticeVisitadoDelCamino(ultimoCamino,resultados[j][0])){
											 caminos.agregaNuevoCaminoCopiando(ultimoCamino, resultados[j][1], resultados[j][0]);
											
										}
										}
                                                                                 // OMP USER CODE ENDS
                                                                                 }
                                                                                 // OMP CRITICAL BLOCK ENDS

										
									}
								}
						}
                                                 // OMP CRITICAL BLOCK BEGINS
                                                 synchronized (jomp.runtime.OMP.getLockByName("caminos")) {
                                                 // OMP USER CODE BEGINS

						{
						caminos.borraCamino(ultimoCamino);
						}
                                                 // OMP USER CODE ENDS
                                                 }
                                                 // OMP CRITICAL BLOCK ENDS

					}else{
							System.out.println("ResultsA o B a es null");
						}
				}
			}
			/*//omp barrier*/
		  }
    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS

}

