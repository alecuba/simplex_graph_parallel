package generador_grafo;

import java.io.PrintStream;
import java.util.Random;

public class GenerarGrafo {
	private int minSecciones = 6;
	private int maxSecciones = 10;
	private int minCruces=1;
	private int maxCruces=3;
	private int[][] tablaGrafo;
	private int seccionesActual;
	private Node verticesVisitados=null;
	private Random rand = new Random();
    private boolean debug=false;
	
	private int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	private void inicializaTabla(){
	  for(int i=0;i<seccionesActual;i++){
		  for(int j=0;j<seccionesActual;j++){
			  if(i!=0 && j!=0){
				  tablaGrafo[i][j]=0;
		  }
		  }
	  }
	}
	
	GenerarGrafo(){
		}
	
	public void setAlgoritmo(int algoritmo){
		if(algoritmo<=2 || algoritmo>=1){
			this.algoritmo=algoritmo;
		}
		
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;	
	}
	
	public void generar(){
		switch(algoritmo){
		 case 1:algoritmoGeneraGrafo1();
		 break;
		 case 2:algoritmoGeneraGrafo2();
		 break;
		 default: algoritmoGeneraGrafo1();
		 break;
		 }
		preparaEntradaGrafo();
	}
	
	private int algoritmo=2;
	
	private void algoritmoGeneraGrafo1(){
		int nsecciones=0;
		int ncruces_generados;
		int j;
		int ncruces_minActual;
		seccionesActual=randInt(minSecciones,maxSecciones);
		if(debug)System.out.print("Secciones("+seccionesActual+") \n");
		
		tablaGrafo=new int[seccionesActual+1][seccionesActual+1];
		inicializaTabla();
		for(int i=1;i<seccionesActual;i++){
			ncruces_generados=0;
			j=i+1;
			ncruces_minActual=randInt(minCruces,maxCruces);
				while(((ncruces_generados < ncruces_minActual) && (ncruces_generados<=maxCruces) && (nsecciones<seccionesActual)) || ((ncruces_generados<=maxCruces) && (nsecciones<seccionesActual))){
					
					if(debug)System.out.print("(ncruces_generados<=ncruces_maxActual):"+(ncruces_generados<=maxCruces)+" (nsecciones<secciones):"+(nsecciones<seccionesActual)+" (ncruces_generados < MIN_CRUCES):"+(ncruces_generados < minCruces)+" i("+i+") j("+j+") ncruces_generados("+ncruces_generados+") nsecciones("+nsecciones+") ncruces_minActual("+ncruces_minActual+") secciones("+seccionesActual+")");
					if(j>seccionesActual){j=1;}else{
					if(tablaGrafo[i][j] != 1 && i!=j){
						tablaGrafo[i][j]=tablaGrafo[j][i]=randInt(0,1);
						if(tablaGrafo[i][j]==1){
						ncruces_generados++;
						nsecciones++;
						}
					}
					
					j++;
					}
					if(debug)System.out.print(" newj("+j+")\n"); 
					
				}				
			}
		}
	
	private void algoritmoGeneraGrafo2(){
		int nsecciones=0;
		int ncruces_generados;
		int i,j;
		int ncruces_minActual;
		seccionesActual=randInt(minSecciones,maxSecciones);
		if(debug) System.out.print("Secciones("+seccionesActual+") \n");
		tablaGrafo=new int[seccionesActual][seccionesActual];
		inicializaTabla();
		while(nsecciones<seccionesActual){
			i=randInt(0,seccionesActual-1);
			if(verticesVisitados == null){
				verticesVisitados=new Node(i);
			}else if(!visitado(verticesVisitados,i)){
			insert(verticesVisitados, i,debug);
			j=randInt(0,seccionesActual-1);
			ncruces_generados=0;
			ncruces_minActual=randInt(minCruces,maxCruces);
				while((
						(ncruces_generados <= ncruces_minActual) 
						&& (ncruces_generados<=maxCruces) 
						&& (nsecciones<seccionesActual)) 
						|| ((ncruces_generados<=maxCruces) 
								&& (nsecciones<seccionesActual))){
					if(debug) System.out.print("(ncruces_generados<=ncruces_maxActual):"+(ncruces_generados<=maxCruces)+" (nsecciones<secciones):"+(nsecciones<seccionesActual)+" (ncruces_generados < MIN_CRUCES):"+(ncruces_generados < minCruces)+" i("+i+") j("+j+") ncruces_generados("+ncruces_generados+") nsecciones("+nsecciones+") ncruces_minActual("+ncruces_minActual+") secciones("+seccionesActual+")");
					if(i!=j){
						tablaGrafo[i][j]=tablaGrafo[j][i]=randInt(0,1);
						if(tablaGrafo[i][j]==1){
						ncruces_generados++;
						nsecciones++;
						}
					
					}
					j=randInt(0,seccionesActual-1);
					if(debug) System.out.print(" newj("+j+")\n"); 
				}
			}
		}
	}
	
	
	private int logitudNumero(int n)
	{
		int l;
		if(n>0){
		n=Math.abs(n);
		for (l=0;n>0;++l)
			n/=10;
		}else{
			l=1;
		}
		return l;			
	}
	
