package generacion.jomp;

import java.util.Random;
import gui.Principal;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class GenerarGrafo {

	private int[][] tablaGrafo;
	private static Random rand = new Random(System.currentTimeMillis());
    private boolean debug=false;
	private Principal principal;
    
    public GenerarGrafo(Principal principal){
    	jomp.runtime.OMP.setNumThreads(4);
    	this.principal=principal;
    }
    
	private int randInt(int min, int max) {
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
	

		public void generar(int minSecciones,int maxSecciones,int minCruces,int maxCruces,int minEntradas,int maxEntradas){
			
			if(maxCruces<minCruces) maxCruces = minCruces;
			int anchoTabla=maxCruces+2;
			int nSeccionesmin=minCruces*(anchoTabla-1);
			int nSeccionesmax=maxCruces*(anchoTabla-1);
			if(nSeccionesmin>=minSecciones && nSeccionesmax<=maxSecciones){
				if(maxEntradas>anchoTabla) maxEntradas=anchoTabla;
				if(minEntradas>maxEntradas)	minEntradas=maxEntradas;
				int entradasActual=randInt(minEntradas, maxEntradas);
				tablaGrafo=new int[anchoTabla+2][anchoTabla+2];
				//algoritmoGeneraSeccionesSingle(nSeccionesmin,nSeccionesmax,minCruces,maxCruces);
				algoritmoGeneraSecciones(nSeccionesmin,nSeccionesmax,minCruces,maxCruces);
				if(debug)System.out.println("Termine de generar");
				//algoritmoGeneraSeccionesTest();
				algoritmoGeneraEntradas(entradasActual);
				//preparaEntradaGrafo();
			}else{
				System.out.println("No hay combinacion posible");
			}
	}
	
	private void algoritmoGeneraEntradas(int entradasActual){
		int nEntradas=0;

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.nEntradas = nEntradas;
  __omp_Object0.entradasActual = entradasActual;
  __omp_Object0.principal = principal;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object0);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  nEntradas = __omp_Object0.nEntradas;
  entradasActual = __omp_Object0.entradasActual;
  principal = __omp_Object0.principal;
}
// OMP PARALLEL BLOCK ENDS

	}
	private int cuentaCrucesDeUnNodo(int i){
		int nCrucesGenerados=0;
		int j=1;
		/*//omp parallel for default(shared) reduction(+:nCrucesGenerados)*/
		for(j=1;j<tablaGrafo[i].length;j++){
			if(tablaGrafo[i][j]==1 ||tablaGrafo[j][i]==1 ){
				nCrucesGenerados++;
			}
		}
		if(debug) System.out.print("Habian("+nCrucesGenerados+"),cruces en ("+i+")\n");
		return nCrucesGenerados;
	}
	
	
	private void algoritmoGeneraSecciones(int nseccionesMinGrafo,int nseccionesMaxGrafo, int minCruces, int maxCruces){
		int j=0;
		int i=j,numeroRandom=0,ncrucesLineaActualMax=0,ncrucesEnTotal=0,ncrucesLineaActual=0,crucesDelnodo;	
		 if(debug)System.out.println("Num Cruces a generar: " + nseccionesMaxGrafo);

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class1 __omp_Object1 = new __omp_Class1();
  // shared variables
  __omp_Object1.ncrucesEnTotal = ncrucesEnTotal;
  __omp_Object1.j = j;
  __omp_Object1.maxCruces = maxCruces;
  __omp_Object1.minCruces = minCruces;
  __omp_Object1.nseccionesMaxGrafo = nseccionesMaxGrafo;
  __omp_Object1.nseccionesMinGrafo = nseccionesMinGrafo;
  __omp_Object1.principal = principal;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object1);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  ncrucesEnTotal = __omp_Object1.ncrucesEnTotal;
  crucesDelnodo = __omp_Object1.crucesDelnodo;
  j = __omp_Object1.j;
  maxCruces = __omp_Object1.maxCruces;
  minCruces = __omp_Object1.minCruces;
  nseccionesMaxGrafo = __omp_Object1.nseccionesMaxGrafo;
  nseccionesMinGrafo = __omp_Object1.nseccionesMinGrafo;
  principal = __omp_Object1.principal;
}
// OMP PARALLEL BLOCK ENDS


// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class2 __omp_Object2 = new __omp_Class2();
  // shared variables
  __omp_Object2.crucesDelnodo = crucesDelnodo;
  __omp_Object2.ncrucesLineaActual = ncrucesLineaActual;
  __omp_Object2.ncrucesEnTotal = ncrucesEnTotal;
  __omp_Object2.ncrucesLineaActualMax = ncrucesLineaActualMax;
  __omp_Object2.numeroRandom = numeroRandom;
  __omp_Object2.i = i;
  __omp_Object2.maxCruces = maxCruces;
  __omp_Object2.minCruces = minCruces;
  __omp_Object2.nseccionesMaxGrafo = nseccionesMaxGrafo;
  __omp_Object2.nseccionesMinGrafo = nseccionesMinGrafo;
  __omp_Object2.principal = principal;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object2);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  crucesDelnodo = __omp_Object2.crucesDelnodo;
  ncrucesLineaActual = __omp_Object2.ncrucesLineaActual;
  ncrucesEnTotal = __omp_Object2.ncrucesEnTotal;
  ncrucesLineaActualMax = __omp_Object2.ncrucesLineaActualMax;
  numeroRandom = __omp_Object2.numeroRandom;
  i = __omp_Object2.i;
  maxCruces = __omp_Object2.maxCruces;
  minCruces = __omp_Object2.minCruces;
  nseccionesMaxGrafo = __omp_Object2.nseccionesMaxGrafo;
  nseccionesMinGrafo = __omp_Object2.nseccionesMinGrafo;
  principal = __omp_Object2.principal;
}
// OMP PARALLEL BLOCK ENDS

	}

	public void pintaTabla(){
		if(debug)System.out.println("\nTabla");
		String preComa="";
		int numeroSecciones=tablaGrafo.length;
		for(int i=0;i<numeroSecciones;i++){
			if(debug && i==0){
				if(logitudNumero(numeroSecciones)>1) {System.out.print("    ");}else{System.out.print("   ");}
				  for(int j=0;j<numeroSecciones;j++){
					System.out.print(j);
					if(j<numeroSecciones-1){
					System.out.print("  ");
					}
				  }
				  System.out.print("\n");				
			}
			 
			  for(int j=0;j<numeroSecciones;j++){
				if(j==0){
					if(debug){
					if(logitudNumero(numeroSecciones)>1) {
						  if(logitudNumero(numeroSecciones)>logitudNumero(i)){
							for(int n=0;n<(logitudNumero(numeroSecciones)-logitudNumero(j));n++){
								System.out.print(" ");
							}
						  }
						}
				  preComa=i+" [";
				  }else{preComa="[";}
				}else{
				  preComa=",";
				}
				if(j>0 && debug){
				if(logitudNumero(numeroSecciones)>1) {
					  if(logitudNumero(numeroSecciones)==logitudNumero(j)){
						for(int n=0;n<=(logitudNumero(numeroSecciones)-logitudNumero(j));n++){
							System.out.print(" ");
						}
					  }
					  System.out.print(" ");
					}
				
				}
				
				System.out.print(preComa+tablaGrafo[i][j]);			
			  }
			  System.out.print("]");
			  if(i<numeroSecciones-1){
			  System.out.print(",\n");
			  }else{System.out.print("\n");}
		  }
		System.out.print("\n");
	}
	
	private int logitudNumero(int n)
	{
		int l;
		if(n>0){
		n=Math.abs(n);
		for (l=0;n>0;++l)
			n/=10;
		}else{
			l=1;
		}
		return l;			
	}
	
	public void insertaGrafoCQL(boolean recreaBD){
	      try{
	    	  principal.getCassandraSession().execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  principal.getCassandraSession().execute("CREATE KEYSPACE IF NOT EXISTS Bd WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
	    	  principal.getCassandraSession().execute("USE Bd");
	      }
	      try{
	    	  principal.getCassandraSession().execute("SELECT idseccion FROM vertices LIMIT 1");
	    	  if(recreaBD){
	    		  principal.getCassandraSession().execute("DROP TABLE Bd.vertices");
	    		  principal.getCassandraSession().execute("CREATE TABLE Bd.vertices (vertA int, vertB int, idseccion text PRIMARY KEY)");
	    		  principal.getCassandraSession().execute("CREATE INDEX vertices_vertA ON vertices (vertA)");
	    		  principal.getCassandraSession().execute("CREATE INDEX vertices_vertB ON vertices (vertB)");
	      	  }
	    	       }catch (InvalidQueryException e){
	    	    	   System.out.println("No habia tabla vertices Recreando\n");
	    	    	   principal.getCassandraSession().execute("CREATE TABLE Bd.vertices (vertA int, vertB int, idseccion text PRIMARY KEY)");
	    	    	   principal.getCassandraSession().execute("CREATE INDEX vertices_vertA ON Bd.vertices (vertA)");
	 	    		  principal.getCassandraSession().execute("CREATE INDEX vertices_vertB ON Bd.vertices (vertB)");
	  }
	      try{ 
	    	  principal.getCassandraSession().execute("SELECT idseccion FROM Bd.caracteristicasVertices LIMIT 1");
	    	    }catch (InvalidQueryException e){
	    	    	System.out.println("No habia tabla caracteristicasVertices Recreando\n");
	    	    	 principal.getCassandraSession().execute("CREATE TABLE Bd.caracteristicasVertices (idseccion text PRIMARY KEY, consumoMax int, coste float)");
	    	    	 }	    
	      
	      PreparedStatement psExtremos = principal.getCassandraSession().prepare("INSERT INTO Bd.vertices (vertA,vertB,idseccion) VALUES (?, ?, ?)");
		   PreparedStatement psCaracteristicas = principal.getCassandraSession().prepare("INSERT INTO Bd.caracteristicasVertices (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
		   BatchStatement batch = new BatchStatement();
		   int i,j,consumoMax;
		   float coste;
		   String idseccion;
		   for(i=0;i<tablaGrafo.length;i++){
			   for(j=i+1;j<tablaGrafo.length;j++){
				   if(tablaGrafo[i][j]==1){
				   if(debug)System.out.printf("Estoy en tabla[%d][%d]\n",i,j);
				  consumoMax=randInt(1000,20000);
				  coste=randfloat((float)0.1,(float)1.5);
				  idseccion=String.valueOf(i)+"_"+String.valueOf(j);
				  batch.add(psExtremos.bind(i,j,idseccion));		  
				  batch.add(psCaracteristicas.bind(idseccion,consumoMax,coste));
				  //batch.add(ps.bind(UUID.randomUUID(),j, i,consumoMax,coste));
			//batch.add(ps.bind(UUID.randomUUID(),j, i,randInt(1000,20000),randfloat((float)0.1,(float)1.5)));
				   }
			}
		   }
		   principal.getCassandraSession().execute(batch);
	}
	

	
	private static float randfloat(float min, float max) {
		     float randomNum=((max - min) * rand.nextFloat()) + min;
		    return randomNum;
	}
	
	public int numeroSecciones(){
		return tablaGrafo.length;
		
	}
	
	public boolean compruebaSiConexion(int i){
		boolean conexion=false;
		int j=1;
		if(i>0){
		while(j<tablaGrafo.length && !conexion){
			if(tablaGrafo[i][j]==1) conexion=true;
			j++;
		}}
		return conexion;		
	}

	public void pintaBD(){
		boolean vacia=false;
		try{
			principal.getCassandraSession().execute("USE Bd");
			}catch (InvalidQueryException e){
				vacia=true;
				System.out.println("Vacia o no existe BD\n");
				}
		if(!vacia){
			ResultSet results = principal.getCassandraSession().execute("SELECT * FROM vertices");
			System.out.println(String.format("%-14s%-14s%-14s","idseccion", "vertA","vertB"));
			//Iterator iter=results.iterator();
			while(results.iterator().hasNext()){
				Row row = results.iterator().next();
				System.out.println(String.format("%s","---------------+---------------+---------------"));	   				
				System.out.println(String.format("%-20s%-20s%-20s", row.getString("idseccion"),row.getInt("vertA"),row.getInt("vertB")));
			}
			System.out.println();
			results = principal.getCassandraSession().execute("SELECT * FROM caracteristicasVertices");
			System.out.println(String.format("%-14s%-14s%-5s","idseccion", "consumoMax","coste"));
			//iter=results.iterator();
			while(results.iterator().hasNext()){
				Row row = (Row)results.iterator().next();
				System.out.println(String.format("%s","---------------+---------------+---------------"));	   				
				System.out.println(String.format("%-20s%-20s%-3.1f", row.getString("idseccion"),row.getInt("consumoMax"),  row.getFloat("coste")));
			}
			System.out.println();
			}
		}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class2 extends jomp.runtime.BusyTask {
  // shared variables
  int crucesDelnodo;
  int ncrucesLineaActual;
  int ncrucesEnTotal;
  int ncrucesLineaActualMax;
  int numeroRandom;
  int i;
  int maxCruces;
  int minCruces;
  int nseccionesMaxGrafo;
  int nseccionesMinGrafo;
  Principal principal;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  int j;
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
                            jomp.runtime.LoopData __omp_WholeData4 = new jomp.runtime.LoopData();
                            jomp.runtime.LoopData __omp_ChunkData3 = new jomp.runtime.LoopData();
                            __omp_WholeData4.start = (long)(0);
                            __omp_WholeData4.stop = (long)(tablaGrafo.length);
                            __omp_WholeData4.step = (long)(1);
                            jomp.runtime.OMP.setChunkStatic(__omp_WholeData4);
                            while(!__omp_ChunkData3.isLast && jomp.runtime.OMP.getLoopStatic(__omp_me, __omp_WholeData4, __omp_ChunkData3)) {
                            for(;;) {
                              if(__omp_WholeData4.step > 0) {
                                 if(__omp_ChunkData3.stop > __omp_WholeData4.stop) __omp_ChunkData3.stop = __omp_WholeData4.stop;
                                 if(__omp_ChunkData3.start >= __omp_WholeData4.stop) break;
                              } else {
                                 if(__omp_ChunkData3.stop < __omp_WholeData4.stop) __omp_ChunkData3.stop = __omp_WholeData4.stop;
                                 if(__omp_ChunkData3.start > __omp_WholeData4.stop) break;
                              }
                              for( j = (int)__omp_ChunkData3.start; j < __omp_ChunkData3.stop; j += __omp_ChunkData3.step) {
                                // OMP USER CODE BEGINS
{
			tablaGrafo[0][j]=0;
			tablaGrafo[j][0]=0;
			System.out.print("test1\n");
		}
                                // OMP USER CODE ENDS
                                if (j == (__omp_WholeData4.stop-1)) amLast = true;
                              } // of for 
                              if(__omp_ChunkData3.startStep == 0)
                                break;
                              __omp_ChunkData3.start += __omp_ChunkData3.startStep;
                              __omp_ChunkData3.stop += __omp_ChunkData3.startStep;
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



// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class1 extends jomp.runtime.BusyTask {
  // shared variables
  int ncrucesEnTotal;
  int crucesDelnodo;
  int j;
  int maxCruces;
  int minCruces;
  int nseccionesMaxGrafo;
  int nseccionesMinGrafo;
  Principal principal;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  int i;
  int ncrucesLineaActual;
  int ncrucesLineaActualMax;
  int numeroRandom;
  // reduction variables, init to default
    // OMP USER CODE BEGINS

		{
		while(ncrucesEnTotal<nseccionesMaxGrafo){
			i=randInt(1,tablaGrafo.length-1);
			if(debug)System.out.println("soy thread " + jomp.runtime.OMP.getThreadNum());
			while((ncrucesEnTotal<nseccionesMinGrafo) && (tablaGrafo[i][0]==1)){//Generamos una i que no hayamos puesto a 1
				i=randInt(1,tablaGrafo.length-1);
			}
			ncrucesLineaActual=cuentaCrucesDeUnNodo(i);
			ncrucesLineaActualMax=randInt(minCruces,maxCruces);
			int j=randInt(1,tablaGrafo.length-1);
			while((ncrucesLineaActual < ncrucesLineaActualMax) && (ncrucesEnTotal<nseccionesMaxGrafo || ncrucesEnTotal<nseccionesMinGrafo)){
				   j=randInt(1,tablaGrafo.length-1);
				   if(debug)System.out.print("\nLinea i ("+i+") ");
					while((ncrucesLineaActual<ncrucesLineaActualMax)&&(tablaGrafo[i][j]==1 || i==j || (cuentaCrucesDeUnNodo(j)> maxCruces))){//Generamos una j que no hayamos puesto a 1
						j=randInt(1,tablaGrafo.length-1);
						crucesDelnodo=cuentaCrucesDeUnNodo(j);
					}
					numeroRandom=randInt(0,1);
					if(numeroRandom==1){
						if(debug)System.out.print(" marco i("+i+") j("+j+") - i("+j+") j("+i+")");
                                                 // OMP CRITICAL BLOCK BEGINS
                                                 synchronized (jomp.runtime.OMP.getLockByName("")) {
                                                 // OMP USER CODE BEGINS

						{
						if((crucesDelnodo+1)>=maxCruces) tablaGrafo[j][0]=1;
						tablaGrafo[j][i]=1;
						tablaGrafo[i][j]=1;
						ncrucesLineaActual++;
						ncrucesEnTotal+=2;
						}
                                                 // OMP USER CODE ENDS
                                                 }
                                                 // OMP CRITICAL BLOCK ENDS

					}
						
				}
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



// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  int nEntradas;
  int entradasActual;
  Principal principal;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

		{
		while(nEntradas<=entradasActual){
			int j=randInt(0,tablaGrafo.length-1);
			while(tablaGrafo[0][j]==1){
				j=randInt(0,tablaGrafo.length-1);
			}
			int valor=randInt(0,1);
                         // OMP CRITICAL BLOCK BEGINS
                         synchronized (jomp.runtime.OMP.getLockByName("")) {
                         // OMP USER CODE BEGINS

			{
			tablaGrafo[0][j]=valor;
			tablaGrafo[j][0]=valor;
			}
                         // OMP USER CODE ENDS
                         }
                         // OMP CRITICAL BLOCK ENDS

			if(valor==1){
				nEntradas++;
			}
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


