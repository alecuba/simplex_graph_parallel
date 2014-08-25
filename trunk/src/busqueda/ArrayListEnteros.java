package busqueda;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayListEnteros {
	ArrayList<Integer> verticesTemp = new ArrayList<Integer>();
	
	public void add(int valor){
		verticesTemp.add(valor);
	}
	
	public void addAll(ArrayListEnteros verticesActual){
		verticesTemp.addAll(verticesActual.getLista());
	}
	
	public ArrayList<Integer> getLista(){
		return verticesTemp;
	}
	
	public int getUltimoElemento(){
		return verticesTemp.get(verticesTemp.size()-1);
	}
	
	public boolean compruebaSiContiene(int i) {
		boolean contiene = false;
		Iterator<Integer> itr = verticesTemp.iterator();
		while (itr.hasNext() && !contiene) {
			if (itr.next() == i)
				contiene = true;
		}
		return contiene;
	}

}
