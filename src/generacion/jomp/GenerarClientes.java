package generacion.jomp;
import generacion.jomp.GenerarClientes;
import generacion.jomp.GenerarGrafo;
import gui.Principal;

import java.util.Random;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class GenerarClientes {

	private int[][] arrayClientes;
	private Random rand = new Random();
    private boolean debug=false;
    private GenerarGrafo grafo;
    private Principal principal;
    
    public GenerarClientes(GenerarGrafo grafo,Principal principal){
    	this.grafo=grafo;
    	this.principal=principal;
    }
    
	private int randInt(int min, int max) {
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;
	}
	
	
	public void generar(int minClientes,int maxClientes,int minConsumo,int maxConsumo){
		if(maxClientes<minClientes)	maxClientes = minClientes;
		if(maxConsumo<minConsumo) maxConsumo = minConsumo;
		algoritmoGenera(minClientes,maxClientes,minConsumo,maxConsumo);
		//algoritmoGenera2();
	}
	
	private void algoritmoGenera(int minClientes, int maxClientes,int minConsumo,int maxConsumo){
		int clientesActual=randInt(minClientes,maxClientes);
		arrayClientes = new int[clientesActual][2];
		/*//omp parallel for*/
		for(int i=0;i<clientesActual;i++){
			arrayClientes[i][0]=randInt(minConsumo,maxConsumo);
		}		
	}
	
	/*
	private void algoritmoGeneraTest(){
		arrayClientes = new int[1][2];
		arrayClientes[0][0]=2000;
		arrayClientes[0][1]=4;
	}
	*/
	
	public void conectarClientes(int numSecciones){
		/*//omp parallel for*/
		for (int i=0;i<arrayClientes.length;i++){
			int puntoConexion=randInt(1,numSecciones-1);
			while(!grafo.compruebaSiConexion(puntoConexion)){
				puntoConexion=randInt(1,numSecciones-1);
			}
			arrayClientes[i][1]=puntoConexion;
		}
	}
	
	
	private int logitudNumero(int n)
	{
		int l=0;
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
			for(int i=1;i<numClientesActual;i++){
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
				
						
		}
				System.out.print(preComa+i);	
		}
		System.out.print("\n");
	}


	public void insertaGrafoCQL(boolean recreaBD){
		try{
	    	  principal.getCassandraSession().execute("USE BD");
	      }catch (InvalidQueryException e){
	    	  principal.getCassandraSession().execute("CREATE KEYSPACE BD WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
	    	  principal.getCassandraSession().execute("USE BD");
	      }
		
		try{
			principal.getCassandraSession().execute("SELECT id FROM clientes LIMIT 1");
	    	  if(recreaBD){
	    		  principal.getCassandraSession().execute("DROP TABLE clientes");
	    		  principal.getCassandraSession().execute("CREATE TABLE clientes (id int PRIMARY KEY,idseccion int, consumoActual int)"); 	  
	  		}
	      }catch (InvalidQueryException e){
	    	  principal.getCassandraSession().execute("CREATE TABLE clientes (id int PRIMARY KEY, idseccion int,consumoActual int)"); 	  
	      }
		
		   PreparedStatement ps = principal.getCassandraSession().prepare("INSERT INTO clientes (id, idseccion,consumoActual) VALUES (?, ?, ?)");
		   BatchStatement batch = new BatchStatement();
		   for(int i=0;i<arrayClientes.length;i++){
			batch.add(ps.bind(i,arrayClientes[i][1],arrayClientes[i][0]));
		   }
		   principal.getCassandraSession().execute(batch);
	}
	
	public void pintaBD(){
		boolean vacia=false;
	      try{
	    	  principal.getCassandraSession().execute("USE BD");      
	      }catch (InvalidQueryException e){
	    	  vacia=true;
	    	  System.out.println("Vacia o no existe BD\n");
	      }
	      if(!vacia){
	    	  ResultSet results = principal.getCassandraSession().execute("SELECT * FROM clientes");
	          System.out.println(String.format("%-12s%-12s%-12s","id","idseccion", "consumoActual"));
	          while(results.iterator().hasNext()){
	        	  Row row = results.iterator().next();
	   			System.out.println(String.format("%s","--------+---------+-------------------"));
	   				
	   		   System.out.println(String.format("%-20d%-20d%-20d", row.getInt("id"),row.getInt("idseccion"),row.getInt("consumoActual")));
	   		}
			System.out.println();
	      }
	     }}

