package spqEval.lut;

/** 
===============================================================================
* SpermQEvaluator_.java Version 1.0.3
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation
* (http://www.gnu.org/licenses/gpl.txt )
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public
* License along with this program.  If not, see
* <http://www.gnu.org/licenses/gpl-3.0.html>.
*
* Copyright (C) 2018: Jan N. Hansen and Sebastian Raßmann;
*         research group Biophysical Imaging, Institute of Innate 
Immunity, Bonn, Germany
*         (http://www.iii.uni-bonn.de/en/wachten_lab/).
*
* Funding: DFG priority program SPP 1726 “Microswimmers"
*
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
*
* 
=============================================================================== 
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class LUT {

	String lutPath;
	int[] red, green, blue;
	int size;
	
	public LUT(String path) {
		this.lutPath = path;
		fillArrayFormated();		
	}
	
	public LUT(int [] red, int [] green, int [] blue) {
		super();
		this.red = red;
		this.green = green;
		this.blue = blue;
		size = red.length;
	}
	
	/**
	 * @deprecated
	 * @param relativeValue has to be between 0 and 1
	 * @return RGB Color Code
	 */
	
	public int getRGBValue (double relativeValue) {
		if(relativeValue > 1 || relativeValue < 0) return 0;
		int i = (int) Math.round(relativeValue*(size-1));
		return red[i]*0x10000+green[i]*0x100+blue[i];
	}
	
	/**
	 * used to determine encode relative value into RGB value from defined LUT. Methods calculates ratio between smaller and bigger param and overcomes problems with casting.
	 * @return
	 */
	
	public int getRGBValue (double a, double b) {
		double relativeValue;
		if(a < b) {	
			relativeValue = a / b;
		} 
		else {
			relativeValue = b/a;
		}
		return getRGBValue(relativeValue);
	}
	
	private void fillArrayFormated(){		
		try {
			FileReader fr = new FileReader(lutPath);
			BufferedReader br = new BufferedReader(fr);
			int counter = 0;
			String line = "";
			ArrayList<String> l = new ArrayList<String>(0);
			loop: while (true) {
				counter++;
				line = br.readLine();
				if(line.contains("")) {
					break loop;
				}
				else if(counter >= 10000) {
					System.out.println("no Stop Signal found! - used LUT.txt is not compatible!"); //	TODO throw exception
					break;
				}
				else {
					l.add(line);
				}
			}
			size = l.size();
			
			red = new int[size];			
			green = new int[size];
			blue = new int[size];
			
			String red, green, blue;
			
			for(int i = 0; i < size; i++) {
				line = l.get(i);
				red = line.substring(0,line.indexOf("	"));
				green = line.substring(line.indexOf("	")+1,line.lastIndexOf("	"));
				blue = line.substring(line.lastIndexOf("	")+1);

				this.red [i] = Integer.parseInt(red);
				this.green [i] = Integer.parseInt(green);
				this.blue [i] = Integer.parseInt(blue);
			}			
		} 
		catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
	}
	
	/**
	 * fills the Arrays with the LUT format used in the ImageJ lut files where numbers are separated with different number of space bars
	 */

	void fillArrayUnformated(){		
		try {
			FileReader fr = new FileReader(lutPath);
			BufferedReader br = new BufferedReader(fr);
			int counter = 0;
			String line = "";
			ArrayList<String> l = new ArrayList<String>(0);
			loop: while (true) {
				counter++;
				line = br.readLine();
				if(line.contains("")) {
					break loop;
				}
				else if(counter >= 10000) {
					System.out.println("no Stop Signal found! "); //	TODO throw exception
					break;
				}
				else {
					l.add(line);
				}
			}
			size = l.size();
			
			red = new int[size];			
			green = new int[size];
			blue = new int[size];
			
			String red, green, blue;
			
			for(int i = 0; i < size; i++) {
				line = l.get(i);
				blue = deleteSpacebar(line.substring(8,11));
				green = deleteSpacebar(line.substring(4,7));
				red = deleteSpacebar(line.substring(0,3));
				
				this.red [i] = Integer.parseInt(red);
				this.green [i] = Integer.parseInt(green);
				this.blue [i] = Integer.parseInt(blue);
			}			
		} 
		catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
	}

	public static String deleteSpacebar(String s) {
		if(s.contains(" ")) {
			s = s.substring(s.lastIndexOf(" ") + 1, s.length());
		}
		return s;
	}

	public static double readNumber(String s) {
		s = s.substring(s.lastIndexOf("	") + 1);
		s = s.replace(",", ".");
		return Double.parseDouble(s);
	}
	
	static void printAsArrayInitializer(LUT lut) {
		StringBuffer red =  new StringBuffer ("{");
		StringBuffer green = new StringBuffer("{");
		StringBuffer blue = new StringBuffer("{");
		for(int i = 0; i < lut.size; i++) {
			red.append(lut.red[i] + ",");
			green.append(lut.green[i] + ",");
			blue.append(lut.blue[i] + ",");
		}
		String sred = red.toString();
		String sgreen = green.toString();
		String sblue = blue.toString();
		
		sred = deleteLastChar(sred) + "};";
		sgreen = deleteLastChar(sgreen) + "};";
		sblue = deleteLastChar(sblue) + "};";
		
		System.out.println("red \n" + sred);
		System.out.println("green \n" + sgreen);
		System.out.println("blue \n" + sblue); 
	}
	
	/**
	 * used for printing new LUTs as Array initializer to copy to Java source code
	 */
	
	void printAsArrayInitializer() {
		StringBuffer red =  new StringBuffer ("{");
		StringBuffer green = new StringBuffer("{");
		StringBuffer blue = new StringBuffer("{");
		for(int i = 0; i < size; i++) {
			red.append(this.red[i] + ",");
			green.append(this.green[i] + ",");
			blue.append(this.blue[i] + ",");
		}
		String sred = red.toString();
		String sgreen = green.toString();
		String sblue = blue.toString();
		
		sred = deleteLastChar(sred) + "}";
		sgreen = deleteLastChar(sgreen) + "}";
		sblue = deleteLastChar(sblue) + "}";
		
		System.out.println("red \n" + sred);
		System.out.println("green \n" + sgreen);
		System.out.println("blue \n" + sblue); 
	}
	
	/**
	 * used for printing LUT as .txt 
	 */
	
	public void printAsFormatedTXT () {
		for(int i = 0; i < size; i++) {
			System.out.println(red[i] + "	" + green[i] + "	" + blue[i]);
		}
		System.out.println("");
	}
	
	public static String deleteLastChar (String s) {
		return s.substring(0, s.length()-2);
	}
}
