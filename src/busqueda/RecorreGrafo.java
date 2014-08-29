package busqueda;

import java.util.List;
import comun.Gestor;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.SimpleStatement;
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
		long iteraciones=0;
		ResultSet resultsA;
		ResultSet resultsB;
		int ultimoVertice;
		while(!caminos.isFinalizados()){
			iteraciones++;
			posUltimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
			if(posUltimoCamino!=-1){
				ultimoVertice = caminos.getUltimoVerticeDelCamino(posUltimoCamino);
				if(ultimoVertice!=-1){
						try {
							 Statement consulta1 = new SimpleStatement("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ultimoVertice);
							 Statement consulta2 = new SimpleStatement("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ultimoVertice);
							resultsA = gestor.getCassandraSession().execute(consulta1);
							resultsB = gestor.getCassandraSession().execute(consulta2);
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

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.sort2d = sort2d;
  __omp_Object0.rowsB = rowsB;
  __omp_Object0.rowsA = rowsA;
  __omp_Object0.consulta2 = consulta2;
  __omp_Object0.consulta1 = consulta1;
  __omp_Object0.ultimoVertice = ultimoVertice;
  __omp_Object0.resultsB = resultsB;
  __omp_Object0.resultsA = resultsA;
  __omp_Object0.iteraciones = iteraciones;
  __omp_Object0.posUltimoCamino = posUltimoCamino;
  __omp_Object0.i = i;
  __omp_Object0.arrancado = arrancado;
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
  sort2d = __omp_Object0.sort2d;
  rowsB = __omp_Object0.rowsB;
  rowsA = __omp_Object0.rowsA;
  consulta2 = __omp_Object0.consulta2;
  consulta1 = __omp_Object0.consulta1;
  ultimoVertice = __omp_Object0.ultimoVertice;
  resultsB = __omp_Object0.resultsB;
  resultsA = __omp_Object0.resultsA;
  iteraciones = __omp_Object0.iteraciones;
  posUltimoCamino = __omp_Object0.posUltimoCamino;
  i = __omp_Object0.i;
  arrancado = __omp_Object0.arrancado;
  caminos = __omp_Object0.caminos;
  gestor = __omp_Object0.gestor;
}
// OMP PARALLEL BLOCK ENDS

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
	
	public long encuentraCaminosTodosClientes() {
		long elapsedTimeMillis=0;
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
			elapsedTimeMillis = System.currentTimeMillis()-start;
			// limpiaCaminosSinSalida();
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorconsulta\n");
		}
		System.out.println("Terminado encuentraCaminosTodosClientes");
		return elapsedTimeMillis;
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
  BubbleSortInt2d sort2d;
  List rowsB;
  List rowsA;
  Statement consulta2;
  Statement consulta1;
  int ultimoVertice;
  ResultSet resultsB;
  ResultSet resultsA;
  long iteraciones;
  int posUltimoCamino;
  int i;
  boolean arrancado;
  Caminos caminos;
  Gestor gestor;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

                                                                  { // OMP FOR BLOCK BEGINS
                                                                  // copy of firstprivate variables, initialized
                                                                  // copy of lastprivate variables
                                                                  // variables to hold result of reduction
                                                                  boolean amLast=false;
                                                                  {
                                                                    // firstprivate variables + init
                                                                    // [last]private variables
                                                                    // reduction variables + init to default
                                                                    // -------------------------------------
                                                                    jomp.runtime.LoopData __omp_WholeData2 = new jomp.runtime.LoopData();
                                                                    jomp.runtime.LoopData __omp_ChunkData1 = new jomp.runtime.LoopData();
                                                                    __omp_WholeData2.start = (long)( 0);
                                                                    __omp_WholeData2.stop = (long)( resultados.length);
                                                                    __omp_WholeData2.step = (long)(1);
                                                                    jomp.runtime.OMP.setChunkStatic(__omp_WholeData2);
                                                                    while(!__omp_ChunkData1.isLast && jomp.runtime.OMP.getLoopStatic(__omp_me, __omp_WholeData2, __omp_ChunkData1)) {
                                                                    for(;;) {
                                                                      if(__omp_WholeData2.step > 0) {
                                                                         if(__omp_ChunkData1.stop > __omp_WholeData2.stop) __omp_ChunkData1.stop = __omp_WholeData2.stop;
                                                                         if(__omp_ChunkData1.start >= __omp_WholeData2.stop) break;
                                                                      } else {
                                                                         if(__omp_ChunkData1.stop < __omp_WholeData2.stop) __omp_ChunkData1.stop = __omp_WholeData2.stop;
                                                                         if(__omp_ChunkData1.start > __omp_WholeData2.stop) break;
                                                                      }
                                                                      for(int i = (int)__omp_ChunkData1.start; i < __omp_ChunkData1.stop; i += __omp_ChunkData1.step) {
                                                                        // OMP USER CODE BEGINS
 {
								if(!caminos.isVerticeVisitadoDelCamino(posUltimoCamino,resultados[i][0])){
									 caminos.agregaNuevoCaminoCopiando(posUltimoCamino, resultados[i][1], resultados[i][0]);
								}
							}
                                                                        // OMP USER CODE ENDS
                                                                        if (i == (__omp_WholeData2.stop-1)) amLast = true;
                                                                      } // of for 
                                                                      if(__omp_ChunkData1.startStep == 0)
                                                                        break;
                                                                      __omp_ChunkData1.start += __omp_ChunkData1.startStep;
                                                                      __omp_ChunkData1.stop += __omp_ChunkData1.startStep;
                                                                    } // of for(;;)
                                                                    } // of while
                                                                    // call reducer
                                                                    jomp.runtime.OMP.doBarrier(__omp_me);
                                                                    // copy lastprivate variables out
                                                                    if (amLast) {
                                                                    }
                                                                  }
                                                                  // set global from lastprivate variables
                                                                  if (amLast) {
                                                                  }
                                                                  // set global from reduction variables
                                                                  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
                                                                  }
                                                                  } // OMP FOR BLOCK ENDS

    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS

}

