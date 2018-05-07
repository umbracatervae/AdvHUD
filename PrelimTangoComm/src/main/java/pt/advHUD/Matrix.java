package pt.advHUD;

/**
 * Created by Ryan on 3/13/2017.
 */

public class Matrix {
    private int numRows;
    private int numCols;
    private double[] elements;

    public Matrix(int nr,int nc, double[] el) {
        numCols = nc;
        numRows = nr;

        elements = new double[nc*nr];

        for (int i = 0; i < nc*nr; i++)
            elements[i] = el[i];
    }

    public Matrix(int nr, int nc) {
        numCols = nc;
        numRows = nr;


        elements = new double[nc*nr];
        for (int i = 0; i < nc*nr; i++)
            elements[i] = 0;
    }

    public double[] getElements() {
        return elements;
    }
    
    public double getElement(int r, int c) {
        return elements[c + r*numCols];
    }
    
    public double getElement(int pos) {
        return elements[pos];
    }
    
    public void setElement(double val, int r, int c) {
        elements[c + r*numCols] = val;
    }
    
    public void setElement(double val, int pos) {
        elements[pos] = val;
    }

   Matrix multiply(Matrix rhs) throws Exception {
       if (numCols != rhs.numRows)
           throw new Exception("Illegal matrix dimensions.");
       
       Matrix out = new Matrix(numRows, rhs.numCols);
       
        for (int i = 0; i < numRows; i++) { 
            for (int j = 0; j < rhs.numCols; j++) { 
                for (int k = 0; k < numCols; k++) { 
                    out.setElement(out.getElement(i, j) + getElement(i, k) * rhs.getElement(k, j), i, j);
                }
            }
        }
       
//        for(int i =0; i< C.numRows; i++){
//            for( int j=0; j<C.numCols; j++){
//                for( int k=0; k < A.numRows; k++){
//                    C.elements[i][j] += (A.elements[i][k] * rhs.elements[k][j]);
//                }
//            }
//        }
       return out; // returns matrix
   }
}
