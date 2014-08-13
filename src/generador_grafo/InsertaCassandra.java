package generador_grafo;

import java.util.Random;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class InsertaCassandra {
   private Cluster cluster;
   private Session session;
   static Random rand = new Random();
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
			            "extremoA int," + 
			            "extremoB int," + 
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
			          "0," +
			          "1," +
			          "2000," +
			          "0.2)" +
			          ";");
	   session.execute(
			      "INSERT INTO BDCaminos.nodos (id, extremoA, extremoB, consumoMax, coste) " +
			      "VALUES (" +
			          "blobAsUuid(timeuuidAsBlob(now()))," +
			          "1," +
			          "0," +
			          "2500," +
			          "0.5)" +
			          ";");
   }
   
   public void insertDataFromAdjacentTable(int[][] adjacentTable){
	   int i,j;
	   for(i=0;i<adjacentTable.length;i++){
		   for(j=i+1;j<adjacentTable.length;j++){
			   session.execute(
					      "INSERT INTO BDCaminos.nodos (id, extremoA, extremoB, consumoMax, coste) " +
				"VALUES (" +"blobAsUuid(timeuuidAsBlob(now()))," +j+ "," +i+"," +randInt(1000,20000)+ "," +randfloat((float)0.1,(float)1.5)+");");
		   }
	   }
   }
   
   private static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
   
   private static float randfloat(float min, float max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	     float randomNum=((max - min) * rand.nextFloat()) + min;
	    return randomNum;
	}
   
   public void querySchema(){
	//   ResultSet results = session.execute("SELECT * FROM BDCaminos.nodos " +
		//        "WHERE ( extremoA = 00000000-0000-0000-0000-000000000000 ) OR ( extremoB = 00000000-0000-0000-0000-000000000000 );");
	   ResultSet resultsA = session.execute("SELECT * FROM BDCaminos.nodos WHERE extremoA = 1;");
	   ResultSet resultsB = session.execute("SELECT * FROM BDCaminos.nodos WHERE extremoB = 1;");
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
   
   public void prepara(){
	   connect("127.0.0.1");
	   createSchema();	   
   }
   
   public void pinta(){
	   ResultSet results = session.execute("SELECT * FROM BDCaminos.nodos");
	   System.out.println(String.format("%-37s%-37s%-37s%-10s %-5s",
"id", "extremoA", "extremoB","consumoMax","coste"));
	   
		for (Row row : results) {
			System.out.println(String.format("%s",
				       "------------------------------------+------------------------------------+------------------------------------+----------+-----"));
				
		    System.out.println(String.format("%-37s%-37s%-37s%-10d %-3.1f", row.getUUID("id"),
		    	row.getInt("extremoA"),  row.getInt("extremoB"),row.getInt("consumoMax"),  row.getFloat("coste")));
		}
		System.out.println();
   }
   
   public void cierra(){
	   close();
   }
   
   public static void main(String[] args) {
	  InsertaCassandra client = new InsertaCassandra();
	  client.connect("127.0.0.1");
	  client.createSchema();
	  client.loadData();
	  client.querySchema();
	   client.close();
   }
}
