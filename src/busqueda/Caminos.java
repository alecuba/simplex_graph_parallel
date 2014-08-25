package busqueda;

import java.util.ArrayList;
import java.util.Iterator;

public class Caminos {
	public ArrayList<Camino> getCaminos() {
		return caminosFinal;
	}

	private ArrayList<Camino> caminosFinal = new ArrayList<Camino>();
	
	public void pintaCaminosGenerados() {
		System.out.println(String.format("%-14s%-14s%-14s", "cliente",
				"idcamino", "secciones"));
		System.out.println(String.format("%s",
				"---------------+---------------+---------------"));
		Iterator<Camino> itrCamino = caminosFinal.iterator();
		String camino;
		while (itrCamino.hasNext()) {
			camino="";
			Camino caminoTemp = itrCamino.next();
			System.out.println("Secciones length "+caminoTemp.secciones.size());
			Iterator<Seccion> itrseccion = caminoTemp.secciones.iterator();
			while(itrseccion.hasNext()){
				if(camino.length()!=0) camino=camino+"-";
				camino=camino+itrseccion.next().idSeccion;
			}
			System.out.println(String.format("%-20s%-20s%-20s",caminoTemp.idcliente, caminoTemp.idcamino,camino));
		}
	}
	
	public void agregaCamino(Camino caminoAagregar){
		caminoAagregar.idcamino = caminosFinal.size();
	   caminosFinal.add(caminoAagregar);
	}
}
