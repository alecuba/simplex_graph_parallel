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

public class GenerarClientes {
	private int minClientes = 6;
	private int maxClientes = 10;
	private int minConsumo=1;
	private int maxConsumo=3;
	private int[][] arrayClientes;
	private Random rand = new Random();
    private boolean debug=false;
    private GenerarGrafo grafo;
	
    public GenerarClientes(GenerarGrafo grafo){
    	this.grafo=grafo;
    }
    
	private int randInt(int min, int max) {
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
	
	public void generar(int minClientes,int maxClientes,int minConsumo,int maxConsumo){
		this.minClientes=minClientes;
		if(maxClientes<minClientes){
			this.maxClientes = minClientes;
		}else{
			this.maxClientes=maxClientes;
		}
		this.minConsumo=minConsumo;
		if(maxConsumo<minConsumo){
			this.maxConsumo = minConsumo;
		}else{
			this.maxConsumo=maxConsumo;
		}
		algoritmoGenera();
	}
	
	private void algoritmoGenera(){
		int clientesActual=randInt(minClientes,maxClientes);
		arrayClientes = new int[clientesActual][2];
		for(int i=0;i<clientesActual;i++){
			arrayClientes[i][0]=randInt(minConsumo,maxConsumo);
		}		
	}
	
	public void conectarClientes(int numSecciones){
		int puntoConexion;
		for (int i=0;i<arrayClientes.length;i++){
			puntoConexion=randInt(1,numSecciones);
			while(!grafo.compruebaSiConexion(puntoConexion)){
				puntoConexion=randInt(1,numSecciones);
			}
			arrayClientes[i][1]=puntoConexion;
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
		int numClientesActual=arrayClientes.length;
		for(int i=0;i<numClientesActual;i++){
			if(debug){
				if(logitudNumero(numClientesActual)>1) {System.out.print("    ");}else{System.out.print("   ");}
				  for(int j=0;j<numClientesActual;j++){
					System.out.print(j);
					if(j<numClientesActual-1){
					System.out.print("  ");
					}
				  }
				  System.out.print("\n");				
			}
		}
			 
		 for(int j=0;j<numClientesActual;j++){
				if(j==0){
					if(debug){
					if(logitudNumero(numClientesActual)>1) {
							for(int n=0;n<(logitudNumero(numClientesActual)-logitudNumero(j));n++){
								System.out.print(" ");
							}
						}
				  preComa=" [";
				  }else{preComa="[";}
				}else{
				  preComa=",";
				}
				if(j>0 && debug){
				if(logitudNumero(numClientesActual)>1) {
					  if(logitudNumero(numClientesActual)==logitudNumero(j)){
						for(int n=0;n<=(logitudNumero(numClientesActual)-logitudNumero(j));n++){
							System.out.print(" ");
						}
					  }
					  System.out.print(" ");
					}
				
				}
				
				System.out.print(preComa+arrayClientes[j]);			
			  }
		System.out.print("\n");
	}


	public void insertaGrafoCQL(boolean recreaBD){
		conecta();
		try{
	    	  session.execute("USE BD");
	      }catch (InvalidQueryException e){
	    	  session.execute("CREATE KEYSPACE BD WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
	    	  session.execute("USE BD");
	      }
		
		try{
	    	  session.execute("SELECT id FROM clientes LIMIT 1");
	    	  if(recreaBD){
	    		  session.execute("DROP TABLE clientes");
	    		  session.execute("CREATE TABLE clientes (id int PRIMARY KEY,idseccion int, consumoActual int)"); 	  
	  		}
	      }catch (InvalidQueryException e){
	    	  session.execute("CREATE TABLE clientes (id int PRIMARY KEY, idseccion int,consumoActual int)"); 	  
	      }
		
		   PreparedStatement ps = session.prepare("INSERT INTO clientes (id, idseccion,consumoActual) VALUES (?, ?, ?)");
		   BatchStatement batch = new BatchStatement();
		   for(int i=0;i<arrayClientes.length;i++){
			batch.add(ps.bind(i,arrayClientes[i][1],arrayClientes[i][0]));
		   }
			session.execute(batch);
		  desconecta();
	}
	
	public void pintaBD(){
		boolean vacia=false;
		conecta();
	      try{
			     session.execute("USE BD");      
	      }catch (InvalidQueryException e){
	    	  vacia=true;
	    	  System.out.println("Vacia o no existe BD\n");
	      }
	      if(!vacia){
	    	  ResultSet results = session.execute("SELECT * FROM clientes");
	          System.out.println(String.format("%-12s%-12s%-12s","id","idseccion", "consumoActual"));
	   		for (Row row : results) {
	   			System.out.println(String.format("%s","--------+---------+-------------------"));
	   				
	   		   System.out.println(String.format("%-20d%-20d%-20d", row.getInt("id"),row.getInt("idseccion"),row.getInt("consumoActual")));
	   		}
			System.out.println();
	      }
	      desconecta();
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
	
	private void desconecta(){
		session.close();
	}
}
