package spqEval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;

import ij.IJ;
import ij.ImagePlus;
import spqEval.tools.*;

/** ===============================================================================
* SpermQEvaluator_.java Version 1.0.1
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
* 		research group Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany
* 		(http://www.iii.uni-bonn.de/en/wachten_lab/).
* 
* Funding: DFG priority program SPP 1726 “Microswimmers"
*    
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
*
* =============================================================================== */

public class Result {
	boolean valid;
	int medianSize;
	String directory;
	
	//METADATA
	double timePerFrame, frameRate;
	double calibration;
	
//	float [][] cAngleResults;
	
	public Result(String dir, int mediSize){
		valid = true;
		directory = dir;
		medianSize = mediSize;
		readMetadata();
//		cAngleResults = this.getKymoResults("cAng");
//		if(cAngleResults != null){
//			
//		}
		
	}
	
	private void readMetadata (){
		try{
			FileReader fr = new FileReader(directory + System.getProperty("file.separator") + "results.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
//			int i = 0;
			boolean settings = false;
			copyPaste: while(true){
				try {
					line = br.readLine();
					if(settings){
						if(line.contains("Sample rate [Hz]")){
							line = line.substring(line.lastIndexOf("	")+1);
							line = line.replace(",", ".");
							frameRate = Double.parseDouble(line);
							timePerFrame = 1.0/frameRate;
						}else if(line.contains("calibration")){
							line = line.substring(line.lastIndexOf("	")+1);
							line = line.replace(",", ".");
//							System.out.println(line);
							calibration = Double.parseDouble(line);
						}else if(line.startsWith("RESULTS")){
							break copyPaste;
						}
					}else if(line.startsWith("SETTINGS")){
//						System.out.println("settings true");
						settings = true;
					}
					
//					i++;
				}catch (Exception e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					System.out.println(out);	
					e.printStackTrace();
					break copyPaste;
				}
			}
			br.close();
			fr.close();
//			System.out.println("Cal " + calibration);
			if(settings = false){
				System.out.println("Settings false");
				valid = false;
				return;
			}			
		}catch(Exception e){
			e.printStackTrace();
			valid = false;
			return;
		}
	}
	
	public float [][] getHeadResults(){
		float [][] results;
		{
			LinkedList<Float> thetaResults = new LinkedList<Float>(),
					velocityResults = new LinkedList<Float>(),
					hriMaxIResults = new LinkedList<Float>();
			try{
				FileReader fr = new FileReader(directory + System.getProperty("file.separator") + "results.txt");
				BufferedReader br = new BufferedReader(fr);
				String line = "", lineh = "";
				double x,y,z,
					xa=Double.NaN,ya=Double.NaN,za=Double.NaN;
				boolean thetaAngle = false, velocity = false, hriMaxI = false;
				copyPaste: while(true){
					try {
						line = br.readLine();
						if(line == null)	break copyPaste;
						if(line.equals("")){
							thetaAngle = false;
							velocity = false; 
							hriMaxI = false;
							continue copyPaste;
						}
						if(thetaAngle){
							if(line.contains("Average found primary freq")){
								thetaAngle = false;
							}else{
								line = line.substring(line.lastIndexOf("	")+1);
								line = line.replace(",", ".");
								thetaResults.add(Float.parseFloat(line));
							}						
						}else if(line.startsWith("Angle Theta")){
							thetaAngle = true;
						}else if(velocity){
							if(line.equals("")){
								velocity = false;
							}else{
								line = line.replace(",", ".");
								z = Double.parseDouble(line.substring(line.lastIndexOf("	")+1));
								line = line.substring(0, line.lastIndexOf("	"));
								line = line.substring(0, line.lastIndexOf("	"));
								line = line.substring(0, line.lastIndexOf("	"));
								y = Double.parseDouble(line.substring(line.lastIndexOf("	")+1));
								line = line.substring(0, line.lastIndexOf("	"));
								x = Double.parseDouble(line.substring(line.lastIndexOf("	")+1));
								if(xa==Double.NaN){
									velocityResults.add(Float.NaN);
								}else{
									velocityResults.add((float)(getDistance2D(x,y,xa,ya)));	//TODO z representative?
								}
								za = z;
								ya = y;
								xa = x;
							}						
						}else if(line.contains("head position")){
							velocity = true;
						}else if(hriMaxI){
							if(line.contains("Average found primary freq")){
								hriMaxI = false;
							}else{
								line = line.replace(",", ".");
								line = line.substring(0, line.lastIndexOf("	"));
								line = line.substring(0, line.lastIndexOf("	"));							
								hriMaxIResults.add(Float.parseFloat(line.substring(line.lastIndexOf("	")+1)));
							}						
						}else if(line.contains("head rotation")){
							hriMaxI = true;
						}
						
					}catch (Exception e) {
						e.printStackTrace();
						break copyPaste;
					}
				}
				br.close();
				fr.close();
//				System.out.println("Cal " + calibration);
//				if(thetaAngle = false || ){
//					System.out.println("Settings false");
//					valid = false;
//					return null;
//				}			
			}catch(Exception e){
				e.printStackTrace();
				valid = false;
				return null;
			}
			
			if(thetaResults.size() != velocityResults.size() 
					|| thetaResults.size() != hriMaxIResults.size()){
				System.out.println("Wrong sizes");
				valid = false;
				return null;
			}
			
			results = new float [3][thetaResults.size()];
			for(int i = 0; i < thetaResults.size(); i++){
				results [0][i] = thetaResults.get(i);
				results [1][i] = velocityResults.get(i);
				results [2][i] = hriMaxIResults.get(i);
			}
			
			thetaResults.clear();
			velocityResults.clear();
			hriMaxIResults.clear();
			thetaResults = null;
			velocityResults = null;
			hriMaxIResults = null;			
		}		
		System.gc();
		return results;
	}
	
	public float [][] getHeadFrequencyResults(String headKeySeq){
		float [][] results;		
		{
			String fileName = this.getFileName(headKeySeq + "_f.txt");			
			LinkedList<Float[]> resultsList = new LinkedList<Float[]>();
			try{
				FileReader fr = new FileReader(directory + System.getProperty("file.separator") + fileName);
				BufferedReader br = new BufferedReader(fr);
				String line = "";
				boolean readNumbers = false;
				copyPaste: while(true){
					try {
						line = br.readLine();
						if(line == null)	break copyPaste;
						
						if(line.equals("")){
							readNumbers = false;
							continue copyPaste;
						}
						
						if(readNumbers){
							Float [] newFreqResult = new Float [5];
							line = line.replace(",", ".");
							newFreqResult [4] = Float.parseFloat(line.substring(line.lastIndexOf("	")+1));
							line = line.substring(0,line.lastIndexOf("	"));
							newFreqResult [3] = Float.parseFloat(line.substring(line.lastIndexOf("	")+1));
							line = line.substring(0,line.lastIndexOf("	"));
							newFreqResult [2] = Float.parseFloat(line.substring(line.lastIndexOf("	")+1));
							line = line.substring(0,line.lastIndexOf("	"));
							newFreqResult [1] = Float.parseFloat(line.substring(line.lastIndexOf("	")+1));
							line = line.substring(0,line.lastIndexOf("	"));
							newFreqResult [0] = Float.parseFloat(line.substring(line.lastIndexOf("	")+1));
							resultsList.add(newFreqResult);						
						}else if(line.startsWith("t [")){
							readNumbers = true;
						}
						
					}catch (Exception e) {
						e.printStackTrace();
						break copyPaste;
					}
				}
				br.close();
				fr.close();
			}catch(Exception e){
				e.printStackTrace();
				valid = false;
				return null;
			}
			
			results = new float [5][resultsList.size()];
			for(int i = 0; i < resultsList.size(); i++){
				for(int j = 0; j < 5; j++){
					results [j][i] = resultsList.get(i) [j];
				}				
			}
			
			resultsList.clear();
			resultsList = null;		
		}		
		System.gc();
		return results;
	}
		
	public float [][] getKymoResults(String keySeq){	//TODO
		String [] list = new File(directory).list();
		if(list.length == 0){
			System.out.println("listlength=0");
			valid = false;
			return null;
		}
		
		String fileName = "",
			fileInfoName = "";
		for(int i = 0; i < list.length; i++){
			if(list[i].endsWith(keySeq + ".tif")){
				fileName = list[i];
			}
			if(list[i].endsWith(keySeq + "_info.txt")){
				fileInfoName = list[i];
			}
		}
		
		if((fileName == "" || fileInfoName == "") && keySeq == "Z"){
			for(int i = 0; i < list.length; i++){
				if(list[i].endsWith(keySeq + "medi.tif")){
					fileName = list[i];
				}
				if(list[i].endsWith(keySeq + "medi_info.txt")){
					fileInfoName = list[i];
				}
			}
			
			if(fileName == "" && fileInfoName == ""){
				for(int i = 0; i < list.length; i++){
					if(list[i].endsWith(keySeq + "mean.tif")){
						fileName = list[i];
					}
					if(list[i].endsWith(keySeq + "mean_info.txt")){
						fileInfoName = list[i];
					}
				}
			}			
		}
		
		if((fileName == "" || fileInfoName == "")){
			System.out.println("no file Name: " + fileName + "/" + fileInfoName);
			valid = false;
			return null;	
		}
		
		FileReader fr;
		BufferedReader br;	
		String title = "", helpl;
		double minSize = 0.0, maxSize = 0.0;
		try{
			fr = new FileReader(directory + System.getProperty("file.separator") + fileInfoName);
			br = new BufferedReader(fr);
			String line = "";
			int i = 0;
			copyPaste: while(true){
				try {
					line = br.readLine();					
					if(i == 5){
						if(line.contains(",")){
							line = line.replace(",", ".");
						}
						title = "" + line;
						title = title.substring(0, title.lastIndexOf("	"));
						title = title.substring(title.indexOf("	")+1,title.lastIndexOf("	"));
						
						helpl = "" + line;
						helpl = helpl.substring(0,helpl.lastIndexOf("	"));
						minSize = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	")+1));
						
						helpl = "" + line;
						maxSize = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	")+1));
					}
					i++;
					if(line==null) break copyPaste;
				}catch (Exception e) {
					break copyPaste;
				}
			}
			br.close();
			fr.close();	
		}catch(Exception e){
			e.printStackTrace();
			valid = false;
			return null;
		}
				
		if(maxSize == 0.0 && minSize == 0.0){
			System.out.println("maxminSize = 0");
			valid = false;
			return null;	//TODO
		}
		
		//Analyze image
		ImagePlus imp = IJ.openImage(directory + System.getProperty("file.separator") + fileName);
//		imp.show();
		float value, value1, value2;
		
		float [][] results;
		float [] toSort, median = new float [medianSize]; 
		int sortCt;
		
		results = new float [imp.getWidth()][4];	//0 = min, 1 = max, 2 = median, 3 = average
		toSort = new float [imp.getHeight()];
		Arrays.fill(median, 0.0f);

//			for(int s = 0; s < imp.getNSlices(); s++){
		{
			int s = 0;
			for(int al = 0; al < imp.getWidth(); al++){
				Arrays.fill(toSort, Float.POSITIVE_INFINITY);
				sortCt = 0;
				for(int pos = 1; pos < imp.getHeight()-1; pos++){
					if(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1) > 0.0){
						value = (float) tools.getValueFromEncodedIntensity16bit(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1),
								minSize, maxSize);
						value1 = (float) tools.getValueFromEncodedIntensity16bit(imp.getStack().getVoxel(al, pos-1, imp.getStackIndex(1, 1+s, 1)-1),
								minSize, maxSize);
						value2 = (float) tools.getValueFromEncodedIntensity16bit(imp.getStack().getVoxel(al, pos+1, imp.getStackIndex(1, 1+s, 1)-1),
								minSize, maxSize);
						
						if(tools.mathAbs(value) < tools.mathAbs(value1)
								&& tools.mathAbs(value) < tools.mathAbs(value2)){
							toSort [sortCt] = value;
							sortCt++;
						}else if(tools.mathAbs(value) > tools.mathAbs(value1)
								&& tools.mathAbs(value) > tools.mathAbs(value2)){
							toSort [sortCt] = value;
							sortCt++;
						}							
					}
				}
				
				if(sortCt >= medianSize*2){
					Arrays.sort(toSort);
					
					//get minimum
					results [al][0] = tools.getMedianOfRange(toSort, 0, medianSize-1);
					
					//get maximum
					results [al][1] = tools.getMedianOfRange(toSort, sortCt-1-medianSize, sortCt-1);
				}else{
					results [al][0] = Float.NEGATIVE_INFINITY;
					results [al][1] = Float.NEGATIVE_INFINITY;
				}
				if(sortCt >= 3){
					//get median
					results [al][2] = tools.getMedianOfRange(toSort, 0, sortCt-1);
					
					//get average
					results [al][3] = tools.getAverageOfRange(toSort, 0, sortCt-1);
				}else{
					results [al][2] = Float.NEGATIVE_INFINITY;
					results [al][3] = Float.NEGATIVE_INFINITY;
				}
									
			}
		}
		imp.changes = false;
		imp.close();	
		return results;
	}
	
	public float [][][] getFreqResults(String keySeq){
		String [] list = new File(directory).list();
		if(list.length == 0){
			valid = false;
			return null;
		}
		
		String fileName = "",
			fileInfoName = "";
		for(int i = 0; i < list.length; i++){
			if(list[i].endsWith(keySeq + ".tif")){
				fileName = list[i];
			}
			if(list[i].endsWith(keySeq + "_info.txt")){
				fileInfoName = list[i];
			}
		}
		
		if(fileName == "" || fileInfoName == ""){
			valid = false;
			return null;
		}
		
		FileReader fr;
		BufferedReader br;	
		String title = "", title2 = "", helpl;
		double minSize = 0.0, maxSize = 0.0, minSize2 = 0.0, maxSize2 = 0.0;
		try{
			fr = new FileReader(directory + System.getProperty("file.separator") + fileInfoName);
			br = new BufferedReader(fr);
			String line = "";
			int i = 0;
			copyPaste: while(true){
				try {
					line = br.readLine();					
					if(i == 6){
						if(line.contains(",")){
							line = line.replace(",", ".");
						}
						title = "" + line;
						title = title.substring(0, title.lastIndexOf("	"));
						title = title.substring(title.indexOf("	")+1,title.lastIndexOf("	"));
						
						helpl = "" + line;
						helpl = helpl.substring(0,helpl.lastIndexOf("	"));
						minSize = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	")+1));
						
						helpl = "" + line;
						maxSize = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	")+1));
					}else if(i == 7){
						if(line.contains(",")){
							line = line.replace(",", ".");
						}
						title2 = "" + line;
						title2 = title2.substring(0, title2.lastIndexOf("	"));
						title2 = title2.substring(title2.indexOf("	")+1,title2.lastIndexOf("	"));
						
						helpl = "" + line;
						helpl = helpl.substring(0,helpl.lastIndexOf("	"));
						minSize2 = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	")+1));
						
						helpl = "" + line;
						maxSize2 = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	")+1));
					}
					i++;
					if(line==null) break copyPaste;
				}catch (Exception e) {
					break copyPaste;
				}
			}
			br.close();
			fr.close();
		}catch(Exception e){
			return null;
		}
		
		if(maxSize == 0.0 && minSize == 0.0){
			valid = false;
			return null;
		}
		
		//Analyze image
		ImagePlus imp = IJ.openImage(directory + System.getProperty("file.separator") + fileName);
		float value;
		
		float [][][] results;
		float [] toSort, median = new float [medianSize]; 
		int sortCt;
		
		results = new float [imp.getNSlices()][imp.getWidth()][4];	//0 = min, 1 = max, 2 = median, 3 = average
		toSort = new float [imp.getHeight()];
		Arrays.fill(median, 0.0f);

		for(int s = 0; s < imp.getNSlices(); s++){
			for(int al = 0; al < imp.getWidth(); al++){
				Arrays.fill(toSort, Float.POSITIVE_INFINITY);
				sortCt = 0;
				for(int pos = 0; pos < imp.getHeight(); pos++){
//					IJ.log("a " + al + " - s " + s + " - pos " + pos + ": " + imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1));
					if(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1) > 0.0){
						if(s==0 || s == 2 || s == 4){	//0,2,4 = frequency
							value = (float) tools.getValueFromEncodedIntensity16bit(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1),
									minSize, maxSize);
						}else{	//1,3 = amplitude
							value = (float) tools.getValueFromEncodedIntensity16bit(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1),
									minSize2, maxSize2);
						}	
						toSort [sortCt] = value;
						sortCt++;
					}
				}
				
				if (sortCt == 0){
					results [s][al][0] = Float.NEGATIVE_INFINITY;
					results [s][al][1] = Float.NEGATIVE_INFINITY;
					results [s][al][2] = Float.NEGATIVE_INFINITY;
					results [s][al][3] = Float.NEGATIVE_INFINITY;
//					IJ.log("sort count = 0");
				}else{
					if(sortCt >= medianSize*2){
						Arrays.sort(toSort);
						
						//get minimum
						results [s][al][0] = tools.getMedianOfRange(toSort, 0, medianSize-1);
						
						//get maximum
						results [s][al][1] = tools.getMedianOfRange(toSort, sortCt-1-medianSize, sortCt-1);
					}else{
						results [s][al][0] = Float.NEGATIVE_INFINITY;
						results [s][al][1] = Float.NEGATIVE_INFINITY;
					}
					
					//get median
					results [s][al][2] = tools.getMedianOfRange(toSort, 0, sortCt-1);
					
					//get average
					results [s][al][3] = tools.getAverageOfRange(toSort, 0, sortCt-1);
				}			
				
			}
		}
		imp.changes = false;
		imp.close();
//		IJ.log(keySeq + " put out");
		return results;
	}
	
	double getDistance(double x1, double y1, double z1, double x2, double y2, double z2){
		return Math.sqrt(Math.pow(x1-x2, 2.0)
				+ Math.pow(y1-y2, 2.0)
				+ Math.pow(z1-z2, 2.0));
	}
	
	double getDistance2D(double x1, double y1, double x2, double y2){
		return Math.sqrt(Math.pow(x1-x2, 2.0)
				+ Math.pow(y1-y2, 2.0));
	}
	
	private String getFileName (String endsWith){
		String [] list = new File(directory).list();
		if(list.length == 0){
			System.out.println("listlength=0");
			valid = false;
			return null;
		}
		
		String fileName = "";
		for(int i = 0; i < list.length; i++){
			if(list[i].endsWith(endsWith)){
				fileName = list[i];
			}
		}
		
		if((fileName == "")){
			System.out.println("no file name found: " + fileName);
			valid = false;
			return null;				
		}
		return fileName;
	}
}
