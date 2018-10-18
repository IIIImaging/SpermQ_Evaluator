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

public class FFTPlot extends PDFPlot {
	
	final int nOfPlots = 2;
	int plotWidth, plotHeight;
	XYSeries [] xySeries;
	
	final static String [] names = {"primFreq" , "primPow" ,"secFreq" , "secPow" , "COMFreq"  };
	static final Color [] freqColors = {Color.RED, Color.GRAY, Color.MAGENTA};
	static final Color [] powColors = {Color.RED, Color.GRAY};
	
	double xMinPow = 0, xMinFreq = 0, xMaxPow, xMaxFreq;
	double yMinPow = 0, yMinFreq = 0, yMaxPow = Float.NEGATIVE_INFINITY, yMaxFreq = Float.NEGATIVE_INFINITY;
	int highestUndefined;
	int xBaseValue = 50; 
	
	float[][][] data;

	static final String nameOfFreqPlot = "freq_plot.png";
	static final String nameOfPowPlot = "pow_plot.png";
	String freqPlotPath, powPlotPath;
		
	int fX0, fY0, fW, fH;
	int pX0, pY0, pW, pH;

	int spaceLeftDesc;
	
	double relativeWidth;
			
	double yBaseValueFreq = 20;
	double yBaseValuePow = 1000;

	public FFTPlot(int posX, int posY, int width, int height) {
		
		super("FFT_plot");
		plotWidth = width;
		plotHeight = height;
		super.x = posX;
		y = posY;
		freqPlotPath = targetPath + nameOfFreqPlot;
		powPlotPath = targetPath + nameOfPowPlot;
		
		desc = "local beat frequency";
		
		getData();
		setXCalibration();
		renderPlot();
		addPlot();
		addLegend();
	}

	private void setXCalibration() {
		double xMax = xMaxFreq;
		xMax *= xyCalibration;
		if(xMax < 80) {
			xBaseValue = 10;
		}
		else if(xMax < 200) {
			xBaseValue = 20;
		}
		xBaseValue = (int) PDFTools.getBaseValue(xMax - 0, 7, 0.25,1,2,5);
		xMax = PDFTools.getNextMultipleOf(xBaseValue, xMax);
		xMaxFreq = xMax;
		xMaxPow = xMax;		
	}
	
	protected void renderPlot() {
		
		Plot plot1 = new Plot(plotWidth*pdt.resolution/nOfPlots, plotHeight*pdt.resolution/nOfPlots, freqPlotPath, nameOfFreqPlot, xySeries[0], xySeries[2], xySeries[4]);
		plot1.setLineColor(freqColors);
		plot1.setRanges(xMaxFreq, yMaxFreq);
		plot1.renderPlot(false, true, pdt.plotLineWidthFactor*pdt.resolution/nOfPlots);
		
		Plot plot2 = new Plot(plotWidth*pdt.resolution/nOfPlots, plotHeight*pdt.resolution/nOfPlots, powPlotPath, nameOfPowPlot, xySeries[1], xySeries[3]);
		plot2.setLineColor(powColors);
		plot2.setRanges(xMaxPow, yMaxPow);
		plot2.renderPlot(false, true, pdt.plotLineWidthFactor*pdt.resolution/nOfPlots);
	}
	
	private void addPlot() {
		
		spaceLeftDesc = PDFTools.calculateMaxWidth(pdt.subDescSize, "0000") + pdt.space;
		fW = plotWidth;
		fH = plotHeight / 2 - pdt.space*2;
		fX0 = x + spaceLeftDesc + pdt.subDescSize;
		fY0 = y - pdt.header - fH;
		
		pW = fW;
		pH = plotHeight / 2 - pdt.space*2;
		pX0 = fX0;
		pY0 = fY0 -pH - pdt.space*2;
		
		try {			
			addDesc();
			PDImageXObject freqPlot = PDImageXObject.createFromFile(freqPlotPath, doc);
			PDImageXObject powPlot = PDImageXObject.createFromFile(powPlotPath, doc);
			
			cts.drawImage(freqPlot, fX0, fY0, fW, fH);	
			cts.drawImage(powPlot, pX0, pY0, pW, pH);
					
			addDescFreq();
			addDescPow();
			addLowerDesc();
			
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error! - caught Exception!");
		}
	}
		
