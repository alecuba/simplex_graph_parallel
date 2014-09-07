package simplex;

import java.text.DecimalFormat;

/*************************************************************************
 *  Compilation:  javac Simplex.java
 *  Execution:    java Simplex
 *
 *  Given an M-by-N matrix A, an M-length vector b, and an
 *  N-length vector c, solve the  LP { max cx : Ax <= b, x >= 0 }.
 *  Assumes that b >= 0 so that x = 0 is a basic feasible solution.
 *
 *  Creates an (M+1)-by-(N+M+1) simplex tableaux with the 
 *  RHS in column M+N, the objective function in row M, and
 *  slack variables in columns M through M+N-1.
 *
 *************************************************************************/

public class SimplexV0 {
    private static final double EPSILON = 1.0E-10;
    private static final double EPSILON2 = 1.0E+10;
    private double[][] a;   // tabla
    private int nRes;          // number of constraints
    private int nVarObj;          // number of original variables

    private int[] base;    // basis[i] = basic variable corresponding to row i
                            // only needed to print out solution, not book

    // sets up the simplex tableaux
    public SimplexV0(double[][] A, double[] b, double[] c) {
        nRes = b.length;
        nVarObj = c.length;
        a = new double[nRes+1][nVarObj+nRes+1];
        for (int i = 0; i < nRes; i++)
            for (int j = 0; j < nVarObj; j++)
                a[i][j] = A[i][j];
        for (int i = 0; i < nRes; i++) a[i][nVarObj+i] = 1.0;
        for (int j = 0; j < nVarObj; j++) a[nRes][j]   = c[j];
        for (int i = 0; i < nRes; i++) a[i][nRes+nVarObj] = b[i];

        base = new int[nRes];
        for (int i = 0; i < nRes; i++) base[i] = nVarObj + i;

        resolver();

        // check optimality conditions
        assert compruebaOptimo(A, b, c);
    }
    
 // sets up the simplex tableaux
    public SimplexV0(double[][] A,int nRes, int nVarObj) {
        this.nRes = nRes;
        this.nVarObj = nVarObj;
        this.a=A;
       // a = new double[nRes+1][nVarObj+nRes+1];
        //for (int i = 0; i < nRes; i++)
        //    for (int j = 0; j < nVarObj; j++)
         //       a[i][j] = A[i][j];
        //for (int i = 0; i < nRes; i++) a[i][nVarObj+i] = 1.0;
        //for (int j = 0; j < nVarObj; j++) a[nRes][j]   = c[j];
        //for (int i = 0; i < nRes; i++) a[i][nRes+nVarObj] = b[i];

        base = new int[nRes];
        for (int i = 0; i < nRes; i++) base[i] = nVarObj + i;

        resolver();

        // check optimality conditions
        //assert compruebaOptimo(A, b, c);
    }

    public void pintaTabla(){
    	String texto="";
    	for(int i=0;i<a.length;i++){
    		for(int j=0;j<a[0].length;j++){
    			texto=texto+a[i][j]+" ";
    		}
    		texto=texto+"\n";
    	}
    	texto=texto+"\n";
    	for(int j=0;j<base.length;j++){
    		texto=texto+a[a.length-1][base[j]]+"("+base[j]+")"+" ";
    	}
    	System.out.print(texto);
    }
    
    // run simplex algorithm starting from initial BFS
    private void resolver() {
        while (true) {

            // find entering column q
            int q = entra();
            if (q == -1) {System.out.println("Optimo");break;}  // optimal}

            // find leaving row p
            int p = minRatioRule(q);
            if (p == -1) break;
            //if (p == -1) throw new ArithmeticException("Linear program is unbounded");

            // pivot
            pivot(p, q);

            // update basis
            base[p] = q;
        }
    }

    // lowest index of a non-basic column with a positive cost
    private int bland() {
        for (int j = 0; j < nRes + nVarObj; j++)
            if (a[nRes][j] > 0) return j;
        return -1;  // optimal
    }
    
