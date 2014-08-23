package busqueda;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gui.Principal;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class RecorreGrafo {
	private Principal principal;
    
	public RecorreGrafo(Principal principal){
		this.principal=principal;
	}
	
	private int[] consultarSiguientesSaltos(int idcliente,int idanterior,int idseccionnueva){
			
		//CaminoClienteN[id].id=idanterior;
		//CaminoClienteN[id].siguiente=null; 
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
				  //consultarSiguientesSaltos(resultados[j]);
			  }
		      }catch (InvalidQueryException e){
		    	  if(debug)System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
		    	  e.printStackTrace();
		      }
		System.out.println();
		return resultados;
	}
	
	public void generaCaminos(Camino caminoTemp){
		 int idcamino=caminoTemp.idcamino;
		 int idcliente=caminoTemp.idcliente;
		 int ultimoVertice=caminoTemp.vertices.get(caminoTemp.vertices.size()-1);
		if(debug)System.out.printf("generaCaminos cliente("+idcliente+") en camino("+idcamino+")\n");
		int[] resultados=null;
		try{
		      ResultSet resultsA = principal.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertA="+ultimoVertice);
		      ResultSet resultsB = principal.getCassandraSession().execute("SELECT vertA,vertB,idseccion FROM Bd.vertices WHERE vertB="+ultimoVertice);
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
	          Camino caminoAvisitar;
			  for(int j=0;j<resultados.length;j++){
				  if(debug) System.out.print(" \nllamando para subcamino("+resultados[j]+")");
				  if(!compruebaVerticeVisitado(caminoTemp,resultados[j])){
					  caminoAvisitar = new Camino();
					  caminoAvisitar.idcliente=idcliente;
					  caminoAvisitar.idcamino=idcamino;
					  caminoAvisitar.vertices.addAll(caminoTemp.vertices);
					  caminoAvisitar.vertices.add(resultados[j]);
					  if(resultados[j]!=0){
						  generaCaminos(caminoAvisitar);
					  }else{
						  caminoAvisitar.idcamino=caminosFinal.size();
						  caminosFinal.add(caminoAvisitar);
						  if(debug) System.out.print(" \nEncontre el 0");  
					  }
				  }
			  }
		      }catch (InvalidQueryException e){
		    	  if(debug)System.out.printf("errorconsultaconsultarSiguientesSaltos\n");
		    	  e.printStackTrace();
		      }
	}
	
   private boolean compruebaVerticeVisitado(Camino camino,int i) {
	   boolean contiene=false;
	   Iterator<Integer> itr=camino.vertices.iterator();
	   while(itr.hasNext()&&!contiene){
		   if(itr.next()==i) contiene=true;
	   }
	  return contiene;
	}

   private class Camino{
	   public int idcliente;
	   public int idcamino;
	   public ArrayList<Integer> vertices=new ArrayList<Integer>();
   }
   
   private ArrayList<Camino> caminosFinal=new ArrayList<Camino>();
   void sigueCamino(int idcamino,int camino, int vertActual){
	   /*caminos[idcliente][idcamino][camino]=vertActual;
	    * camino++;
	    * Select vertA,vertB where A,B = vertActual
	    * for each row
	    * if(!compruebaVerticeVisitado(caminos[idcliente][idcamino],row.getInt(vertA))
	    * {
	    * sigueCamino(idcliente,idcamino,camino,row.getInt(vert));
	    * idcamino++;
	    * 
	    * }
	    */
	     
   }
   
   	public double consultaNumeroVertices(){
   		double nVertices=0;
   		if(debug)System.out.printf("consultaNumeroVertices:");
	      try{
	    	  principal.getCassandraSession().execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorbd\n");
	      }
	      try{
	      nVertices = principal.getCassandraSession().execute("SELECT COUNT(1) FROM Bd.vertices;").all().get(0).getLong("count");
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorconsulta\n");
	      }
	      if(debug)System.out.printf(nVertices+"\n");
		return nVertices;
   	}
   
   	public void caminosGenerados(){
   		System.out.println(String.format("%-14s%-14s%-14s","cliente", "idcamino","secciones"));
   		System.out.println(String.format("%s","---------------+---------------+---------------"));
   		Iterator<Camino> itr = caminosFinal.iterator();
   		while(itr.hasNext())
   		{
   		 Camino caminotemp=itr.next();
   		 System.out.println(String.format("%-20s%-20s%-20s", caminotemp.idcliente,caminotemp.idcamino,caminotemp.vertices.toString()));
   		}
   	}
	
	public void encuentraCaminosTodosClientes(){
		  if(debug)System.out.printf("encuentraCaminosTodosClientes\n");
	      try{
	    	  principal.getCassandraSession().execute("USE Bd");
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorbd\n");
	      }
	      try{
	      ResultSet clientes = principal.getCassandraSession().execute("SELECT * FROM clientes");
          List<Row> rows = clientes.all();
   		for (Row row : rows) {
   			System.out.print("Buscando camino para el cliente("+row.getInt("id")+") con conexion("+row.getInt("idseccion")+"),");
   			Camino caminoTemp = new Camino();
   			caminoTemp.idcliente=row.getInt("id");
   			caminoTemp.vertices.add(row.getInt("idseccion"));
   			caminoTemp.idcamino=-1;
   			generaCaminos(caminoTemp);
   			//consultarSiguientesSaltos(row.getInt("id"),row.getInt("idseccion"));
   		}
   		//limpiaCaminosSinSalida();
	      }catch (InvalidQueryException e){
	    	  if(debug)System.out.printf("errorconsulta\n");
	      }
	     System.out.println("Terminado encuentraCaminosTodosClientes");
	}

	private boolean debug=false;
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
}
