package spqEval.pdf;

/** 
===============================================================================
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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ij.IJ;
import ij.ImagePlus;
import spqEval.lut.PhysicsLUT;

public class HeadPositionTracker extends PDFPage{
	
	int imageBoxSize;
	
	float cfX = .07f;			//centration factor in x-axis for the box
	
	int iH, iW, bH, bW;				//defines sizes of the image and box
	int iX0, iY0;					//origin (lower left corner) of the image
	int bX0, bY0;					//origin (lower left corner) of the box
	int lDescSpacer;		//defines the space of the description to the right	
	
	PhysicsLUT lut;
	
	//mpl-plasma lut alternative
		
	HeadPositionTracker(int posX, int posY, int imageSize){
		super("Head position in space", "physicsLUT.txt");

		lut = new PhysicsLUT();
		
		this.imageBoxSize = imageSize;
		super.x = posX;
		super.y = posY;
		
		renderHeadPositionRepr();
		renderCalibrationBox();
		addHeadPositionRep();	
	}

	protected void renderCalibrationBox() {
		calibrationBoxName = "calibration_box_" + name;
		calibrationBoxPath = pdt.targetPath + calibrationBoxName + ".bmp"; 
		
		int width = 256;
		int height = 1;
		ImagePlus box = IJ.createImage(calibrationBoxName, "RGB", width, height, 1);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				box.getProcessor().putPixel(x, y, lut.getRGBValue(x, width-1));
			}
		}
		IJ.saveAs(box, "BMP", calibrationBoxPath);
		box.changes = false;
		box.close();
	}
	
	@SuppressWarnings("deprecation")
	private void addHeadPositionRep() {
		
		lDescSpacer = PDFTools.calculateMaxWidth(pdt.subDescSize, dFormat1.format(figRange), "µm");

		iW = imageBoxSize;
		iH = imageBoxSize;
		iX0 = x + lDescSpacer;
		iY0 = y - pdt.header - iH;
		
		bW = (int)(iW *(1-2*cfX));
		bH = pdt.lutHeight;
		bX0 = (int)(iX0 + cfX*iW);
		bY0 = iY0 - pdt.space - bH;		
		
		try {
			super.addDesc();
			PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);
			PDImageXObject pdBox = PDImageXObject.createFromFile(calibrationBoxPath, doc);
			
			cts.drawImage(pdImage, iX0, iY0, iW, iH);			//draw image
			cts.drawImage(pdBox, bX0, bY0, bW, bH);			//draw Calibration Box
			
			if(figRange == 0) {
				PDFTools.insertTextBoxXCentered(cts, iX0 + iW/2, (iY0 + iH/2 + 10 + pdt.space), "Figure not created", 10);
				PDFTools.insertTextBoxXCentered(cts, iX0 + iW/2, (iY0 + iH/2), "Head does not move", 10);
			}
			
			PDFTools.insertTextBoxXCentered(cts, bX0, bY0-pdt.space, dFormat2.format(0.000f), pdt.descSize);
			PDFTools.insertTextBoxXCentered(cts, bX0+bW, bY0-pdt.space, dFormat2.format(timeOfStack), pdt.descSize);		
			PDFTools.insertTextBoxXCentered(cts, bX0+(bW/2), bY0-pdt.space, "time (sec)", pdt.descSize);	
			
			PDFTools.drawPolygon(cts, iX0, iY0, iW, iH);
			PDFTools.drawPolygon(cts, bX0, bY0, bW, bH);
			cts.drawLine(bX0, bY0 + bH, bX0, Math.round(bY0 - pdt.space));
			cts.drawLine(bX0 + bW, bY0 + bH, bX0+ bW, Math.round(bY0 - pdt.space));
			
			cts.drawLine(iX0, iY0, iX0 - pdt.space, iY0);
			cts.drawLine(iX0, iY0 + iH, iX0 - pdt.space, iY0 + iH);
			cts.drawLine(iX0 , iY0, iX0, iY0 + iH);
			PDFTools.insertTextBoxLowerY(cts, iX0 - lDescSpacer - pdt.space, iY0 + (iH/2), dFormat1.format(figRange), pdt.subDescSize);
			PDFTools.insertTextBoxUpperY(cts, iX0 - lDescSpacer - pdt.space, iY0 + (iH/2), "µm", pdt.subDescSize);	
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error! - caught exception while inserting content to PDF");
		}		
	}
	
	private void renderHeadPositionRepr() {
		imagePath = pdt.targetPath + name + ".bmp";		
		
		fillPositionArray();
		
		ImagePlus impOut;
		
		if(figRange == 0) {
			impOut = IJ.createImage(name, "RGB white", 1, 1, 1);
		}
		else {
			int imageSize = (int) (figRange / xyCalibration); // defines height and width of the output images in IJ (in pixels)
			impOut = IJ.createImage(name, "RGB white", imageSize, imageSize, 1);
			for (int t = 0; t < numberOfFrames; t++) {
				impOut.getProcessor().putPixel((int) Math.round(xValues[t] * imageSize),
						(int) Math.round(yValues[t] * imageSize), lut.getRGBValue(t, numberOfFrames - 1));
			}
		}
		IJ.saveAs(impOut, "BMP", imagePath);
		impOut.changes = false;
		impOut.close();
		return;
	}
	
	double [] xValues;		//index represents the the frame (time)
	double [] yValues;		//index represents the the frame (time)
	double xMax, xMin, yMax, yMin, figRange;
	
	/**
	 * fills an the x and y position arrays with the relative values between 0 and 1, the index codes the frame (time)
	 */
	
	private void fillPositionArray() {
		xValues = new double [(int) numberOfFrames];
		yValues = new double [(int) numberOfFrames];
		
		try {
			FileReader fr = new FileReader(pdt.sourcePath + "results.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			scanningForOffset: while (true) {
				line = br.readLine();
				if (line.contains("head position")) {
					break scanningForOffset;
				}
			}
			ArrayList<Double> lineNumbers;
			for (int i = 0; i < numberOfFrames; i++) {
				line = br.readLine();
				lineNumbers = PDFTools.seperateNumbers(line, 3);
				if (lineNumbers.get(0) != i) {
					System.out.println("Error: Line number and counter do not match!");
					break;
				}
				xValues[i] = lineNumbers.get(1);
				yValues[i] = lineNumbers.get(2);
			}

			br.close();
		} catch (Exception e) {

		}
		xMin = PDFTools.getMin(xValues);
		xMax = PDFTools.getMax(xValues);
		yMin = PDFTools.getMin(yValues);
		yMax = PDFTools.getMax(yValues);
		
		double oXRange = xMax-xMin;		//original (absolute/scaled/µm) x,y Range
		double oYRange = yMax-yMin;
		
		if(oXRange > oYRange) {
			figRange = oXRange;
		}
		else {
			figRange = oYRange;
		}
		
		for(int t = 0; t < xValues.length; t++) {
			xValues[t] = normalizeValue(xValues[t],xMin, oXRange, figRange);
			yValues[t] = normalizeValue(yValues[t],yMin, oYRange, figRange);
		}
		
	}
	
	/**
	 * normalizes and corrects for the relative value, so that a the range is centered inside a square
	 * @param v absolute value
	 * @param min offset or minimum value
	 * @param oRange original range
	 * @param range of the square
	 * @return relative value between 0 and 1
	 */
	
	private double normalizeValue (double v, double min, double oRange, double range) {		
		return (v- ((min + 0.5*oRange) - 0.5*range)) / range;				
//		double oMean = min + 0.5*oRange;
//		double newOffset = oMean - 0.5*range;		
//		double relativePos = (v-newOffset) / range;	
	}
	
}
