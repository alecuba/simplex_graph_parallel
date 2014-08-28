package busqueda.jomp;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import jomp.compiler.Jomp;
import comun.Gestor;
import busqueda.jomp.Caminos;
import busqueda.jomp.BubbleSortInt2d;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {

	private Gestor gestor;
	private Caminos caminos;
	
	
	public RecorreGrafo(Gestor gestor,Caminos caminos) {
		this.caminos=caminos;
		this.gestor = gestor;
	}
	
	public void generaCaminos(){
		boolean arrancado=false;
		int i=0;
		int posUltimoCamino;
		while(!caminos.isFinalizados()){

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.caminos = caminos;
  __omp_Object0.gestor = gestor;
  __omp_Object0.arrancado = arrancado;
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
  arrancado = __omp_Object0.arrancado;
}
// OMP PARALLEL BLOCK ENDS

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

	}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  Caminos caminos;
  Gestor gestor;
  boolean arrancado;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  int i;
  int posUltimoCamino;
  // reduction variables, init to default
    // OMP USER CODE BEGINS

			{
                             // OMP CRITICAL BLOCK BEGINS
                             synchronized (jomp.runtime.OMP.getLockByName("")) {
                             // OMP USER CODE BEGINS

				{
					posUltimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
				}
                             // OMP USER CODE ENDS
                             }
                             // OMP CRITICAL BLOCK ENDS

				if(posUltimoCamino!=-1){
						int ultimoVertice = caminos.getUltimoVerticeDelCamino(posUltimoCamino);
						if(ultimoVertice!=-1){
								try {
									ResultSet resultsA = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ultimoVertice);
									ResultSet resultsB = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ultimoVertice);
									List rowsA = (List)resultsA.all();
									List rowsB = (List)resultsB.all();
									int[][] resultados = new int[rowsA.size() + rowsB.size()][2];
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
									for (i = 0; i < resultados.length; i++) {				
										if(!caminos.isVerticeVisitadoDelCamino(posUltimoCamino,resultados[i][0])){
                                                                                         // OMP CRITICAL BLOCK BEGINS
                                                                                         synchronized (jomp.runtime.OMP.getLockByName("")) {
                                                                                         // OMP USER CODE BEGINS

											{
											 caminos.agregaNuevoCaminoCopiando(posUltimoCamino, resultados[i][1], resultados[i][0]);
											}
                                                                                         // OMP USER CODE ENDS
                                                                                         }
                                                                                         // OMP CRITICAL BLOCK ENDS

										}
									}
							    } catch (InvalidQueryException e) {
									if (debug)
										System.out.println("Error al obtener ultima seccion");
									e.printStackTrace();
								}
                                                     // OMP CRITICAL BLOCK BEGINS
                                                     synchronized (jomp.runtime.OMP.getLockByName("")) {
                                                     // OMP USER CODE BEGINS

							{
						    caminos.borraCamino(posUltimoCamino);
							}
                                                     // OMP USER CODE ENDS
                                                     }
                                                     // OMP CRITICAL BLOCK ENDS

						}
						if (posUltimoCamino==0 && arrancado){posUltimoCamino=-1;}else{arrancado=true;}
						/*//omp barrier*/
				}
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

