package spqEval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;

import ij.IJ;
import ij.ImagePlus;
import spqEval.tools.*;

/**
 * =============================================================================
 * == SpermQEvaluator_.java Version 1.0.6
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation (http://www.gnu.org/licenses/gpl.txt )
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 * Copyright (C) 2018: Jan N. Hansen and Sebastian Raßmann; research group
 * Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany
 * (http://www.iii.uni-bonn.de/en/wachten_lab/).
 * 
 * Funding: DFG priority program SPP 1726 “Microswimmers"
 * 
 * For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
 *
 * =============================================================================
 * ==
 */

public class Result {
	boolean valid;
	String directory;
	double threshold;
	// METADATA
	double timePerFrame, frameRate;
	double calibration;

	// float [][] cAngleResults;

	public Result(String dir, double coverageThreshold) {
		valid = true;
		directory = dir;
		threshold = coverageThreshold;
		readMetadata();
	}

	private void readMetadata() {
		try {
			FileReader fr = new FileReader(directory + System.getProperty("file.separator") + "results.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			// int i = 0;
			boolean settings = false;
			copyPaste: while (true) {
				try {
					line = br.readLine();
					if (settings) {
						if (line.contains("Sample rate [Hz]")) {
							line = line.substring(line.lastIndexOf("	") + 1);
							line = line.replace(",", ".");
							frameRate = Double.parseDouble(line);
							timePerFrame = 1.0 / frameRate;
						} else if (line.contains("calibration")) {
							line = line.substring(line.lastIndexOf("	") + 1);
							line = line.replace(",", ".");
							// System.out.println(line);
							calibration = Double.parseDouble(line);
						} else if (line.startsWith("RESULTS")) {
							break copyPaste;
						}
					} else if (line.startsWith("SETTINGS")) {
						// System.out.println("settings true");
						settings = true;
					}

					// i++;
				} catch (Exception e) {
					String out = "";
					for (int err = 0; err < e.getStackTrace().length; err++) {
						out += " \n " + e.getStackTrace()[err].toString();
					}
					System.out.println(out);
					e.printStackTrace();
					break copyPaste;
				}
			}
			br.close();
			fr.close();
			// System.out.println("Cal " + calibration);
			if (settings = false) {
				System.out.println("Settings false");
				valid = false;
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			valid = false;
			return;
		}
	}

	public float[][] getHeadResults() {
		float[][] results;
		{
			LinkedList<Float> thetaResults = new LinkedList<Float>(), 
					velocityResults = new LinkedList<Float>(),
					headXResults = new LinkedList<Float>(),
					headYResults = new LinkedList<Float>(),
					headZResults = new LinkedList<Float>(),
					hriMaxIResults = new LinkedList<Float>();
			LinkedList<Integer> thetaResultsTime = new LinkedList<Integer>(),
					velocityResultsTime = new LinkedList<Integer>(), hriMaxIResultsTime = new LinkedList<Integer>();
			try {
				FileReader fr = new FileReader(directory + System.getProperty("file.separator") + "results.txt");
				BufferedReader br = new BufferedReader(fr);
				String line = "", lineh = "";
				double x, y, z, xa = Double.NaN, ya = Double.NaN, za = Double.NaN;
				boolean thetaAngle = false, velocity = false, hriMaxI = false;
				copyPaste: while (true) {
					try {
						line = br.readLine();
						if (line == null)
							break copyPaste;
						if (line.equals("")) {
							thetaAngle = false;
							velocity = false;
							hriMaxI = false;
							continue copyPaste;
						}
						if (thetaAngle) {
							if (line.contains("Average found primary freq")) {
								thetaAngle = false;
							} else {
								thetaResultsTime.add(Integer.parseInt(line.substring(1, line.lastIndexOf("	"))));
								// System.out.println("thetaT " +
								// Integer.parseInt(line.substring(1,
								// line.lastIndexOf(" "))));
								line = line.substring(line.lastIndexOf("	") + 1);
								line = line.replace(",", ".");
								thetaResults.add(Float.parseFloat(line));
							}
						} else if (line.startsWith("Angle Theta")) {
							thetaAngle = true;
						} else if (velocity) {
							if (line.equals("")) {
								velocity = false;
							} else {
								lineh = line.substring(line.indexOf("	") + 1);
								velocityResultsTime.add(Integer.parseInt(lineh.substring(0, lineh.indexOf("	"))));
								// System.out.println("hvelo " +
								// Integer.parseInt(lineh.substring(0,lineh.indexOf("
								// "))));
								line = line.replace(",", ".");
								z = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
								line = line.substring(0, line.lastIndexOf("	"));
								line = line.substring(0, line.lastIndexOf("	"));
								line = line.substring(0, line.lastIndexOf("	"));
								y = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
								line = line.substring(0, line.lastIndexOf("	"));
								x = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
								if (Double.isNaN(xa)) {
									velocityResults.add(Float.NaN);
								} else {
									velocityResults.add((float) (getDistance2D(x, y, xa, ya)));
								}
								headXResults.add((float) (x));
								headYResults.add((float) (y));
								headZResults.add((float) (z));
								za = z;
								ya = y;
								xa = x;
							}
						} else if (line.contains("head position")) {
							velocity = true;
						} else if (hriMaxI) {
							if (line.contains("Average found primary freq")) {
								hriMaxI = false;
							} else {
								lineh = line.substring(line.indexOf("	") + 1);
								hriMaxIResultsTime.add(Integer.parseInt(lineh.substring(0, lineh.indexOf("	"))));
								// System.out.println("hri " +
								// Integer.parseInt(lineh.substring(0,lineh.indexOf("
								// "))));
								line = line.replace(",", ".");
								line = line.substring(0, line.lastIndexOf("	"));
								line = line.substring(0, line.lastIndexOf("	"));
								hriMaxIResults.add(Float.parseFloat(line.substring(line.lastIndexOf("	") + 1)));
								// if(hriMaxIResultsTime.getLast() == 0)
								// System.out.println("hri0 " +
								// hriMaxIResults.getLast());
							}
						} else if (line.contains("head rotation")) {
							hriMaxI = true;
						}

					} catch (Exception e) {
						String out = "";
						for (int err = 0; err < e.getStackTrace().length; err++) {
							out += " \n " + e.getStackTrace()[err].toString();
						}
						System.out.println(out);
						break copyPaste;
					}
				}
				br.close();
				fr.close();
			} catch (Exception e) {
				String out = "";
				for (int err = 0; err < e.getStackTrace().length; err++) {
					out += " \n " + e.getStackTrace()[err].toString();
				}
				System.out.println(out);
				valid = false;
				return null;
			}

			int maxSize = 0;
			for (int i = 0; i < thetaResults.size(); i++) {
				if (thetaResultsTime.get(i) > maxSize)
					maxSize = thetaResultsTime.get(i);
			}
			for (int i = 0; i < velocityResults.size(); i++) {
				if (velocityResultsTime.get(i) > maxSize)
					maxSize = velocityResultsTime.get(i);
			}
			for (int i = 0; i < hriMaxIResults.size(); i++) {
				if (hriMaxIResultsTime.get(i) > maxSize)
					maxSize = hriMaxIResultsTime.get(i);
			}
			maxSize += 1;

			results = new float[6][maxSize];
			Arrays.fill(results[0], Float.NEGATIVE_INFINITY);
			Arrays.fill(results[1], Float.NEGATIVE_INFINITY);
			Arrays.fill(results[2], Float.NEGATIVE_INFINITY);
			Arrays.fill(results[3], Float.NEGATIVE_INFINITY);
			Arrays.fill(results[4], Float.NEGATIVE_INFINITY);
			Arrays.fill(results[5], Float.NEGATIVE_INFINITY);
			for (int i = 0; i < thetaResults.size(); i++) {
//				if (!Float.isNaN(results[0][thetaResultsTime.get(i)])) {
					results[0][thetaResultsTime.get(i)] = thetaResults.get(i);
//				}
			}
			for (int i = 0; i < velocityResults.size(); i++) {
//				if (!Float.isNaN(results[1][velocityResultsTime.get(i)])) {
					results[1][velocityResultsTime.get(i)] = velocityResults.get(i);
//				}
			}			
			for (int i = 0; i < hriMaxIResults.size(); i++) {
//				if (!Float.isNaN(results[2][hriMaxIResultsTime.get(i)])) {
					results[2][hriMaxIResultsTime.get(i)] = hriMaxIResults.get(i);
//				}
				// System.out.println("T" + hriMaxIResultsTime.get(i) + ": " +
				// results [2][hriMaxIResultsTime.get(i)]);
			}
			for (int i = 0; i < headXResults.size(); i++) {
//				if (!Float.isNaN(results[3][velocityResultsTime.get(i)])) {
					results[3][velocityResultsTime.get(i)] = headXResults.get(i);
//				}
			}
			for (int i = 0; i < headYResults.size(); i++) {
//				if (!Float.isNaN(results[3][velocityResultsTime.get(i)])) {
					results[4][velocityResultsTime.get(i)] = headYResults.get(i);
//				}
			}
			for (int i = 0; i < headZResults.size(); i++) {
//				if (!Float.isNaN(results[3][velocityResultsTime.get(i)])) {
					results[5][velocityResultsTime.get(i)] = headZResults.get(i);
//				}
			}
			
			thetaResults.clear();
			velocityResults.clear();
			hriMaxIResults.clear();
			headXResults.clear();
			headYResults.clear();
			headZResults.clear();
			thetaResults = null;
			velocityResults = null;
			hriMaxIResults = null;
			headXResults = null;
			headYResults = null;
			headZResults = null;
			
			thetaResultsTime.clear();
			velocityResultsTime.clear();
			hriMaxIResultsTime.clear();
			thetaResultsTime = null;
			velocityResultsTime = null;
			hriMaxIResultsTime = null;
		}
		System.gc();
		return results;
	}

	public double [][] getHeadFrequencyResults(String headKeySeq) {
		double [][] results;
		{
			String fileName = this.getFileName(headKeySeq + "_f.txt");
			LinkedList<Double[]> resultsList = new LinkedList<Double[]>();
			try {
				FileReader fr = new FileReader(directory + System.getProperty("file.separator") + fileName);
				BufferedReader br = new BufferedReader(fr);
				String line = "";
				boolean readNumbers = false;
				copyPaste: while (true) {
					try {
						line = br.readLine();
						if (line == null)
							break copyPaste;

						if (line.equals("")) {
							readNumbers = false;
							continue copyPaste;
						}

						if (readNumbers) {
							Double[] newFreqResult = new Double[5];
							line = line.replace(",", ".");
							newFreqResult[4] = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
							line = line.substring(0, line.lastIndexOf("	"));
							newFreqResult[3] = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
							line = line.substring(0, line.lastIndexOf("	"));
							newFreqResult[2] = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
							line = line.substring(0, line.lastIndexOf("	"));
							newFreqResult[1] = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
							line = line.substring(0, line.lastIndexOf("	"));
							newFreqResult[0] = Double.parseDouble(line.substring(line.lastIndexOf("	") + 1));
							resultsList.add(newFreqResult);
						} else if (line.startsWith("t [")) {
							readNumbers = true;
						}

					} catch (Exception e) {
						e.printStackTrace();
						break copyPaste;
					}
				}
				br.close();
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
				valid = false;
				return null;
			}

			results = new double [5][resultsList.size()];
			for (int i = 0; i < resultsList.size(); i++) {
				for (int j = 0; j < 5; j++) {
					results[j][i] = resultsList.get(i)[j];
				}
			}

			resultsList.clear();
			resultsList = null;
		}
		System.gc();
		return results;
	}

	/**
	 * From Version v1.0.4: get data from the text file, use double to increase precision
	 */
	public double [][] getFlagellarParameterResult(String keySeq, int slicesPerCycle){
//		System.out.println("get flagellar param " + keySeq);
		String [] list = new File(directory).list();
		if(list.length == 0){
			System.out.println("listlength=0");
			valid = false;
			return null;
		}
		
		String fileName = "";
		for(int i = 0; i < list.length; i++){
			if(list[i].endsWith(keySeq + ".txt")){
				fileName = list[i];
			}
		}
		
		if(fileName == "" && keySeq == "Z"){
			for(int i = 0; i < list.length; i++){
				if(list[i].endsWith(keySeq + "medi.txt")){
					fileName = list[i];
				}
			}
			
			if(fileName == ""){
				for(int i = 0; i < list.length; i++){
					if(list[i].endsWith(keySeq + "mean.txt")){
						fileName = list[i];
					}
				}
			}			
		}
		
		if(fileName == ""){
			System.out.println("no file Name: " + fileName);
			valid = false;
			return null;	
		}
		
		FileReader fr;
		BufferedReader br;	
		int maxFrameNr = 0, maxArcLengthStep = 0;
		double maxArcLength = 0;
		boolean start = false;
		
		//read basic parameters
		try{
			fr = new FileReader(directory + System.getProperty("file.separator") + fileName);
			br = new BufferedReader(fr);
			String line = "";
			start = false;
			copyPaste: while(true){
				try {
					line = br.readLine();	
					if(line==null) break copyPaste;
					if(start){
						maxFrameNr = Integer.parseInt(line.substring(0,line.indexOf("	")));
					}
					if(line.contains("frame")){
						if(line.contains(",")){
							line = line.replace(",", ".");
						}
						maxArcLength = Double.parseDouble(line.substring(line.lastIndexOf("	")+1));
//						System.out.println("max al = " + maxArcLength);
						maxArcLengthStep = line.length() - line.replaceAll("	", "").length();
//						System.out.println("max al step = " + maxArcLengthStep);
						start = true;
					}
				}catch (Exception e) {
					break copyPaste;
				}
			}
			br.close();
			fr.close();	
			maxFrameNr ++;
//			System.out.println("max frame = " + maxFrameNr);
		}catch(Exception e){
			e.printStackTrace();
			valid = false;
			return null;
		}
		
		//read results
		double kymograph [][] = new double [maxFrameNr][maxArcLengthStep];
		for(int x = 0; x < kymograph.length; x++){
			Arrays.fill(kymograph[x], Double.NaN);
		}
		
		try{
			fr = new FileReader(directory + System.getProperty("file.separator") + fileName);
			br = new BufferedReader(fr);
			String line = "";
			int frameNr, alNr;
			start = false;
			copyPaste: while(true){
				try {
					line = br.readLine();
					if(line.contains(",")){
						line = line.replace(",", ".");
					}
					if(line==null) break copyPaste;
					if(start){
						if(line.length()==0)	break copyPaste;
						frameNr = Integer.parseInt(line.substring(0,line.indexOf("	")));
						alNr = line.length() - line.replaceAll("	", "").length();
						for(int i = 0; i < alNr; i++){							
							line = line.substring(line.indexOf("	")+1);
							if(line.contains("	")){
								if(!line.substring(0,line.indexOf("	")).equals("")){
									kymograph [frameNr][i] = Double.parseDouble(line.substring(0,line.indexOf("	")));
								}					
							}else if(!line.equals("")){
								kymograph [frameNr][i] = Double.parseDouble(line);
							}
						}						
					}
					if(line.contains("frame")){						
						start = true;
					}					
				}catch (Exception e) {
//					System.out.println("Problem " + line);
//					e.printStackTrace();
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
		
		//Analyze image
		double [][] results;
		double [] data = new double [maxFrameNr]; 
		int dataCount;
		results = new double [maxArcLengthStep][4];	//0 = min, 1 = max, 2 = median, 3 = average
		
		/**For new method to determine avg, medi, min, max from version v1.0.3 on*
		 * Method: get minimum as median value of mins determined in chunks of 3 beat cycles*
		 * Apply a threshold. Only chunks that have at least a fraction of values included defined by the threshold will be used* 
		 * */
//		System.out.println("h " + (double)imp.getHeight());
//		System.out.println("spc3: " + ((double)slicesPerCycle*3.0));
		int numberOfChunks = (int)((double)maxFrameNr / ((double)slicesPerCycle*3.0))+1;
//		System.out.println("noc " + numberOfChunks);
		double chunkMin [] = new double [numberOfChunks];
		double chunkMax [] = new double [numberOfChunks];	
		int counter, chunkCounter, chunkPos, includedChunks;
		{
			int s = 0;
			for(int al = 0; al < maxArcLengthStep; al++){
				Arrays.fill(data, Double.POSITIVE_INFINITY);
				dataCount = 0;
				
				includedChunks = 0;
				chunkPos = 0;
				counter = 0;
				chunkCounter = 0;
				
				Arrays.fill(chunkMin, Double.POSITIVE_INFINITY);
				Arrays.fill(chunkMax, Double.NEGATIVE_INFINITY);
				for(int pos = 0; pos < maxFrameNr; pos++){
					if(!Double.isNaN(kymograph[pos][al])){
						data [pos] = kymograph[pos][al];
						dataCount++;
						if(chunkMin[chunkPos]>data[pos]){
							chunkMin[chunkPos]=data[pos];
						}
						
						if(chunkMax[chunkPos]<data[pos]){
							chunkMax[chunkPos]=data[pos];
						}
						chunkCounter++;
					}
					counter ++;
					
					if(counter == slicesPerCycle*3){
						//Check if threshold fits						
						if(chunkCounter / (double)(slicesPerCycle*3.0) >= threshold){
							includedChunks ++;
						}else{
							chunkMin[chunkPos] = Double.POSITIVE_INFINITY;
							chunkMax[chunkPos] = Double.NEGATIVE_INFINITY;
						}
						
						//go to next chunk
						chunkPos++;
						//Reset
						counter = 0;
					}else if(pos == maxFrameNr-1){
						if(chunkCounter / (double)(maxFrameNr-(chunkPos*slicesPerCycle*3)) >= threshold){
							includedChunks ++;
						}else{
							chunkMin[chunkPos] = Double.POSITIVE_INFINITY;
							chunkMax[chunkPos] = Double.NEGATIVE_INFINITY;
						}
						
						//go to next chunk
						chunkPos++;
						//Reset
						counter = 0;
					}
				}
				Arrays.sort(chunkMin);
				Arrays.sort(chunkMax);
				
				if(includedChunks>0){					
					//get minimum
					results [al][0] = tools.getMedianOfRange(chunkMin, 0, includedChunks-1);
					
					//get maximum
					results [al][1] = tools.getMedianOfRange(chunkMax, chunkMax.length-includedChunks, chunkMax.length-1);
				}else{
					results [al][0] = Double.NEGATIVE_INFINITY;
					results [al][1] = Double.NEGATIVE_INFINITY;
				}
				
				if((double) dataCount / (double)maxFrameNr >= threshold){
					Arrays.sort(data);
					//get median
					results [al][2] = tools.getMedianOfRange(data, 0, dataCount-1);
					
					//get average
					results [al][3] = tools.getAverageOfRange(data, 0, dataCount-1);
					
				}else{
//					System.out.println(al + ": exclude data because " + (double) dataCount + "/" + (double)imp.getHeight() + "<" + threshold);
					results [al][2] = Double.NaN;
					results [al][3] = Double.NaN;
				}
//				if(al == 50) 
//					System.out.println(keySeq + "  ***  " + al + "min=" + results [al][0]+ " max=" + results [al][1] + ": median=" + results [al][2]+ " avg=" + results [al][3]);
			}
		}		
		return results;
	}
	
	/**
	 * @deprecated
	 * From version v1.0.3 on: threshold defines the fraction of existing values that is at least needed at each arc length position to give an output of data. 
	 * Is applied only within each chunk. 0 <= threshold <= 1.
	 * From version v1.0.4 on: @deprecated, instead use getFlagellarParameterResults: get data from the text file and do not convert image (higher precision), 
	 * runs with double and not float for higher precision * 
	 * */
	public float [][] getKymoResults(String keySeq, int slicesPerCycle){
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
			return null;
		}
		
		//Analyze image
		ImagePlus imp = IJ.openImage(directory + System.getProperty("file.separator") + fileName);
		float [][] results;
		float [] data = new float [imp.getHeight()]; 
		int dataCount;
		results = new float [imp.getWidth()][4];	//0 = min, 1 = max, 2 = median, 3 = average
		
		/**For new method to determine avg, medi, min, max from version v1.0.3 on*
		 * Method: get minimum as median value of mins determined in chunks of 3 beat cycles*
		 * Apply a threshold. Only chunks that have at least a fraction of values included defined by the threshold will be used* 
		 * */
//		System.out.println("h " + (double)imp.getHeight());
//		System.out.println("spc3: " + ((double)slicesPerCycle*3.0));
		int numberOfChunks = (int)((double)imp.getHeight() / ((double)slicesPerCycle*3.0))+1;
//		System.out.println("noc " + numberOfChunks);
		double chunkMin [] = new double [numberOfChunks];
		double chunkMax [] = new double [numberOfChunks];	
		int counter, chunkCounter, chunkPos, includedChunks;
		{
			int s = 0;
			for(int al = 0; al < imp.getWidth(); al++){
				Arrays.fill(data, Float.POSITIVE_INFINITY);
				dataCount = 0;
				
				includedChunks = 0;
				chunkPos = 0;
				counter = 0;
				chunkCounter = 0;
				
				Arrays.fill(chunkMin, Float.POSITIVE_INFINITY);
				Arrays.fill(chunkMax, Float.NEGATIVE_INFINITY);
				for(int pos = 0; pos < imp.getHeight(); pos++){
					if(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1) > 0.0){
//						System.out.println("cp" + chunkPos + " of " + chunkMin.length);
						data [pos] = (float) tools.getValueFromEncodedIntensity16bit(imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1+s, 1)-1),
								minSize, maxSize);
						dataCount++;
						if(chunkMin[chunkPos]>data[pos]){
							chunkMin[chunkPos]=data[pos];
						}
						
						if(chunkMax[chunkPos]<data[pos]){
							chunkMax[chunkPos]=data[pos];
						}
						chunkCounter++;
					}
					counter ++;
					
					if(counter == slicesPerCycle*3){
						//Check if threshold fits						
						if(chunkCounter / (double)(slicesPerCycle*3.0) >= threshold){
							includedChunks ++;
						}else{
							chunkMin[chunkPos] = Float.POSITIVE_INFINITY;
							chunkMax[chunkPos] = Float.NEGATIVE_INFINITY;
//							System.out.println(al + ": exclude chunk because " + chunkCounter + "/" + (slicesPerCycle*3));
						}
						
						//go to next chunk
						chunkPos++;
						//Reset
						counter = 0;
					}else if(pos == imp.getHeight()-1){
						if(chunkCounter / (double)(imp.getHeight()-(chunkPos*slicesPerCycle*3)) >= threshold){
							includedChunks ++;
						}else{
							chunkMin[chunkPos] = Float.POSITIVE_INFINITY;
							chunkMax[chunkPos] = Float.NEGATIVE_INFINITY;
//							System.out.println(al + ": exclude chunk because " + chunkCounter + "/" + ((double)(imp.getHeight()-(chunkPos*slicesPerCycle*3))));
						}
						
						//go to next chunk
						chunkPos++;
						//Reset
						counter = 0;
					}
				}
				Arrays.sort(chunkMin);
				Arrays.sort(chunkMax);
				
				if(includedChunks>0){					
					//get minimum
					results [al][0] = (float) tools.getMedianOfRange(chunkMin, 0, includedChunks-1);
					
					//get maximum
					results [al][1] = (float) tools.getMedianOfRange(chunkMax, chunkMax.length-includedChunks, chunkMax.length-1);
				}else{
//					System.out.println(al + ": exclude data because " + chunkCounter + "/" + (slicesPerCycle*3));
					results [al][0] = Float.NEGATIVE_INFINITY;
					results [al][1] = Float.NEGATIVE_INFINITY;
				}
				
				if((double) dataCount / (double)imp.getHeight() >= threshold){
					Arrays.sort(data);
//					if(al == 50){
//						for(int d = 0; d < data.length; d++){
//							System.out.println(d + ":	" + data[d]);
//						}
//						System.out.println("ct" + (dataCount-1));
//					}					
					//get median
					results [al][2] = tools.getMedianOfRange(data, 0, dataCount-1);
					
					//get average
					results [al][3] = tools.getAverageOfRange(data, 0, dataCount-1);
//					if(keySeq == "cAng")	System.out.println(results[al][3]);
//					if(al == 50) 
//						System.out.println(keySeq + "  ***  " + al + ": " + (double) data [dataCount-1]+ "/" + data[0]);
					
				}else{
//					System.out.println(al + ": exclude data because " + (double) dataCount + "/" + (double)imp.getHeight() + "<" + threshold);
					results [al][2] = Float.NaN;
					results [al][3] = Float.NaN;
				}
//				if(al == 50) 
//					System.out.println(keySeq + "  ***  " + al + "min=" + results [al][0]+ " max=" + results [al][1] + ": median=" + results [al][2]+ " avg=" + results [al][3]);
			}
		}
		imp.changes = false;
		imp.close();	
		return results;
	}

	public float[][][] getFreqResults(String keySeq) {
		String[] list = new File(directory).list();
		if (list.length == 0) {
			valid = false;
			return null;
		}

		String fileName = "", fileInfoName = "";
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(keySeq + ".tif")) {
				fileName = list[i];
			}
			if (list[i].endsWith(keySeq + "_info.txt")) {
				fileInfoName = list[i];
			}
		}

