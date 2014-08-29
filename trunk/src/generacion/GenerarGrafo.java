package generacion;

import java.util.Random;
import comun.Gestor;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class GenerarGrafo {

	private int[][] tablaGrafo;
	private static Random rand = new Random(System.currentTimeMillis());
    private boolean debug=false;
	private Gestor gestor;
	private int minCruces=0;
	private int maxCruces=0;
	private int minSecciones=0;
	private int maxSecciones=0;
    
    public GenerarGrafo(Gestor gestor){
    	this.gestor=gestor;
    }
    
	private int randInt(int min, int max) {
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
	
	public boolean generar(int minSecciones,int maxSecciones,int minCruces,int maxCruces,int minEntradas,int maxEntradas){
		boolean retorno=true;
		if(maxCruces<minCruces){ this.maxCruces = minCruces;} else{ this.maxCruces=maxCruces;}
		this.minCruces = minCruces;
		this.minSecciones=minSecciones;
		if(maxSecciones<minSecciones){ this.maxSecciones = minSecciones;} else  {this.maxSecciones = maxSecciones;}
		int anchoTabla=this.maxCruces+2;
		int largo=(int)Math.rint((double)this.minSecciones/(double)this.minCruces);
		if(largo>anchoTabla) anchoTabla=largo;
		if((anchoTabla*this.maxCruces)>this.maxSecciones){
			System.out.println("No hay combinacion posible debes poner maxSecciones a:"+(anchoTabla*this.maxCruces));
			retorno=false;
		}else{
			if(this.maxCruces*(anchoTabla-1)<maxSecciones) maxSecciones=(this.maxCruces*(anchoTabla-1));
			if(maxEntradas>anchoTabla) maxEntradas=anchoTabla;
			if(minEntradas>maxEntradas)	minEntradas=maxEntradas;
			int entradasActual=randInt(minEntradas, maxEntradas);
			tablaGrafo=new int[anchoTabla][anchoTabla];
			//algoritmoGeneraSeccionesSingle(nSeccionesmin,nSeccionesmax,minCruces,maxCruces);
			algoritmoGeneraSecciones();
			if(debug)System.out.println("Termine de generar");
			//algoritmoGeneraSeccionesTest();
			algoritmoGeneraEntradas(entradasActual);
			//preparaEntradaGrafo();
		}
		/*int nSeccionesmin=minCruces*(anchoTabla-1);
		int nSeccionesmax=maxCruces*(anchoTabla-1);
		if(nSeccionesmax<=maxSecciones){
			if(nSeccionesmin>=minSecciones){
			
			}else{
			System.out.println("No hay combinacion posible debes poner almenos minCruces a:"+(minSecciones/(maxCruces+1)));
			retorno=false;
			}
		}else{
			System.out.println("No hay combinacion posible debes poner almenos maxCruces a:"+(maxSecciones/(maxCruces+1)));
			retorno=false;
		}*/
		return retorno;
	}
	
	private void algoritmoGeneraEntradas(int entradasActual){
		int nEntradas=0;
		/*//omp parallel shared(nEntradas)*/
		{
		while(nEntradas<=entradasActual){
			int j=randInt(0,tablaGrafo.length-1);
			while(tablaGrafo[0][j]==1){
				j=randInt(0,tablaGrafo.length-1);
			}
			int valor=randInt(0,1);
			/*//omp critical*/
			{
			tablaGrafo[0][j]=valor;
			tablaGrafo[j][0]=valor;
			}
			if(valor==1){
				nEntradas++;
			}
		}
		}
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
	
	private void algoritmoGeneraSecciones(){
		int j=0;
		int i=j,ncrucesLineaActualMax=0,ncrucesEnTotal=0,ncrucesLineaActual=0;	
		// if(debug)System.out.println("Num Cruces a generar: " + nseccionesMaxGrafo);		
		while(ncrucesEnTotal<minSecciones&&!visitadosTodaslasIes()){

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.ncrucesEnTotal = ncrucesEnTotal;
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
  ncrucesEnTotal = __omp_Object0.ncrucesEnTotal;
  gestor = __omp_Object0.gestor;
}
// OMP PARALLEL BLOCK ENDS

			System.out.println("Zona paralela:"+jomp.runtime.OMP.inParallel());
		}
		//Limpieza de primera columna y fila usada para ver si visitado
		/*//omp parallel for private(j)*/
		for(j=0;j<tablaGrafo.length;j++){
			tablaGrafo[0][j]=0;
			tablaGrafo[j][0]=0;
		}
	}
	
	private boolean visitadosTodaslasIes(){
		boolean visitados=true;
		int i=1;
		while(i<tablaGrafo.length && visitados){
			if(tablaGrafo[i][0]==0) visitados = false;
			i++;
		}
		return visitados;
	}
	
	private boolean visitadosTodaslasJotas(int[] visitadosJ){
		boolean visitados=true;
		int i=0;
		while(i<visitadosJ.length && visitados){
			if(visitadosJ[i]==0) visitados = false;
			i++;
		}
		return visitados;
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
	    	  gestor.getCassandraSession().execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  gestor.getCassandraSession().execute("CREATE KEYSPACE IF NOT EXISTS Bd WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
	    	  gestor.getCassandraSession().execute("USE Bd");
	      }
	      try{
	    	  gestor.getCassandraSession().execute("SELECT idseccion FROM vertices LIMIT 1");
	    	  if(recreaBD){
	    		  gestor.getCassandraSession().execute("DROP TABLE Bd.vertices");
	    		  gestor.getCassandraSession().execute("CREATE TABLE Bd.vertices (idseccion int PRIMARY KEY,vertA int, vertB int)");
	    		  gestor.getCassandraSession().execute("CREATE INDEX vertices_vertA ON vertices (vertA)");
	    		  gestor.getCassandraSession().execute("CREATE INDEX vertices_vertB ON vertices (vertB)");
	      	  }
	    	       }catch (InvalidQueryException e){
	    	    	   System.out.println("No habia tabla vertices Recreando\n");
	    	    	   gestor.getCassandraSession().execute("CREATE TABLE Bd.vertices (idseccion int PRIMARY KEY,vertA int, vertB int)");
	    	    	   gestor.getCassandraSession().execute("CREATE INDEX vertices_vertA ON Bd.vertices (vertA)");
	 	    		  gestor.getCassandraSession().execute("CREATE INDEX vertices_vertB ON Bd.vertices (vertB)");
	  }
	      try{ 
	    	  gestor.getCassandraSession().execute("SELECT idseccion FROM Bd.caracteristicasVertices LIMIT 1");
	    	    }catch (InvalidQueryException e){
	    	    	System.out.println("No habia tabla caracteristicasVertices Recreando\n");
	    	    	 gestor.getCassandraSession().execute("CREATE TABLE Bd.caracteristicasVertices (idseccion int PRIMARY KEY, consumoMax int, coste float)");
	    	    	 }	    
	      
	      PreparedStatement psExtremos = gestor.getCassandraSession().prepare("INSERT INTO Bd.vertices (idseccion,vertA,vertB) VALUES (?, ?, ?)");
		   PreparedStatement psCaracteristicas = gestor.getCassandraSession().prepare("INSERT INTO Bd.caracteristicasVertices (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
		   BatchStatement batch = new BatchStatement();
		   int i,j,consumoMax;
		   float coste;
		   int idseccion=0;
		   for(i=0;i<tablaGrafo.length;i++){
			   for(j=i+1;j<tablaGrafo.length;j++){
				   if(tablaGrafo[i][j]==1){
				   if(debug)System.out.printf("Estoy en tabla[%d][%d]\n",i,j);
				  consumoMax=randInt(1000,20000);
				  coste=randfloat((float)0.1,(float)1.5);
				  idseccion++;
				  batch.add(psExtremos.bind(idseccion,i,j));		  
				  batch.add(psCaracteristicas.bind(idseccion,consumoMax,coste));
				  //batch.add(ps.bind(UUID.randomUUID(),j, i,consumoMax,coste));
			//batch.add(ps.bind(UUID.randomUUID(),j, i,randInt(1000,20000),randfloat((float)0.1,(float)1.5)));
				   }
			}
		   }
		   gestor.getCassandraSession().execute(batch);
	}
	

	
	private static float randfloat(float min, float max) {
		     float randomNum=((max - min) * rand.nextFloat()) + min;
		    return randomNum;
	}
	
	public int getNumeroSecciones(){
		return tablaGrafo.length-1;
		
	}
	
	public boolean compruebaSiConexion(int i){
		boolean conexion=false;
		int j=1;
		if(i>0&&cuentaCrucesDeUnNodo(i)>=minCruces){
		while(j<tablaGrafo.length && !conexion){
			if(i!=j&&tablaGrafo[i][j]==1&&cuentaCrucesDeUnNodo(j)>=minCruces) conexion=true;
			j++;
		}}
		return conexion;		
	}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  int ncrucesEnTotal;
  Gestor gestor;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  int i;
  int j;
  int ncrucesLineaActual;
  int ncrucesLineaActualMax;
  // reduction variables, init to default
    // OMP USER CODE BEGINS

			{
			
			i=randInt(1,tablaGrafo.length-1);
			while((ncrucesEnTotal<minSecciones) && (tablaGrafo[i][0]==1)){//Generamos una i que no hayamos puesto a 1
				i=randInt(1,tablaGrafo.length-1);
			}
                         // OMP CRITICAL BLOCK BEGINS
                         synchronized (jomp.runtime.OMP.getLockByName("MarcaVisitado")) {
                         // OMP USER CODE BEGINS

			{
			tablaGrafo[i][0]=1;
			}
                         // OMP USER CODE ENDS
                         }
                         // OMP CRITICAL BLOCK ENDS

			ncrucesLineaActual=cuentaCrucesDeUnNodo(i);
			ncrucesLineaActualMax=randInt(minCruces,maxCruces);
			int[] visitadosJ = new int[tablaGrafo.length-1];
			boolean visitados=false;
			while((ncrucesLineaActual < ncrucesLineaActualMax) && !visitados && (ncrucesEnTotal<maxSecciones || ncrucesEnTotal<minSecciones)){
				if(debug)System.out.print("\nSoyThread:" + jomp.runtime.OMP.getThreadNum());	
				   j=randInt(1,tablaGrafo.length-1);
				   if(debug)System.out.print(" Linea i ("+i+") ");
				   visitados=visitadosTodaslasJotas(visitadosJ);
				   while((ncrucesLineaActual<ncrucesLineaActualMax)&&!visitados&&(tablaGrafo[i][j]==1 || i==j || (cuentaCrucesDeUnNodo(j)>= maxCruces))){//Generamos una j que no hayamos puesto a 1
						j=randInt(1,tablaGrafo.length-1);
						visitadosJ[j-1]=1;
						visitados=visitadosTodaslasJotas(visitadosJ);
					}
				   if(!visitados){
                                            // OMP CRITICAL BLOCK BEGINS
                                            synchronized (jomp.runtime.OMP.getLockByName("ActualizaTablaYcontadores")) {
                                            // OMP USER CODE BEGINS

						{
						if(debug)System.out.print(" marco i("+i+") j("+j+") - i("+j+") j("+i+")");
						if((cuentaCrucesDeUnNodo(j)+1)>maxCruces) {tablaGrafo[j][0]=1;}else{
						tablaGrafo[j][i]=1;
						tablaGrafo[i][j]=1;
						ncrucesLineaActual++;
						ncrucesEnTotal+=2;
						}
						}
                                            // OMP USER CODE ENDS
                                            }
                                            // OMP CRITICAL BLOCK ENDS

				   }
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


