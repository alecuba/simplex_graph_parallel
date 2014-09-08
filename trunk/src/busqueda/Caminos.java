package busqueda;

import java.util.ArrayList;
import java.util.Iterator;

import busqueda.Camino;
import busqueda.Secciones;

public class Caminos {

	public Secciones secciones;
	public ArrayList listaCaminos = new ArrayList();
	private long id=0;
	
	public Caminos(Secciones secciones){
		this.secciones=secciones;
	}
	
	public void pintaCaminosGenerados() {
		System.out.println(String.format("%-14s%-14s%-14s", "cliente",
				"idcamino", "secciones"));
		System.out.println(String.format("%s",
				"---------------+---------------+---------------"));
		Iterator itrCamino = listaCaminos.iterator();
		String camino;
		int numCaminos=0;
		while (itrCamino.hasNext()) {
			numCaminos++;
			camino="";
			Camino caminoTemp = (Camino)itrCamino.next();
			Iterator itrseccion = caminoTemp.idsecciones.iterator();
			while(itrseccion.hasNext()){
				if(camino.length()!=0) camino=camino+"-";
				IdSeccion seccion = (IdSeccion)itrseccion.next();
				camino=camino+secciones.getidSeccion(seccion.idseccion)+"("+seccion.sentido+")";
			}
			System.out.println(String.format("%-20s%-20s%-20s",caminoTemp.idcliente, caminoTemp.idcamino,camino));
		}
		System.out.println("Numero Caminos total:"+numCaminos);
	}
			
	public void agregaNuevoCamino(Camino caminoAcopiarSecciones,int idSeccion,int vertB){
		Camino caminoTemp = new Camino();
		caminoTemp.idcamino=id;	
		Iterator iteradorIDsecciones = caminoAcopiarSecciones.idsecciones.iterator();
		while(iteradorIDsecciones.hasNext()){
			caminoTemp.idsecciones.add(iteradorIDsecciones.next());
		}
		caminoTemp.idcliente=caminoAcopiarSecciones.idcliente;
		int vertA=getUltimoVerticeDelCamino(caminoAcopiarSecciones);
		int posSecciones[]=this.secciones.agregaSeccion(idSeccion, 0, 0, vertA, vertB);
		IdSeccion seccion = new IdSeccion((int)posSecciones[0],(int)posSecciones[1]);		
		caminoTemp.idsecciones.add(seccion);
		if(vertA == 0 || vertB == 0){
			caminoTemp.finalizadoOvisitando=-1;
		}else{
			caminoTemp.finalizadoOvisitando=0;
		}
		listaCaminos.add(caminoTemp);
		id++;
	}
	
	public void agregaNuevoCamino(int idcliente, int verticeInicial,int idSeccion,int vertB){
		Camino caminoTemp = new Camino();
		caminoTemp.idcamino=id;	
		caminoTemp.idcliente=idcliente;
		int posSecciones[]=this.secciones.agregaSeccion(idSeccion, 0, 0, verticeInicial, vertB);
		IdSeccion seccion = new IdSeccion((int)posSecciones[0],(int)posSecciones[1]);		
		caminoTemp.idsecciones.add(seccion);
		if(verticeInicial == 0 || vertB == 0){
			caminoTemp.finalizadoOvisitando=-1;
		}else{
			caminoTemp.finalizadoOvisitando=0;
		}
		listaCaminos.add(caminoTemp);
		id++;
	}
	
	public void borraCamino(Camino posUltimoCamino){
		listaCaminos.remove(posUltimoCamino);
		//listaCaminos.trimToSize();
	}
	
	public void limpia(Secciones secciones){
		secciones.limpia();
		listaCaminos = new ArrayList();
		id=0;
	}

	public Camino getUltimoCaminoNoFinalizadoNiVisitando(){
		//System.out.println("Entrada getUltimoCaminoNoFinalizadoNiVisitando");
		boolean encontradoUno=false;
		int i=listaCaminos.size();
		int micopiai;
		Camino caminoFinal = null;
			while(!encontradoUno&&i>0){
				/*//omp parallel shared(i,encontradoUno,caminoFinal) private(micopiai)*/
				//{
					/*//omp critical getUltimoCaminoNoFinalizadoNiVisitando*/
					//{
						i--;
						micopiai=i;
					//}
					Camino caminoActual=(Camino)listaCaminos.get(micopiai);
				if(caminoActual != null && caminoActual.finalizadoOvisitando==0){
					
					/*//omp critical getUltimoCaminoNoFinalizadoNiVisitando */
						//{
							encontradoUno=true;
							 caminoFinal=caminoActual;
							 caminoActual.finalizadoOvisitando=1;
						//}
					
				}else if(micopiai>0){
					Camino caminoMenos1 = ((Camino)listaCaminos.get(micopiai-1));
					if(caminoMenos1!=null&&caminoActual.idcliente!=caminoMenos1.idcliente){
						/*//omp critical getUltimoCaminoNoFinalizadoNiVisitando*/ 
							//{
								i=-1;
							//}
						}
				}				
				//}
			}
	   if(!encontradoUno) caminoFinal=null;
	   //System.out.println("Thread num:"+jomp.runtime.OMP.getThreadNum()+" idcamino:"+caminoFinal.idcamino);
	   //System.out.println("Sale getUltimoCaminoNoFinalizadoNiVisitando");
	   return caminoFinal;
	}
	
