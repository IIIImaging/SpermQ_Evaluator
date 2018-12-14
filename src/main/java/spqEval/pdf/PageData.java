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

public class PageData {
	
	//Metadata
	
	String expName;		//name of the individual experiment
	String sourcePath;	//path of raw data (stacks, metadata etc)
	String prefix;		//sourcePath + name of experiment
	String targetPath;	//path where process data is saved to
	String dateAsString;
	
	//Page Settings
	
	int headerSize = 11;
	int space = 3;
	int header = headerSize + space;		//is the size of the header plus some pixels of space to the next content
	int descSize = 8;
	int subDescSize = 6;					//size for subdescription such as numbers 
	float lineWidth = 0.5f;
	int lutHeight = 10;
	
	//Plot settings
	int resolution = 12;						//defines resolution of the plot
	float plotLineWidthFactor = 0.8f;
	
	public PageData(String expName, String sourcePath, String targetPath) {
		this.expName = expName;
		this.sourcePath = sourcePath;
		this.prefix = sourcePath + expName;
		this.targetPath = targetPath;		
	}
	
	public void setPageSettings(int headerSize, int descSize) {
		this.descSize = descSize;
		this.headerSize = headerSize;
		this.header = headerSize + space;
	}
}
