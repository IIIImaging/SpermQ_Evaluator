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

import java.awt.Color;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.data.xy.XYSeries;

import spqEval.Result;
import spqEval.plot.Plot;

public class HeadResultsPlot extends PDFPlot {
	
	final int nOfPlots = 3;
	int plotWidth, plotHeight;
	XYSeries velocity, theta, hri;
	
	static final Color [] thetaColors = {Color.RED};
	static final Color [] velocityColors = {Color.BLUE};
	static final Color [] hriMaxColors = {Color.GREEN};
	
//	int xMaxV = 0, xMaxT = 0, xMinV, xMin, xMinH, xMaxH;
	double yMinT = 0, yMinV = 0, yMaxT = Float.NEGATIVE_INFINITY, yMaxV = Float.NEGATIVE_INFINITY, yMinH, yMaxH;
	int highestUndefined;
	double xBaseValue = 0.2;
	double yBaseValueT;
	double yBaseValueV;
	float yBaseValueH = 0.25f;
	double arcLenghtµm;
	
	static final String nameOfVelocityPlot = "Velocity_plot.png";
	static final String nameOfThetaPlot = "Theta_plot.png";
	static final String nameOfHRIMaxPlot = "HRIMax_plot.png";
	String thetaPlotPath, velocityPlotPath, hriMaxPlotPath;
		
	int tX0, tY0, tW, tH;
	int vX0, vY0, vW, vH;
	int hX0, hY0, hW, hH;

	int spaceLeftDesc;
	
	double xMax;
	int xMin = 0;

	public HeadResultsPlot(int posX, int posY, int width, int height) {
		
		super("FFT_plot");
		plotWidth = width;
		plotHeight = height;
		x = posX;
		y = posY;
		thetaPlotPath = targetPath + nameOfVelocityPlot;
		velocityPlotPath = targetPath + nameOfThetaPlot;
		hriMaxPlotPath = targetPath + nameOfHRIMaxPlot;
		
		desc = "upper panel: orientation of head-midpiece axis in space, middle panel: velocity of head in space, bottom panel: head rolling";
		
		getData();

		setXCalibration();
		
		setYCalibrationTheta();

		setYCalibrationVelocity();
		
		setYCalibrationHRI();

		renderPlot();
		addPlot();
	}
	
	protected void renderPlot() {
		
		Plot plot1 = new Plot(plotWidth*pdt.resolution/nOfPlots, plotHeight*pdt.resolution/nOfPlots, thetaPlotPath, nameOfThetaPlot, theta);
		plot1.setLineColor(thetaColors);
		plot1.setRanges(xMin, yMinT, xMax, yMaxT);
		plot1.renderPlot(false, true, pdt.plotLineWidthFactor*pdt.resolution/nOfPlots);
		
		Plot plot2 = new Plot(plotWidth*pdt.resolution/nOfPlots, plotHeight*pdt.resolution/nOfPlots, velocityPlotPath,  nameOfVelocityPlot , velocity);
		plot2.setLineColor(velocityColors);
		plot2.setRanges(xMin, yMinV, xMax, yMaxV);
		plot2.renderPlot(false, true, pdt.plotLineWidthFactor*pdt.resolution/nOfPlots);
		
		Plot plot3 = new Plot(plotWidth*pdt.resolution/nOfPlots, plotHeight*pdt.resolution/nOfPlots, hriMaxPlotPath,  nameOfHRIMaxPlot , hri);
		plot3.setLineColor(hriMaxColors);
		plot3.setRanges(xMin, yMinH, xMax, yMaxH);
		plot3.renderPlot(false, true, pdt.plotLineWidthFactor*pdt.resolution/nOfPlots);
	}

