package busqueda;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import comun.Gestor;
import busqueda.BubbleSortInt2d;
import busqueda.Camino;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {


	private Gestor gestor;
	private Caminos caminos;
	private boolean debug = false;
	private boolean paralelo=true;

	public void setFlags(boolean debug,boolean paralelo) {
		this.debug = debug;
		this.paralelo=paralelo;
	}
	
	public RecorreGrafo(Gestor gestor,Caminos caminos) {
		this.caminos=caminos;
		this.gestor = gestor;
	}
	
	public int generaCaminos(int idcliente,int verticeInicial){
		if(debug)System.out.println("Genera Caminos secuencial");
		boolean iniciado=false;
		Camino ultimoCamino=null;
		int ultimoVertice=-1;
		ArrayList resultados;
		int maxThreads=0;
		int idseccion;
		int vertice;
	
		while(!caminos.isFinalizados()||!iniciado){
		    if(iniciado){//iniciado
		    		if(jomp.runtime.OMP.getThreadNum()>maxThreads)maxThreads = jomp.runtime.OMP.getThreadNum();
		    		ultimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
		    		if(ultimoCamino!=null){
							ultimoVertice = caminos.getUltimoVerticeDelCamino(ultimoCamino);
							if(ultimoVertice!=-1){
								resultados=getVerticesDelVertice(ultimoVertice);
								if(resultados!=null&&!resultados.isEmpty()){
									Iterator itrResultados=resultados.iterator();
									while(itrResultados.hasNext()){
										idseccion=(int)itrResultados.next();
										vertice=(int)itrResultados.next();
										if(!caminos.isVerticeVisitadoDelCamino(ultimoCamino,vertice)) caminos.agregaNuevoCamino(ultimoCamino, idseccion,vertice);								
									}
									
								}else{
										System.out.println("resultados es null");
									}
							}
							caminos.borraCamino(ultimoCamino);
						}
			}else{//No iniciado
					//if(debug) System.out.println("Busqueda Camino soy tread"+jomp.runtime.OMP.getThreadNum());
					resultados=getVerticesDelVertice(verticeInicial);
					if(resultados!=null&&!resultados.isEmpty()){
						Iterator itrResultados=resultados.iterator();
						while(itrResultados.hasNext()){
							idseccion=(int)itrResultados.next();
							vertice=(int)itrResultados.next();
							caminos.agregaNuevoCamino(idcliente,verticeInicial,idseccion, vertice);	
						}
						iniciado=true;
					}else{
						System.out.println("resultados es null");
						}
				}	
		}
		return maxThreads;
	}
	
	public int generaCaminosParalelo(int idcliente,int verticeInicial){
		if(debug)System.out.println("Genera Caminos paralelo");
		boolean iniciado=false;
		Camino ultimoCamino=null;
		int ultimoVertice=-1;
		int maxThreads=0;
		
		while(!caminos.isFinalizados()||!iniciado){

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.iniciado = iniciado;
  __omp_Object0.maxThreads = maxThreads;
  __omp_Object0.verticeInicial = verticeInicial;
  __omp_Object0.idcliente = idcliente;
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
  iniciado = __omp_Object0.iniciado;
  maxThreads = __omp_Object0.maxThreads;
  verticeInicial = __omp_Object0.verticeInicial;
  idcliente = __omp_Object0.idcliente;
  caminos = __omp_Object0.caminos;
  gestor = __omp_Object0.gestor;
}
// OMP PARALLEL BLOCK ENDS

		}
		System.out.println("Terminado paralelo");
		return maxThreads;
	}
	
	private int[] secciones;
	
	private void cargaSecciones(){
		boolean error;
		Statement  consulta1 = new SimpleStatement("SELECT idseccion,vertA,vertB FROM Bd.vertices");
		ResultSet  resultsA=null;
			try {
				resultsA = gestor.getCassandraSession().execute(consulta1);
				error=false;
		    } catch (InvalidQueryException e) {
				if (debug)
					System.out.println("Error al obtener secciones");
				e.printStackTrace();
				error=true;
		    }
		if(!error){
			if(resultsA!=null){
				List rowsA = (List)resultsA.all();
				if(rowsA.size()>0){
					secciones = new int[rowsA.size()*3];
						int i=0;
						int ii=0;
						for(i=0;i<rowsA.size();i++){
							secciones[ii] = ((Row) rowsA.get(i)).getInt("idseccion");
							ii++;
							secciones[ii] = ((Row) rowsA.get(i)).getInt("vertA");
							ii++;
							secciones[ii] = ((Row) rowsA.get(i)).getInt("vertB");
							ii++;
						}
				}
			}
		}		
	}
	
	private ArrayList getVerticesDelVertice(int vertice){
		ArrayList posiciones= new ArrayList();
		for(int i=0;i<secciones.length;i+=3){
			if(secciones[i+1]==vertice){//vertA
				posiciones.add(secciones[i]);
				posiciones.add(secciones[i+2]);
			}else if(secciones[i+2]==vertice){//vertB
				posiciones.add(secciones[i]);
				posiciones.add(secciones[i+1]);
			}
		}
		return posiciones;
	}
	
	
	public long encuentraCaminosTodosClientes() {
		int threads=0;
		long elapsedTimeMillis=0;
		long start=0;
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
			cargaSecciones();
			
			List rows = (List)clientes.all();
			if(rows.size()>0){
				for (int i=0;i<rows.size();i++) {
					System.out.print("Buscando camino para el cliente("
							+ ((Row)rows.get(i)).getInt("id") + ") conectado a la Seccion("
							+ ((Row)rows.get(i)).getInt("idseccion") + ")\n");
					start = System.nanoTime();
					if(paralelo){
						threads=generaCaminosParalelo(((Row)rows.get(i)).getInt("id"),((Row)rows.get(i)).getInt("idseccion"));}
					else{
						generaCaminos(((Row)rows.get(i)).getInt("id"),((Row)rows.get(i)).getInt("idseccion"));
					}
					elapsedTimeMillis = elapsedTimeMillis+(System.nanoTime()-start);
				}
				
				
			}
			}
	}
		System.out.println("Terminado encuentraCaminosTodosClientes threads utilizados="+(threads+1));
		return elapsedTimeMillis;
	}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  boolean iniciado;
  int maxThreads;
  int verticeInicial;
  int idcliente;
  Caminos caminos;
  Gestor gestor;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  Camino ultimoCamino = new Camino();
  int ultimoVertice;
  // reduction variables, init to default
    // OMP USER CODE BEGINS

			{
			if(iniciado){
                                 // OMP CRITICAL BLOCK BEGINS
                                 synchronized (jomp.runtime.OMP.getLockByName("getultimocamino")) {
                                 // OMP USER CODE BEGINS

			    	{
		    		if(jomp.runtime.OMP.getThreadNum()>maxThreads)maxThreads = jomp.runtime.OMP.getThreadNum();
		    		ultimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
			    	}
                                 // OMP USER CODE ENDS
                                 }
                                 // OMP CRITICAL BLOCK ENDS

		    		if(ultimoCamino!=null){
							ultimoVertice = caminos.getUltimoVerticeDelCamino(ultimoCamino);
							if(ultimoVertice!=-1){
								ArrayList resultados=getVerticesDelVertice(ultimoVertice);
								if(resultados!=null&&!resultados.isEmpty()){
									Iterator itrResultados=resultados.iterator();
									int idseccion;
									int vertice;
									while(itrResultados.hasNext()){
										idseccion=(int)itrResultados.next();
										vertice=(int)itrResultados.next();
										if(!caminos.isVerticeVisitadoDelCamino(ultimoCamino,vertice)){
                                                                                         // OMP CRITICAL BLOCK BEGINS
                                                                                         synchronized (jomp.runtime.OMP.getLockByName("getultimocamino")) {
                                                                                         // OMP USER CODE BEGINS

											{
												caminos.agregaNuevoCamino(ultimoCamino, idseccion,vertice);
											}
                                                                                         // OMP USER CODE ENDS
                                                                                         }
                                                                                         // OMP CRITICAL BLOCK ENDS

										}
									}
									
								}else{
										System.out.println("resultados es null");
									}
							}
                                                         // OMP CRITICAL BLOCK BEGINS
                                                         synchronized (jomp.runtime.OMP.getLockByName("getultimocamino")) {
                                                         // OMP USER CODE BEGINS

							{
							caminos.borraCamino(ultimoCamino);
							}
                                                         // OMP USER CODE ENDS
                                                         }
                                                         // OMP CRITICAL BLOCK ENDS

						}
			}else{
                                 { // OMP SINGLE BLOCK BEGINS
                                 if(jomp.runtime.OMP.getTicket(__omp_me) == 0) {
                                 // copy of firstprivate variables, initialized
                                 {
                                   // firstprivate variables + init
                                   // private variables
                                    // OMP USER CODE BEGINS

				{
					//if(debug) System.out.println("Busqueda Camino soy tread"+jomp.runtime.OMP.getThreadNum());
					ArrayList resultados=getVerticesDelVertice(verticeInicial);
					if(resultados!=null&&!resultados.isEmpty()){
						Iterator itrResultados=resultados.iterator();
						int idseccion;
						int vertice;
						while(itrResultados.hasNext()){
							idseccion=(int)itrResultados.next();
							vertice=(int)itrResultados.next();
							caminos.agregaNuevoCamino(idcliente,verticeInicial,idseccion, vertice);	
						}
						iniciado=true;
					}else{
							System.out.println("ResultsA o B a es null");
						}
				}
                                    // OMP USER CODE ENDS
                                 }
                                 }
                                 jomp.runtime.OMP.resetTicket(__omp_me);
                                 jomp.runtime.OMP.doBarrier(__omp_me);
                                 } // OMP SINGLE BLOCK ENDS

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


