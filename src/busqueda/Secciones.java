package busqueda;

import java.util.ArrayList;

public class Secciones {

	 private class Seccion {

			public int idSeccion;
			public float coste;
			public int maximo;
			public int vertA;
			public int vertB;}
  
  ArrayList secciones= new ArrayList();
  public int agregaSeccion(int idSeccion,float coste, int maximo, int vertA, int vertB){
	  int pos=-1;
	  pos=buscaPosSiExiste(idSeccion,vertA, vertB);
	  if(pos==-1){
	  Seccion nuevaSeccion = new Seccion();
	  nuevaSeccion.idSeccion=idSeccion;
	  nuevaSeccion.coste=coste;
	  nuevaSeccion.maximo=maximo;
	  nuevaSeccion.vertA=vertA;
	  nuevaSeccion.vertB=vertB;
	  secciones.add(nuevaSeccion);
	  pos=secciones.indexOf(nuevaSeccion);
	  }
	  return pos;
  } 
  
  private int buscaPosSiExiste(int idSeccion, int vertA, int vertB){
	  int i=0;
	  int pos=-1;
	  Seccion seccion;
	  if(secciones.size()>0){
	  //omp parallel for shared(pos) private(seccion)
	  for(i=(secciones.size()-1);i>=0;i--){
		  seccion = ((Seccion)secciones.get(i));
		  if(seccion.idSeccion==idSeccion && seccion.vertA == vertA &&  seccion.vertB == vertB) {pos=i;break; }
	  }
	  }
	  return pos;
  }
  
  public int getVertBSeccionID(int id){
	  int vertB=-1;
	  Seccion seccion = ((Seccion)secciones.get(id));
	  if(id<0 || id< secciones.size()) vertB=seccion.vertB;
	  return vertB;
  }
  
  public int getVertASeccionID(int id){
	  int vertA=-1;
	  Seccion seccion = ((Seccion)secciones.get(id));
	  if(id<0 || id< secciones.size()) vertA=seccion.vertA;
	  return vertA;
  }
  
  public int getidSeccion(int id){
	  Seccion seccion = ((Seccion)secciones.get(id));
	  return seccion.idSeccion;
  }}

