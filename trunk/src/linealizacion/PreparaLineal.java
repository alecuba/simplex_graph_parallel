package linealizacion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import busqueda.Caminos;
import busqueda.Camino;
import busqueda.IdSeccion;
import busqueda.Secciones;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import comun.Gestor;


public class PreparaLineal {
	private ArrayList<ArrayList<Float>> seccionesTemp=new ArrayList<ArrayList<Float>>();
	  private static final double M = 1.0E+38;
	  private static final double IGUAL=4;
	  private static final double MENORIGUAL=5;
	  private static final double MAYORIGUAL=6;

		private double[][] preTabla;

		private int numeroSecciones;
		private Gestor gestor;
		private int numeroClientes;
		private int numeroCaminos;
		private int numeroRestricciones;
		private int numeroVarsObjetivo;
		private boolean debug;
		private boolean paralelo;
	  
	public PreparaLineal(Gestor gestor,boolean debug){
		this.gestor=gestor;
		this.debug=debug;
		//while(caminos.iterator().hasNext()){
			//Camino camino= caminos.iterator().next();
			//agregaSecciones(camino.idcamino,camino.vertices);
		//}
	}
	
	private void agregaSecciones(int idCamino, ArrayList<Integer>  vertices){
		int pos=-1;
		for(int i=0;i<vertices.size();i++){
			pos=buscaEnSecciones(vertices.get(i));
			if(pos==-1){
				pos=anadeSeccionNueva(vertices.get(i));
			}
			//caminosecciones[idCamino][i]=pos;
		}
	}
	
	private int buscaEnSecciones(int i){
		int pos=-1;
		int j=0;
		while(pos==-1 && j<seccionesTemp.size()){
			if(seccionesTemp.get(j).get(0)==i){
				pos=j;
			}
			j++;
		}
		return pos; 
	}
	
	private int anadeSeccionNueva(int i){
		
		
		
		return i;
		
	}
	
	private int cuentaNumClientes(Caminos caminos){
		int numeroClientes=0;
		for(int i=0;i<caminos.listaCaminos.size();i++){
			if(i!=0){
			if(((Camino)caminos.listaCaminos.get(i)).idcliente!=((Camino)caminos.listaCaminos.get(i-1)).idcliente) numeroClientes++;
			}else{
				numeroClientes++;
			}
			}
		System.out.println("Numero Clientes:"+numeroClientes);
		return numeroClientes;
	}
	
	private boolean existeSeccionEnCamino(ArrayList secciones,int posSeccion){
		boolean existe=false;
		int i=0;
		while(!existe && i<secciones.size()){
		 if(((IdSeccion)secciones.get(i)).idseccion==posSeccion){
			 existe=true;
		 }
		 i++;
		}
		return existe;
	}

	
	private int getConsumoCliente(int idcliente){
		int consumo=0;
		boolean error=false;
		Statement  consulta1 = new SimpleStatement("SELECT consumoActual FROM Bd.clientes WHERE id="+idcliente+" LIMIT 1");
		 ResultSet  resultsA=null;
		try {
			resultsA = gestor.getCassandraSession().execute(consulta1);
			error=false;
			 } catch (InvalidQueryException e) {
						System.out.println("Error al obtener datos del cliente "+idcliente);
					e.printStackTrace();
					error=true;
			  }
			  if(!error){
				if(resultsA!=null){
					List rowsA = (List)resultsA.all();
					if(rowsA.size()>0){
						for(int j=0;j<rowsA.size();j++){
							consumo = ((Row) rowsA.get(j)).getInt("consumoActual");
						}
					}
				}
			  }
		return consumo;
	}

