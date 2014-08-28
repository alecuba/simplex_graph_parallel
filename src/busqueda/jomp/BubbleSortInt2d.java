package busqueda.jomp;

public class BubbleSortInt2d {

    public int[][] sort(int[][] resultados){
    int i=0;
    int j=0;
    /*//omp parallel for private(i,j)*/
    for (i = 0; i < resultados.length-1; i++){
        for(j = 0; j < resultados.length-1; j++){
        	if(resultados[j+1][0]<resultados[j][0]){
              int[] temp = resultados[j];
              resultados[j] = resultados[j+1];
              resultados[j+1] = temp;
            }
        }
    }
	return resultados;
}}
