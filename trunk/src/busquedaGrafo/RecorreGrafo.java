package busquedaGrafo;

import generador_grafo.Principal;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {
	private Principal principal;
    
	public RecorreGrafo(Principal principal){
		this.principal=principal;
	}
	
	private int[] consultarSiguientesSaltos(int i){
		//principal.getCassandraSession().execute("CREATE TABLE vertices (vertA int, vertB int, idseccion text, PRIMARY KEY (vertA, vertB))");
		int[] resultados=null;
		try{
		      ResultSet resultsA = principal.getCassandraSession().execute("SELECT vertA FROM Bd.vertices WHERE vertA="+i+" ALLOW FILTERING");
		      ResultSet resultsB = principal.getCassandraSession().execute("SELECT vertB FROM Bd.vertices WHERE vertB="+i+" ALLOW FILTERING");
	          resultados =new int[resultsA.all().size()+resultsB.all().size()];
	   		  int pos=0;
	          for (Row row : resultsA) {
	   			resultados[pos]=row.getInt("idseccion");
	   			pos++;
	   		   }
	          for (Row row : resultsB) {
		   			resultados[pos]=row.getInt("idseccion");
		   			pos++;
		   		   }
			  System.out.print("Caminos Posibles:");
			  for(int j=0;j<resultados.length;j++){
				  System.out.print(resultados[j]+",");
			  }
		      }catch (InvalidQueryException e){
		    	  if(debug)System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
		    	  e.printStackTrace();
		      }
		System.out.println();
		return resultados;
	}

	private boolean debug=false;
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
	public void consultaExtremoGrafoSQL(int extremo){
		  if(debug)System.out.printf("consultaExtremo\n");
	      try{
	    	  principal.getCassandraSession().execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorbd\n");
	      }
	      try{
	      ResultSet results = principal.getCassandraSession().execute("SELECT * FROM clientes");
          System.out.println(String.format("%-80s%-30s%-14s",
          		"id","idseccion", "extremo"));
          
   		for (Row row : results) {
   			System.out.println("Buscando camino para el cliente("+row.getInt("id")+")\n");
   			//session.execute("CREATE TABLE clientes (id int PRIMARY KEY,idseccion int, consumoActual int)"); 	  
   			consultarSiguientesSaltos(row.getInt("idseccion"));
   		}
		System.out.println();
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorconsulta\n");
	      }
	}
}
