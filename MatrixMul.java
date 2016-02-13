/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package test;
import com.jopdesign.sys.*;
import util.*;

/**
 * Matrix multiplication is very easy to parallelize.
 * 
 * @author martin
 *
 */
public class MatrixMul {

	final static int N = 20;
	public static final int IO_BASE = 0xffffff80;
	public static final int IO_CNT = IO_BASE+0;
	public static int[][] arrayA;
	public static int[][] arrayB;
	public static int[][] arrayC;

	public static int rowCounter = 0;
	public static int endCalculation = 0;
	
	public MatrixMul() {
	}
	
	public String toString() {

		return "matrix multiplication";
	}
	/**
	 * Here comes the workload. Do one vector multiplication.
	 */
	
	public static void main(String[] args) {
		
		int t1, diff;
		t1 = Native.rd(IO_CNT);
		t1 = Native.rd(IO_CNT)-t1;
		diff = t1;
		
		arrayA = new int[N][N];
		arrayB = new int[N][N];
		arrayC = new int[N][N];
		int i, j, nr, val = 0;
		// set some values in the source matrices
		for (i=0; i<N; ++i) {// @WCA loop=100
			for (j=0; j<N; ++j) {// @WCA loop=100
				arrayA[i][j] = val;
				val += 12345;
				arrayB[i][j] = val;
				val += 67890;
			}
		}
		
		int[] colB;
		for (nr=0; nr<N; nr++)	{// @WCA loop=100
		for (i = 0; i < N; i++) { // @WCA loop=100
			val = 0;
			colB = arrayB[i];
			for (j = 0; j < N; j++) {// @WCA loop=100
				val += arrayA[j][nr] * colB[j];
			}
			arrayC[i][nr] = val;
		}
		}
		
		t1 = Native.rd(Const.IO_CNT)-t1;
		System.out.println(t1-diff);
	}

	/**
	 * return number of independent tasks
	 */
	public int getNrOfUnits() {
		return N;
	}

}