	public int getUltimoVerticeDelCamino(Camino posUltimoCamino){
		int posSeccion[]={(int)((IdSeccion)posUltimoCamino.idsecciones.get(posUltimoCamino.idsecciones.size()-1)).idseccion,(int)((IdSeccion)posUltimoCamino.idsecciones.get(posUltimoCamino.idsecciones.size()-1)).sentido};
		int seccionUltimaVertA,seccionUltimaVertB;
		if(posSeccion[1]==0){
		 seccionUltimaVertA= secciones.getVertASeccionID(posSeccion[0]);
	     seccionUltimaVertB= secciones.getVertBSeccionID(posSeccion[0]);
		}else{
			seccionUltimaVertA= secciones.getVertBSeccionID(posSeccion[0]);
		    seccionUltimaVertB= secciones.getVertASeccionID(posSeccion[0]);
		}
	    if(seccionUltimaVertA!=seccionUltimaVertB){
		if(posUltimoCamino.idsecciones.size()>1){
			int seccionPenUltimaVertB;
			int posSeccionMenos2[]={(int)((IdSeccion)posUltimoCamino.idsecciones.get(posUltimoCamino.idsecciones.size()-2)).idseccion,(int)((IdSeccion)posUltimoCamino.idsecciones.get(posUltimoCamino.idsecciones.size()-2)).sentido};
			if(posSeccionMenos2[1]==0){
				seccionPenUltimaVertB= secciones.getVertBSeccionID(posSeccionMenos2[0]);
			}else{
				seccionPenUltimaVertB= secciones.getVertASeccionID(posSeccionMenos2[0]);
			}
	    if(seccionUltimaVertA == seccionPenUltimaVertB) return seccionUltimaVertB ;
	    else return seccionUltimaVertA;
		} else{
		return  seccionUltimaVertB;
		}
		}else{return -1;}
	}
	
	public boolean isVerticeVisitadoDelCamino(Camino ultimoCamino, int vertice){
		//System.out.println("Entrada isVerticeVisitadoDelCamino");
		boolean visitada=false;
		int i=-1;
		int micopiai;
		if(ultimoCamino!=null){
			while(!visitada&&(i+1)<ultimoCamino.idsecciones.size()){
				/*//omp parallel shared(i,visitada,vertice) private(micopiai)*/
				//{
					/*//omp critical isVerticeVisitadoDelCamino*/
					//{
						i++;
						micopiai=i;
					//}
				if(micopiai<ultimoCamino.idsecciones.size()&&secciones.getVertASeccionID((int)((IdSeccion)ultimoCamino.idsecciones.get(micopiai)).idseccion)==vertice || secciones.getVertBSeccionID((int)((IdSeccion)ultimoCamino.idsecciones.get(micopiai)).idseccion)==vertice){
					/*//omp critical isVerticeVisitadoDelCamino*/
					//{
					visitada=true;
					//}
				//}
				}
			}
		}
		//System.out.println("Sale isVerticeVisitadoDelCamino");
		return visitada;
	}
	
	public boolean isFinalizados(){
		//System.out.println("Entrada isVerticeVisitadoDelCamino");
		boolean finalizados=true;
		int i=-1;
		int micopiai;
		while(finalizados&&i<listaCaminos.size()){
			/*//omp parallel shared(i,finalizados) private(micopiai)*/
				//{
					/*//omp critical isFinalizados*/
					//{
						i++;
						micopiai=i;
					//}
			if(micopiai<listaCaminos.size()&&((Camino)listaCaminos.get(micopiai))!=null&&((Camino)listaCaminos.get(micopiai)).finalizadoOvisitando!=-1){
				/*//omp critical isFinalizados*/
				//{
					finalizados=false;
				//}
				}
			
			//}
		 }
		//System.out.println("Sale isVerticeVisitadoDelCamino");
		return finalizados;
	}}

