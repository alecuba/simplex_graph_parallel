package generador_grafo;

public class ArbolVisitados {
	private Nodo arbolVisitados=null;
	private boolean debug=false;
	
	ArbolVisitados(boolean debug){
		this.debug=debug;		
	}
	
	public Nodo getVisitados(){
		return this.arbolVisitados;
	}
	 static class Nodo {
		    Nodo izquierda;
		    Nodo derecha;
		    int valor;
		    public Nodo(int valor) {
		      this.valor = valor;
		    }
		  }
	 
	 	public void insertar(int valor){
	 		if(arbolVisitados==null){
	 			arbolVisitados = new Nodo(valor);
	 		}else{
	 		insertarRec(arbolVisitados,valor);
	 		}
	 	}

		 private void insertarRec(Nodo nodo, int valor) {
		    if (valor < nodo.valor) {
		      if (nodo.izquierda != null) {
		    	  insertarRec(nodo.izquierda, valor);
		      } else {
		        if(debug) System.out.println("  Insertado " + valor + " izquierda de "+ nodo.valor);
		        nodo.izquierda = new Nodo(valor);
		      }
		    } else if (valor > nodo.valor) {
		      if (nodo.derecha != null) {
		    	  insertarRec(nodo.derecha, valor);
		      } else {
		    	  if(debug) System.out.println("  Insertado " + valor + " derecha de "+ nodo.valor);
		        nodo.derecha = new Nodo(valor);
		      }
		    }
		  }
		  
		  private boolean visitadoRec(Nodo nodo,int i){
			  boolean visitado=false;
			  if(nodo!=null){
			  if(i!=nodo.valor){
				if(i<nodo.valor){
			      visitado=visitadoRec(nodo.izquierda,i);
			    }else{
				  visitado=visitadoRec(nodo.derecha,i);  
			  }
			  }else{visitado=true;}
			  }
			  return visitado;
		  }
		  
		  public boolean isVisitado(int i){
			  boolean visitado=false;
			  if(arbolVisitados!=null){
				 visitado=visitadoRec(arbolVisitados,i);
			  }
			return visitado;
		  }
}