    private int entra(){
    	boolean cambiado=false;
    	int pos=-1;
        double cjzjpos=0;
    	for(int j = 0; j < nRes + nVarObj; j++){
        	double zj=0;
        	for(int i=0;i<nRes;i++){
        		zj=zj+a[a.length-1][base[i]]*a[i][j];
        	}
        	double cj=a[a.length-1][j];
        	if(pos!=-1){   	
        	 if ((cj-zj)>cjzjpos){
        		 pos=j;
        		 cambiado=true;
        	 }
        	}else if((cj-zj)>0){
        	 pos=j;
        	 cjzjpos=(cj-zj);
        	 cambiado=true;
        	 }
        }
    	
        if(!cambiado) pos=-1;
        return pos;  // optimal
    }
    
    // index of a non-basic column with most positive cost
    private int entra2() {
        int q = 0;
        for (int j = 1; j < nRes + nVarObj; j++)
            if (a[nRes][j]<0&&a[nRes][j] < a[nRes][q]) q = j;

        if (a[nRes][q] >= 0) return -1;  // optimal
        else return q;
    }

   // index of a non-basic column with most positive cost
    private int dantzig() {
        int q = 0;
        for (int j = 1; j < nRes + nVarObj; j++)
            if (a[nRes][j] > a[nRes][q]) q = j;

        if (a[nRes][q] <= 0) return -1;  // optimal
        else return q;
    }

    // find row p using min ratio rule (-1 if no such row)
    private int minRatioRule(int q) {
        int p = -1;
        for (int i = 0; i < nRes; i++) {
            if (a[i][q] <= 0) continue;
            else if (p == -1) p = i;
            else if ((a[i][nRes+nVarObj] / a[i][q]) < (a[p][nRes+nVarObj] / a[p][q])) p = i;
        }
        return p;
    }

    // pivot on entry (p, q) using Gauss-Jordan elimination
    private void pivot(int p, int q) {

        // everything but row p and column q
        for (int i = 0; i <= nRes; i++)
            for (int j = 0; j <= nRes + nVarObj; j++)
                if (i != p && j != q) a[i][j] -= a[p][j] * a[i][q] / a[p][q];

        // zero out column q
        for (int i = 0; i <= nRes; i++)
            if (i != p) a[i][q] = 0.0;

        // scale row p
        for (int j = 0; j <= nRes + nVarObj; j++)
            if (j != q) a[p][j] /= a[p][q];
        a[p][q] = 1.0;
    }

    // return optimal objective value
    public double value() {
        return -a[nRes][nRes+nVarObj];
    }

    // return primal solution vector
    public double[] primal() {
        double[] x = new double[nVarObj];
        for (int i = 0; i < nRes; i++)
            if (base[i] < nVarObj) x[base[i]] = a[i][nRes+nVarObj];
        return x;
    }

    // return dual solution vector
    public double[] dual() {
        double[] y = new double[nRes];
        for (int i = 0; i < nRes; i++)
            y[i] = -a[nRes][nVarObj+i];
        return y;
    }


    // is the solution primal feasible?
    private boolean isPrimalFeasible(double[][] A, double[] b) {
        double[] x = primal();

        // check that x >= 0
        for (int j = 0; j < x.length; j++) {
            if (x[j] < 0.0) {
                System.out.println("x[" + j + "] = " + x[j] + " is negative");
                return false;
            }
        }

        // check that Ax <= b
        for (int i = 0; i < nRes; i++) {
            double sum = 0.0;
            for (int j = 0; j < nVarObj; j++) {
                sum += A[i][j] * x[j];
            }
            if (sum > b[i] + EPSILON) {
                System.out.println("not primal feasible");
                System.out.println("b[" + i + "] = " + b[i] + ", sum = " + sum);
                return false;
            }
        }
        return true;
    }

    // is the solution dual feasible?
    private boolean isDualFeasible(double[][] A, double[] c) {
        double[] y = dual();

        // check that y >= 0
        for (int i = 0; i < y.length; i++) {
            if (y[i] < 0.0) {
                System.out.println("y[" + i + "] = " + y[i] + " is negative");
                return false;
            }
        }

        // check that yA >= c
        for (int j = 0; j < nVarObj; j++) {
            double sum = 0.0;
            for (int i = 0; i < nRes; i++) {
                sum += A[i][j] * y[i];
            }
            if (sum < c[j] - EPSILON) {
                System.out.println("not dual feasible");
                System.out.println("c[" + j + "] = " + c[j] + ", sum = " + sum);
                return false;
            }
        }
        return true;
    }

