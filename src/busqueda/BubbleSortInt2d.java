package busqueda;

import busqueda.BubbleSortInt2d;

public class BubbleSortInt2d {

	public int[][] sort(int[][] resultados){
	    for (int ij = 0; ij < (resultados.length-1)* (resultados.length-1); ij++){
	        	int i = ij / (resultados.length-1);
	        	int j= ij % (resultados.length-1);
	        	//System.out.println("BubbleSort soy thread "+jomp.runtime.OMP.getThreadNum());
	        	if(resultados[j+1][0]<resultados[j][0]){
	              int[] temp = resultados[j];
	              resultados[j] = resultados[j+1];
	              resultados[j+1] = temp;
	        		}
	            }
		 //System.out.println("Final bublesort");
			return resultados;
	}}
