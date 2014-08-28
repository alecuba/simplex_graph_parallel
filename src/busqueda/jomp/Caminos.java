package busqueda.jomp;

import java.util.ArrayList;
import java.util.Iterator;

import busqueda.jomp.Secciones;

public class Caminos {
		
	public class Camino {
		public int idcliente;
		public long idcamino;
		public ArrayList idsecciones = new ArrayList();
	}
	
	public Secciones secciones = new Secciones();
	public ArrayList finalizadoOvisitando = new ArrayList();//-1 finalizado , 1 visitandose
	public ArrayList caminosFinal = new ArrayList();
	private long id=0;
	
	public Caminos(Secciones secciones){
		this.secciones=secciones;
	}
	
	public void pintaCaminosGenerados() {
		System.out.println(String.format("%-14s%-14s%-14s", "cliente",
				"idcamino", "secciones"));
		System.out.println(String.format("%s",
				"---------------+---------------+---------------"));
		Iterator itrCamino = caminosFinal.iterator();
		String camino;
		int pos=0;
		while (itrCamino.hasNext()) {
			camino="";
			Camino caminoTemp = (Camino)itrCamino.next();
			if(((int)finalizadoOvisitando.get(pos))!=-2){
			Iterator itrseccion = caminoTemp.idsecciones.iterator();
			while(itrseccion.hasNext()){
				if(camino.length()!=0) camino=camino+"-";
				camino=camino+secciones.getidSeccion((int)itrseccion.next());
			}
			System.out.println(String.format("%-20s%-20s%-20s",caminoTemp.idcliente, caminoTemp.idcamino,camino));
			}
			pos++;
		}
	}
	
	public void agregaNuevoCamino(int idcliente,int idSeccion ,int vertA,int vertB){
		Camino caminoTemp = new Camino();
		caminoTemp.idcamino=id;	
		caminoTemp.idcliente=idcliente;
		caminoTemp.idsecciones.add(this.secciones.agregaSeccion(idSeccion, 0, 0, vertA, vertB));
		caminosFinal.add(caminoTemp);
		if(vertA == 0 || vertB == 0){
		finalizadoOvisitando.add(-1);
		//secciones.borraSeccion(caminoTemp.idsecciones.get(0));
		//caminoTemp.idsecciones.remove(0);
		}else{
			finalizadoOvisitando.add(0);
		}
		id++;
	}
	
		
	public void agregaNuevoCaminoCopiando(int posCaminoAcopiarSecciones,int idSeccion,int vertB){
		Camino caminoTemp = new Camino();
		caminoTemp.idcamino=id;	
		Iterator iteradorIDsecciones = ((Camino)caminosFinal.get(posCaminoAcopiarSecciones)).idsecciones.iterator();
		while(iteradorIDsecciones.hasNext()){
			caminoTemp.idsecciones.add((int)iteradorIDsecciones.next());
		}
		caminoTemp.idcliente=((Camino)caminosFinal.get(posCaminoAcopiarSecciones)).idcliente;
		int vertA=getUltimoVerticeDelCamino(posCaminoAcopiarSecciones);
		caminoTemp.idsecciones.add(this.secciones.agregaSeccion(idSeccion, 0, 0, vertA, vertB));
		caminosFinal.add(caminoTemp);
		if(vertA == 0 || vertB == 0){
		finalizadoOvisitando.add(-1);
		//secciones.borraSeccion(caminoTemp.idsecciones.get(0));
		//caminoTemp.idsecciones.remove(0);
		}else{
			finalizadoOvisitando.add(0);
		}
		id++;
	}
	
	public void borraCamino(int posCaminoAborrar){
		//busca camino con id caminoAborrar.id
		finalizadoOvisitando.set(posCaminoAborrar, -2);
		//caminosFinal.remove(posCaminoAborrar);
		//finalizadoOvisitando.remove(posCaminoAborrar);
		//caminosFinal.trimToSize();
		//finalizadoOvisitando.trimToSize();
	}
	
	public int getUltimoCaminoNoFinalizadoNiVisitando(){
		boolean encontradoUno=false;
		int i=finalizadoOvisitando.size()-1;
		int uno=1;
		 while(!encontradoUno&&i>=0){
			if(((int)finalizadoOvisitando.get(i))==0){
				encontradoUno=true;
				finalizadoOvisitando.set(i, uno);
			}else if(i>0&&((Camino)caminosFinal.get(i)).idcliente!=((Camino)caminosFinal.get(i-1)).idcliente){
				i=0;
			  }else{
			i--;
			}
		 }
        if(!encontradoUno) i=-1;
		return i;
	}
	
	public int getUltimoVerticeDelCamino(int id){
		int seccionUltimaVertA= secciones.getVertASeccionID((int)((Camino)caminosFinal.get(id)).idsecciones.get(((Camino)caminosFinal.get(id)).idsecciones.size()-1));
	    int seccionUltimaVertB= secciones.getVertBSeccionID((int)((Camino)caminosFinal.get(id)).idsecciones.get(((Camino)caminosFinal.get(id)).idsecciones.size()-1));
		if(((Camino)caminosFinal.get(id)).idsecciones.size()>1){	    
	   int seccionPenUltimaVertB= secciones.getVertBSeccionID((int)((Camino)caminosFinal.get(id)).idsecciones.get(((Camino)caminosFinal.get(id)).idsecciones.size()-2));
	    if(seccionUltimaVertA == seccionPenUltimaVertB) return seccionUltimaVertB ;
	    else return seccionUltimaVertA;
		} else{
		return  seccionUltimaVertB;
		}
	}
	
	public int getUltimoIdClienteDeLaSeccion(int id){
		return secciones.getidSeccion((int)((Camino)caminosFinal.get(id)).idsecciones.get((((Camino)caminosFinal.get(id)).idsecciones.size()-1)));
	}
	
	/*
	public int getUltimoIdSeccionDelCamino(int idCamino){
		return caminosFinal.get(idCamino).getUltimoIdSeccion();
	}
	
	public int getUltimoVerticeVisitadoDelCamino(int idDelCamino){
		return caminosFinal.get(idDelCamino).getUltimoIdSeccion();
	}
	
	public int getdelCaminoYseccionElid(int idDelCamino,int idDelaSeccion){
		return caminosFinal.get(idDelCamino).getdelSeccionElid(idDelaSeccion);
	}
	*/
	public boolean isVerticeVisitadoDelCamino(int idDelCamino, int vertice){
		boolean visitada=false;
		int i=0;
		while(!visitada&&i<((Camino)caminosFinal.get(idDelCamino)).idsecciones.size()){
			if(secciones.getVertASeccionID((int)((Camino)caminosFinal.get(idDelCamino)).idsecciones.get(i))==vertice || secciones.getVertBSeccionID((int)((Camino)caminosFinal.get(idDelCamino)).idsecciones.get(i))==vertice){
				visitada=true;
			}
			i++;
		}
		return visitada;
	}
	
	
	
	
	public boolean isFinalizados(){
		boolean finalizados=true;
		int i=0;
		 while(finalizados&&i<finalizadoOvisitando.size()){
			if(((int)finalizadoOvisitando.get(i))!=-1&&((int)finalizadoOvisitando.get(i))!=-2){
				finalizados=false;
			}
			i++;
		 }
		return finalizados;
	}
	/*
	public int getUltimoCamino(){
		return (caminosFinal.size()-1);
	}
	*/
}
