package simplex;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.datastax.driver.core.Row;

public class SimplexMinimizar {

    private static final double EPSILON = 1.0E-10;
    private static final double M = 1.0E+38;
	private static boolean debug=false;
    private double[][] a=null;   // tabla
    private int nRes=0;          // number of constraints
    private int nVarObj=0;          // number of original variables

    private int[] bj=null;    // basis[i] = basic variable corresponding to row i
                            // only needed to print out solution, not book
	private boolean debugIteracion=true;
	private boolean paralelo;
    
    public SimplexMinimizar(){
    	
    }
    
    public void ponTabla(double[][] tabla,int nRes, int nVarObj) {
        this.nRes = nRes;
        this.nVarObj = nVarObj;
        this.a=tabla; 
        bj = new int[nRes];
    }
    
    public boolean calcula(){
    	//Hacemos la base
    	construyeBj();
    	 if(bj==null)System.out.println("error construir BJ"); else resolver();
    	 return inf;
    }
    
    private void construyeBj(){
    	boolean asignado[]= new boolean[nRes];
    	int identidad[]=new int[]{-1,-1};
    	//if(debug) System.out.println(a[0].length);
    	for (int columna = a[0].length-2; columna >= 0; columna--){
    		identidad=esIdentidad(columna);
        	if(identidad[0]==1 && asignado[identidad[1]]==false){
        		bj[identidad[1]] = columna;
        		asignado[identidad[1]]=true;
        	}
        }
    }
    
    private long tiempoAnterior=0;

    private int[] esIdentidad(int columna){
    	boolean encontrado1=false;
    	int[] identidad=new int[]{-1,-1};

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class0 __omp_Object0 = new __omp_Class0();
  // shared variables
  __omp_Object0.identidad = identidad;
  __omp_Object0.columna = columna;
  // firstprivate variables
  __omp_Object0._fp_encontrado1 = encontrado1;
  try {
    jomp.runtime.OMP.doParallel(__omp_Object0);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  identidad = __omp_Object0.identidad;
  columna = __omp_Object0.columna;
}
// OMP PARALLEL BLOCK ENDS

        //if(debug) System.out.println("Identidad columna("+columna+"):"+identidad[0]+" su fila("+identidad[1]+")");
        return identidad;
    }
    private int iteracion=0;
    private boolean inf=false;
    private void resolver() {
        while (true) {
        	if(System.currentTimeMillis()-tiempoAnterior>2000){ tiempoAnterior=System.currentTimeMillis();System.out.println("Simplex Iteracion "+iteracion);}
        	if(debug) pintaTabla(this.a);
        	
            int columnaEntrante = entra();
            if (columnaEntrante == -1) {System.out.println("------------------------Optimo encontrado---------------------");break;}  // optimal}


            int filaSaliente = sale(columnaEntrante);
            if (filaSaliente == -1) {System.out.println("------------------------Infinitas soluciones---------------------");inf=true;break;}

            // recalculamos con el pivote
            recalculaFilasConPivote(filaSaliente, columnaEntrante);

            // actualizandoBj(base)
            bj[filaSaliente] = columnaEntrante;
            iteracion++;
        }
    }
            	
    private int entra(){
        int p = -1;
        double zj_cjmin = 0;
        for (int j = 0; j < a[0].length-1; j++) {
        	double cj=0;

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class4 __omp_Object4 = new __omp_Class4();
  // shared variables
  __omp_Object4.cj = cj;
  __omp_Object4.j = j;
  __omp_Object4.zj_cjmin = zj_cjmin;
  __omp_Object4.p = p;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object4);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  cj = __omp_Object4.cj;
  j = __omp_Object4.j;
  zj_cjmin = __omp_Object4.zj_cjmin;
  p = __omp_Object4.p;
}
// OMP PARALLEL BLOCK ENDS