	@SuppressWarnings("deprecation")
	private void addLowerDesc() {
		
		try {
			cts.drawLine(pX0, pY0, pX0 + pW + pdt.lineWidth, pY0);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		int y0 = pY0 - pdt.space;
		float x;
		float numberOfIndicators = (int) (xMaxFreq / xBaseValue);
		double desValue;
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				x = pX0 + pW * z/numberOfIndicators;
				cts.drawLine(x, y0, x, pY0);
				desValue = (z/numberOfIndicators * xMaxFreq);
				PDFTools.insertTextBoxXCentered(cts, x, y0 - pdt.space, Integer.toString((int) (desValue)) , pdt.subDescSize);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxXCentered(cts, pX0 + pW/2, pY0 - pdt.space*2 - pdt.descSize, "arc lenght (µm)", pdt.subDescSize);
		
	}
	
	@SuppressWarnings("deprecation")
	private void addDescFreq() {
		float y = 0;
		int x1 = fX0 - pdt.space;
		int x2 = fX0 - pdt.space*2;
		float yCorrectionNumbers = pdt.subDescSize/2;
		
		int numberOfIndicators = (int) (yMaxFreq / yBaseValueFreq);
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				y = fY0 + fH * (numberOfIndicators-z)/numberOfIndicators;
				cts.drawLine(x1, y, fX0, y);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, fY0 + yCorrectionNumbers, Integer.toString((int) (0)), pdt.subDescSize);
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, fY0 + fH + yCorrectionNumbers, Integer.toString((int) (yMaxFreq)), pdt.subDescSize);
		try {
			cts.drawLine(fX0, fY0 - pdt.lineWidth, fX0, fY0 + fH + pdt.lineWidth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFTools.insertTextBoxRotatedUpward(cts, x, fY0+fH/2, "curvature angle: freq.", pdt.subDescSize);
		PDFTools.insertTextBoxRotatedUpward(cts, x + pdt.space+ pdt.subDescSize, fY0+fH/2, "(Hz)", pdt.subDescSize);
	}
	
	@SuppressWarnings("deprecation")
	private void addDescPow() {
		float y = 0;
		int x1 = pX0 - pdt.space;
		int x2 = pX0 - pdt.space*2;
		float yCorrectionNumbers = pdt.subDescSize/2;
		
		int numberOfIndicators = (int) (yMaxPow / yBaseValuePow);
		
		for(int z = 0; z <= numberOfIndicators; z++) {
			try {
				y = pY0 + pH * (numberOfIndicators - z)/numberOfIndicators;
				cts.drawLine(x1, y, pX0, y);
			} catch (IOException e) {
				System.out.println("exception in addSideDesc");
			}
		}
		PDFTools.insertTextBoxToRightBoundYCentrated(cts, x2, pY0 + yCorrectionNumbers, Integer.toString((int) (0)), pdt.subDescSize);
		PDFTools.insertTextBoxToRightBound(cts, x2, pY0 + pH + yCorrectionNumbers, Integer.toString((int) (yMaxPow)), pdt.subDescSize);
		try {
			cts.drawLine(pX0, pY0, pX0, pY0 + pH + pdt.lineWidth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFTools.insertTextBoxRotatedUpward(cts, x, pY0+pH/2, "power of freq. peaks", pdt.subDescSize);
		PDFTools.insertTextBoxRotatedUpward(cts, x + pdt.space+ pdt.subDescSize, pY0+pH/2, "(°/Hz)", pdt.subDescSize);
	}
	
	private void getData() {
		
		xySeries = new XYSeries [5];
		for (int i = 0 ; i < 5 ; i++) {
			xySeries [i] = new XYSeries(names[i]);
		}

		Result r = new Result(PDFPage.sourcePath, 5);
		data = r.getFreqResults("cAng_f");

		int arcL;
		for (arcL = 0; arcL < data[0].length; arcL++) {
			for(int i = 0; i < 5; i++) {
				addValue(i, arcL);
			}			
		}
		xMaxPow = arcL;
		xMaxFreq = arcL;
		findMaxFreq();
		findMaxPow();
	}
	
	private void addValue(int id, int arcL) {
		if(data[id][arcL][3] > 0) {
			xySeries[id].add(arcL, data[id][arcL][3]);
		}
		else {
			highestUndefined = arcL;
		}
	}
	
	private  void findMaxFreq() {
		yMaxFreq =  xySeries[0].getMaxY();
		double max = xySeries[2].getMaxY();
		if(yMaxFreq < max) {
			yMaxFreq = max;
		}
		max = xySeries[4].getMaxY();
		if(yMaxFreq < max) {
			yMaxFreq = max;
		}
		yBaseValueFreq = PDFTools.getBaseValue(yMaxFreq, 4, 0.25,1,2,5);
		yMaxFreq = PDFTools.getNextMultipleOf(yBaseValueFreq, yMaxFreq);	
	}
	
	private  void findMaxPow() {
		yMaxPow =  xySeries[1].getMaxY();
		double max = xySeries[3].getMaxY();
		if(yMaxPow < max) {
			yMaxPow = max;
		}
		yBaseValuePow = PDFTools.getBaseValue(yMaxPow, 4, 0.25,1,2,5);
		yMaxPow = PDFTools.getNextMultipleOf(yBaseValuePow, yMaxPow);
	}
	
	private void addLegend() {
		
		PDFTools.insertTextBoxYCorrected(cts, fX0 + fW + pdt.space, (fY0 + fH / 2) + pdt.subDescSize*1.5f, "- primary peak", pdt.subDescSize, freqColors[0]);
		PDFTools.insertTextBoxYCorrected(cts, fX0 + fW + pdt.space, (fY0 + fH / 2) + pdt.subDescSize*0.5f, "- secondary peak", pdt.subDescSize, freqColors[1]);
		PDFTools.insertTextBoxYCorrected(cts, fX0 + fW + pdt.space, (fY0 + fH / 2) - pdt.subDescSize*0.5f, "- center of mass", pdt.subDescSize, freqColors[2]);
		
		PDFTools.insertTextBoxYCorrected(cts, pX0 + pW + pdt.space, (pY0 + pH/2) + pdt.subDescSize, "- primary peak", pdt.subDescSize, powColors[0]);
		PDFTools.insertTextBoxYCorrected(cts, pX0 + pW + pdt.space, (pY0 + pH/2), "- secondary peak" , pdt.subDescSize, powColors[1]);
	}
}