    // check that optimal value = cx = yb
    private boolean isOptimal(double[] b, double[] c) {
        double[] x = primal();
        double[] y = dual();
        double value = value();

        // check that value = cx = yb
        double value1 = 0.0;
        for (int j = 0; j < x.length; j++)
            value1 += c[j] * x[j];
        double value2 = 0.0;
        for (int i = 0; i < y.length; i++)
            value2 += y[i] * b[i];
        if (Math.abs(value - value1) > EPSILON || Math.abs(value - value2) > EPSILON) {
            System.out.println("value = " + value + ", cx = " + value1 + ", yb = " + value2);
            return false;
        }

        return true;
    }

    private boolean compruebaOptimo(double[][]A, double[] b, double[] c) {
        return isPrimalFeasible(A, b) && isDualFeasible(A, c) && isOptimal(b, c);
    }

    // print tableaux
    public void show() {
        System.out.println("M = " + nRes);
        System.out.println("N = " + nVarObj);
        for (int i = 0; i <= nRes; i++) {
            for (int j = 0; j <= nRes + nVarObj; j++) {
                System.out.printf("%7.2f ", a[i][j]);
            }
            System.out.println();
        }
        System.out.println("value = " + value());
        for (int i = 0; i < nRes; i++)
            if (base[i] < nVarObj) System.out.println("x_" + base[i] + " = " + a[i][nRes+nVarObj]);
        System.out.println();
    }

    public void imprime(){
    	System.out.println("value = " + -1*this.value());
        double[] x = this.primal();
        for (int i = 0; i < x.length; i++)
            System.out.println("x[" + i + "] = " + x[i]);
        double[] y = this.dual();
        for (int j = 0; j < y.length; j++)
            System.out.println("y[" + j + "] = " + y[j]);
    }

    private static void test(double[][] A, double[] b, double[] c) {
        SimplexV0 lp = new SimplexV0(A, b, c);
        System.out.println("value = " + lp.value());
        double[] x = lp.primal();
        for (int i = 0; i < x.length; i++)
            System.out.println("x[" + i + "] = " + x[i]);
        double[] y = lp.dual();
        for (int j = 0; j < y.length; j++)
            System.out.println("y[" + j + "] = " + y[j]);
        lp.pintaTablaPreSimplex(lp.getTabla());
    }

    public static void test1() {
        double[][] A = {
        		{1,0,0},
        		{0,1,1},
        		{1,1,0},
        		{1,0,0},
        		{0,1,0},
        		{0,0,1},
        };
        double[] c = {-1427.3,-1835.1,-1019.5};
        double[] b = {1,2,1,1,1,1};
        test(A, b, c);
    }
    

    
    public static void test2() {
        /*
    	double[][] A = {
        		{1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
        		{0,0,0,0,1,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
        		{1,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
        		{1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,1},
        		{2039,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,15040},
        		{0,0,0,0,2039,0,0,0,0,0,0,1,0,0,0,0,0,0,0,11273},
        		{0,0,0,0,0,2039,0,0,0,0,0,0,1,0,0,0,0,0,0,13438},
        		{1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1},
        		{0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
        		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1},
        		{0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1},
        		{0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1},
        		{0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
        		//{1427.3,0,0,0,1835.1,1019.5,0,-EPSILON2,-EPSILON2,-EPSILON2,0,0,0,0,0,0,0,0,0,0}//orig max z=
        		//{-1427.3,0,0,0,-1835.1,-1019.5,0,+EPSILON2,+EPSILON2,+EPSILON2,0,0,0,0,0,0,0,0,0,0}//max
        		{-1427.3,0,0,0,-1835.1,-1019.5,0,-EPSILON2,-EPSILON2,-EPSILON2,0,0,0,0,0,0,0,0,0,0}
        };
        */
    	double num1 = -1427.0E+0;
       double num2 = -1835.0E+0;
        double num3= -1019.0E+0;
        double[][] A = {
        		{1,0,0,1,0,0,0,0,0,1},
        		{0,1,-1,0,1,0,0,0,0,0},
        		{1,1,0,0,0,1,0,0,0,1},
        		{1,0,0,0,0,0,1,0,0,1},
        		{0,1,0,0,0,0,0,1,0,1},
        		{0,0,1,0,0,0,0,0,1,1},
        		//{1427.3,0,0,0,1835.1,1019.5,0,-EPSILON2,-EPSILON2,-EPSILON2,0,0,0,0,0,0,0,0,0,0}//orig max z=
        		//{-1427.3,0,0,0,-1835.1,-1019.5,0,+EPSILON2,+EPSILON2,+EPSILON2,0,0,0,0,0,0,0,0,0,0}//max
        		{num1,num2,num3,0,0,0,0,0,0,0}
        }; 
       
        SimplexV0 lp = new SimplexV0(A, 6, 3);
    	lp.pintaTablaPreSimplex(A);
        System.out.println("value = " + lp.value());
        double[] x = lp.primal();
        for (int i = 0; i < x.length; i++)
            System.out.println("x[" + i + "] = " + x[i]);
        double[] y = lp.dual();
        for (int j = 0; j < y.length; j++)
            System.out.println("y[" + j + "] = " + y[j]);
        lp.pintaTablaPreSimplex(lp.getTabla());
    }
    
