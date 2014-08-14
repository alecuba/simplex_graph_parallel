package generador_grafo;
import java.io.PrintStream;
import java.util.Random;

public class GenerarClientes {
	int MIN_NSECCIONES = 6;
	int MAX_NSECCIONES = 10;
	int MIN_CRUCES=1;
	int MAX_CRUCES=3;
	static int[][] tablaGrafo;
	int secciones;
	private Node visitados=null;
	static Random rand = new Random();
    private boolean debug=false;
	private int algoritmo;
	
    
    
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
		 case 2:algoritmoGeneraGrafo1();
		 break;
		 default: algoritmoGeneraGrafo1();
		 break;
		 }
		preparaEntradaGrafo();
	}
    
	private void algoritmoGeneraGrafo1() {
		// TODO Auto-generated method stub
		
	}

	private static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	private void inicializaTabla(){
	  for(int i=0;i<secciones;i++){
		  for(int j=0;j<secciones;j++){
			  if(i!=0 && j!=0){
				  tablaGrafo[i][j]=0;
		  }
		  }
	  }
	}
	
	GenerarClientes(){
		 this.debug=debug;
		 switch(algoritmo){
		 case 1:generarGrafo1();
		 break;
		 case 2:generarGrafo2();
		 break;
		 default: generarGrafo1();
		 break;
		 }
		}
	
	private void generarGrafo1(){
	}
	
	private void generarGrafo2(){
		int nsecciones=0;
		int ncruces_generados;
		int i,j;
		int ncruces_minActual;
		secciones=randInt(MIN_NSECCIONES,MAX_NSECCIONES);
		if(debug) System.out.print("Secciones("+secciones+") \n");
		tablaGrafo=new int[secciones][secciones];
		inicializaTabla();
		while(nsecciones<secciones){
			i=randInt(0,secciones-1);
			if(visitados == null){
				visitados=new Node(i);
			}else if(!visitado(visitados,i)){
			insert(visitados, i,debug);
			j=randInt(0,secciones-1);
			ncruces_generados=0;
			ncruces_minActual=randInt(MIN_CRUCES,MAX_CRUCES);
				while((
						(ncruces_generados <= ncruces_minActual) 
						&& (ncruces_generados<=MAX_CRUCES) 
						&& (nsecciones<secciones)) 
						|| ((ncruces_generados<=MAX_CRUCES) 
								&& (nsecciones<secciones))){
					if(debug) System.out.print("(ncruces_generados<=ncruces_maxActual):"+(ncruces_generados<=MAX_CRUCES)+" (nsecciones<secciones):"+(nsecciones<secciones)+" (ncruces_generados < MIN_CRUCES):"+(ncruces_generados < MIN_CRUCES)+" i("+i+") j("+j+") ncruces_generados("+ncruces_generados+") nsecciones("+nsecciones+") ncruces_minActual("+ncruces_minActual+") secciones("+secciones+")");
					if(i!=j){
						tablaGrafo[i][j]=tablaGrafo[j][i]=randInt(0,1);
						if(tablaGrafo[i][j]==1){
						ncruces_generados++;
						nsecciones++;
						}
					
					}
					j=randInt(0,secciones-1);
					if(debug) System.out.print(" newj("+j+")\n"); 
				}
			}
		}
	}
	
	
	private int numlength(int n)
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
		 for(int i=0;i<secciones;i++){
			  for(int j=0;j<secciones;j++){
				if(j==0 && numlength(secciones)!=numlength(i) ){
					for(int n=0;n<(numlength(secciones)-numlength(j));n++){
						if(debug)System.out.print(" ");
					}
				}
				//System.out.println("j("+j+") numlength("+numlength(j)+")");
				if(debug)System.out.print(tablaGrafo[i][j]);
				if(i!=0 && numlength(secciones)>1) {
				  if(numlength(secciones)==numlength(j)){
					for(int n=0;n<=(numlength(secciones)-numlength(j));n++){
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
		
		for(int i=0;i<secciones;i++){
			if(debug && i==0){
				if(numlength(secciones)>1) {System.out.print("    ");}else{System.out.print("   ");}
				  for(int j=0;j<secciones;j++){
					System.out.print(j);
					if(j<secciones-1){
					System.out.print("  ");
					}
				  }
				  System.out.print("\n");				
			}
			 
			  for(int j=0;j<secciones;j++){
				if(j==0){
					if(debug){
					if(numlength(secciones)>1) {
						  if(numlength(secciones)>numlength(i)){
							for(int n=0;n<(numlength(secciones)-numlength(j));n++){
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
				if(numlength(secciones)>1) {
					  if(numlength(secciones)==numlength(j)){
						for(int n=0;n<=(numlength(secciones)-numlength(j));n++){
							System.out.print(" ");
						}
					  }
					  System.out.print(" ");
					}
				
				}
				
				System.out.print(preComa+tablaGrafo[i][j]);			
			  }
			  System.out.print("]");
			  if(i<secciones-1){
			  System.out.print(",\n");
			  }else{System.out.print("\n");}
		  }
		System.out.print("\n");
	}
	
	public Node getVisitados(){
		return this.visitados;
	}
	
	public void preparaEntradaGrafo(){
		boolean entrada=false;
		int j=0;
		while(j<secciones && !entrada){
		  if(tablaGrafo[0][j]==1){
			  entrada=true;
			}
			j++;
		}
		if(!entrada){
		  j=randInt(0,secciones-1);
		  if(debug){System.out.print("No habia entrada, se ha creado una en [0]["+j+"]\n");}
		  tablaGrafo[0][j]=1;
		  tablaGrafo[j][0]=1;
		}
		
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
		grafo.preparaEntradaGrafo();
		System.setOut(original);		
		//grafo.pintaTabla();
		grafo.pintaTabla2();
		gestorCassandra cas = new gestorCassandra();
		cas.prepara();
		cas.insertDataFromAdjacentTable(tablaGrafo);
		cas.pinta();
		cas.close();
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