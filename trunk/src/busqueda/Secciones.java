package busqueda;

import java.util.ArrayList;

public class Secciones {
  ArrayList<Seccion> secciones= new ArrayList<Seccion>();
  public int agregaSeccion(Seccion seccion){
	  secciones.add(seccion);
	  return secciones.indexOf(seccion);
  }
  
}