    public double[][] getTabla(){
    	return this.a;
    }
    
    public void pintaTablaPreSimplex(double[][] b){
		int i=0;
		int longitudNumeroActual=0;
		for(i=0;i<b[0].length;i++){
			if(i<this.nVarObj) System.out.print("x"); else System.out.print("y");
			System.out.print(i);
			longitudNumeroActual=logitudNumero(i);
			if(longitudNumeroActual<7){
				for(int t=longitudNumeroActual;t<7;t++){
					System.out.print(" ");						
				}
			
			}
		}
		System.out.println();
		DecimalFormat df = new DecimalFormat("#0.0#");
		for(i=0;i<b.length;i++){
			for(int j=0;j<b[0].length;j++){
				if(b[i][j]!=EPSILON2){
					longitudNumeroActual=logitudNumero(b[i][j]);
					System.out.print(df.format(b[i][j]));
					}else{
						System.out.print("-M");
						longitudNumeroActual=-1;
				}
				if(b[i][j]<0) longitudNumeroActual++;
				if(longitudNumeroActual<5&&j<(b[0].length-2)){
					for(int t=longitudNumeroActual;t<5;t++){
						System.out.print(" ");						
					}
					
				}else if(longitudNumeroActual<5&&j<(b[0].length-1)){
					for(int t=longitudNumeroActual;t<5;t++){
						System.out.print(" ");						
					}
				}
				System.out.print(" ");
			}
			System.out.print("\n");
		}
	}
	
	private int logitudNumero(double nf)
	{
		int n=(int)nf;
		int l=0;
		if(n>0||n<0){
		n=Math.abs(n);
		for (l=0;n>0;++l)
			n/=10;
		}else{
			l=1;
		}
		//System.out.println("longitud:"+l);
		return l;			
	}

    // test client
    public static void main(String[] args) {
/*
        try                           { test1();             }
        catch (ArithmeticException e) { e.printStackTrace(); }
        System.out.println("--------------------------------");

        try                           { test2();             }
        catch (ArithmeticException e) { e.printStackTrace(); }
        System.out.println("--------------------------------");

        try                           { test3();             }
        catch (ArithmeticException e) { e.printStackTrace(); }
        System.out.println("--------------------------------");

        try                           { test4();             }
        catch (ArithmeticException e) { e.printStackTrace(); }
        System.out.println("--------------------------------");
        */
        
        try                           { test1();             }
        catch (ArithmeticException e) { e.printStackTrace(); }
        System.out.println("--------------------------------");

/*
        int M = Integer.parseInt(args[0]);
        int N = Integer.parseInt(args[1]);
        double[] c = new double[N];
        double[] b = new double[M];
        double[][] A = new double[M][N];
        for (int j = 0; j < N; j++)
            c[j] = StdRandom.uniform(1000);
        for (int i = 0; i < M; i++)
            b[i] = StdRandom.uniform(1000);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A[i][j] = StdRandom.uniform(100);
        Simplex lp = new Simplex(A, b, c);
        System.out.println(lp.value());
        */
    }

}
