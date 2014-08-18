package generador_grafo;
import java.util.Random;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class GenerarGrafo {
	private int[][] tablaGrafo;
	private static Random rand = new Random();
    private boolean debug=false;
	private Principal principal;
    
    public GenerarGrafo(Principal principal){
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
		if(maxSecciones<minSecciones) maxSecciones = minSecciones;
		if(maxCruces<minCruces) maxCruces = minCruces;
		int seccionesActual=randInt(minSecciones,maxSecciones);
		if(maxEntradas>seccionesActual) maxEntradas=seccionesActual;
		if(minEntradas>maxEntradas)	minEntradas=maxEntradas;
		int entradasActual=randInt(minEntradas,maxEntradas);
		algoritmoGeneraSecciones(seccionesActual,minCruces,maxCruces);
		algoritmoGeneraEntradas(entradasActual);
		//preparaEntradaGrafo();
	}
	
	private void algoritmoGeneraEntradas(int entradasActual){
		int nEntradas=0;
		int j=randInt(0,this.tablaGrafo.length-1);
		while(nEntradas<=entradasActual){
			while(tablaGrafo[0][j]==1){
				j=randInt(0,tablaGrafo.length-1);
			}
			tablaGrafo[0][j]=tablaGrafo[j][0]=randInt(0,1);
			if(tablaGrafo[0][j]==1){
				nEntradas++;
			}
		}
	}
	private int cuentaCrucesVerticeActual(int i){
		int ncruces_generados=0;
		for(int j=1;j<tablaGrafo[i].length;j++){
			if(tablaGrafo[i][j]==1 ||tablaGrafo[j][i]==1 ){
				ncruces_generados++;
			}
		}
		if(debug) System.out.print("Habian("+ncruces_generados+"),cruces en ("+i+")\n");
		return ncruces_generados;
	}
	
	private void algoritmoGeneraSecciones(int nseccionesMaxGrafo, int minCruces, int maxCruces){
		int nseccionesAcumuladas=0;
		int ncrucesActumuladosActual=0;
		int i=randInt(1,nseccionesMaxGrafo);
		int j=randInt(1,nseccionesMaxGrafo);
		int numeroRandom;
		int ncrucesActual;
		tablaGrafo=new int[nseccionesMaxGrafo+1][nseccionesMaxGrafo+1];
		while(nseccionesAcumuladas<=nseccionesMaxGrafo){
			while(tablaGrafo[i][0]==1){//Generamos una i que no hayamos puesto a 1
				i=randInt(1,nseccionesMaxGrafo);
			}
			tablaGrafo[i][0]=1;
			ncrucesActumuladosActual=cuentaCrucesVerticeActual(i);
			ncrucesActual=randInt(minCruces,maxCruces);
			while((ncrucesActumuladosActual < ncrucesActual) && (nseccionesAcumuladas<=nseccionesMaxGrafo)){   
					while(tablaGrafo[i][j]==1 || i==j || (cuentaCrucesVerticeActual(j)> maxCruces)){//Generamos una j que no hayamos puesto a 1
						j=randInt(1,nseccionesMaxGrafo);
					}
                    if(debug) System.out.println(
								"(ncrucesActumuladosActual <= ncrucesActual):"+(ncrucesActumuladosActual <= ncrucesActual)
								+" (nsecciones<=nseccionesMaxGrafo):"+(nseccionesAcumuladas<=nseccionesMaxGrafo)
								+" i("+i+") j("+j+") ncrucesActumuladosActual("+ncrucesActumuladosActual+") nseccionesAcumuladas("
								+nseccionesAcumuladas+") nseccionesMaxGrafo("+nseccionesMaxGrafo+")");
						numeroRandom=randInt(0,1);
						if(numeroRandom==1){
							tablaGrafo[i][j]=tablaGrafo[j][i]=1;
							ncrucesActumuladosActual++;
							nseccionesAcumuladas++;
						}
				}
			}
		//Limpieza de primera columna y fila usada para ver si visitado
		for(j=0;j<=nseccionesMaxGrafo;j++){
			tablaGrafo[0][j]=0;
			tablaGrafo[j][0]=0;
		}
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
	    		  principal.getCassandraSession().execute("DROP TABLE vertices");
	    		  principal.getCassandraSession().execute("CREATE TABLE vertices (vertA int, vertB int, idseccion text PRIMARY KEY)");
	    		  principal.getCassandraSession().execute("CREATE INDEX vertices_vertA ON vertices (vertA)");
	    		  principal.getCassandraSession().execute("CREATE INDEX vertices_vertB ON vertices (vertB)");
	      	  }
	    	       }catch (InvalidQueryException e){
	    	    	   principal.getCassandraSession().execute("CREATE TABLE vertices (vertA int, vertB int, idseccion text PRIMARY KEY)");
	    	    	   principal.getCassandraSession().execute("CREATE INDEX vertices_vertA ON vertices (vertA)");
	 	    		  principal.getCassandraSession().execute("CREATE INDEX vertices_vertB ON vertices (vertB)");
	    	    		 System.out.println("Error vertices\n");}
	      try{ 
	    	  principal.getCassandraSession().execute("SELECT idseccion FROM Bd.caracteristicasVertices LIMIT 1");
	    	    }catch (InvalidQueryException e){
	    	    	 principal.getCassandraSession().execute("CREATE TABLE caracteristicasVertices (idseccion text PRIMARY KEY, consumoMax int, coste float)");
				  	System.out.println("Error caracteristicasVertices\n");}	    
	      
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
	          System.out.println(String.format("%-14s%-14s%-14s",
	          		"idseccion", "vertA","vertB"));
	   		for (Row row : results) {
	   			System.out.println(String.format("%s",
	   				       "---------------+---------------+---------------"));
	   				
	   		   System.out.println(String.format("%-20s%-20s%-20s", row.getString("idseccion"),
	   		    	row.getInt("vertA"),row.getInt("vertB")));
	   		}
			System.out.println();
			results = principal.getCassandraSession().execute("SELECT * FROM caracteristicasVertices");
	          System.out.println(String.format("%-14s%-14s%-5s",
	          		"idseccion", "consumoMax","coste"));
	   		for (Row row : results) {
	   			System.out.println(String.format("%s",
	   				       "---------------+---------------+-----------"));
	   		   System.out.println(String.format("%-20s%-20s%-3.1f", row.getString("idseccion"),row.getInt("consumoMax"),  row.getFloat("coste")));
	   		}
			System.out.println();
	      }
	     }
}

