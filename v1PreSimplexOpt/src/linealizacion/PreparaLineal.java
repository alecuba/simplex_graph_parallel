package linealizacion;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;


public class PreparaLineal {
	private ArrayList<ArrayList<Float>> seccionesTemp=new ArrayList<ArrayList<Float>>();
	
	public PreparaLineal(){

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

}