	public void creaTablaPreSimplex(Caminos caminos, Secciones secciones) {
		numeroSecciones = secciones.secciones.size();
		numeroClientes=cuentaNumClientes(caminos);
		numeroCaminos=caminos.listaCaminos.size();
		numeroRestricciones=numeroCaminos*2+numeroClientes*2+numeroSecciones*2;//una restriccion por camino + una restriccion de un camino por cliente + restriccion de consumo cada camino + funcion objetivo
		numeroVarsObjetivo=numeroCaminos*numeroSecciones;//Numero de caminos * numero de secciones = total variables
		//System.out.println("NumeroClientes:"+numeroClientes+" NumeroCaminos:"+numeroCaminos+" bloque:"+numeroSecciones);
		preTabla = new double[numeroCaminos+numeroClientes+(numeroSecciones*2)+1][numeroVarsObjetivo+2];
		//preTabla = new float[numeroCaminos+numeroClientes+numeroSecciones+1][numeroCaminos*numeroSecciones+2];
		boolean agregadoRest1camino;
		int numeroSeccionesDeUnCamino=0;
		int posIniRestricciones1camino=numeroCaminos;
		int multiplicador=1;
		for(int i=0;i<numeroCaminos;i++){
			agregadoRest1camino=false;
			numeroSeccionesDeUnCamino=0;
			multiplicador=1;
			for(int j=0;j<numeroSecciones;j++){
				//Agrego restriccion de una sola xij como maximo
				preTabla[numeroCaminos+numeroClientes+numeroSecciones+j][j]=1;
				preTabla[numeroCaminos+numeroClientes+numeroSecciones+j][preTabla[0].length-2]=MENORIGUAL;
				preTabla[numeroCaminos+numeroClientes+numeroSecciones+j][preTabla[0].length-1]=1;
				if(existeSeccionEnCamino(((Camino)caminos.listaCaminos.get(i)).idsecciones,j)){
					//Agrego la seccion a la restriccion de maxmin 1 camino por cliente si no se ha añadido ya
					if(!agregadoRest1camino){
						 preTabla[posIniRestricciones1camino][i*numeroSecciones+j]=1;
						 //marco =
						 preTabla[posIniRestricciones1camino][preTabla[0].length-2]=IGUAL;
						 agregadoRest1camino=true;
					}
					//Utilizo esta seccion X1=X2=X3
					preTabla[i][i*numeroSecciones+j]=1*multiplicador;
					multiplicador=multiplicador*-1;//Para cuando tenemos X1=X2=X3 -> X1-X2+X3=0
					//Limites de consumo de cada seccion
					preTabla[numeroCaminos+numeroClientes+j][i*numeroSecciones+j]=getConsumoCliente(((Camino)caminos.listaCaminos.get(i)).idcliente);
					preTabla[numeroCaminos+numeroClientes+j][preTabla[0].length-1]=secciones.getLimiteSeccion(j);//Total del limite de una seccion
					preTabla[numeroCaminos+numeroClientes+j][preTabla[0].length-2]=MENORIGUAL;
					//preTabla[numeroCaminos+numeroClientes+j][(numeroCaminos*numeroSecciones)+numeroCaminos+numeroClientes+j]=1;//Variable holgura restricciones de cada limite de cada seccion
					//Coste de usar cada sección en funcion objetivo (multiplicado por -1 por usar maximizar)
					preTabla[preTabla.length-1][i*numeroSecciones+j]=getConsumoCliente(((Camino)caminos.listaCaminos.get(i)).idcliente) * secciones.getCosteSeccion(j);
					//Sirve para calcular las secciones que hay en un camino en total , para si se marca una seccion de ese camino obligue a marcar todos
					numeroSeccionesDeUnCamino++;
				}
				//Cuando cambia de cliente o termina todos los caminos, pon el limite de un camino por cliente
				if((((i+1)<numeroCaminos)&&((Camino)caminos.listaCaminos.get(i)).idcliente!=((Camino)caminos.listaCaminos.get(i+1)).idcliente)&&j==(numeroSecciones-1)
						||
					j==(numeroSecciones-1)&&i==numeroCaminos-1){//Cambiamos de cliente debemos añadir las restricciones del camino del cliente
					//iguala al total
					preTabla[posIniRestricciones1camino][preTabla[0].length-1]=1;
					//preTabla[posIniRestricciones1camino][(numeroCaminos*numeroSecciones)+numeroCaminos+posIniRestricciones1camino-numeroCaminos]=1;
						//preTabla[preTabla.length-1][(numeroCaminos*numeroSecciones)+numeroCaminos+posIniRestricciones1camino-numeroCaminos]=EPSILON;
						//preTabla[preTabla.length-1][(numeroCaminos*numeroSecciones)+numeroCaminos+posIniRestricciones1camino-numeroCaminos]=(double) EPSILON;
					if(i+1<caminos.listaCaminos.size()-1){			
						posIniRestricciones1camino++;
					}
				}
			}
			
				//preTabla[preTabla.length-1][numeroCaminos*numeroSecciones+i]=(float) EPSILON;
			//Pongo el total de secciones que tiene que tener el camino actual
			if(numeroSeccionesDeUnCamino>1) {
				preTabla[i][preTabla[0].length-1]=0;
				preTabla[i][preTabla[0].length-2]=IGUAL;
				//preTabla[preTabla.length-1][(numeroCaminos*numeroSecciones)+i]=(double) EPSILON;
				//preTabla[i][(numeroCaminos*numeroSecciones)+i]=-1;
				} else{ 
					preTabla[i][preTabla[0].length-1]=1;
					preTabla[i][preTabla[0].length-2]=MENORIGUAL;
					}
		}//Fin for camino
		if(debug) pintaTablaPreSimplexSimbolo();
		acondicionaTabla();
		if(debug) pintaTablaPreSimplexFinal();
	}
	
