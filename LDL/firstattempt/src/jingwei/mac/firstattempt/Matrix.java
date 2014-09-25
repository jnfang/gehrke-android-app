package jingwei.mac.firstattempt;

import java.util.ArrayList;

//creates an ordered list of integers that imitates a matrix
	public class Matrix{
		
		private int matRows, matCols;
		private ArrayList <Integer> matArray;
		
		public Matrix(int rows, int cols){
			matRows = rows;
			matCols = cols;
			matArray = new ArrayList <Integer>(rows*cols);
			for (int i = 0; i<rows*cols; i++){
				matArray.add(0);
			}
		}
		
		public int get(int i, int j) throws ArrayIndexOutOfBoundsException{
			return matArray.get(i*matCols + j);
		}
		
		//i = row, j = col
		public void put(int i, int j, int val){
			//System.out.println(matArray.size());
			matArray.set(i*matCols + j, val);
		}
		
		public Matrix cloneMatrix(){
			Matrix clone = new Matrix(matRows, matCols);
			for (int i = 0; i < matRows; i++){
				for (int j = 0; j < matCols; j++){
					clone.put(i, j, get(i, j));
				}
			}
			return clone;
		}
		
		public Matrix compressMatrix(int newRows, int newCols){
			Matrix compressedMatrix = new Matrix(newRows, newCols);
			for (int row = 0; row < newRows; row ++){
				for (int col = 0; col < newCols; col ++){
					int oldRow = row*matRows/newRows;
					int oldCol = col*matCols/newCols;
					if (oldRow < matRows && oldCol < matCols){
						compressedMatrix.put(row, col, get(row*matRows/newRows, col*matCols/newCols));
					}
				}
			}
			
			return compressedMatrix;
		}
		
		public Matrix subMatrix(int ceiling, int floor, int left, int right){
			int rows = floor - ceiling + 1;
			int cols = right - left + 1;
			Matrix subMatrix = new Matrix (floor - ceiling +1, right - left + 1);
			for (int row = 0 ; row < rows; row ++){
				for (int col = 0; col < cols; col ++){
					subMatrix.put(row, col, get(row + ceiling, col + left));
				}
			}
			
			return subMatrix;
		}
		
	}
