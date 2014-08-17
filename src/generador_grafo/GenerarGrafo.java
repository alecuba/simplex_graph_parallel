package generador_grafo;
import java.util.Random;
import java.util.UUID;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class GenerarGrafo {
	private int minSecciones = 6;
	private int maxSecciones = 10;
	private int minCruces=1;
	private int maxCruces=3;
	private int minEntradas=1;
	private int maxEntradas=2;
	private int[][] tablaGrafo;
	private static Random rand = new Random();
    private boolean debug=false;
	
	private int randInt(int min, int max) {
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
	
	public void generar(int minSecciones,int maxSecciones,int minCruces,int maxCruces){
		this.minSecciones=minSecciones;
		if(maxSecciones<minSecciones){
			this.maxSecciones = minSecciones;
		}else{
			this.maxSecciones=maxSecciones;
		}
		this.minCruces=minCruces;
		if(maxCruces<minCruces){
			this.maxCruces = minCruces;
		}else{
			this.maxCruces=maxCruces;
		}
		algoritmoGenera();
		//preparaEntradaGrafo();
	}
	
	private void algoritmoGenera(){
		ArbolVisitados verticesVisitados = new ArbolVisitados(debug);
		int nsecciones=0;
		int ncruces_generados;
		int i,j;
		int ncruces_minActual;
		int ncruces_maxActual;
		int seccionesActual=randInt(minSecciones,maxSecciones);
		if(debug) System.out.print("Secciones("+seccionesActual+") \n");
		tablaGrafo=new int[seccionesActual][seccionesActual];
		while(nsecciones<seccionesActual){
			i=randInt(0,seccionesActual-1);
			if(!verticesVisitados.isVisitado(i)){
			verticesVisitados.insertar(i);
			j=randInt(0,seccionesActual-1);
			ncruces_generados=0;
			if(i==0){ncruces_maxActual = maxEntradas;ncruces_minActual=minEntradas;}else{ncruces_minActual=randInt(minCruces,maxCruces);}
			while((ncruces_generados <= ncruces_minActual) && (ncruces_generados<=maxCruces) && (nsecciones<seccionesActual)){
					if(debug) System.out.print("(ncruces_generados<=ncruces_maxActual):"+(ncruces_generados<=maxCruces)
												+" (nsecciones<secciones):"+(nsecciones<seccionesActual)
												+" (ncruces_generados < MIN_CRUCES):"+(ncruces_generados < minCruces)
												+" i("+i+") j("+j+") ncruces_generados("+ncruces_generados+") nsecciones("
												+nsecciones+") ncruces_minActual("+ncruces_minActual+") secciones("+seccionesActual+")");
					if(i!=j){
						tablaGrafo[i][j]=tablaGrafo[j][i]=randInt(0,1);
						if(tablaGrafo[i][j]==1){
						ncruces_generados++;
						nsecciones++;
						}
					
					}
					j=randInt(0,seccionesActual-1);
					if(debug) System.out.print(" newj("+j+")\n"); 
				}
			}
			
			
		}
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
	
	
	
	public void preparaEntradaGrafo(){
		boolean entrada=false;
		int j=0;
		int seccionesActual=tablaGrafo.length;
		 if(debug){System.out.print("tablaGrafo.length:"+tablaGrafo.length+"\n");}
		while(j<seccionesActual && !entrada){
		  if(tablaGrafo[0][j]==1){
			  entrada=true;
			  if(debug){System.out.print("Encontrado entrada en[0]["+j+"]\n");}
			}
			j++;
		}
		if(!entrada){
		  j=randInt(1,seccionesActual-1);
		  if(debug){System.out.print("No habia entrada, se ha creado una en [0]["+j+"]\n");}
		  tablaGrafo[0][j]=1;
		  tablaGrafo[j][0]=1;
		}
		
	}
	
	private Session session;
	
	private void conecta(){
		Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
	      Metadata metadata = cluster.getMetadata();
	      if(debug)System.out.printf("Conectado al cluster: %s\n", metadata.getClusterName());
	      if(debug){
	    	  for ( Host host : metadata.getAllHosts() ) {
	         System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
	               host.getDatacenter(), host.getAddress(), host.getRack());
	      }
	      }
	      session = cluster.connect();
	}
	
	public void insertaGrafoCQL(){
		  conecta();
		  session.execute("CREATE KEYSPACE IF NOT EXISTS Bd WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
	      session.execute("USE Bd");
	      try{
	    	  session.execute("SELECT id FROM extremosNodos LIMIT 1");	    	  
	      }catch (InvalidQueryException e){
	    	  session.execute("CREATE TABLE extremosNodos (id uuid PRIMARY KEY,idseccion text, extremo int)");
	    	  session.execute("CREATE TABLE caracteristicasNodos (idseccion text PRIMARY KEY, consumoMax int, coste float)");
			  session.execute("CREATE INDEX extremo_IDX ON extremosNodos (extremo)");    	  
	      }
	    	  
		   PreparedStatement psExtremos = session.prepare("INSERT INTO extremosNodos (id,idseccion, extremo) VALUES (?, ?, ?)");
		   PreparedStatement psCaracteristicas = session.prepare("INSERT INTO caracteristicasNodos (idseccion, consumoMax, coste) VALUES (?, ?, ?)");
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
				  batch.add(psExtremos.bind(UUID.randomUUID(),idseccion,i));
				  batch.add(psExtremos.bind(UUID.randomUUID(),idseccion,j));				  
				  batch.add(psCaracteristicas.bind(idseccion,consumoMax,coste));
				  //batch.add(ps.bind(UUID.randomUUID(),j, i,consumoMax,coste));
			//batch.add(ps.bind(UUID.randomUUID(),j, i,randInt(1000,20000),randfloat((float)0.1,(float)1.5)));
				   }
			}
		   }
			session.execute(batch);
			desconecta();
	}
	
	private void desconecta(){
		session.close();
	}
	
	private static float randfloat(float min, float max) {
		     float randomNum=((max - min) * rand.nextFloat()) + min;
		    return randomNum;
	}
	
	public int numeroSecciones(){
		return tablaGrafo.length;
		
	}
	
	public void pintaBD(){
		boolean vacia=false;
		conecta();
	      try{
			     session.execute("USE Bd");      
	      }catch (InvalidQueryException e){
	    	  vacia=true;
	    	  System.out.println("Vacia o no existe BD\n");
	      }
	      if(!vacia){
	    	  ResultSet results = session.execute("SELECT * FROM extremosNodos");
	          System.out.println(String.format("%-80s%-14s",
	          		"id", "extremo"));
	   		for (Row row : results) {
	   			System.out.println(String.format("%s",
	   				       "-----------------------------------------------------------+---------------"));
	   				
	   		   System.out.println(String.format("%-46.46s%-20s", row.getUUID("id"),
	   		    	row.getInt("extremo")));
	   		}
			System.out.println();
			results = session.execute("SELECT * FROM caracteristicasNodos");
	          System.out.println(String.format("%-80s%-14s%-5s",
	          		"id", "consumoMax","coste"));
	   		for (Row row : results) {
	   			System.out.println(String.format("%s",
	   				       "-----------------------------------------------------------+---------------+-----------"));
	   		   System.out.println(String.format("%-46.46s%-20s%-3.1f", row.getString("idseccion"),row.getInt("consumoMax"),  row.getFloat("coste")));
	   		}
			System.out.println();
	      }
	      desconecta();
	     }
}