	public int getNumRestricciones(){
		return this.numeroRestricciones;
	}
	public int getNumVarsObjetivo(){
		return this.numeroVarsObjetivo;
	}
	
	public double[][] getTabla(){
		return this.preTabla;
	}
	
	
	private int mod(int x, int y)
	{
	    int result = x % y;
	    if (result < 0)
	    {
	        result += y;
	    }
	    return result;
	}
	
	private void acondicionaTabla(){
		int largoTabla=preTabla.length;
		for(int i=0;i<largoTabla;i++){
			switch((int)preTabla[i][preTabla[0].length-2]){
			case (int) IGUAL: 
						preTabla=agregaColumna(preTabla);
						preTabla=agregaFila(preTabla,i);
						preTabla[i][preTabla[0].length-3]=1;
						preTabla=agregaColumna(preTabla);
						preTabla[i+1][preTabla[0].length-3]=-1;
						preTabla=agregaColumna(preTabla);
						preTabla[i+1][preTabla[0].length-3]=1;
						largoTabla++;
						i++;
						preTabla[largoTabla-1][preTabla[0].length-3]=M;
						//numeroRestricciones++;
						break;
			case (int) MAYORIGUAL:
						preTabla=agregaColumna(preTabla);
						preTabla=agregaColumna(preTabla);
			    		preTabla[i][preTabla[0].length-3]=1;
			    		preTabla[largoTabla-1][preTabla[0].length-3]=M;
			    		preTabla[i][preTabla[0].length-4]=-1;			    		
						break;
			case (int) MENORIGUAL:
						preTabla=agregaColumna(preTabla);
		    			preTabla[i][preTabla[0].length-3]=1;
						break;
			}
		}
		preTabla=quitaColumnaSimbolo(preTabla);
	}
	
	private double[][] quitaColumnaSimbolo(double[][] tablain){
		double[][] tablaout=new double[tablain.length][tablain[0].length-1];
		for(int i=0;i<tablain.length;i++){
			for(int j=0;j<tablain[0].length;j++){
				if(j<tablain[0].length-2){
				 tablaout[i][j]=tablain[i][j];
				}else if(j==tablain[0].length-1){
				tablaout[i][j-1]=tablain[i][j];
				}
			}
		}
		return tablaout;
	}
	
	private double[][] agregaColumna(double[][] tablain){
		double[][] tablaout=new double[tablain.length][tablain[0].length+1];
		for(int i=0;i<tablain.length;i++){
			for(int j=0;j<tablain[0].length;j++){
				if(j==tablain[0].length-1||j==tablain[0].length-2){
				 tablaout[i][j+1]=tablain[i][j];
				}else{
				 tablaout[i][j]=tablain[i][j];
				}
			}
		}
		return tablaout;
	}
	
