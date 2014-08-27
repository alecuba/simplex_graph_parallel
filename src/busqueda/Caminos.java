package busqueda;

import java.util.ArrayList;
import java.util.Iterator;

public class Caminos {
	public ArrayList<Camino> getCaminos() {
		return caminosFinal;
	}

	private ArrayList<Integer> finalizadoOvisitando = new ArrayList<Integer>();//-1 finalizado , 1 visitandose
	private ArrayList<Camino> caminosFinal = new ArrayList<Camino>();
	private long id=0;
	
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
			Iterator<Seccion> itrseccion = caminoTemp.secciones.iterator();
			while(itrseccion.hasNext()){
				if(camino.length()!=0) camino=camino+"-";
				camino=camino+itrseccion.next().idSeccion;
			}
			System.out.println(String.format("%-20s%-20s%-20s",caminoTemp.idcliente, caminoTemp.idcamino,camino));
		}
	}
	
	public void agregaNuevoCamino(Camino caminoNuevo,boolean finalizado){
		caminoNuevo.idcamino=id;
		
		if(finalizado){
		finalizadoOvisitando.add(-1);
		caminoNuevo.secciones.remove(0);
		}else{
			finalizadoOvisitando.add(0);
		}
		caminosFinal.add(caminoNuevo);
		id++;
	}
	
	public void borraCamino(Camino caminoAborrar){
		//busca camino con id caminoAborrar.id
		boolean encontrado=false;
		int i=0;
		while(!encontrado&&i<caminosFinal.size()){
		 if(caminosFinal.get(i).idcamino == caminoAborrar.idcamino){encontrado=true;}else{i++;}
		}
		finalizadoOvisitando.set(i, -1);
		caminosFinal.remove(i);
		finalizadoOvisitando.remove(i);
		caminosFinal.trimToSize();
		finalizadoOvisitando.trimToSize();
	}
	
	public Camino getUltimoCaminoNoFinalizadoNiVisitando(){
		boolean encontradoUno=false;
		Camino camino=null;
		int i=0;
		int uno=1;
		 while(!encontradoUno&&i<finalizadoOvisitando.size()){
			if(finalizadoOvisitando.get(i)==0){
				encontradoUno=true;
				camino=caminosFinal.get(i); 
				finalizadoOvisitando.set(i, uno);
			}
			i++;
		 }
		return camino;
	}
	
	public boolean isFinalizados(){
		boolean finalizados=true;
		int i=0;
		 while(finalizados&&i<finalizadoOvisitando.size()){
			if(finalizadoOvisitando.get(i)!=-1){
				finalizados=false;
			}
			i++;
		 }
		return finalizados;
	}
}