        	double zj=a[a.length-1][j];
        	double cj_zj=cj-zj;
            if (cj_zj<= 0) continue;
            else if (p == -1) {p = j;zj_cjmin=cj_zj;}
            else if (cj_zj > zj_cjmin) {p = j;zj_cjmin=cj_zj;}
        }
        if(p!=-1){
        if(debug) System.out.print("Entra:");
        if(p<this.nVarObj) if(debug) System.out.print("x"); else if(a[a.length-1][p]!=M&&a[a.length-1][p]!=-M)if(debug) System.out.print("s"); else if(debug) System.out.print("a");
        if(debug) System.out.println(p);
        }
        return p;
    }
    
    
    private int sale(int columna) {
    	 int filaMenor = -1;
         for (int fila = 0; fila < nRes; fila++) {
             if (a[fila][columna] <= 0) continue;
             else if (filaMenor == -1) filaMenor = fila;
             else if ((a[fila][nRes+nVarObj] / a[fila][columna]) < (a[filaMenor][nRes+nVarObj] / a[filaMenor][columna])) filaMenor = fila;
         }
         if(filaMenor!=-1){
         if(debug) System.out.print("Sale:");
        if(bj[filaMenor]<this.nVarObj) if(debug) System.out.print("x"); else if(a[a.length-1][bj[filaMenor]]!=M&&a[a.length-1][bj[filaMenor]]!=-M)if(debug) System.out.print("s"); else if(debug) System.out.print("a");
        if(debug) System.out.println(bj[filaMenor]);}
        return filaMenor;
    }

   
    private void recalculaFilasConPivote(int p, int q) {

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class8 __omp_Object8 = new __omp_Class8();
  // shared variables
  __omp_Object8.q = q;
  __omp_Object8.p = p;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object8);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  q = __omp_Object8.q;
  p = __omp_Object8.p;
}
// OMP PARALLEL BLOCK ENDS


// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class12 __omp_Object12 = new __omp_Class12();
  // shared variables
  __omp_Object12.q = q;
  __omp_Object12.p = p;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object12);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  q = __omp_Object12.q;
  p = __omp_Object12.p;
}
// OMP PARALLEL BLOCK ENDS

        { // OMP FOR BLOCK BEGINS
        // copy of firstprivate variables, initialized
        // copy of lastprivate variables
        // variables to hold result of reduction
        boolean amLast=false;
        int __omp_me = jomp.runtime.OMP.getAbsoluteID();
        {
          // firstprivate variables + init
          // [last]private variables
          // reduction variables + init to default
          // -------------------------------------
          jomp.runtime.LoopData __omp_WholeData17 = new jomp.runtime.LoopData();
          jomp.runtime.LoopData __omp_ChunkData16 = new jomp.runtime.LoopData();
          __omp_WholeData17.start = (long)( 0);
          __omp_WholeData17.stop = (long)( a[0].length);
          __omp_WholeData17.step = (long)(1);
          __omp_WholeData17.chunkSize = (long)(1);
          jomp.runtime.OMP.resetOrderer(__omp_me, __omp_WholeData17.start);
          jomp.runtime.OMP.initTicket(__omp_me, __omp_WholeData17);
          while(!__omp_ChunkData16.isLast && jomp.runtime.OMP.getLoopDynamic(__omp_me, __omp_WholeData17, __omp_ChunkData16)) {
            for(int j = (int)__omp_ChunkData16.start; j < __omp_ChunkData16.stop; j += __omp_ChunkData16.step) {
              // OMP USER CODE BEGINS

            if (j != q) a[p][j] /= a[p][q];
              // OMP USER CODE ENDS
              if (j == (__omp_WholeData17.stop-1)) amLast = true;
            } // of for 
          } // of while
          // call reducer
          jomp.runtime.OMP.resetTicket(__omp_me);
          jomp.runtime.OMP.doBarrier(__omp_me);
          // copy lastprivate variables out
          if (amLast) {
          }
        }
        // set global from lastprivate variables
        if (amLast) {
        }
        // set global from reduction variables
        if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
        }
        } // OMP FOR BLOCK ENDS

        a[p][q] = 1.0;
    }

    
    public double getZ(){
    	
    	double temp;
    	double z=0;

// OMP PARALLEL BLOCK BEGINS
{
  __omp_Class19 __omp_Object19 = new __omp_Class19();
  // shared variables
  __omp_Object19.z = z;
  // firstprivate variables
  try {
    jomp.runtime.OMP.doParallel(__omp_Object19);
  } catch(Throwable __omp_exception) {
    System.err.println("OMP Warning: Illegal thread exception ignored!");
    System.err.println(__omp_exception);
  }
  // reduction variables
  // shared variables
  z = __omp_Object19.z;
  temp = __omp_Object19.temp;
}
// OMP PARALLEL BLOCK ENDS

    	temp=z;
    	return temp;
    }

    public void imprime(){
    	System.out.println("Zmin:"+this.getZ());
    }
    
    public void setFlags(boolean debug,boolean paralelo) {
		this.debug = debug;
		this.paralelo=paralelo;
	}
    
    public int[] getResultado(){
    	int[] resultado=new int[nVarObj];
    	for(int i=0;i<bj.length;i++){
    		if(bj[i]<nVarObj&&a[i][a[0].length-1]==1) resultado[bj[i]]=1;
    	}
    	return resultado;
    }
    
    public static void test2() {
    	double num1 = 1427.0E+0;
       double num2 = 1835.0E+0;
        double num3= 1019.0E+0;
        double[][] A = {
        		{1,0,0,1,0,0,0,0,0,0,0,0,0,1},
        		{0,1,-1,0,1,0,0,0,0,0,0,0,0,0},
        		{0,1,-1,0,0,-1,1,0,0,0,0,0,0,0},
        		{1,1,0,0,0,0,0,1,0,0,0,0,0,1},
        		{1,1,0,0,0,0,0,0,-1,1,0,0,0,1},
        		{1,0,0,0,0,0,0,0,0,0,1,0,0,1},
        		{0,1,0,0,0,0,0,0,0,0,0,1,0,1},        		
        		{0,0,1,0,0,0,0,0,0,0,0,0,1,1},
        		{num1,num2,num3,0,0,0,M,0,0,M,0,0,0,0}
        }; 
       
        long elapsedTimeMillis=0;
        SimplexMinimizar lp = new SimplexMinimizar();
        lp.ponTabla(A, 8, 3);
        lp.setFlags(false, true);
        long start;
        for(int i=0;i<10;i++){
        start = System.nanoTime();
        lp.ponTabla(A, 8, 3);
        lp.calcula();        
        elapsedTimeMillis = elapsedTimeMillis+(System.nanoTime()-start);
        }
        
        long tiempoMs=elapsedTimeMillis/10000000;
        long tiempoS=tiempoMs/1000;
		//caminos.pintaCaminosGenerados();
		System.out.println("\ntiempo ("+(elapsedTimeMillis/1000)+"Ns ,"+tiempoMs+"Ms ,"+tiempoS+" S)");
    	//lp.pintaTablaPreSimplex(A);
		lp.pintaTabla(lp.getTabla());
		debug=true;
    	if(debug) System.out.println("Z vale:"+lp.getZ());
        /*
    	if(debug) System.out.println("value = " + lp.value());
        double[] x = lp.primal();
        for (int i = 0; i < x.length; i++)
            if(debug) System.out.println("x[" + i + "] = " + x[i]);
        double[] y = lp.dual();
        for (int j = 0; j < y.length; j++)
            if(debug) System.out.println("y[" + j + "] = " + y[j]);
        lp.pintaTablaPreSimplex(lp.getTabla());
        */
    }
  
    public static void test4() {
        double[][] A = {
        		{1,1,0,1,0,0,7},
        		{3,1,1,0,-1,1,10},
        		//{1427.3,0,0,0,1835.1,1019.5,0,-M,-M,-M,0,0,0,0,0,0,0,0,0,0}//orig max z=
        		//{-1427.3,0,0,0,-1835.1,-1019.5,0,+M,+M,+M,0,0,0,0,0,0,0,0,0,0}//max
        		{3,2,1,0,0,M,0}
        }; 
       
        SimplexMinimizar lp = new SimplexMinimizar();
        lp.ponTabla(A, 2, 3);
    	//lp.pintaTablaPreSimplex(A);
    	if(debug) System.out.println("Z vale:"+lp.getZ());
        //if(debug) System.out.println("value = " + lp.value());
        //double[] x = lp.primal();
        //for (int i = 0; i < x.length; i++)
        //    if(debug) System.out.println("x[" + i + "] = " + x[i]);
        //double[] y = lp.dual();
        //for (int j = 0; j < y.length; j++)
        //    if(debug) System.out.println("y[" + j + "] = " + y[j]);
        //lp.pintaTablaPreSimplex(lp.getTabla());
    }
    
    public double[][] getTabla(){
    	return this.a;
    }
    
    public void pintaTabla(double[][] b){
		int i=0;
		int espaciado=12;
		for(i=0;i<b[0].length+2;i++){
			if(i==0){
				System.out.print("Bj");
				pintaEspacios(espaciado-4);
			}else if(i==1){
				System.out.print("Cj");
				pintaEspacios(espaciado-4);
			} else if(i==2){
				System.out.print("Vb");
				pintaEspacios(espaciado-4);
			}else{ 
				if(i-3<this.nVarObj) System.out.print("x"); else if(b[b.length-1][i-3]!=M&&b[b.length-1][i-3]!=-M)System.out.print("s"); else System.out.print("a");
				System.out.print(i-3);
				pintaEspacios(espaciado-longitudNumero(i-2)-2);
			}
		}
		System.out.println();
		DecimalFormat df = new DecimalFormat("#0.0####");
		for(i=0;i<=b.length;i++){
			for(int j=0;j<b[0].length+2;j++){
			 if(i!=b.length) {
				if(j==0&&i<bj.length&&i<b.length-1){
						if(bj[i]<nVarObj)System.out.print("x"); else if(b[b.length-1][bj[i]]!=M&&b[b.length-1][bj[i]]!=-M) System.out.print("s"); else System.out.print("a"); 
						//System.out.print(bj[i]+"/"+b[b.length-1][bj[i]]);
						//longitudNumeroActual=logitudNumero(b[b.length-1][bj[i]])+logitudNumero(bj[i])
						System.out.print(bj[i]);
						pintaEspacios(espaciado-longitudNumero(bj[i])-3);		
				}else if(j==1&&i<b.length-1){//Imprime columna CJ
					double valorActual=(b[b.length-1][bj[i]]);
			    	if(valorActual!=M&&valorActual!=-M){
			    	System.out.print(df.format(valorActual));
					pintaEspacios(espaciado-longitudNumero(valorActual)-3);
			    	}else if(valorActual==M){
			    		System.out.print("M");
						pintaEspacios(espaciado-4);
			    	}else{
			    		System.out.print("-M");
						pintaEspacios(espaciado-5);
			    	}
			    }else if(j==2&&i<b.length-1){//Imprime columna VB
			    	String numero=df.format(b[i][b[0].length-1]);
			    	System.out.print(numero);
					pintaEspacios(espaciado-numero.length()-1);
			    }
				if(i==b.length-1&&j==0){pintaEspacios(espaciado*2+6);}
				if(j>2){
				if(b[i][j-3]!=M&&b[i][j-3]!=-M){
					String numero=df.format(b[i][j-3]);
					System.out.print(numero);
					pintaEspacios(espaciado-numero.length()-1);
					}else if(b[i][j-3]==M){						
						System.out.print("M");
						pintaEspacios(espaciado-2);
				} else{
					System.out.print("-M");
					pintaEspacios(espaciado-3);
				}	
				}
			}else if(j<b[0].length-1){
				if(j==0){
					System.out.print("Cj-Zj");
					pintaEspacios(espaciado*2+1);
					}
				double cj=0;
				double cjnoN=0;
				for(int fila=0;fila<b.length-1;fila++){
	        		cj=cj+b[b.length-1][bj[fila]]*b[fila][j];
	        		if(b[b.length-1][bj[fila]]!=M) cjnoN=cjnoN+b[b.length-1][bj[fila]]*b[fila][j];
	        	}
				double zj=a[a.length-1][j];
				
				if((cj-zj)==0){
					System.out.print("0");
					pintaEspacios(espaciado-3);
				}else{
					int unidadescj=0;
					int signocj=1;
					int unidadeszj=0;
					int signozj=1;
					String texto="";
					if((cj%M)==0||(Math.abs(cj)%M>1E22)){
						unidadescj=(int)(Math.abs(Math.abs(cj))/M);
						if(cj<0) signocj=-1;
					}				
					if((zj%M)==0||(Math.abs(zj)%M>1E22)){
						unidadeszj=(int)(Math.abs(Math.abs(zj))/M);
						if(zj<0) signozj=-1;
					}
					if(unidadescj>0){
						texto=texto+signocj*unidadescj+"M";
					}else if(cjnoN!=0){
						if(cjnoN>0){
							texto=texto+(df.format(cjnoN));
						}else{
							texto=texto+"-"+(df.format(cjnoN));
						}
					}
					if(unidadeszj>0){
						texto=texto+signozj*unidadeszj+"M";
					}else if(zj<0){
						texto=texto+"+"+df.format(zj);
					}else if(zj!=0){
						texto=texto+"-"+df.format(zj);
					}
					System.out.print(texto);
					pintaEspacios(espaciado-texto.length());
				}
			}
			 
		}
			System.out.print("\n");
		}
	}
    
    private void pintaEspacios(int espacios){
			for(int t=0;t<espacios;t++){
			 System.out.print(" ");						
			}
	}
	
	private int longitudNumero(double nfin)
	{
		String num=""+nfin;
		String[] splitter = num.toString().split("\\.");
		splitter[0].length();   // Before Decimal Count
		return splitter[0].length()+splitter[1].length()+1;
	}
	
	private int longitudNumero(int nin)
	{
		int n=nin;
		int l=0;
		if(n>0||n<0){
		n=Math.abs(n);
		for (l=0;n>0;++l)
			n/=10;
		}else{
			l=1;//0 = 1
		}
		//if(debug) System.out.println("longitud("+nin+"):"+l);
		return l;			
	}

    // test client
    public static void main(String[] args) {
/*

        */
        
        try                           { test2();             }
        catch (ArithmeticException e) { e.printStackTrace(); }
        if(debug) System.out.println("--------------------------------");
    }

// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class19 extends jomp.runtime.BusyTask {
  // shared variables
  double z;
  double temp;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

                  { // OMP FOR BLOCK BEGINS
                  // copy of firstprivate variables, initialized
                  // copy of lastprivate variables
                  // variables to hold result of reduction
                  double _cp_z;
                  boolean amLast=false;
                  {
                    // firstprivate variables + init
                    // [last]private variables
                    // reduction variables + init to default
                    double  z = 0;
                    // -------------------------------------
                    jomp.runtime.LoopData __omp_WholeData21 = new jomp.runtime.LoopData();
                    jomp.runtime.LoopData __omp_ChunkData20 = new jomp.runtime.LoopData();
                    __omp_WholeData21.start = (long)(0);
                    __omp_WholeData21.stop = (long)(bj.length);
                    __omp_WholeData21.step = (long)(1);
                    __omp_WholeData21.chunkSize = (long)(1);
                    jomp.runtime.OMP.resetOrderer(__omp_me, __omp_WholeData21.start);
                    jomp.runtime.OMP.initTicket(__omp_me, __omp_WholeData21);
                    while(!__omp_ChunkData20.isLast && jomp.runtime.OMP.getLoopDynamic(__omp_me, __omp_WholeData21, __omp_ChunkData20)) {
                      for(int i = (int)__omp_ChunkData20.start; i < __omp_ChunkData20.stop; i += __omp_ChunkData20.step) {
                        // OMP USER CODE BEGINS
{
    		z+=a[a.length-1][bj[i]]*a[i][a[0].length-1];
    	}
                        // OMP USER CODE ENDS
                        if (i == (__omp_WholeData21.stop-1)) amLast = true;
                      } // of for 
                    } // of while
                    // call reducer
                    _cp_z = (double) jomp.runtime.OMP.doPlusReduce(__omp_me, z);
                    jomp.runtime.OMP.resetTicket(__omp_me);
                    jomp.runtime.OMP.doBarrier(__omp_me);
                    // copy lastprivate variables out
                    if (amLast) {
                    }
                  }
                  // set global from lastprivate variables
                  if (amLast) {
                  }
                  // set global from reduction variables
                  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
                    z+= _cp_z;
                  }
                  } // OMP FOR BLOCK ENDS

    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS



// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class12 extends jomp.runtime.BusyTask {
  // shared variables
  int q;
  int p;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

                  { // OMP FOR BLOCK BEGINS
                  // copy of firstprivate variables, initialized
                  // copy of lastprivate variables
                  // variables to hold result of reduction
                  boolean amLast=false;
                  {
                    // firstprivate variables + init
                    // [last]private variables
                    // reduction variables + init to default
                    // -------------------------------------
                    jomp.runtime.LoopData __omp_WholeData14 = new jomp.runtime.LoopData();
                    jomp.runtime.LoopData __omp_ChunkData13 = new jomp.runtime.LoopData();
                    __omp_WholeData14.start = (long)( 0);
                    __omp_WholeData14.stop = (long)( nRes);
                    __omp_WholeData14.step = (long)(1);
                    __omp_WholeData14.chunkSize = (long)(1);
                    jomp.runtime.OMP.resetOrderer(__omp_me, __omp_WholeData14.start);
                    jomp.runtime.OMP.initTicket(__omp_me, __omp_WholeData14);
                    while(!__omp_ChunkData13.isLast && jomp.runtime.OMP.getLoopDynamic(__omp_me, __omp_WholeData14, __omp_ChunkData13)) {
                      for(int i = (int)__omp_ChunkData13.start; i < __omp_ChunkData13.stop; i += __omp_ChunkData13.step) {
                        // OMP USER CODE BEGINS

            if (i != p) a[i][q] = 0.0;
                        // OMP USER CODE ENDS
                        if (i == (__omp_WholeData14.stop-1)) amLast = true;
                      } // of for 
                    } // of while
                    // call reducer
                    jomp.runtime.OMP.resetTicket(__omp_me);
                    jomp.runtime.OMP.doBarrier(__omp_me);
                    // copy lastprivate variables out
                    if (amLast) {
                    }
                  }
                  // set global from lastprivate variables
                  if (amLast) {
                  }
                  // set global from reduction variables
                  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
                  }
                  } // OMP FOR BLOCK ENDS

    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS



// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class8 extends jomp.runtime.BusyTask {
  // shared variables
  int q;
  int p;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

                  { // OMP FOR BLOCK BEGINS
                  // copy of firstprivate variables, initialized
                  // copy of lastprivate variables
                  // variables to hold result of reduction
                  boolean amLast=false;
                  {
                    // firstprivate variables + init
                    // [last]private variables
                    // reduction variables + init to default
                    // -------------------------------------
                    jomp.runtime.LoopData __omp_WholeData10 = new jomp.runtime.LoopData();
                    jomp.runtime.LoopData __omp_ChunkData9 = new jomp.runtime.LoopData();
                    __omp_WholeData10.start = (long)( 0);
                    __omp_WholeData10.stop = (long)( nRes*a[0].length);
                    __omp_WholeData10.step = (long)(1);
                    __omp_WholeData10.chunkSize = (long)(1);
                    jomp.runtime.OMP.resetOrderer(__omp_me, __omp_WholeData10.start);
                    jomp.runtime.OMP.initTicket(__omp_me, __omp_WholeData10);
                    while(!__omp_ChunkData9.isLast && jomp.runtime.OMP.getLoopDynamic(__omp_me, __omp_WholeData10, __omp_ChunkData9)) {
                      for(int ij = (int)__omp_ChunkData9.start; ij < __omp_ChunkData9.stop; ij += __omp_ChunkData9.step) {
                        // OMP USER CODE BEGINS
{
        	int i = ij/a[0].length;
        	int j=ij%a[0].length;
        	if (i != p && j != q) a[i][j] -= a[p][j] * a[i][q] / a[p][q];
        }
                        // OMP USER CODE ENDS
                        if (ij == (__omp_WholeData10.stop-1)) amLast = true;
                      } // of for 
                    } // of while
                    // call reducer
                    jomp.runtime.OMP.resetTicket(__omp_me);
                    jomp.runtime.OMP.doBarrier(__omp_me);
                    // copy lastprivate variables out
                    if (amLast) {
                    }
                  }
                  // set global from lastprivate variables
                  if (amLast) {
                  }
                  // set global from reduction variables
                  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
                  }
                  } // OMP FOR BLOCK ENDS

    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS



// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class4 extends jomp.runtime.BusyTask {
  // shared variables
  double cj;
  int j;
  double zj_cjmin;
  int p;
  // firstprivate variables
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

                          { // OMP FOR BLOCK BEGINS
                          // copy of firstprivate variables, initialized
                          // copy of lastprivate variables
                          // variables to hold result of reduction
                          double _cp_cj;
                          boolean amLast=false;
                          {
                            // firstprivate variables + init
                            // [last]private variables
                            // reduction variables + init to default
                            double  cj = 0;
                            // -------------------------------------
                            jomp.runtime.LoopData __omp_WholeData6 = new jomp.runtime.LoopData();
                            jomp.runtime.LoopData __omp_ChunkData5 = new jomp.runtime.LoopData();
                            __omp_WholeData6.start = (long)(0);
                            __omp_WholeData6.stop = (long)(a.length-1);
                            __omp_WholeData6.step = (long)(1);
                            __omp_WholeData6.chunkSize = (long)(1);
                            jomp.runtime.OMP.resetOrderer(__omp_me, __omp_WholeData6.start);
                            jomp.runtime.OMP.initTicket(__omp_me, __omp_WholeData6);
                            while(!__omp_ChunkData5.isLast && jomp.runtime.OMP.getLoopDynamic(__omp_me, __omp_WholeData6, __omp_ChunkData5)) {
                              for(int i = (int)__omp_ChunkData5.start; i < __omp_ChunkData5.stop; i += __omp_ChunkData5.step) {
                                // OMP USER CODE BEGINS
{
        		cj+=a[a.length-1][bj[i]]*a[i][j];
        	}
                                // OMP USER CODE ENDS
                                if (i == (__omp_WholeData6.stop-1)) amLast = true;
                              } // of for 
                            } // of while
                            // call reducer
                            _cp_cj = (double) jomp.runtime.OMP.doPlusReduce(__omp_me, cj);
                            jomp.runtime.OMP.resetTicket(__omp_me);
                            jomp.runtime.OMP.doBarrier(__omp_me);
                            // copy lastprivate variables out
                            if (amLast) {
                            }
                          }
                          // set global from lastprivate variables
                          if (amLast) {
                          }
                          // set global from reduction variables
                          if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
                            cj+= _cp_cj;
                          }
                          } // OMP FOR BLOCK ENDS

    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS



