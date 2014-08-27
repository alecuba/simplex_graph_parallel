package busqueda.jomp;

import java.util.List;
import comun.Gestor;
import busqueda.Camino;
import busqueda.Seccion;
import busqueda.Caminos;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {

	private Gestor gestor;
	private Caminos caminos = new Caminos();
	
	public RecorreGrafo(Gestor gestor) {
		this.gestor = gestor;
	}
	
	public void generaCaminos(){
		Camino ultimoCamino;
		while(!caminos.isFinalizados()){

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
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
  gestor = __omp_Object0.gestor;
}
// OMP PARALLEL BLOCK ENDS

		}
	}

	/*
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
			int j;
			Camino caminoAvisitar;
			Seccion seccion;
			ArrayListEnteros verticesTemp;
			//omp parallel for private(j,caminoAvisitar,seccion,verticesTemp)
			for (j = 0; j < resultados.length; j++) {
				if (debug)
					System.out.print(" \nllamando para subcamino("
							+ resultados[j][0] + ")");
				if (!verticesActual.compruebaSiContiene(resultados[j][0])) {
					caminoAvisitar = new Camino();
					caminoAvisitar.idcliente = caminoActual.idcliente;
					caminoAvisitar.idcamino = caminoActual.idcamino;
					seccion=new Seccion();
					seccion.idSeccion=resultados[j][1];
					caminoAvisitar.secciones.addAll(caminoActual.secciones);
					caminoAvisitar.secciones.add(seccion);
					verticesTemp = new ArrayListEnteros();
					verticesTemp.addAll(verticesActual);
					verticesTemp.add(resultados[j][0]);
					if (resultados[j][0] != 0) {
						generaCaminos(caminoAvisitar,verticesTemp);
					} else {
						
						{
							caminosEncontrados.agregaCamino(caminoAvisitar);
						}
					}
				}
			}
		} catch (InvalidQueryException e) {
			if (debug)
				System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
			e.printStackTrace();
		}
	}
	*/

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
						+ ((Row)rows.get(i)).getInt("id") + ") con conexion("
						+ ((Row)rows.get(i)).getInt("idseccion") + "),");
				Camino caminoTemp = new Camino();
				caminoTemp.idcliente = ((Row)rows.get(i)).getInt("id");
				caminoTemp.idcamino = 0;
				Seccion seccion = new Seccion();
				seccion.idSeccion=-1;
				seccion.vertA=-1;
				seccion.vertB=((Row)rows.get(i)).getInt("idseccion");
				caminoTemp.secciones.add(seccion);
				caminos.agregaNuevoCamino(caminoTemp,false);
				//ArrayListEnteros vertices = new ArrayListEnteros();
				//vertices.add(((Row)rows.get(i)).getInt("idseccion"));
				
				
				
				
				generaCaminos();
				//rellenaResultado();
				// consultarSiguientesSaltos(row.getInt("id"),row.getInt("idseccion"));
			}
			long elapsedTimeMillis = System.currentTimeMillis()-start;
			System.out.println("Zona paralela:"+jomp.runtime.OMP.inParallel()+" tiempo("+(elapsedTimeMillis/1000F)+")");
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

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  Gestor gestor;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  Camino ultimoCamino = new Camino();
  // reduction variables, init to default
    // OMP USER CODE BEGINS

			{
                                 // OMP CRITICAL BLOCK BEGINS
                                 synchronized (jomp.runtime.OMP.getLockByName("")) {
                                 // OMP USER CODE BEGINS

				{
				ultimoCamino=caminos.getUltimoCaminoNoFinalizadoNiVisitando();
				}
                                 // OMP USER CODE ENDS
                                 }
                                 // OMP CRITICAL BLOCK ENDS

				if(ultimoCamino!=null){
				ResultSet resultsA=null;
				ResultSet resultsB=null;
			    try {
				resultsA = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ ultimoCamino.getUltimoVerticeSeccion());
				resultsB = gestor.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ ultimoCamino.getUltimoVerticeSeccion());
			    } catch (InvalidQueryException e) {
					if (debug)
						System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
					e.printStackTrace();
				}
			    if(resultsA!=null && resultsB!=null){
				List rowsA = (List)resultsA.all();
				List rowsB = (List)resultsB.all();
				int pos = 0;
				int[][] resultados = new int[rowsA.size() + rowsB.size()][2];
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
				int j;
				Camino caminoTemp;
				Seccion seccionTemp;
				boolean visitado;
				for (j = 0; j < resultados.length; j++) {
					visitado=ultimoCamino.isSecionVisitada(resultados[j][0]);
					if(!visitado){
					caminoTemp = new Camino();
					caminoTemp.idcliente=ultimoCamino.idcliente;
					seccionTemp = new Seccion();
					seccionTemp.idSeccion=resultados[j][1];
					seccionTemp.vertA=ultimoCamino.getUltimoVerticeSeccion();
					seccionTemp.vertB=resultados[j][0];
					caminoTemp.secciones.addAll(ultimoCamino.secciones);
					caminoTemp.secciones.add(seccionTemp);
                                         // OMP CRITICAL BLOCK BEGINS
                                         synchronized (jomp.runtime.OMP.getLockByName("")) {
                                         // OMP USER CODE BEGINS

					{
					if(seccionTemp.vertB==0){
					caminos.agregaNuevoCamino(caminoTemp,true);
					}else{
						caminos.agregaNuevoCamino(caminoTemp,false);
					}
					}
                                         // OMP USER CODE ENDS
                                         }
                                         // OMP CRITICAL BLOCK ENDS

				}
				}
				}
                           // OMP CRITICAL BLOCK BEGINS
                           synchronized (jomp.runtime.OMP.getLockByName("")) {
                           // OMP USER CODE BEGINS

				{
			    caminos.borraCamino(ultimoCamino);
				}
                           // OMP USER CODE ENDS
                           }
                           // OMP CRITICAL BLOCK ENDS

				}
                                 // OMP BARRIER BLOCK BEGINS
                                 jomp.runtime.OMP.doBarrier(__omp_me);
                                 // OMP BARRIER BLOCK ENDS

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

