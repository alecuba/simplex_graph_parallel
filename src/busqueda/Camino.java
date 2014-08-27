package busqueda;

import java.util.ArrayList;

public class Camino {
	public int idcliente;
	public long idcamino;
	public ArrayList<Seccion> secciones = new ArrayList<Seccion>();
	
	public Seccion getUltimaSeccion(){
		return secciones.get((secciones.size()-1));
	}
	
	public int getUltimoIdSeccion(){
		return secciones.get((secciones.size()-1)).idSeccion;
	}
	
	public int getUltimoVerticeSeccion(){
		return secciones.get((secciones.size()-1)).vertB;
	}
	
	public boolean isSecionVisitada(int seccion){
		boolean visitada=false;
		int i=0;
		while(!visitada&&i<secciones.size()){
			if(secciones.get(i).vertA==seccion || secciones.get(i).vertB==seccion){
				visitada=true;
			}
			i++;
		}
		return visitada;
	}
}
