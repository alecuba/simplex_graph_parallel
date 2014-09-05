package linealizacion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import busqueda.Caminos;
import busqueda.Caminos.Camino;
import busqueda.Caminos.IdSeccion;
import busqueda.Secciones;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import comun.Gestor;


public class PreparaLineal {
	private ArrayList<ArrayList<Float>> seccionesTemp=new ArrayList<ArrayList<Float>>();
	  private static final double EPSILON = 1.0E+10;
	  private static final double IGUAL=4;
	  private static final double MENORIGUAL=5;
	  private static final double MAYORIGUAL=6;
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

	private double[][] preTabla;
	private int numeroSecciones;
	private Gestor gestor;
	private int numeroClientes;
	private int numeroCaminos;
	private int numeroRestricciones;
	private int numeroVarsObjetivo;
	private boolean debug;
	
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
		numeroRestricciones=numeroCaminos+numeroClientes+numeroSecciones;//una restriccion por camino + una restriccion de un camino por cliente + restriccion de consumo cada camino + funcion objetivo
		numeroVarsObjetivo=numeroCaminos*numeroSecciones;//Numero de caminos * numero de secciones = total variables
		System.out.println("NumeroClientes:"+numeroClientes+" NumeroCaminos:"+numeroCaminos+" bloque:"+numeroSecciones);
		preTabla = new double[numeroRestricciones+1][numeroVarsObjetivo+numeroRestricciones+2];
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
					preTabla[numeroCaminos+numeroClientes+j][(numeroCaminos*numeroSecciones)+numeroCaminos+numeroClientes+j]=1;//Variable holgura restricciones de cada limite de cada seccion
					//Coste de usar cada sección en funcion objetivo (multiplicado por -1 por usar maximizar)
					preTabla[preTabla.length-1][i*numeroSecciones+j]=-1*getConsumoCliente(((Camino)caminos.listaCaminos.get(i)).idcliente) * secciones.getCosteSeccion(j);
					//Sirve para calcular las secciones que hay en un camino en total , para si se marca una seccion de ese camino obligue a marcar todos
					numeroSeccionesDeUnCamino++;
				}
				//Cuando cambia de cliente o termina todos los caminos, pon el limite de un camino por cliente
				if((((i+1)<numeroCaminos)&&((Camino)caminos.listaCaminos.get(i)).idcliente!=((Camino)caminos.listaCaminos.get(i+1)).idcliente)&&j==(numeroSecciones-1)
						||
					j==(numeroSecciones-1)&&i==numeroCaminos-1){//Cambiamos de cliente debemos añadir las restricciones del camino del cliente
					//iguala al total
					preTabla[posIniRestricciones1camino][preTabla[0].length-1]=1;
					preTabla[posIniRestricciones1camino][(numeroCaminos*numeroSecciones)+numeroCaminos+posIniRestricciones1camino-numeroCaminos]=1;
					//preTabla[preTabla.length-1][(numeroCaminos*numeroSecciones)+numeroCaminos+posIniRestricciones1camino-numeroCaminos]=(double) EPSILON;
					if(i+1<caminos.listaCaminos.size()-1){			
						posIniRestricciones1camino++;
					}
				}
			}
			//Marco la variable de holgura correspondiente al camino actual
			preTabla[i][numeroCaminos*numeroSecciones+i]=1;
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
		for(int i=0;i<preTabla.length;i++){
		    if(preTabla[i][preTabla[0].length-2]==IGUAL){
		    	//Añadir una columna en preTabla[0].length-2
		    	preTabla=agregaColumna(preTabla);
		    	preTabla[i][preTabla[0].length-3]=+1;
		    	//Añadir una restriccion -M en preTabla[preTabla.length-1][preTabla[0].length-2]=EPSILON
		    	preTabla[preTabla.length-1][preTabla[0].length-3]=+EPSILON;
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
	
	private int logitudNumero(double nf)
	{
		int n=(int)nf;
		int l=0;
		if(n>0||n<0){
		n=Math.abs(n);
		for (l=0;n>0;++l)
			n/=10;
		}else{
			l=1;
		}
		//System.out.println("longitud:"+l);
		return l;			
	}
	
	public void pintaTablaPreSimplexFinal(){
		int i=0;
		int longitudNumeroActual=0;
		for(i=0;i<preTabla[0].length;i++){
			System.out.print(i);
			longitudNumeroActual=logitudNumero(i);
			if(longitudNumeroActual<8){
				for(int t=longitudNumeroActual;t<8;t++){
					System.out.print(" ");						
				}
			
			}
		}
		System.out.println();
		//enunciado
		for(i=0;i<preTabla[0].length;i++){
			if(i>=numeroCaminos*numeroSecciones&&i<(preTabla[0].length-1)){
				System.out.print("art"+(i-numeroCaminos*numeroSecciones)+"    ");
			} else if(i==(preTabla[0].length-1)){
				System.out.print("c");
			}else{
				System.out.print(i/numeroSecciones+"/"+mod(i,numeroSecciones)+"     ");
			}
		}
		System.out.println();
		DecimalFormat df = new DecimalFormat("#0.0#");
		for(i=0;i<preTabla.length;i++){
			for(int j=0;j<preTabla[0].length;j++){
				if(preTabla[i][j]!=-EPSILON){
					longitudNumeroActual=logitudNumero(preTabla[i][j]);
					System.out.print(df.format(preTabla[i][j]));
					}else if(j!=preTabla[0].length-1){
						System.out.print("-M");
						longitudNumeroActual=-1;
				}
				if(preTabla[i][j]<0) longitudNumeroActual++;
				if(longitudNumeroActual<5&&j<(preTabla[0].length-2)){
					for(int t=longitudNumeroActual;t<5;t++){
						System.out.print(" ");						
					}
					
				}else if(longitudNumeroActual<3&&j<(preTabla[0].length-1)){
					for(int t=longitudNumeroActual;t<3;t++){
						System.out.print(" ");						
					}
				}
				System.out.print(" ");
			}
			System.out.print("\n");
		}
	}
	
	
	public void pintaTablaPreSimplexSimbolo(){
		int i=0;
		int longitudNumeroActual=0;
		for(i=0;i<preTabla[0].length;i++){
			System.out.print(i);
			longitudNumeroActual=logitudNumero(i);
			if(longitudNumeroActual<8){
				for(int t=longitudNumeroActual;t<8;t++){
					System.out.print(" ");						
				}
			
			}
		}
		System.out.println();
		//enunciado
		for(i=0;i<preTabla[0].length;i++){
			if(i>=numeroCaminos*numeroSecciones&&i<(preTabla[0].length-2)){
				System.out.print("art"+(i-numeroCaminos*numeroSecciones)+"    ");
			} else if(i==(preTabla[0].length-1)){
				System.out.print("c");
			}else if(i==(preTabla[0].length-2)){
				System.out.print("s       ");
			}else{
				System.out.print(i/numeroSecciones+"/"+mod(i,numeroSecciones)+"     ");
			}
		}
		System.out.println();
		DecimalFormat df = new DecimalFormat("#0.0#");
		for(i=0;i<preTabla.length;i++){
			for(int j=0;j<preTabla[0].length;j++){
				if(preTabla[i][j]!=-EPSILON&&(j!=preTabla[0].length-2||i==preTabla.length-1)){
					longitudNumeroActual=logitudNumero(preTabla[i][j]);
					System.out.print(df.format(preTabla[i][j]));
					}else if(j!=preTabla[0].length-2){
						System.out.print("-M");
						longitudNumeroActual=-1;
				}else{
					if(preTabla[i][j]==IGUAL){
					System.out.print("=");
					longitudNumeroActual=-2;
					}
					if(preTabla[i][j]==5){
						longitudNumeroActual=-1;
						System.out.print("<=");
					}
					if(preTabla[i][j]==6){
						longitudNumeroActual=-1;
						System.out.print(">=");						
					}
				}
				if(preTabla[i][j]<0) longitudNumeroActual++;
				if(longitudNumeroActual<5&&j<(preTabla[0].length-2)){
					for(int t=longitudNumeroActual;t<5;t++){
						System.out.print(" ");						
					}
					
				}else if(longitudNumeroActual<3&&j<(preTabla[0].length-1)){
					for(int t=longitudNumeroActual;t<3;t++){
						System.out.print(" ");						
					}
				}
				System.out.print(" ");
			}
			System.out.print("\n");
		}
	}
	
}