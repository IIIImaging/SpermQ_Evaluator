package spqEval.pdf;

/** 
===============================================================================
* SpermQEvaluator_.java Version 1.0.6
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;

import ij.IJ;
import ij.ImagePlus;


public class PDFPage {
	String pdfName;
	
	static String sourcePath, expName, rawTargetPath, targetPath;	
	static byte ID = 0;
	static double imageFreq, timeOfStack, slicesPerCycle, beatPeriodTime, numberOfFrames, xyCalibration, maxArcLenght, totalTime, coverageThreshold;
	static double zMin, zMax, nZMin, nZMax;
	static PDDocument doc;
	static PDPage page;
	static PDPageContentStream cts;
	static PageData pdt;
	
	static DecimalFormat dFormat0 = new DecimalFormat("#0");
	static DecimalFormat dFormat1 = new DecimalFormat("#0.0");
	static DecimalFormat dFormat2 = new DecimalFormat("#0.00");
	
	int pageNumber = 1;

	String name = "class_name";
	char cID;
	int x,y;
//	lut.LUT lut; --> used if LUTs are accessed from resource .txt
	
	final String lutParentPath = "";			//enter LUT directory here
	
	String imageName, imagePath, calibrationBoxName, calibrationBoxPath, lutName, lutPath;
	
	int undefinedColor = 0x00E000;
	
	public PDFPage(String sourcePath, String expName, String targetPath, double threshold){	
		coverageThreshold = threshold;
		
		pdfName = expName + "_results.pdf";
		PDFPage.rawTargetPath = targetPath;
		PDFPage.targetPath = targetPath + "files_for_PDF" + System.getProperty("file.separator"); //subfolder for the generated data
		PDFPage.sourcePath = sourcePath + System.getProperty("file.separator");
		PDFPage.expName = expName;
		
		dFormat0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dFormat1.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dFormat2.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

		pdt = new PageData(PDFPage.expName, PDFPage.sourcePath, PDFPage.targetPath);
		
		initDoc();
		
		readResults();
		getMaxArcLength();
		
		int leftBound1 = 40;
		int leftBound2 = 320;			//left bound of 2nd column
		int height = 150;
		int width = 170;
		int upperBound = 730;
		int space = height + 40;
		int widthKymo = 180;
			
		new KCAngle(leftBound1, upperBound, 120, 280);
		new KZMedi(leftBound1 + widthKymo, upperBound, 120, 280);
		new KY(leftBound1 + 2*widthKymo , upperBound, 120, 280);
		
		new BeatCircleRepr(leftBound1+20, upperBound - 410, 180);	
		new HeadPositionTracker(leftBound1 + 270, upperBound-410, 180);
		
		addPage();
		
		PDFPlot.dX0 = leftBound1;
		PDFPlot.dY0 = (int) (upperBound - 3.1 * space);
		
		//first column
			
		new YPlot(				leftBound1, 	upperBound - 0 * space,		width, 	height);
		new FFTPlot(			leftBound1, 	upperBound - 1 * space, 	width, 	height);
		new KAPlot(				leftBound1, 	upperBound - 2 * space, 	width, 	height);
		
		//second column
		
		new HeadResultsPlot(	leftBound2, 	upperBound - 0 * space, 	width, 	height);
		new ZFreqFFTPlot(		leftBound2, 	upperBound - 1 * space, 	width, 	height); 
		new ZPlot(				leftBound2, 	upperBound - 2 * space, 	width, 	height);
		
		PDFTools.insertTextBoxLowerY(cts, leftBound1, 15, "This file has been created by the SpermQ_Evaluator tool using the Apache PDFBox, JFreeChart and ImageJ packages for Java. The poject is available at https://github.com/IIIImaging/SpermQ_Evaluator", 5);
		
		save(rawTargetPath);
	}

	public PDFPage(String name,String lutName) {
		PDFPage.ID ++;
		this.cID = (char) (PDFPage.ID + 64);
		this.name = name;
		this.imageName = name;
		this.lutName = lutName;
		this.lutPath = lutParentPath + this.lutName;
//		initLUT(); --> used if LUTs are accessed from .txt resource
	}
	
	public PDFPage(String name) {
		PDFPage.ID ++;
		this.cID = (char) (PDFPage.ID + 64);
		this.name = name;
		this.imageName = name;
	}
	
	protected void addDesc() {
		PDFTools.insertTextBoxUpperY(cts, x, y + pdt.space, "(" + cID + ") " + name , pdt.headerSize);
	}
	
//	protected void initLUT () {
//		lut = new lut.LUT(lutParentPath + lutName);
//	}
	
//	protected void renderCalibrationBox() {
//		calibrationBoxName = "calibration_box_" + name;
//		calibrationBoxPath = pdt.targetPath + calibrationBoxName + ".bmp"; 
//		
//		int width = 256;
//		int height = 1;
//		ImagePlus box = IJ.createImage(calibrationBoxName, "RGB", width, height, 1);
//
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				box.getProcessor().putPixel(x, y, lut.getRGBValue(x, width-1));
//			}
//		}
//		IJ.saveAs(box, "BMP", calibrationBoxPath);
//		box.changes = false;
//		box.close();
//	}
	
	public static void readResults() {
		ImagePlus imp = IJ.openImage(pdt.sourcePath + pdt.expName + "_oriZCmedian.tif");
		numberOfFrames = imp.getNSlices();
		imp.close();
		
		try {
			FileReader fr = new FileReader(pdt.sourcePath + "results.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";

			scanningForImagingFreq: while(true) {
				try {
					line = br.readLine();
					if(line.contains("Sample rate [Hz]:")) {
						imageFreq = PDFTools.readNumber(line);
						totalTime = numberOfFrames/imageFreq;
						break scanningForImagingFreq;
					}				
				} catch (IOException e) {
					break scanningForImagingFreq;
				}
			}
			scanningForCalibr: while(true) {
				try {
					line = br.readLine();
					if(line.contains("xy calibration")) {
						xyCalibration = PDFTools.readNumber(line);
						break scanningForCalibr;
					}				
				} catch (IOException e) {
					break scanningForCalibr;
				}
			}
			scanningForTheta: while(true) {
				try {
					line = br.readLine();
					if (line.contains("	Average found primary freq.:")) {
						beatPeriodTime = PDFTools.readNumber(line);
						beatPeriodTime = 1/beatPeriodTime;
						break scanningForTheta;
					}
				} catch (IOException e) {
					break scanningForTheta;
				}
			}		
			slicesPerCycle = beatPeriodTime/(1/imageFreq);	
			fr.close();
			br.close();
		} catch (Exception e) {

		}
		timeOfStack = numberOfFrames/imageFreq;
		return;
	}

	private void getMaxArcLength() {
		try {
			FileReader fr = new FileReader(pdt.sourcePath + pdt.expName +"_kcAng_info.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			boolean lineFound = false;
			scanningForLine: while(!lineFound) {
				try {
					line = br.readLine();
					if(line.contains("x axis:	2D arc length")) {
						lineFound = true;
						double calibration = PDFTools.readNumber(line.substring(line.indexOf("]:") +2));
						line = line.substring(0,line.indexOf("	calibration"));
						line = line.substring(line.lastIndexOf("2D arc length	0") + 1);
						double max = PDFTools.readNumber(line);
						maxArcLenght = max*calibration;
					}									
				} catch (IOException e) {
					System.out.println("exception ");
					break scanningForLine;
				}
			}
			fr.close();
			br.close();		
		}
		catch(Exception e) {
			
		}
	}
	
	protected void calculateNormalizedMinMAxValues() {
		nZMin = (zMin - 1)/65534 * 15;	
		nZMax = (zMax - 1)/65534 * 15;	
	}
	
	private void addPage() {
		try {
			PDFPage.cts.close();
			PDFPage.doc.addPage(PDFPage.page);
			PDFPage.page = new PDPage();
			PDFPage.cts = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, true, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFPage.ID = 0;
		pageNumber++;
		addPageDesc();
	}

	private void initDoc() {
		ID = 0;
		new File(targetPath).mkdirs();
		PDFPage.doc = new PDDocument(); // create document
		PDFPage.page = new PDPage();
		try {
			PDFPage.cts = new PDPageContentStream(doc, page, AppendMode.OVERWRITE, true, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		addPageDesc();
	}
	
	private void addPageDesc() {
		PDFTools.insertTextBoxUpperY(cts, 20, 780, expName, pdt.headerSize);
		PDFTools.insertTextBoxUpperY(cts, 550, 780, Integer.toString(pageNumber), pdt.headerSize);	
		PDFTools.insertTextBoxUpperY(cts, 20, 780-pdt.headerSize-pdt.space, pdt.sourcePath, pdt.subDescSize);	
	}

	private void save(String path) {
		try {
			PDFPage.cts.close();
			PDFPage.doc.addPage(PDFPage.page);
			PDFPage.doc.save(path + pdfName);
			PDFPage.doc.close();
		} catch (Exception e) {
			
		}
	}	
	public double getSlicesPerCycle(){
		return slicesPerCycle;
	}
}