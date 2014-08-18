package busquedaGrafo;

import java.util.List;

import generador_grafo.Principal;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {
	private Principal principal;
    
	public RecorreGrafo(Principal principal){
		this.principal=principal;
	}
	
	private CaminoCliente[] CaminoClienteN;
	
	class CaminoCliente{
		public int id;
		public CaminoCliente siguiente;		
	};
	
	private int[] consultarSiguientesSaltos(int idcliente,int idanterior,int idseccionnueva){
			
		CaminoClienteN[id].id=idanterior;
		CaminoClienteN[id].siguiente=null; 
		//principal.getCassandraSession().execute("CREATE TABLE vertices (vertA int, vertB int, idseccion text, PRIMARY KEY (vertA, vertB))");
		System.out.print("Seccionactual("+idseccionnueva+")");
		int[] resultados=null;
		try{
		      ResultSet resultsA = principal.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+idseccionnueva);
		      ResultSet resultsB = principal.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+idseccionnueva);
		      List<Row> rowsA = resultsA.all();
		      List<Row> rowsB = resultsB.all();
		      int pos=0;
		      resultados =new int[rowsA.size()+rowsB.size()];
	          for (Row row : rowsA) {
	        	  		resultados[pos]=row.getInt("vertB");
		   			 pos++;
		   		}
	          for (Row row : rowsB) {
	        	  resultados[pos]=row.getInt("vertA");
		   			 pos++;
		   		}
	          if(debug){
			  System.out.print(" caminos Posibles:");
			  for(int j=0;j<resultados.length;j++){
				  System.out.print(resultados[j]+",");
				  }
	          }
			  for(int j=0;j<resultados.length;j++){
				  consultarSiguientesSaltos(resultados[j]);
			  }
		      }catch (InvalidQueryException e){
		    	  if(debug)System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
		    	  e.printStackTrace();
		      }
		System.out.println();
		return resultados;
	}
	
	private void viajaRecursivo(){
		
	}


	
	public void consultaExtremoGrafoSQL(int extremo){
		  if(debug)System.out.printf("consultaExtremo\n");
	      try{
	    	  principal.getCassandraSession().execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorbd\n");
	      }
	      try{
	      ResultSet clientes = principal.getCassandraSession().execute("SELECT * FROM clientes");
          System.out.println(String.format("%-80s%-30s%-14s",
          		"id","idseccion", "extremo"));
          List<Row> rows = clientes.all();
	      CaminoClienteN = new CaminoCliente[rows.size()]; 
	      int i=0;
   		for (Row row : rows) {
   			System.out.print("Buscando camino para el cliente("+row.getInt("id")+") con conexion("+row.getInt("idseccion")+"),");
   			//session.execute("CREATE TABLE clientes (id int PRIMARY KEY,idseccion int, consumoActual int)"); 
  			
   			consultarSiguientesSaltos(row.getInt("id"),row.getInt("idseccion"));
   		}
		System.out.println();
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorconsulta\n");
	      }
	}
	
	private boolean debug=false;
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
}
