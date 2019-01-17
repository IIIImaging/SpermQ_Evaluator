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

import java.io.IOException;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ij.IJ;
import ij.ImagePlus;
import spqEval.lut.FireLUT;

public class KZMedi extends PDFPage {
	
	int imageBoxWidth, imageBoxHeight;
	
	final int spaceForText = 5;		//defines space left for the text boxes containing the time
	final int timeDescrCF = 12;		//defines space left for the text boxes containing the "time (sec)"
	final float relativeCalibrationBoxWidth = 0.84f;
	int desBW;				//defines real accessible height of the box excluding the fixed sizes of f.e. descriptions
	int iH, iW, bH, bW, uW, uH, fH, fW;		//defines sizes of the image, box & figure
	int iX0, iY0;					//origin (lower left corner) of the image
	int bX0, bY0;					//origin (lower left corner) of the box
	int fX0, fY0;					//origin (lower left corner) of the sperm figure
	int uX0, uY0;					//defines sizes of the undefined box, it's hight and y0 is equal to the cal. box 
	
	String undefinedBoxName, undefinedBoxPath;
	int factorOfDescriptions = 2;			//defines number of text descriptions to the right
	int numberOfIndicators = 10;
	
	spqEval.lut.FireLUT lut;
	
	KZMedi(int posX, int posY, int width, int height){
		super("Relative z-position", "fireLUT.txt");

		lut = new FireLUT();
		
		imageBoxWidth = width;
		imageBoxHeight = height;
		super.x = posX;
		super.y = posY;
		
		readValues();
		renderKZMediRepr();
		renderCalibrationBox();
		renderUndefinedBox();
		calculateNormalizedMinMAxValues();
		addRepr();		
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
	private void addRepr() {
		
//		calcSpaceToTheLeft();
		desBW = (int)(PDFTools.calculateMaxWidth(pdt.subDescSize, "undefined")*1.35);

		iW = imageBoxWidth;
		iH = imageBoxHeight;
		iX0 = x + desBW ;
		iY0 = y - 2 * pdt.header - iH - pdt.subDescSize*2 - pdt.space * 3;

		bW = (int)(iW * relativeCalibrationBoxWidth);
		bH = pdt.lutHeight;
		bX0 = iX0 + iW - bW;
		bY0 = iY0 - 2*pdt.space - bH;

		uW = bH;								//will be a square 
		uH = bH;
		uX0 = x;
		uY0 = bY0;
			
		int upBor = iY0 + iH;
		
		try {			
			addDesc();
			PDImageXObject image = PDImageXObject.createFromFile(imagePath, doc);
			PDImageXObject calBox = PDImageXObject.createFromFile(calibrationBoxPath, doc);
			PDImageXObject udfBox = PDImageXObject.createFromFile(undefinedBoxPath, doc);
			
			cts.drawImage(image, iX0, iY0, iW, iH);	
			cts.drawImage(calBox, bX0, bY0, bW, bH);
			cts.drawImage(udfBox, uX0 + pdt.lineWidth*2, uY0, uW, uH);
					
			addSideDesc();
			
			PDFTools.insertTextBoxXCentered(cts, iX0 + (iW/2), upBor + pdt.space * 2 + pdt.descSize*2, "arc length (µm)", pdt.descSize);
			PDFTools.insertTextBoxXCentered(cts, iX0, upBor + pdt.space + pdt.descSize, dFormat1.format(0.00f), pdt.subDescSize);
			PDFTools.insertTextBoxToRightBound(cts, iX0 + iW, upBor + pdt.space + pdt.descSize, dFormat1.format(maxArcLenght), pdt.subDescSize);
			PDFTools.insertTextBoxUpperY(cts, x - 2, bY0-pdt.space, "undefined", pdt.subDescSize);
			PDFTools.insertTextBoxUpperY(cts, bX0, bY0-pdt.space, dFormat1.format(nZMin), pdt.subDescSize);
			PDFTools.insertTextBoxToRightBound(cts, bX0 + bW, bY0-pdt.space, dFormat1.format(nZMax), pdt.subDescSize);
			PDFTools.insertTextBoxXCentered(cts, iX0 + (iW/2), bY0-0.5f*pdt.space-pdt.descSize, "z-position (a.u.)", pdt.descSize);
			
			PDFTools.drawPolygon(cts, bX0, bY0, bW, bH);
			PDFTools.drawPolygon(cts, uX0+pdt.lineWidth, uY0, uW, uH);
			cts.drawLine(bX0, bY0 + bH, bX0, bY0 - pdt.space);
			cts.drawLine(bX0+bW, bY0 + bH, bX0+ bW, bY0 - pdt.space);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error! - caught Exception!");
		}		
	}
	
	@SuppressWarnings("deprecation")
	private void addSideDesc() {
		int y = 0;
		int x1 = iX0 - pdt.space;
		int x2 = iX0 - pdt.space*2;
		float yCorrectionNumbers = pdt.subDescSize/2;
		float yCorrectionTime = pdt.subDescSize;
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				y = iY0 + iH * (numberOfIndicators-z)/numberOfIndicators;
				cts.drawLine(x1, y, iX0, y);
				if (z%factorOfDescriptions == 0) {
					double value = z*totalTime;
					value /= 10;
					y = (int) (iY0 + iH * (numberOfIndicators-z)/numberOfIndicators);
					PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, y + yCorrectionNumbers, dFormat1.format(value), pdt.subDescSize);
				}
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		try {
			PDFTools.insertTextBoxLowerY(cts, iX0 - desBW, (int)(iY0 + iH/2 + yCorrectionTime), "time", pdt.descSize);
			PDFTools.insertTextBoxUpperY(cts, iX0 - desBW, (int)(iY0 + iH/2 + yCorrectionTime), "(sec)", pdt.descSize);
			cts.drawLine(iX0, iY0, iX0, iY0 + iH + pdt.lineWidth);
			cts.drawLine(iX0, iY0 + iH, iX0 + iW - pdt.lineWidth, iY0 + iH);
			cts.drawLine(iX0, iY0 + iH, iX0, iY0 + iH + pdt.space);
			cts.drawLine(iX0 + iW - 0.5f, iY0 + iH, iX0 + iW - 0.5f, iY0 + iH + pdt.space);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	double [][] values;							//[x][y] = value (x,y)
	
	private void readValues() {
		String pathOfRawImage = pdt.sourcePath + pdt.expName + "_kzmedi.tif";
		ImagePlus impIn = IJ.openImage(pathOfRawImage);
		double pixelValue;
		double max = Integer.MIN_VALUE;
		double min = Integer.MAX_VALUE;
		values = new double [impIn.getWidth()][impIn.getHeight()];
		
		for (int x = 0; x < impIn.getWidth(); x++) {
			for (int y = 0; y < impIn.getHeight(); y++) {
				pixelValue = impIn.getProcessor().getPixel(x, y);
				if (pixelValue > 0.1) {
					if(pixelValue > max) {
						max = pixelValue;
					}
					if(pixelValue < min) {
						min = pixelValue;
					}
					values [x][y] = pixelValue;
				}
				else {
					values [x][y] = 0;
				}
			}
		}
		PDFPage.zMin = min;
		PDFPage.zMax = max;
		impIn.changes = false;
		impIn.close();
	}
	
	private void renderKZMediRepr() {
		imagePath = pdt.targetPath + name + ".bmp";
		ImagePlus impOut = IJ.createImage(name, values.length, values[0].length , 1, 24);  //RGB with 3*8 bit
		double pixelValue;
		
		int x,y;
		for (x = 0; x < values.length; x++) {
			for (y = 0; y < values[0].length; y++) {
				pixelValue = values [x][y];
				if (pixelValue == 0) {
					impOut.getProcessor().putPixel(x, y, undefinedColor);
				}
				else {
					impOut.getProcessor().putPixel(x, y, lut.getRGBValue(pixelValue - zMin, zMax - zMin));
				}
			}
		}
		IJ.saveAs(impOut, "BMP", imagePath);
		impOut.changes = false;
		impOut.close();
	}
	
	private void renderUndefinedBox() {
		undefinedBoxName = "undefined_box";
		undefinedBoxPath = pdt.targetPath + undefinedBoxName + ".bmp"; 
		
		int width = 10;
		int height = 10;
		ImagePlus box = IJ.createImage(undefinedBoxName, width, height, 1, 24);  //RGB with 3*8 bit

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				box.getProcessor().putPixel(x, y, undefinedColor);
			}
		}
		IJ.saveAs(box, "BMP", undefinedBoxPath);
		box.changes = false;
		box.close();
	}
	
}