// OMP PARALLEL REGION INNER CLASS DEFINITION BEGINS
private class __omp_Class0 extends jomp.runtime.BusyTask {
  // shared variables
  int [ ] identidad;
  int columna;
  // firstprivate variables
  boolean _fp_encontrado1;
  // variables to hold results of reduction

  public void go(int __omp_me) throws Throwable {
  // firstprivate variables + init
  boolean encontrado1 = (boolean) _fp_encontrado1;
  // private variables
  // reduction variables, init to default
    // OMP USER CODE BEGINS

                  { // OMP FOR BLOCK BEGINS
                  // copy of firstprivate variables, initialized
                  // copy of lastprivate variables
                  // variables to hold result of reduction
                  boolean amLast=false;
                  {
                    // firstprivate variables + init
                    // [last]private variables
                    // reduction variables + init to default
                    // -------------------------------------
                    jomp.runtime.LoopData __omp_WholeData2 = new jomp.runtime.LoopData();
                    jomp.runtime.LoopData __omp_ChunkData1 = new jomp.runtime.LoopData();
                    __omp_WholeData2.start = (long)(0);
                    __omp_WholeData2.stop = (long)(a.length-1);
                    __omp_WholeData2.step = (long)(1);
                    __omp_WholeData2.chunkSize = (long)(1);
                    jomp.runtime.OMP.resetOrderer(__omp_me, __omp_WholeData2.start);
                    jomp.runtime.OMP.initTicket(__omp_me, __omp_WholeData2);
                    while(!__omp_ChunkData1.isLast && jomp.runtime.OMP.getLoopDynamic(__omp_me, __omp_WholeData2, __omp_ChunkData1)) {
                      for(int fila = (int)__omp_ChunkData1.start; fila < __omp_ChunkData1.stop; fila += __omp_ChunkData1.step) {
                        // OMP USER CODE BEGINS
{
    		if(a[fila][columna]==1.0&&!encontrado1) {
    			encontrado1=true;
                         // OMP CRITICAL BLOCK BEGINS
                         synchronized (jomp.runtime.OMP.getLockByName("")) {
                         // OMP USER CODE BEGINS

    			{
    			identidad[0]=1;
    			identidad[1]=fila;
    			}
                         // OMP USER CODE ENDS
                         }
                         // OMP CRITICAL BLOCK ENDS

    			}
    		else if(encontrado1&&a[fila][columna]!=0) {
                        // OMP CRITICAL BLOCK BEGINS
                        synchronized (jomp.runtime.OMP.getLockByName("")) {
                        // OMP USER CODE BEGINS

    			{
    				identidad[0]=-1;identidad[1]=-1;
    			}
                        // OMP USER CODE ENDS
                        }
                        // OMP CRITICAL BLOCK ENDS

    			}
    	}
                        // OMP USER CODE ENDS
                        if (fila == (__omp_WholeData2.stop-1)) amLast = true;
                      } // of for 
                    } // of while
                    // call reducer
                    jomp.runtime.OMP.resetTicket(__omp_me);
                    jomp.runtime.OMP.doBarrier(__omp_me);
                    // copy lastprivate variables out
                    if (amLast) {
                    }
                  }
                  // set global from lastprivate variables
                  if (amLast) {
                  }
                  // set global from reduction variables
                  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
                  }
                  } // OMP FOR BLOCK ENDS

    // OMP USER CODE ENDS
  // call reducer
  // output to _rd_ copy
  if (jomp.runtime.OMP.getThreadNum(__omp_me) == 0) {
  }
  }
}
// OMP PARALLEL REGION INNER CLASS DEFINITION ENDS

}

