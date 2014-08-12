package generador_grafo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;

public class GenerarGrafo {
	int MIN_NSECCIONES = 11;
	int MAX_NSECCIONES = 40;
	int MIN_CRUCES=1;
	int MAX_CRUCES=3;
	int[][] tablaGrafo;
	int secciones;
	private Node visitados=null;
	static Random rand = new Random();
	
	private static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	private void inicializaTabla(){
	  for(int i=0;i<secciones+1;i++){
		  for(int j=0;j<secciones+1;j++){
			  if(i!=0 && j!=0){
				  tablaGrafo[i][j]=0;
			  }else if(i==0){
				  tablaGrafo[i][j]=j;
			  	} else {
			  			tablaGrafo[i][j]=i;
			  			}
		  }
	  }
	}
	
	GenerarGrafo(int algoritmo){
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
		int nsecciones=0;
		int ncruces_generados;
		int j;
		int ncruces_minActual;
		secciones=randInt(MIN_NSECCIONES,MAX_NSECCIONES);
		System.out.print("Secciones("+secciones+") \n");
		
		tablaGrafo=new int[secciones+1][secciones+1];
		inicializaTabla();
		for(int i=1;i<secciones;i++){
			ncruces_generados=0;
			j=i+1;
			ncruces_minActual=randInt(MIN_CRUCES,MAX_CRUCES);
				while(((ncruces_generados < ncruces_minActual) && (ncruces_generados<=MAX_CRUCES) && (nsecciones<secciones)) || ((ncruces_generados<=MAX_CRUCES) && (nsecciones<secciones))){
					
					System.out.print("(ncruces_generados<=ncruces_maxActual):"+(ncruces_generados<=MAX_CRUCES)+" (nsecciones<secciones):"+(nsecciones<secciones)+" (ncruces_generados < MIN_CRUCES):"+(ncruces_generados < MIN_CRUCES)+" i("+i+") j("+j+") ncruces_generados("+ncruces_generados+") nsecciones("+nsecciones+") ncruces_minActual("+ncruces_minActual+") secciones("+secciones+")");
					if(j>secciones){j=1;}else{
					if(tablaGrafo[i][j] != 1 && i!=j){
						tablaGrafo[i][j]=tablaGrafo[j][i]=randInt(0,1);
						if(tablaGrafo[i][j]==1){
						ncruces_generados++;
						nsecciones++;
						}
					}
					
					j++;
					}
					System.out.print(" newj("+j+")\n"); 
					
				}				
			}
		}
	
	private void generarGrafo2(){
		int nsecciones=0;
		int ncruces_generados;
		int i,j;
		int ncruces_minActual;
		secciones=randInt(MIN_NSECCIONES,MAX_NSECCIONES);
		System.out.print("Secciones("+secciones+") \n");
		tablaGrafo=new int[secciones+1][secciones+1];
		inicializaTabla();
		while(nsecciones<=secciones){
			i=randInt(1,secciones);
			if(visitados == null){
				visitados=new Node(i);
			}else if(!visitado(visitados,i)){
			insert(visitados, i);
			j=randInt(1,secciones);
			ncruces_generados=0;
			ncruces_minActual=randInt(MIN_CRUCES,MAX_CRUCES);
				while((
						(ncruces_generados <= ncruces_minActual) 
						&& (ncruces_generados<=MAX_CRUCES) 
						&& (nsecciones<=secciones)) 
						|| ((ncruces_generados<=MAX_CRUCES) 
								&& (nsecciones<=secciones))){
					System.out.print("(ncruces_generados<=ncruces_maxActual):"+(ncruces_generados<=MAX_CRUCES)+" (nsecciones<secciones):"+(nsecciones<secciones)+" (ncruces_generados < MIN_CRUCES):"+(ncruces_generados < MIN_CRUCES)+" i("+i+") j("+j+") ncruces_generados("+ncruces_generados+") nsecciones("+nsecciones+") ncruces_minActual("+ncruces_minActual+") secciones("+secciones+")");
					if(tablaGrafo[i][j] != 1 && i!=j){
						tablaGrafo[i][j]=tablaGrafo[j][i]=randInt(0,1);
						if(tablaGrafo[i][j]==1){
						ncruces_generados++;
						nsecciones++;
						}
					
					}
					j=randInt(1,secciones);
					System.out.print(" newj("+j+")\n"); 
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
		System.out.println("Tabla");
		 for(int i=0;i<=secciones;i++){
			  for(int j=0;j<=secciones;j++){
				if(j==1 && numlength(secciones)!=numlength(i) ){
					for(int n=0;n<(numlength(secciones)-numlength(j));n++){
						System.out.print(" ");
					}
				}
				//System.out.println("j("+j+") numlength("+numlength(j)+")");
				  System.out.print(tablaGrafo[i][j]);
				if(i!=0 && numlength(secciones)>1) {
				  if(numlength(secciones)==numlength(j)){
					for(int n=0;n<=(numlength(secciones)-numlength(j));n++){
						System.out.print(" ");
					}
				  }
				}
				System.out.print(" ");
			  }
			  System.out.print("\n");
		  }
	}
	void pintaTabla2(){
		System.out.println("Tabla");
		 for(int i=0;i<=secciones;i++){
			 System.out.print("[");
			  for(int j=0;j<=secciones;j++){
				String comaInicial=",";
				String preComillas="";
				String postComillas="";
				if(j==0){
					comaInicial="";
					preComillas="\"";
					postComillas="\"";
				}
				if(i==0){
					preComillas="\"";
					postComillas="\"";
				}
				System.out.print(comaInicial+preComillas+tablaGrafo[i][j]+postComillas);			
			  }
			  System.out.print("]");
			  if(i!=secciones){
			  System.out.print(",\n");
			  }else{System.out.print("\n");}
		  }
	}
	
	public Node getVisitados(){
		return this.visitados;
	}

	public static void main(String[] args) {
		File file = new File("mitabla.txt");  
		FileOutputStream fis = null;
		PrintStream original = System.out;
		/*
		try {
			fis = new FileOutputStream(file);
			PrintStream out = new PrintStream(fis); 
			System.setOut(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		GenerarGrafo grafo = new GenerarGrafo(2);
		System.setOut(original);		
		grafo.pintaTabla();
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

  private void insert(Node node, int value) {
    if (value < node.value) {
      if (node.left != null) {
        insert(node.left, value);
      } else {
        System.out.println("  Inserted " + value + " to left of "
            + node.value);
        node.left = new Node(value);
      }
    } else if (value > node.value) {
      if (node.right != null) {
        insert(node.right, value);
      } else {
        System.out.println("  Inserted " + value + " to right of "
            + node.value);
        node.right = new Node(value);
      }
    }
  }

  private void printInOrder(Node node) {
    if (node != null) {
      printInOrder(node.left);
      System.out.println("  Traversed " + node.value);
      printInOrder(node.right);
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