	private void getData() {
		
		theta = new XYSeries ("theta_plot");
		velocity = new XYSeries ("velocity_plot");
		hri = new XYSeries ("hriMax_plot");

		Result r = new Result(PDFPage.sourcePath, 5);

		float [][] rawData = r.getHeadResults();
		
		xMax = rawData[0].length/timeOfStack;
		
		int arrayBound = (int) xMax;
		if(arrayBound > rawData[0].length) arrayBound = rawData[0].length;		
		int i;
		rawData[2] = PDFTools.normalizeValuesFloat(rawData[2]);
		for (i = 0; i < arrayBound; i++) {
			if(rawData[0][i] > Float.NEGATIVE_INFINITY) {
				theta.add(i, rawData[0][i]);
			}
			if(rawData[1][i] > Float.NEGATIVE_INFINITY) {
				velocity.add(i, rawData[1][i]);
//				System.out.println(rawData[1][i]);
			}
			if(rawData[2][i] > Float.NEGATIVE_INFINITY) {
				hri.add(i, rawData[2][i]);
			}
		}
		
		xMax = (int) (theta.getMaxX());
		xMin = (int) (theta.getMinX());
		yMaxT = theta.getMaxY();
		yMinT = theta.getMinY();
		yMinV = velocity.getMinY();
		yMaxV = velocity.getMaxY();
		yMinH = hri.getMinY();
		yMaxH = hri.getMaxY();
//		System.out.println("ranges : " + xMaxV + "|" + xMinV + "|" + yMaxV + "|" + yMinV);
//		System.out.println("ranges : " + xMaxT + "|" + xMinT + "|" + yMaxT + "|" + yMinT);
//		System.out.println("ranges : " + xMaxH + "|" + xMinH + "|" + yMaxH + "|" + yMinH);
	}
	
	private void setYCalibrationTheta() {
		yBaseValueT = PDFTools.getBaseValue(yMaxT-yMinT, 3, 0.25,1,2,5);		
		yMaxT = PDFTools.getNextMultipleOf(yBaseValueT, yMaxT);
		yMinT = PDFTools.getNextMultipleOf(yBaseValueT, yMinT) - yBaseValueT;
		
	}
	
	private void setYCalibrationHRI() {
		yBaseValueH = 0.25f;
	}
	
	private void setYCalibrationVelocity() {
		yBaseValueV = PDFTools.getBaseValue(yMaxV, 3, 0.25,1,2,5);
		yMaxV = PDFTools.getNextMultipleOf(yBaseValueV, yMaxV);
		yMinV = 0;
	}
	
	private void setXCalibration() {
		xBaseValue = (int) PDFTools.getBaseValue(xMax - 0, 7, 0.25,1,2,5);
	}
	
	private void addPlot() {
		
		spaceLeftDesc = PDFTools.calculateMaxWidth(pdt.subDescSize, "0000") + pdt.space;
		tW = plotWidth;
		tH = plotHeight / 3 - pdt.space*2;
		tX0 = x + spaceLeftDesc + pdt.subDescSize;
		tY0 = y - pdt.header - tH;
		
		vW = tW;
		vH = plotHeight / 3 - pdt.space*2;
		vX0 = tX0;
		vY0 = tY0 -vH - pdt.space*2;
		
		hW = vW;
		hH = plotHeight / 3 - pdt.space*2;
		hX0 = vX0;
		hY0 = vY0 - vH - pdt.space*2;
		
		try {			
			addDesc();
			PDImageXObject velocityPlot = PDImageXObject.createFromFile(thetaPlotPath, doc);
			PDImageXObject thetaPlot = PDImageXObject.createFromFile(velocityPlotPath, doc);
			PDImageXObject hriPlot = PDImageXObject.createFromFile(hriMaxPlotPath, doc);
			
			cts.drawImage(velocityPlot, tX0, tY0, tW, tH);	
			cts.drawImage(thetaPlot, vX0, vY0, vW, vH);
			cts.drawImage(hriPlot, hX0, hY0, hW, hH);
					
			addDescTheta();
			addDescVelocity();
			addDescHRI();
			addLowerDesc();
			
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error! - caught Exception !");
		}
	}
	
