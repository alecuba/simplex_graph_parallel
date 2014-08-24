package busqueda;

import java.util.ArrayList;

public class Camino {
	 public int idcliente;
	   public int idcamino;
	   public ArrayList<Integer> vertices=new ArrayList<Integer>();
	   public ArrayList<CaracteristicasSeccion> caracteristicas=new ArrayList<CaracteristicasSeccion>();
	private class CaracteristicasSeccion{
		public int idSeccion;
		public float coste;
		public int maximo;
	}
}
