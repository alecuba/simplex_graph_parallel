package busquedaGrafo;

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

public class RecorreGrafo {
	private UUID uuidNodoActual=null;
	public RecorreGrafo(){
		
	}
	
	private void recorre(){
		if(uuidNodoActual==null){
			
		}
		//if vacio then
				//Consultar los 0 CQ
		//else
			//Consultar los actual		
	}
	
	private Session session;
	private boolean debug=false;
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
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
	
	public void consultaExtremoGrafoSQL(int extremo){
		  conecta();
		  if(debug)System.out.printf("consultaExtremo\n");
	      try{
	    	  session.execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorbd\n");
	      }
	      try{
	      ResultSet results = session.execute("SELECT * FROM extremosNodos WHERE extremo = 0");
          System.out.println(String.format("%-80s%-30s%-14s",
          		"id","idseccion", "extremo"));
   		for (Row row : results) {
   			System.out.println(String.format("%s",
   				       "-----------------------------------------------------------+----------------------+---------------"));
   				
   		   System.out.println(String.format("%-46.46s%-30s%-20s", row.getUUID("id"),row.getString("idseccion"),
   		    	row.getInt("extremo")));
   		}
		System.out.println();
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorconsulta\n");
	      }
	      
	      
		desconecta();
	}
	
	private void desconecta(){
		session.close();
	}

}