	@SuppressWarnings("deprecation")
	private void addLowerDesc() {
		
		try {
			cts.drawLine(hX0, hY0, hX0 + hW + pdt.lineWidth, hY0);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		int y0 = hY0 - pdt.space;
		float x;
		float numberOfIndicators = 5;
		double desValue;
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				x = hX0 + hW * z/numberOfIndicators;
				cts.drawLine(x, y0, x, hY0);
				desValue = (z/numberOfIndicators);
				PDFTools.insertTextBoxXCentered(cts, x, y0 - pdt.space, dFormat1.format((desValue)) , pdt.subDescSize);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxXCentered(cts, hX0 + hW/2, hY0 - pdt.space*2 - pdt.descSize, "time (s)", pdt.subDescSize);		
	}
	
	@SuppressWarnings("deprecation")
	private void addDescTheta() {
		float y = 0;
		int x1 = tX0 - pdt.space;
		int x2 = tX0 - pdt.space*2;
		float yCorrectionNumbers = pdt.subDescSize/2;
		
		int numberOfIndicators = (int) ((yMaxT - yMinT) / yBaseValueT);
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				y = tY0 + tH * (numberOfIndicators-z)/numberOfIndicators;
				cts.drawLine(x1, y, tX0, y);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, tY0 + yCorrectionNumbers, Integer.toString((int) (yMinT)), pdt.subDescSize);
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, tY0 + tH + yCorrectionNumbers, Integer.toString((int) (yMaxT)), pdt.subDescSize);
		try {
			cts.drawLine(tX0, tY0 - pdt.lineWidth, tX0, tY0 + tH + pdt.lineWidth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFTools.insertTextBoxRotatedUpward(cts, x, tY0+tH/2, "angle theta", pdt.subDescSize);
		PDFTools.insertTextBoxRotatedUpward(cts, x + pdt.space+ pdt.subDescSize, tY0+tH/2, "(°)", pdt.subDescSize);
	}
	
	@SuppressWarnings("deprecation")
	private void addDescVelocity() {
		float y = 0;
		int x1 = vX0 - pdt.space;
		int x2 = vX0 - pdt.space*2;
		float yCorrectionNumbers = pdt.subDescSize/2;
		
		int numberOfIndicators = (int) ((yMaxV-yMinV) / yBaseValueV);
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				y = vY0 + vH * (numberOfIndicators - z)/numberOfIndicators;
				cts.drawLine(x1, y, vX0, y);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, vY0 + yCorrectionNumbers, dFormat2.format(yMinV), pdt.subDescSize);
		PDFTools.insertTextBoxToRightBound(cts, x2, vY0 + vH + yCorrectionNumbers, dFormat2.format(yMaxV), pdt.subDescSize);
		try {
			cts.drawLine(vX0, vY0, vX0, vY0 + vH + pdt.lineWidth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFTools.insertTextBoxRotatedUpward(cts, x, vY0+vH/2, "head velocity", pdt.subDescSize);
		PDFTools.insertTextBoxRotatedUpward(cts, x + pdt.space+ pdt.subDescSize, vY0+vH/2, "(µm/s)", pdt.subDescSize);
	}
	
	@SuppressWarnings("deprecation")
	private void addDescHRI() {
		float y = 0;
		int x1 = hX0 - pdt.space;
		int x2 = hX0 - pdt.space*2;
		float yCorrectionNumbers = pdt.subDescSize/2;
		
		int numberOfIndicators = (int) (yMaxH / yBaseValueH);
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				y = hY0 + hH * (numberOfIndicators - z)/numberOfIndicators;
				cts.drawLine(x1, y, hX0, y);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, hY0 + yCorrectionNumbers, dFormat0.format(yMinH), pdt.subDescSize);
		PDFTools.insertTextBoxToRightBound(cts, x2, hY0 + hH + yCorrectionNumbers, dFormat0.format(yMaxH), pdt.subDescSize);
		try {
			cts.drawLine(hX0, hY0, hX0, hY0 + hH + pdt.lineWidth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFTools.insertTextBoxRotatedUpward(cts, x, hY0+hH/2, "max intensity", pdt.subDescSize);
		PDFTools.insertTextBoxRotatedUpward(cts, x + pdt.space+ pdt.subDescSize, hY0+hH/2, "in head (a.u.)", pdt.subDescSize);
	}
	
}
