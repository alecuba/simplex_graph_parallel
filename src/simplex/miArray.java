package simplex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public class miArray {
	protected int length = 0;       // array length
	 protected ArrayList<Object> array = null;    // internal array
	 protected int type = -1;        // 0 double, 1 Double, 2 float, 3 Float, 4 long, 5 Long, 6 int, 7 Integer, 8 short, 9 Short, 10 byte, 11 Byte
     // 12 BigDecimal, 13 BigInteger, 14 Complex, 15 Phasor, 16 char, 17 Character, 18 String
	 protected int[] originalTypes = null;     // list of entered types in the array
	 protected ArrayList<Object> minmax = new ArrayList<Object>(2);      // element at 0 -  maximum value
     // element at 1 -  minimum value
	 
	 protected int maxIndex = -1;                        // index of the maximum value array element
	    protected int minIndex = -1;                        // index of the minimum value array element
	    protected String[] typeName = {"double", "Double", "float", "Float", "long", "Long", "int", "Integer", "short", "Short", "byte", "Byte", "BigDecimal", "BigInteger", "Complex", "Phasor", "char", "Character", "String"};
	    protected boolean suppressMessages = false;         // = true when suppress 'possible loss of precision' messages has been set
	 
	 public miArray(int[] array){
	        this.length = array.length;
	        this.array = new ArrayList<Object>(this.length);
	        this.type = 6;
	        for(int i=0; i<this.length; i++)this.array.add(new Integer(array[i]));
	        this.originalTypes = new int[this.length];
	        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
	        this.minmax();
	    }

	 // MAXIMUM AND MINIMUM
	    // protected method to call search method for maximum and minimum values
	    // called by public methods
	    protected void minmax(){
	        int[] maxminIndices = new int[2];
	        miArray.findMinMax(this.getArray_as_Object(), this.minmax, maxminIndices, this.typeName, this.type);
	        this.maxIndex = maxminIndices[0];
	        this.minIndex = maxminIndices[1];
	    }
	    public Object[] getArray_as_Object(){
	        Object[] arrayo= new Object[this.length];
	        for(int i=0; i<this.length; i++)arrayo[i] = this.array.get(i);
	        return arrayo;
	    }
	    
	    // protected method that finds the maximum and minimum values
	    // called by protected method minmax which is called by public methods
	    protected static void findMinMax(Object[] arrayo, ArrayList<Object> minmaxx, int[] maxminIndices, String[] aTypeName, int aType){
	        int maxIndexx = 0;
	        int minIndexx = 0;
	        int arraylength = arrayo.length;
	        switch(aType){
	            case 0:
	            case 1: double[] arrayD = new double[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayD[i] = ((Double)arrayo[i]).doubleValue();
	                    double amaxD=arrayD[0];
	                    double aminD=arrayD[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayD[i]>amaxD){
	                            amaxD = arrayD[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayD[i]<aminD){
	                            aminD = arrayD[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Double(amaxD));
	                    minmaxx.add(new Double(aminD));
	                    break;
	            case 4:
	            case 5: long[] arrayL = new long[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayL[i] = ((Long)arrayo[i]).longValue();
	                    long amaxL=arrayL[0];
	                    long aminL=arrayL[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayL[i]>amaxL){
	                            amaxL = arrayL[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayL[i]<aminL){
	                            aminL = arrayL[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Long(amaxL));
	                    minmaxx.add(new Long(aminL));
	                    break;
	            case 2:
	            case 3: float[] arrayF = new float[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayF[i] = ((Float)arrayo[i]).floatValue();
	                    float amaxF=arrayF[0];
	                    float aminF=arrayF[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayF[i]>amaxF){
	                            amaxF = arrayF[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayF[i]<aminF){
	                            aminF = arrayF[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Float(amaxF));
	                    minmaxx.add(new Float(aminF));
	                    break;
	            case 6:
	            case 7: int[] arrayI = new int[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayI[i] = ((Integer)arrayo[i]).intValue();
	                    int amaxI=arrayI[0];
	                    int aminI=arrayI[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayI[i]>amaxI){
	                            amaxI = arrayI[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayI[i]<aminI){
	                            aminI = arrayI[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Integer(amaxI));
	                    minmaxx.add(new Integer(aminI));
	                    break;
	            case 8:
	            case 9: short[] arrayS = new short[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayS[i] = ((Short)arrayo[i]).shortValue();
	                    short amaxS=arrayS[0];
	                    short aminS=arrayS[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayS[i]>amaxS){
	                            amaxS = arrayS[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayS[i]<aminS){
	                            aminS = arrayS[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Short(amaxS));
	                    minmaxx.add(new Short(aminS));
	                    break;
	            case 10:
	            case 11: byte[] arrayB = new byte[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayB[i] = ((Byte)arrayo[i]).byteValue();
	                    byte amaxB=arrayB[0];
	                    byte aminB=arrayB[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayB[i]>amaxB){
	                            amaxB = arrayB[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayB[i]<aminB){
	                            aminB = arrayB[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Byte(amaxB));
	                    minmaxx.add(new Byte(aminB));
	                    break;
	            case 12: BigDecimal[] arrayBD = new BigDecimal[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayBD[i] = (BigDecimal)arrayo[i];
	                    BigDecimal amaxBD = arrayBD[0];
	                    BigDecimal aminBD = arrayBD[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayBD[i].compareTo(amaxBD)==1){
	                            amaxBD = arrayBD[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayBD[i].compareTo(aminBD)==-1){
	                            aminBD = arrayBD[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(amaxBD);
	                    minmaxx.add(aminBD);
	                    break;
	            case 13: BigInteger[] arrayBI= new BigInteger[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayBI[i] = (BigInteger)arrayo[i];
	                    BigInteger amaxBI = arrayBI[0];
	                    BigInteger aminBI = arrayBI[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayBI[i].compareTo(amaxBI)==1){
	                            amaxBI = arrayBI[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayBI[i].compareTo(aminBI)==-1){
	                            aminBI = arrayBI[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(amaxBI);
	                    minmaxx.add(aminBI);
	                    break;
	            case 16:
	            case 17: int[] arrayInt = new int[arraylength];
	                    for(int i=0; i<arraylength; i++)arrayInt[i] = (int)(((Character)arrayo[i]).charValue());
	                    int amaxInt=arrayInt[0];
	                    int aminInt=arrayInt[0];
	                    maxIndexx = 0;
	                    minIndexx = 0;
	                    for(int i=1; i<arraylength; i++){
	                        if(arrayInt[i]>amaxInt){
	                            amaxInt = arrayInt[i];
	                            maxIndexx = i;
	                        }
	                        if(arrayInt[i]<aminInt){
	                            aminInt = arrayInt[i];
	                            minIndexx = i;
	                        }
	                    }
	                    minmaxx.add(new Character((char)amaxInt));
	                    minmaxx.add(new Character((char)aminInt));
	                    break;
	            case 14:
	            case 15:
	            case 18: System.out.println("ArrayMaths:  getMaximum_... or getMinimum_... (findMinMax): the " + aTypeName[aType] + " is not a numerical type for which a maximum or a minimum is meaningful/supported");
	                    break;
	            default: throw new IllegalArgumentException("Data type not identified by this method");
	        }
	        maxminIndices[0] = maxIndexx;
	        maxminIndices[1] = minIndexx;
	    }
	    
	    public int getMaximum_as_int(){
	    	int max = ((Integer)this.minmax.get(0)).intValue();
	        
	      return max;
	    }
	    public double[] getArray_as_double(){
	        double[] retArray = new double[this.length];
	        return retArray;
	    }
}
