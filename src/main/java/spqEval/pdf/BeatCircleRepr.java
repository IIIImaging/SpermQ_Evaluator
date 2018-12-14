package spqEval.pdf;

/** 
===============================================================================
* SpermQEvaluator_.java Version 1.0.2
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

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ij.IJ;
import ij.ImagePlus;
import spqEval.lut.BeatCircleLUT;

public class BeatCircleRepr extends PDFPage{

	int imageBoxSize;

	final float cfX = .07f;			//center factor in x-axis for the box
	int iH, iW, bH, bW;				//defines sizes of the image and box
	int iX0, iY0;					//origin (lower left corner) of the image
	int bX0, bY0;					//origin (lower left corner) of the box
	
	double scaleFactor = 1.25;
	double imageRange, imageRangeµm;
	int lDescSpacer;
	
	BeatCircleLUT lut;	
	
	BeatCircleRepr(int posX, int posY, int imageSize){
		super("Overlay of one beat cycle", "beatCircleLUT.txt");
		
		lut = new BeatCircleLUT();

		this.imageBoxSize = imageSize;
		super.x = posX;
		super.y = posY;

		renderBeatCycleRepr();
		renderCalibrationBox();
		addFullBeatCycleRep();	
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
	private void addFullBeatCycleRep() {
		
		lDescSpacer = PDFTools.calculateMaxWidth(pdt.subDescSize, dFormat1.format(imageRangeµm), "µm");

		iW = imageBoxSize;
		iH = imageBoxSize;
		iX0 = x + lDescSpacer;
		iY0 = y - pdt.header - iH;

		bW = (int)(iW *(1-2*cfX));
		bH = pdt.lutHeight;
		bX0 = (int)(iX0 + cfX*iW);
		bY0 = iY0 - pdt.space - bH;
		
		try {
			addDesc();
			
			PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);
			PDImageXObject pdBox = PDImageXObject.createFromFile(calibrationBoxPath, doc);
			
			cts.drawImage(pdImage, iX0, iY0, iW, iH);			//draw Beat Circle Repr
			cts.drawImage(pdBox, bX0, bY0, bW, bH);			//draw Calibration Box
		
			PDFTools.insertTextBoxXCentered(cts, bX0, bY0-pdt.space, dFormat2.format(0.000f), pdt.descSize);
			PDFTools.insertTextBoxXCentered(cts, bX0+bW, bY0-pdt.space, dFormat2.format(beatPeriodTime), pdt.descSize);		
			PDFTools.insertTextBoxXCentered(cts, bX0+(bW/2), bY0-pdt.space, "time (sec)", pdt.descSize);
			
			PDFTools.insertTextBoxLowerY(cts, iX0 - lDescSpacer - pdt.space, iY0 + (iH/2), dFormat1.format(imageRangeµm), pdt.subDescSize);
			PDFTools.insertTextBoxUpperY(cts, iX0 - lDescSpacer - pdt.space, iY0 + (iH/2), "µm", pdt.subDescSize);
			cts.drawLine(iX0, iY0 + pdt.lineWidth, iX0 - pdt.space, iY0 + pdt.lineWidth);
			cts.drawLine(iX0, iY0 + iH - pdt.lineWidth, iX0 - pdt.space, iY0 + iH - pdt.lineWidth);
			
			PDFTools.drawPolygon(cts, bX0, bY0, bW, bH);
			cts.drawLine(bX0, bY0 + bH, bX0, Math.round(bY0 - pdt.space));
			cts.drawLine(bX0+bW, bY0 + bH, bX0+ bW, Math.round(bY0 - pdt.space));
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error! - caught Exception!");
		}		
	}
	
	int upperX = 0, upperY = 0, lowerX = Integer.MAX_VALUE, lowerY = Integer.MAX_VALUE;
	int xCorrection, yCorrection;
	
	private void getBounds(ImagePlus impIn) {
		for (int t = 1; t <= slicesPerCycle + 1; t++) {
			for (int x = 0; x < impIn.getWidth(); x++) {
				for (int y = 0; y < impIn.getHeight(); y++) {
					if (impIn.getStack().getVoxel(x, y, t) != 0) {
						if(x > upperX) upperX = x;
						if(y > upperY) upperY = y;
						if(x < lowerX) lowerX = x;
						if(y < lowerY) lowerY = y;
					}	
				}
			}
		}
		double yRange = upperY - lowerY;
		double xRange = upperX - lowerX;
		imageRange = yRange;
		if(imageRange < xRange) {
			imageRange = xRange;
		}
		imageRange = Math.round(imageRange * scaleFactor);
		
		xCorrection = (int) (lowerX - (imageRange - xRange)/2);
		yCorrection = (int) (lowerY - (imageRange - yRange)/2);
		
		imageRangeµm = imageRange * xyCalibration;
	}
	
	private void renderBeatCycleRepr() {
		String pathOfRawImage = pdt.sourcePath + pdt.expName + "_oriZCmedian.tif";
		imagePath = pdt.targetPath + name + ".bmp";

		ImagePlus impIn = IJ.openImage(pathOfRawImage);		
		getBounds(impIn);		
		ImagePlus impOut = IJ.createImage(name, "RGB black", (int) imageRange, (int) imageRange, 1);		
		int width = impIn.getWidth();
		int height = impIn.getHeight();
		
		int nX, nY, oX, oY;		
		for (int t = 1; t <= slicesPerCycle + 1; t++) {
			for (nX = 0; nX <= imageRange; nX++) {
				for (nY = 0; nY <= imageRange; nY++) {
					oX = xCorrection + nX;
					oY = yCorrection + nY;
					if(PDFTools.isInBounds(width,height,oX, oY)) {
						if (impIn.getStack().getVoxel(oX, oY, t) != 0) {
							impOut.getProcessor().putPixel(nX, nY, PDFTools.handleChannelOverlapInRGB(impOut.getProcessor().get(nX, nY), lut.getRGBValue(t, slicesPerCycle -1)));
						}
					}
				}
			}
		}

		IJ.saveAs(impOut, "BMP", imagePath);
		impOut.changes = false;
		impOut.close();
		impIn.changes = false;
		impIn.close();
		return;
	}

}
