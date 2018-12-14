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

public class PDFPlot extends PDFPage {
	
	//TODO move all common functions and variables/fields to this superclass
	
//	int nOfPlots;
//	int plotWidth, plotHeight;
//	ArrayList<XYSeries> series;
//	ArrayList<Color[]> colors;			//index of the List represents the plot index (same as index of series) and index of Array represents the Color of the line inside of the plot
//	ArrayList<Double> xMin, yMin, xMax, yMax;
	
	static int dX0;			//defines bounds of description box
	static int dY0;
	static int yOffset;
	String desc = "description missing!";

	public PDFPlot(String sourcePath, String expName, String targetPath) {
		super(sourcePath, expName, targetPath);
		// TODO Auto-generated constructor stub
	}

	public PDFPlot(String name) {
		super(name);
	}
	
	@Override
	protected void addDesc() {
		PDFTools.insertTextBoxUpperY(cts, x, y + 2*pdt.space, "(" + cID + ")" , pdt.headerSize);
		PDFTools.insertTextBoxLowerY(cts, dX0, dY0 - ID * (pdt.descSize + pdt.space), "(" + cID + ") " + desc, pdt.descSize);	
	}
}
