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
	  boolean encontrado=false;
	  int i=0;
	  Seccion seccion;
	  if(secciones.size()>0){
	  /*//omp parallel for shared(encontrado) private(seccion) */
	  for(i=secciones.size()-1;i>=0;i--){
		  seccion = ((Seccion)secciones.get(i));
		  if(seccion.idSeccion==idSeccion && seccion.vertA == vertA &&  seccion.vertB == vertB) {encontrado=true;break; }
	  }
	  }
	  if(!encontrado){ i=-1;} 
	  return i;
  }
  
  public int getVertBSeccionID(int id){
	  int vertB=-1;
	  if(id>=0&&id<secciones.size()&& ((Seccion)secciones.get(id))!=null){
		  vertB=((Seccion)secciones.get(id)).vertB;
	  }
	  return vertB;
  }
  
  public int getVertASeccionID(int id){
	  int vertA=-1;
	  if(id>=0&&id<secciones.size()&& ((Seccion)secciones.get(id))!=null){
		  	vertA=((Seccion)secciones.get(id)).vertA;
	  }
	  return vertA;
  }
  
  public int getidSeccion(int id){
	  Seccion seccion = ((Seccion)secciones.get(id));
	  return seccion.idSeccion;
  }}

