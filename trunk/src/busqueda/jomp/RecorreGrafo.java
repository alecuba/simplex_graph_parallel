package busqueda.jomp;

import java.util.List;

import comun.Gestor;
import busqueda.ArrayListEnteros;
import busqueda.Camino;
import busqueda.Seccion;
import busqueda.Caminos;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {

	private Gestor gestor;

	public RecorreGrafo(Gestor gestor) {
		this.gestor = gestor;
	}

	public void generaCaminos(Camino caminoActual, ArrayListEnteros verticesActual) {
		if (debug)
			System.out.printf("generaCaminos cliente(" + caminoActual.idcliente
					+ ") en camino(" + caminoActual.idcamino + ") ultimo("+verticesActual.getUltimoElemento()+")\n");
		int[][] resultados = null;
		try {
			ResultSet resultsA = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ verticesActual.getUltimoElemento());
			ResultSet resultsB = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ verticesActual.getUltimoElemento());
			List rowsA = (List)resultsA.all();
			List rowsB = (List)resultsB.all();
			int pos = 0;
			resultados = new int[rowsA.size() + rowsB.size()][2];
			for(int i=0;i<rowsA.size();i++){
				resultados[pos][0] = ((Row) rowsA.get(i)).getInt("vertB");
				resultados[pos][1] = ((Row) rowsA.get(i)).getInt("idseccion");
				pos++;
			}
			for(int i=0;i<rowsB.size();i++){
				resultados[pos][1] = ((Row) rowsB.get(i)).getInt("idseccion");
				resultados[pos][0] = ((Row) rowsB.get(i)).getInt("vertA");
				pos++;
			}

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.pos = pos;
  __omp_Object0.rowsB = rowsB;
  __omp_Object0.rowsA = rowsA;
  __omp_Object0.resultsB = resultsB;
  __omp_Object0.resultsA = resultsA;
  __omp_Object0.resultados = resultados;
  __omp_Object0.verticesActual = verticesActual;
  __omp_Object0.caminoActual = caminoActual;
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
  pos = __omp_Object0.pos;
  rowsB = __omp_Object0.rowsB;
  rowsA = __omp_Object0.rowsA;
  resultsB = __omp_Object0.resultsB;
  resultsA = __omp_Object0.resultsA;
  resultados = __omp_Object0.resultados;
  verticesActual = __omp_Object0.verticesActual;
  caminoActual = __omp_Object0.caminoActual;
  gestor = __omp_Object0.gestor;
}
// OMP PARALLEL BLOCK ENDS

		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
			e.printStackTrace();
		}
	}

	/*
	private void rellenaResultado() {
		for(int i=0;i<caminosFinal.size();i++){
			for(int j=0;j<caminosFinal.get(i).secciones.size();j++){
				ResultSet resultsA = gestor.getCassandraSession().execute("SELECT idseccion,consumoMax,coste FROM Bd.caracteristicasVertices WHERE idseccion="+ caminosFinal.get(i).secciones.get(j).idSeccion+" LIMIT 1");
				List<Row> rowsA = resultsA.all();
				if(rowsA.size()==1){
					caminosFinal.get(i).secciones.get(j).coste=rowsA.get(0).getFloat("coste");
					caminosFinal.get(i).secciones.get(j).coste=rowsA.get(0).getInt("consumoMax");
				}				
			}
			
		}

	}
	*/

	
	

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

	private Caminos caminosEncontrados;

	public void encuentraCaminosTodosClientes(Caminos caminos) {
		if (debug)
			System.out.printf("encuentraCaminosTodosClientes\n");
		try {
			gestor.getCassandraSession().execute("USE Bd");
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorbd\n");
		}
		try {
			caminosEncontrados=caminos;
			ResultSet clientes = gestor.getCassandraSession().execute(
					"SELECT * FROM Bd.clientes");
			List rows = (List)clientes.all();
			for (int i=0;i<rows.size();i++) {
				System.out.print("Buscando camino para el cliente("
						+ ((Row)rows.get(i)).getInt("id") + ") con conexion("
						+ ((Row)rows.get(i)).getInt("idseccion") + "),");
				Camino caminoTemp = new Camino();
				caminoTemp.idcliente = ((Row)rows.get(i)).getInt("id");
				caminoTemp.idcamino = -1;
				ArrayListEnteros vertices = new ArrayListEnteros();
				vertices.add(((Row)rows.get(i)).getInt("idseccion"));
				generaCaminos(caminoTemp,vertices);
				//rellenaResultado();
				// consultarSiguientesSaltos(row.getInt("id"),row.getInt("idseccion"));
			}
			// limpiaCaminosSinSalida();
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorconsulta\n");
		}
		System.out.println("Terminado encuentraCaminosTodosClientes");
	}

	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  int pos;
  List rowsB;
  List rowsA;
  ResultSet resultsB;
  ResultSet resultsA;
  int [ ] [ ] resultados;
  ArrayListEnteros verticesActual;
  Camino caminoActual;
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
                                      for(int j = (int)__omp_ChunkData1.start; j < __omp_ChunkData1.stop; j += __omp_ChunkData1.step) {
                                        // OMP USER CODE BEGINS
 {
				if (debug)
					System.out.print(" \nllamando para subcamino("
							+ resultados[j][0] + ")");
				if (!verticesActual.compruebaSiContiene(resultados[j][0])) {
					Camino caminoAvisitar = new Camino();
					caminoAvisitar.idcliente = caminoActual.idcliente;
					caminoAvisitar.idcamino = caminoActual.idcamino;
					Seccion seccion=new Seccion();
					seccion.idSeccion=resultados[j][1];
					caminoAvisitar.secciones.addAll(caminoActual.secciones);
					caminoAvisitar.secciones.add(seccion);
					ArrayListEnteros verticesTemp = new ArrayListEnteros();
					verticesTemp.addAll(verticesActual);
					verticesTemp.add(resultados[j][0]);
					if (resultados[j][0] != 0) {
						generaCaminos(caminoAvisitar,verticesTemp);
					} else {
						/*//omp critical*/
						{
							caminosEncontrados.agregaCamino(caminoAvisitar);
						}
					}
				}
		}
                                        // OMP USER CODE ENDS
                                        if (j == (__omp_WholeData2.stop-1)) amLast = true;
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

