package busqueda;

import java.util.ArrayList;
import java.util.Iterator;

import busqueda.Caminos.Camino;

public class Caminos {


		
	public class Camino {

		public int idcliente;
		public long idcamino;
		public ArrayList idsecciones = new ArrayList();
		public int finalizadoOvisitando=0;//-1 finalizado , 1 visitandose , 0 sin visitar
		
		public void Camino(){
			
		}}


	
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
			itrseccion.next();
			while(itrseccion.hasNext()){
				if(camino.length()!=0) camino=camino+"-";
				camino=camino+secciones.getidSeccion((int)itrseccion.next());
			}
			System.out.println(String.format("%-20s%-20s%-20s",caminoTemp.idcliente, caminoTemp.idcamino,camino));
		}
		System.out.println("Numero Caminos total:"+numCaminos);
	}
	
	
	public void agregaNuevoCamino(int idcliente,int idSeccion ,int vertA,int vertB){
		Camino caminoTemp = new Camino();
		caminoTemp.idcamino=id;	
		caminoTemp.idcliente=idcliente;
		caminoTemp.idsecciones.add(this.secciones.agregaSeccion(idSeccion, 0, 0, vertA, vertB));
		if(vertA == 0 || vertB == 0){
			caminoTemp.finalizadoOvisitando=-1;
		//secciones.borraSeccion(caminoTemp.idsecciones.get(0));
		//caminoTemp.idsecciones.remove(0);
		}else{
			caminoTemp.finalizadoOvisitando=0;
		}
		listaCaminos.add(caminoTemp);
		id++;
	}
	
		
	public void agregaNuevoCaminoCopiando(Camino caminoAcopiarSecciones,int idSeccion,int vertB){
		Camino caminoTemp = new Camino();
		caminoTemp.idcamino=id;	
		Iterator iteradorIDsecciones = caminoAcopiarSecciones.idsecciones.iterator();
		while(iteradorIDsecciones.hasNext()){
			caminoTemp.idsecciones.add((int)iteradorIDsecciones.next());
		}
		caminoTemp.idcliente=caminoAcopiarSecciones.idcliente;
		int vertA=getUltimoVerticeDelCamino(caminoAcopiarSecciones);
		caminoTemp.idsecciones.add(this.secciones.agregaSeccion(idSeccion, 0, 0, vertA, vertB));
		if(vertA == 0 || vertB == 0){
			caminoTemp.finalizadoOvisitando=-1;
		//secciones.borraSeccion(caminoTemp.idsecciones.get(0));
		//caminoTemp.idsecciones.remove(0);
		}else{
			caminoTemp.finalizadoOvisitando=0;
		}
		listaCaminos.add(caminoTemp);
		id++;
	}
	
	public void borraCamino(Camino posUltimoCamino){
		listaCaminos.remove(posUltimoCamino);
		listaCaminos.trimToSize();
	}
	
	
	public Camino getUltimoCaminoNoFinalizadoNiVisitando(){
		boolean encontradoUno=false;
		int i=-1;
		Camino caminoFinal = null;
		if(listaCaminos.size()>0){
			i=listaCaminos.size()-1;
			while(!encontradoUno && i>=0){

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.i = i;
  __omp_Object0.encontradoUno = encontradoUno;
  __omp_Object0.caminoFinal = caminoFinal;
  __omp_Object0.secciones = secciones;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object0);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  i = __omp_Object0.i;
  encontradoUno = __omp_Object0.encontradoUno;
  caminoFinal = __omp_Object0.caminoFinal;
  secciones = __omp_Object0.secciones;
}
// OMP PARALLEL BLOCK ENDS

			}
       }
	   if(!encontradoUno) caminoFinal=null;
	   return caminoFinal;
	}
	
	public int getUltimoVerticeDelCamino(Camino posUltimoCamino){
		int pos = listaCaminos.indexOf(posUltimoCamino);
		if(pos!=-1){
		int seccionUltimaVertA= secciones.getVertASeccionID((int)((Camino)listaCaminos.get(pos)).idsecciones.get(((Camino)listaCaminos.get(pos)).idsecciones.size()-1));
	    int seccionUltimaVertB= secciones.getVertBSeccionID((int)((Camino)listaCaminos.get(pos)).idsecciones.get(((Camino)listaCaminos.get(pos)).idsecciones.size()-1));
	    if(seccionUltimaVertA!=seccionUltimaVertB){
		if(((Camino)listaCaminos.get(pos)).idsecciones.size()>1){	    
	   int seccionPenUltimaVertB= secciones.getVertBSeccionID((int)((Camino)listaCaminos.get(pos)).idsecciones.get(((Camino)listaCaminos.get(pos)).idsecciones.size()-2));
	    if(seccionUltimaVertA == seccionPenUltimaVertB) return seccionUltimaVertB ;
	    else return seccionUltimaVertA;
		} else{
		return  seccionUltimaVertB;
		}
		}else{return -1;}
		}else{return -1;}
	}
	
	public int getUltimoIdClienteDeLaSeccion(int id){
		return secciones.getidSeccion((int)((Camino)listaCaminos.get(id)).idsecciones.get((((Camino)listaCaminos.get(id)).idsecciones.size()-1)));
	}
	
	public boolean isVerticeVisitadoDelCamino(Camino ultimoCamino, int vertice){
		boolean visitada=false;
		int i=0;
		int pos = listaCaminos.indexOf(ultimoCamino);
		while(!visitada&&pos!=-1&&i<((Camino)listaCaminos.get(pos)).idsecciones.size()){
			if(secciones.getVertASeccionID((int)((Camino)listaCaminos.get(pos)).idsecciones.get(i))==vertice || secciones.getVertBSeccionID((int)((Camino)listaCaminos.get(pos)).idsecciones.get(i))==vertice){
				visitada=true;
			}
			i++;
		}
		return visitada;
	}
	
	public boolean isFinalizados(){
		boolean finalizados=true;
		int i=0;
		 while(finalizados&&i<listaCaminos.size()){
			if(((Camino)listaCaminos.get(i))!=null&&((Camino)listaCaminos.get(i)).finalizadoOvisitando!=-1){
				finalizados=false;
			}
			i++;
		 }
		return finalizados;
	}

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  int i;
  boolean encontradoUno;
  Camino caminoFinal;
  Secciones secciones;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

			   {
				Camino caminoActual=(Camino)listaCaminos.get(i);
				if(caminoActual != null && caminoActual.finalizadoOvisitando==0){
                                                                         // OMP CRITICAL BLOCK BEGINS
                                                                         synchronized (jomp.runtime.OMP.getLockByName("encontrado")) {
                                                                         // OMP USER CODE BEGINS

									{
									 caminoFinal=caminoActual;
									 encontradoUno=true;
									((Camino)listaCaminos.get(i)).finalizadoOvisitando=1;
									}
                                                                         // OMP USER CODE ENDS
                                                                         }
                                                                         // OMP CRITICAL BLOCK ENDS

						}else if(i>0){
								Camino caminoMenos1 = ((Camino)listaCaminos.get(i-1));
								if(caminoMenos1!=null&&caminoActual.idcliente!=caminoMenos1.idcliente){
                                                                         // OMP CRITICAL BLOCK BEGINS
                                                                         synchronized (jomp.runtime.OMP.getLockByName("encontrado")) {
                                                                         // OMP USER CODE BEGINS

										{
											i=0;
										}
                                                                         // OMP USER CODE ENDS
                                                                         }
                                                                         // OMP CRITICAL BLOCK ENDS

									}
						}
                                 // OMP CRITICAL BLOCK BEGINS
                                 synchronized (jomp.runtime.OMP.getLockByName("encontrado")) {
                                 // OMP USER CODE BEGINS

				{
					i--;
				}
                                 // OMP USER CODE ENDS
                                 }
                                 // OMP CRITICAL BLOCK ENDS

			   }
    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS

}