		if (fileName == "" || fileInfoName == "") {
			valid = false;
			return null;
		}

		FileReader fr;
		BufferedReader br;
		String title = "", title2 = "", helpl;
		double minSize = 0.0, maxSize = 0.0, minSize2 = 0.0, maxSize2 = 0.0;
		try {
			fr = new FileReader(directory + System.getProperty("file.separator") + fileInfoName);
			br = new BufferedReader(fr);
			String line = "";
			int i = 0;
			copyPaste: while (true) {
				try {
					line = br.readLine();
					if (i == 6) {
						if (line.contains(",")) {
							line = line.replace(",", ".");
						}
						title = "" + line;
						title = title.substring(0, title.lastIndexOf("	"));
						title = title.substring(title.indexOf("	") + 1, title.lastIndexOf("	"));

						helpl = "" + line;
						helpl = helpl.substring(0, helpl.lastIndexOf("	"));
						minSize = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	") + 1));

						helpl = "" + line;
						maxSize = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	") + 1));
					} else if (i == 7) {
						if (line.contains(",")) {
							line = line.replace(",", ".");
						}
						title2 = "" + line;
						title2 = title2.substring(0, title2.lastIndexOf("	"));
						title2 = title2.substring(title2.indexOf("	") + 1, title2.lastIndexOf("	"));

						helpl = "" + line;
						helpl = helpl.substring(0, helpl.lastIndexOf("	"));
						minSize2 = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	") + 1));

						helpl = "" + line;
						maxSize2 = Double.parseDouble(helpl.substring(helpl.lastIndexOf("	") + 1));
					}
					i++;
					if (line == null)
						break copyPaste;
				} catch (Exception e) {
					break copyPaste;
				}
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			return null;
		}

		if (maxSize == 0.0 && minSize == 0.0) {
			valid = false;
			return null;
		}

		// Analyze image
		ImagePlus imp = IJ.openImage(directory + System.getProperty("file.separator") + fileName);
		float value;

		float[][][] results;
		float[] toSort;
		int sortCt;

		results = new float[imp.getNSlices()][imp.getWidth()][4]; // 0 = min, 1
																	// = max, 2
																	// = median,
																	// 3 =
																	// average
		toSort = new float[imp.getHeight()];

		for (int s = 0; s < imp.getNSlices(); s++) {
			for (int al = 0; al < imp.getWidth(); al++) {
				Arrays.fill(toSort, Float.POSITIVE_INFINITY);
				sortCt = 0;
				for (int pos = 0; pos < imp.getHeight(); pos++) {
					// IJ.log("a " + al + " - s " + s + " - pos " + pos + ": " +
					// imp.getStack().getVoxel(al, pos, imp.getStackIndex(1,
					// 1+s, 1)-1));
					if (imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1 + s, 1) - 1) > 0.0) {
						if (s == 0 || s == 2 || s == 4) { // 0,2,4 = frequency
							value = (float) tools.getValueFromEncodedIntensity16bit(
									imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1 + s, 1) - 1), minSize,
									maxSize);
						} else { // 1,3 = amplitude
							value = (float) tools.getValueFromEncodedIntensity16bit(
									imp.getStack().getVoxel(al, pos, imp.getStackIndex(1, 1 + s, 1) - 1), minSize2,
									maxSize2);
						}
						toSort[sortCt] = value;
						sortCt++;
					}
				}

				if (sortCt == 0) {
					results[s][al][0] = Float.NEGATIVE_INFINITY;
					results[s][al][1] = Float.NEGATIVE_INFINITY;
					results[s][al][2] = Float.NEGATIVE_INFINITY;
					results[s][al][3] = Float.NEGATIVE_INFINITY;
					// IJ.log("sort count = 0");
				}else{
					// get minimum
					results[s][al][0] = tools.getMinimumWithinRange(toSort, 0, sortCt-1);

					// get maximum
					results[s][al][1] = tools.getMaximumWithinRange(toSort, 0, sortCt-1);
					
					// get median
					results[s][al][2] = tools.getMedianOfRange(toSort, 0, sortCt - 1);

					// get average
					results[s][al][3] = tools.getAverageOfRange(toSort, 0, sortCt - 1);
				}

			}
		}
		imp.changes = false;
		imp.close();
		// IJ.log(keySeq + " put out");
		return results;
	}

	double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0) + Math.pow(z1 - z2, 2.0));
	}

	double getDistance2D(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0));
	}

	private String getFileName(String endsWith) {
		String[] list = new File(directory).list();
		if (list.length == 0) {
			System.out.println("listlength=0");
			valid = false;
			return null;
		}

		String fileName = "";
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(endsWith)) {
				fileName = list[i];
			}
		}

		if ((fileName == "")) {
			System.out.println("no file name found: " + fileName);
			valid = false;
			return null;
		}
		return fileName;
	}
}
