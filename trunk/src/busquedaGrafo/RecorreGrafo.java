package busquedaGrafo;

import java.util.UUID;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
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
	      try{
	    	  session.execute("USE BD");
	    	  session.execute("SELECT * FROM nodos WHERE extremoA=0 UNION ALL SELECT * FROM nodos WHERE extremoB=0 ");	 
	      }catch (InvalidQueryException e){
	    	
	      }
		desconecta();
	}
	
	private void desconecta(){
		session.close();
	}

}
