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
	
	public PreparaLineal(Gestor gestor){
		this.gestor=gestor;

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

	private float[][] preTabla;
	private int bloque;
	private Gestor gestor;
	private int numeroClientes;
	private int numeroCaminos;
	
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
		bloque = secciones.secciones.size();
		numeroClientes=cuentaNumClientes(caminos);
		numeroCaminos=caminos.listaCaminos.size();
		preTabla = new float[numeroCaminos+numeroClientes+bloque+1][numeroCaminos*bloque+2];
		boolean agregadoRest1camino;
		int numeroSecciones=0;
		int posIniRestricciones1camino=caminos.listaCaminos.size();
		for(int i=0;i<caminos.listaCaminos.size();i++){
			agregadoRest1camino=false;
			numeroSecciones=0;
			for(int j=0;j<bloque;j++){
				if(existeSeccionEnCamino(((Camino)caminos.listaCaminos.get(i)).idsecciones,j)){
					//Agrego la seccion a la restriccion de maxmin 1 camino por cliente si no se ha añadido ya
					if(!agregadoRest1camino){
						 preTabla[posIniRestricciones1camino][i*bloque+j]=1;
						 agregadoRest1camino=true;
					}
					//Utilizo esta seccion
					preTabla[i][i*bloque+j]=1;
					//Limites de consumo de cada seccion
					preTabla[numeroCaminos+numeroClientes+j][i*bloque+j]=getConsumoCliente(((Camino)caminos.listaCaminos.get(i)).idcliente);
					preTabla[numeroCaminos+numeroClientes+j][preTabla[0].length-2]=-1;
					preTabla[numeroCaminos+numeroClientes+j][preTabla[0].length-1]=secciones.getLimiteSeccion(j);
					//Coste de usar cada sección en funcion objetivo
					preTabla[preTabla.length-1][i*bloque+j]=getConsumoCliente(((Camino)caminos.listaCaminos.get(i)).idcliente) * secciones.getCosteSeccion(j);
					//Sirve para calcular las secciones que hay en un camino en total , para si se marca una seccion de ese camino obligue a marcar todos
					numeroSecciones++;
				}
				
				if((((i+1)<numeroCaminos)&&((Camino)caminos.listaCaminos.get(i)).idcliente!=((Camino)caminos.listaCaminos.get(i+1)).idcliente)&&j==(bloque-1)
						||
						j==(bloque-1)&&i==caminos.listaCaminos.size()-1){//Cambiamos de cliente debemos añadir las restricciones del camino del cliente
				  //iguala al total
					preTabla[posIniRestricciones1camino][preTabla[0].length-2]=0;
					preTabla[posIniRestricciones1camino][preTabla[0].length-1]=1;
					if(i+1<caminos.listaCaminos.size()-1){
					System.out.println("entro para i"+i+", j"+j+ " comparacion id cliente:"+(((Camino)caminos.listaCaminos.get(i)).idcliente!=((Camino)caminos.listaCaminos.get(i+1)).idcliente));
					
					posIniRestricciones1camino++;
					}
				}
			}
			preTabla[i][preTabla[0].length-2]=0;
			preTabla[i][preTabla[0].length-1]=numeroSecciones;
		}
	}
	
	public void transformaAtablaSimplex(){
		
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
	
	public void pintaTablaPreSimplex(){
		//enunciado
		for(int i=0;i<preTabla[0].length;i++){
			if(i==(preTabla[0].length-2)){
				System.out.print("s     ");
			} else if(i==(preTabla[0].length-1)){
				System.out.print("c");
			}else{
				System.out.print(i/bloque+"/"+mod(i,bloque)+"    ");
			}
		}
		System.out.println();
		int longitudNumeroActual=0;
		DecimalFormat df = new DecimalFormat("#0.0#");
		for(int i=0;i<preTabla.length;i++){
			for(int j=0;j<preTabla[0].length;j++){
				System.out.print(df.format(preTabla[i][j]));
				longitudNumeroActual=logitudNumero(preTabla[i][j]);
				if(preTabla[i][j]<0) longitudNumeroActual++;
				if(longitudNumeroActual<4&&j<(preTabla[0].length-2)){
					for(int t=longitudNumeroActual;t<4;t++){
						System.out.print(" ");						
					}
					
				}else if(longitudNumeroActual<2&&j<(preTabla[0].length-1)){
					for(int t=longitudNumeroActual;t<2;t++){
						System.out.print(" ");						
					}
				}
				System.out.print(" ");
			}
			System.out.print("\n");
		}
	}
	
	private int logitudNumero(float nf)
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
}