	private double[][] agregaFila(double[][] tablain,int filaCopiar){
		double[][] tablaout=new double[tablain.length+1][tablain[0].length];
		int filaActual=0;
		for(int i=0;i<tablain.length;i++){
			for(int j=0;j<tablain[0].length;j++){
				 tablaout[filaActual][j]=tablain[i][j];
			}
			if(i==filaCopiar){
				filaActual++;
				for(int j=0;j<tablain[0].length;j++){
					 tablaout[filaActual][j]=tablain[i][j];
				}
			}
			filaActual++;
		}
		return tablaout;
	}

	
	public void pintaTablaPreSimplexFinal(){
		int i=0;
		int separacion=10;
		String texto="";
		for(i=0;i<preTabla[0].length;i++){
			texto=Integer.toString(i);
			System.out.print(i);
			pintaEspacios(separacion-texto.length());
		}
		System.out.println();
		//enunciado
		for(i=0;i<preTabla[0].length;i++){
			texto="";
			if(i>=numeroCaminos*numeroSecciones&&i<(preTabla[0].length-1)){
				if(preTabla[preTabla.length-1][i]==M){
				texto="A"+(i-numeroCaminos*numeroSecciones);
				}else{
					texto="S"+(i-numeroCaminos*numeroSecciones);
				}
			} else if(i==(preTabla[0].length-1)){
				texto="c";
			}else{
				texto=i/numeroSecciones+"/"+mod(i,numeroSecciones);
			}
		  System.out.print(texto);
		  pintaEspacios(separacion-texto.length());
		}
		System.out.println();
		DecimalFormat df = new DecimalFormat("#0.0#");
		for(i=0;i<preTabla.length;i++){
			for(int j=0;j<preTabla[0].length;j++){
				if(preTabla[i][j]!=M){
					texto=df.format(preTabla[i][j]);
					}else{
						texto="M";
					}
				System.out.print(texto);
				pintaEspacios(separacion-texto.length());
			}
			System.out.print("\n");
		}
	}
	
	 private void pintaEspacios(int espacios){
			for(int t=0;t<espacios;t++){
				System.out.print(" ");						
			}
	}
	
	public void pintaTablaPreSimplexSimbolo(){
		int i=0;
		int separacion=10;
		String texto="";
		for(i=0;i<preTabla[0].length;i++){
			texto=Integer.toString(i);
			System.out.print(i);
			pintaEspacios(separacion-texto.length());
		}
		System.out.println();
		//enunciado
		for(i=0;i<preTabla[0].length;i++){
			texto="";
			if(i>=numeroCaminos*numeroSecciones&&i<(preTabla[0].length-2)){
				if(preTabla[preTabla.length-1][i]==M){
				texto="A"+(i-numeroCaminos*numeroSecciones);
				}else{
					texto="S"+(i-numeroCaminos*numeroSecciones);
				}
			} else if(i==(preTabla[0].length-1)){
				texto="c";
			}else if(i==(preTabla[0].length-2)){
				texto=("<>=");
			}else{
				texto=i/numeroSecciones+"/"+mod(i,numeroSecciones);
			}
		  System.out.print(texto);
		  pintaEspacios(separacion-texto.length());
		}
		System.out.println();
		DecimalFormat df = new DecimalFormat("#0.0#");
		for(i=0;i<preTabla.length;i++){
			for(int j=0;j<preTabla[0].length;j++){
				if(preTabla[i][j]!=M&&(j!=preTabla[0].length-2||i==preTabla.length-1)){
					texto=df.format(preTabla[i][j]);
					}else if(j!=preTabla[0].length-2){
						texto="M";
							}else{
								if(preTabla[i][j]==IGUAL) texto="=";
								if(preTabla[i][j]==5) texto="<=";
								if(preTabla[i][j]==6) texto=">=";
								}
				System.out.print(texto);
				pintaEspacios(separacion-texto.length());
			}
			System.out.print("\n");
		}
	}
	
	public void formateaResultadoSimplex(int[] seccionesSimplex,Caminos caminos){
		System.out.println("Caminos a utilizar:");
		int camino=0;
		for(int bloque=0;bloque<seccionesSimplex.length;bloque+=numeroSecciones){
			Camino micamino=caminos.getCamino(camino);
			int secciones=0;
			boolean noSeUsa=false;
			while(secciones<micamino.idsecciones.size() && !noSeUsa){
				if(seccionesSimplex[bloque+((IdSeccion)micamino.idsecciones.get(secciones)).idseccion]!=1){
					  noSeUsa=true;
					}else{secciones++;}
			}
			if(!noSeUsa){System.out.println("Cliente("+micamino.idcliente+") Camino("+micamino.idcamino+")");}
			camino++;	
			}
	}
	
	 public void setFlags(boolean debug,boolean paralelo) {
			this.debug = debug;
			this.paralelo=paralelo;
		}
	
}