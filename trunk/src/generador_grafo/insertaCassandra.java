package generador_grafo;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class insertaCassandra {
   private Cluster cluster;
   private Session session;
   public void connect(String node) {
	  cluster = Cluster.builder()
            .addContactPoint(node)
            .build();
      Metadata metadata = cluster.getMetadata();
      System.out.printf("Connected to cluster: %s\n", 
            metadata.getClusterName());
      for ( Host host : metadata.getAllHosts() ) {
         System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
               host.getDatacenter(), host.getAddress(), host.getRack());
      }
      session = cluster.connect();
   }

   public void close() {
      cluster.close();
   }

   public void createSchema() {
	   try{
	       session.execute("USE BDCaminos;");
	       session.execute("DROP KEYSPACE BDCaminos;");
	       
	   } catch (InvalidQueryException e){
			  
	       }
	   session.execute("CREATE KEYSPACE BDCaminos WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};");
	   session.execute("USE BDCaminos;");
	   session.execute(
			      "CREATE TABLE BDCaminos.nodos (" +
			            "id uuid PRIMARY KEY," + 
			            "extremoA uuid," + 
			            "extremoB uuid," + 
			            "consumoMax int," + 
			            "coste float," + 
			            ");");
	   session.execute("CREATE INDEX extremoA_IDX ON BDCaminos.nodos (extremoA)");
	   session.execute("CREATE INDEX extremoB_IDX ON BDCaminos.nodos (extremoB)");
   }
   
   public void loadData() {
	   session.execute(
			      "INSERT INTO BDCaminos.nodos (id, extremoA, extremoB, consumoMax, coste) " +
			      "VALUES (" +
			          "blobAsUuid(timeuuidAsBlob(now()))," +
			          "00000000-0000-0000-0000-000000000000," +
			          "00000000-0000-0000-0000-000000000001," +
			          "2000," +
			          "0.2)" +
			          ";");
	   session.execute(
			      "INSERT INTO BDCaminos.nodos (id, extremoA, extremoB, consumoMax, coste) " +
			      "VALUES (" +
			          "blobAsUuid(timeuuidAsBlob(now()))," +
			          "00000000-0000-0000-0000-000000000001," +
			          "00000000-0000-0000-0000-000000000000," +
			          "2500," +
			          "0.5)" +
			          ";");
   }
   
   public void querySchema(){
	//   ResultSet results = session.execute("SELECT * FROM BDCaminos.nodos " +
		//        "WHERE ( extremoA = 00000000-0000-0000-0000-000000000000 ) OR ( extremoB = 00000000-0000-0000-0000-000000000000 );");
	   ResultSet resultsA = session.execute("SELECT * FROM BDCaminos.nodos WHERE extremoA = 00000000-0000-0000-0000-000000000000;");
	   ResultSet resultsB = session.execute("SELECT * FROM BDCaminos.nodos WHERE extremoB = 00000000-0000-0000-0000-000000000000;");
	   System.out.println(String.format("%-37s%-37s%-37s%-10s %-5s",
"id", "extremoA", "extremoB","consumoMax","coste"));
	   
		for (Row row : resultsA) {
			System.out.println(String.format("%s",
				       "------------------------------------+------------------------------------+------------------------------------+----------+-----"));
				
		    System.out.println(String.format("%-37s%-37s%-37s%-10d %-3.1f", row.getUUID("id"),
		    	row.getUUID("extremoA"),  row.getUUID("extremoB"),row.getInt("consumoMax"),  row.getFloat("coste")));
		}
		for (Row row : resultsB) {
			System.out.println(String.format("%s",
				       "------------------------------------+------------------------------------+------------------------------------+----------+-----"));
				
		    System.out.println(String.format("%-37s%-37s%-37s%-10d %-3.1f", row.getUUID("id"),
		    	row.getUUID("extremoA"),  row.getUUID("extremoB"),row.getInt("consumoMax"),  row.getFloat("coste")));
		}
		System.out.println();
   }
   public static void main(String[] args) {
	  insertaCassandra client = new insertaCassandra();
	  client.connect("127.0.0.1");
	  client.createSchema();
	  client.loadData();
	  client.querySchema();
	   client.close();
   }
}