	void pintaTabla(){
		if(debug)System.out.println("Tabla");
		 for(int i=0;i<seccionesActual;i++){
			  for(int j=0;j<seccionesActual;j++){
				if(j==0 && logitudNumero(seccionesActual)!=logitudNumero(i) ){
					for(int n=0;n<(logitudNumero(seccionesActual)-logitudNumero(j));n++){
						if(debug)System.out.print(" ");
					}
				}
				//System.out.println("j("+j+") numlength("+numlength(j)+")");
				if(debug)System.out.print(tablaGrafo[i][j]);
				if(i!=0 && logitudNumero(seccionesActual)>1) {
				  if(logitudNumero(seccionesActual)==logitudNumero(j)){
					for(int n=0;n<=(logitudNumero(seccionesActual)-logitudNumero(j));n++){
						if(debug)System.out.print(" ");
					}
				  }
				}
				if(debug)System.out.print(" ");
			  }
			  if(debug)System.out.print("\n");
		  }
	}
	void pintaTabla2(){
		if(debug)System.out.println("\nTabla");
		String preComa="";
		
		for(int i=0;i<seccionesActual;i++){
			if(debug && i==0){
				if(logitudNumero(seccionesActual)>1) {System.out.print("    ");}else{System.out.print("   ");}
				  for(int j=0;j<seccionesActual;j++){
					System.out.print(j);
					if(j<seccionesActual-1){
					System.out.print("  ");
					}
				  }
				  System.out.print("\n");				
			}
			 
			  for(int j=0;j<seccionesActual;j++){
				if(j==0){
					if(debug){
					if(logitudNumero(seccionesActual)>1) {
						  if(logitudNumero(seccionesActual)>logitudNumero(i)){
							for(int n=0;n<(logitudNumero(seccionesActual)-logitudNumero(j));n++){
								System.out.print(" ");
							}
						  }
						}
				  preComa=i+" [";
				  }else{preComa="[";}
				}else{
				  preComa=",";
				}
				if(j>0 && debug){
				if(logitudNumero(seccionesActual)>1) {
					  if(logitudNumero(seccionesActual)==logitudNumero(j)){
						for(int n=0;n<=(logitudNumero(seccionesActual)-logitudNumero(j));n++){
							System.out.print(" ");
						}
					  }
					  System.out.print(" ");
					}
				
				}
				
				System.out.print(preComa+tablaGrafo[i][j]);			
			  }
			  System.out.print("]");
			  if(i<seccionesActual-1){
			  System.out.print(",\n");
			  }else{System.out.print("\n");}
		  }
		System.out.print("\n");
	}
	
	public Node getVisitados(){
		return this.verticesVisitados;
	}
	
	public void preparaEntradaGrafo(){
		boolean entrada=false;
		int j=0;
		while(j<seccionesActual && !entrada){
		  if(tablaGrafo[0][j]==1){
			  entrada=true;
			}
			j++;
		}
		if(!entrada){
		  j=randInt(0,seccionesActual-1);
		  if(debug){System.out.print("No habia entrada, se ha creado una en [0]["+j+"]\n");}
		  tablaGrafo[0][j]=1;
		  tablaGrafo[j][0]=1;
		}
		
	}

	public int[][] getTablaGrafo(){
		return tablaGrafo;
		
	}
	
	public static void main(String[] args) {
		boolean debug = true;
		PrintStream original = System.out;
		/*
		try {
			File file = new File("mitabla.txt");  
		FileOutputStream fis = null;
			fis = new FileOutputStream(file);
			PrintStream out = new PrintStream(fis); 
			System.setOut(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		GenerarGrafo grafo = new GenerarGrafo();
		System.setOut(original);		
		//grafo.pintaTabla();
		grafo.pintaTabla2();
		
  }
   
  static class Node {
    Node left;

    Node right;

    int value;

    public Node(int value) {
      this.value = value;
    }
  }

  private void insert(Node node, int value,boolean debug) {
    if (value < node.value) {
      if (node.left != null) {
        insert(node.left, value,debug);
      } else {
        if(debug) System.out.println("  Inserted " + value + " to left of "+ node.value);
        node.left = new Node(value);
      }
    } else if (value > node.value) {
      if (node.right != null) {
        insert(node.right, value,debug);
      } else {
    	  if(debug) System.out.println("  Inserted " + value + " to right of "+ node.value);
        node.right = new Node(value);
      }
    }
  }
  
  private boolean visitadoRec(Node node,int i){
	  boolean visitado=false;
	  if(node!=null){
	  if(i!=node.value){
		if(i<node.value){
	      visitado=visitadoRec(node.left,i);
	    }else{
		  visitado=visitadoRec(node.right,i);  
	  }
	  }else{visitado=true;}
	  }
	  return visitado;
  }
  
  private boolean visitado(Node node,int i){
	  boolean visitado = false;
	  visitado=visitadoRec(node,i);
	return visitado;
  }
}

