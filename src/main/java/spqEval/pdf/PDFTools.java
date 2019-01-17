package spqEval.pdf;

/** 
===============================================================================
* SpermQEvaluator_.java Version 1.0.5
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
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;


public class PDFTools {
	
	/**
	 * inserts textbox to PDPageContentStream. Textbox is above y-Coordinate and left bound to given x-coord.
	 */
	
	public static PDPageContentStream insertTextBoxLowerY(PDPageContentStream cts, int x, int y, String text, int textSize) {
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;			
			cts.beginText();
			cts.setFont(font, textSize);			
			cts.newLineAtOffset(x, y);
			cts.showText(text);
			cts.endText();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cts;
	}
	
	/**
	 * inserts textbox to PDPageContentStream. Textbox is above y-Coordinate and left bound to given x-coord.
	 */
	
	public static PDPageContentStream insertTextBoxYCorrected(PDPageContentStream cts, float x, float f, String text, int textSize, Color color) {
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;
			cts.beginText();
			cts.setFont(font, textSize);
			cts.setNonStrokingColor(color);
			cts.newLineAtOffset(x, f);
			cts.showText(text);
			cts.endText();
			cts.setNonStrokingColor(Color.BLACK);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cts;
	}
	
	/**
	 * inserts textbox to PDPageContentStream. Textbox is below given y-Coordinate and left bound to given x-coord.
	 */
	
	public static PDPageContentStream insertTextBoxUpperY(PDPageContentStream cts, int x, int y, String text, int textSize) {
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;			
			float height = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * textSize;
			cts.beginText();
			cts.setFont(font, textSize);			
			cts.newLineAtOffset(x, y - height);
			cts.showText(text);
			cts.endText();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cts;
	}
	
	/**
	 * inserts textbox to PDPageContentStream centered on given x-coordinate. Textbox is below Y-Coordinate.
	 */
	
	public static PDPageContentStream insertTextBoxXCentered(PDPageContentStream cts, float x, float y, String text, int textSize) {
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;			
			float width = font.getStringWidth(text)/1000*textSize;
			float height = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * textSize;
			cts.beginText();
			cts.setFont(font, textSize);			
			cts.newLineAtOffset(x - Math.round(width/2), y - height);
			cts.showText(text);
			cts.endText();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cts;
	}
	
	/**
	 * inserts textbox below defined y-value and bound the defined x-coordinate on the right side of the box
	 */
	
	public static PDPageContentStream insertTextBoxToRightBound(PDPageContentStream cts, float x, float f, String text, int textSize) {
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;			
			float width = font.getStringWidth(text)/1000*textSize;
			float height = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * textSize;
			cts.beginText();
			cts.setFont(font, textSize);			
			cts.newLineAtOffset(x - Math.round(width), f - height);
			cts.showText(text);
			cts.endText();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cts;
	}
	
	/**
	 * inserts textbox to PDPageContentStream bounding to the right. Textbox is centered on y-coordinate.
	 */
	
	public static PDPageContentStream insertTextBoxToRightBoundYCentrated(PDPageContentStream cts, float x, float y, String text, int textSize) {
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;			
			float width = font.getStringWidth(text)/1000*textSize;
			float height = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * textSize;
			cts.beginText();
			cts.setFont(font, textSize);			
			cts.newLineAtOffset(x - Math.round(width), Math.round( y - height/2));
			cts.showText(text);
			cts.endText();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cts;
	}
	
	/**
	 * inserts a rotated textbox, so that the text is written upward. Box is localized to the left bound and centered on y-axis
	 */
	
	public static PDPageContentStream insertTextBoxRotatedUpward(PDPageContentStream cts, float leftBound, float yCenter, String text, int textSize) {      
		try {
            cts.beginText();
            PDFont font = PDType1Font.HELVETICA_BOLD;
    		float length = font.getStringWidth(text)/1000*textSize;
			cts.setFont(font, textSize);
			cts.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, leftBound, yCenter - length/2)); 			// rotate the text according to the page rotation
			cts.showText(text);
			cts.endText();
        }
		catch(Exception e) {
			
		}
		return cts;
	}
		
	/**
	 * @param textSize
	 * @param text
	 * @return the maximum length of the all inserted Strings, useful to determine needed space to the left of another object
	 */
	
	public static int calculateMaxWidth (int textSize, String... text) {
		float width = 0;
		float localWidth = Float.NEGATIVE_INFINITY; 
		try {
			PDFont font = PDType1Font.HELVETICA_BOLD;
			for(String s : text) {
				localWidth = font.getStringWidth(s)/1000*textSize;
				if(localWidth > width) {
					width = localWidth;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return Math.round(width);
	}
	
	@SuppressWarnings("deprecation")
	public static PDPageContentStream drawPolygon(PDPageContentStream cts, float x0, float y0, float width,
			float height) {
		float[] x = { x0, x0 + width, x0 + width, x0 };
		float[] y = { y0, y0, y0 + height, y0 + height };
		try {
			cts.drawPolygon(x, y);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cts;
	}
	
	public static double getBaseValue (double range, int maxNumber, double... possibleValues) {
		double baseValue = 1;
		int i = 0;
		int pow = -8;
		looping: for(int j = 0; j < 100; j++,i++) {
			if(i%possibleValues.length == 0) pow++;
			baseValue = possibleValues[i%possibleValues.length] * Math.pow(10, pow);
			if(range/baseValue < maxNumber)break looping;
		}
		return baseValue;
	}
	
	/**
	 * @param base
	 * @param value
	 * @return the next multiple of the base value
	 */
	
	public static double getNextMultipleOf (double base, double value) {
		if(value%base < 0.001*value) {
			return value;
		}
		else if(value < 0) {
			double multiple = (int) (Math.abs(value)/base);
			return (0-(multiple * base));
		}
		else {
			double multiple = (int) (value/base + 1);
			return (multiple * base);
		}		
	}
	
	/**
	 * normalizes input values, so that the values range between 0 and 1.
	 * @return a float array with the normalized values between 0 and 1
	 */
	
	public static float [] normalizeValuesFloat(float [] input) {
		float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
		for(int i = 0; i < input.length; i++) {
			if(input[i] == Float.NEGATIVE_INFINITY){
				continue;
			}
			if(input[i] > max) {
				max = input[i];
			}
			if(input[i] < min) {
				min = input[i];
			}
		}
		float range = max-min;
		for(int i = 0; i < input.length; i++) {
			if(input[i] == Float.NEGATIVE_INFINITY) continue;
			input[i] = (input [i] - min)/(range);
		}
		return input;
	}
	
	/**
	 * normalizes input values, so that the values range between 0 and 1 
	 * - only for a range of values in the array.
	 * @return a float array with the normalized values between 0 and 1
	 */	
	public static float [] normalizeValuesFloatAccordingToRange(float [] input, int minIndex, int maxIndex) {
		float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
		for(int i = minIndex; i < maxIndex; i++) {
			if(input[i] == Float.NEGATIVE_INFINITY){
				continue;
			}
			if(input[i] > max) {
				max = input[i];
			}
			if(input[i] < min) {
				min = input[i];
			}
		}
		float range = max-min;
		for(int i = 0; i < input.length; i++) {
			if(input[i] == Float.NEGATIVE_INFINITY) continue;
			input[i] = (input [i] - min)/(range);
		}
		return input;
	}
	
	public static boolean isInBounds(int width, int height, int x, int y) {
		if(x < 0 || y < 0 || x >= width ||  y >= height) {
			return false;
		}
		else{
			return true;
		}
	}
	
	public static int handleChannelOverlapInRGB(int originalValue, int valueToAdd) {
		if(originalValue == 0) {
			return valueToAdd;
		}
		else {
			int [] out = new int [3];
			int [] ori = splitChannels(originalValue);
			int [] add = splitChannels(valueToAdd);
			
			for (int i = 0; i < out.length; i++) {
				out [i] = ori [i] + add[i];
				if(out [i] > 255) out [i] = 255; 
			}
			return out[0]*65536+out[1]*256+out[2];
		}
	}
	
	public static int [] splitChannels(double rgb) {
		int blue = (int) (rgb % 0x100);
		int green = (int) (rgb % 0x10000 - blue)/0x100;
		int red = (int) (rgb - green - blue)/0x10000;
		int [] a = {red, green, blue};
		return a;
	}
	
	public static double readNumber(String s) {
		s = s.substring(s.lastIndexOf("	") + 1);
		s = s.replace(",", ".");
		return Double.parseDouble(s);
	}

	public static double[] normalizeValues(double[] input) {
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		for (Double d : input) {
			if (d > max) {
				max = d;
			}
			if (d < min) {
				min = d;
			}
		}
		double range = max - min;
		for (int i = 0; i < input.length; i++) {
			input[i] = (input[i] - min) / (range);
		}
		return input;
	}
	
	public static ArrayList<Double> seperateNumbers (String line, int count){
	ArrayList<Double> numbers = new ArrayList<Double>(count);
	for(String s : seperateLineToStrings(line, count)) {
		if(s.contains(",")) {	
			s = s.replace(",", ".");
		}
		numbers.add(Double.parseDouble(s));
	}
	return numbers;
	}
	
	public static ArrayList<String> seperateLineToStrings (String line, int count){
		ArrayList<String> strings = new ArrayList<String>(count);
		if(!line.contains("	")) {
			return strings;
		}
		String arg;
		line = line.substring(1);
		for(int i = 0; i < count - 1; i++) {
			arg = line.substring(0, line.indexOf("	")+1);
			strings.add(arg);
			line = line.substring(line.indexOf("	")+1);			
		}
		if(line.contains("	")) {
			arg = line.substring(0, line.indexOf("	")+1);
		}
		else {
			arg = line;
		}
		strings.add(arg);		
		return strings;
	}
	
	public static double getMin(double[] input) {
		double min = Double.MAX_VALUE;

		for (Double d : input) {
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public static double getMax(double[] input) {
		double max = Double.MIN_VALUE;

		for (Double d : input) {
			if (d > max) {
				max = d;
			}
		}
		return max;
	}
	
	
	
	
	
	
		

//	
//	public static double getBaseValue (double range, int maxNumber) {
//		double [] possibleValues = {1,2,5};
//		double baseValue = 1;
//		int i = 0;
//		int pow = -8;
//		looping: for(int j = 0; j < 100; j++,i++) {
//			if(i%3 == 0) pow++;
//			baseValue = possibleValues[i%3] * Math.pow(10, pow);
//			if(range/baseValue < maxNumber) break looping;
//		}
//		return baseValue;
//	}
//
//
//	public static String tN(String s) {
//		if (s.contains(",")) {
//			s = s.replace(",", ".");
//		}
//		return s;
//	}
//	
//
//	/**
//	 * transforms the inserted bounds into the nomenclature of the PDF
//	 * 
//	 * @param leftBound
//	 *            of the frame of the object within the page
//	 * @param upperBound
//	 *            of the frame of the object within the page
//	 * @param rightBound
//	 *            of the frame of the object within the page
//	 * @param lowerBound
//	 *            of the frame of the object within the page
//	 * @param imR
//	 *            float[] of relative positions within the frame
//	 *            ({left,upper,right,lower bound})
//	 * @return float [] ready to insert like contentStream.drawImage(image, [0],
//	 *         [1], [2], [3]);
//	 */
//
//	public static int[] calculateAbsolutePosition(int leftBound, int upperBound, int rightBound, int lowerBound,
//			float[] imR) {
//		int[] b = new int[4]; // boundsForPDImageXObject
//		int frameHeight = (upperBound - lowerBound);
//		int frameWidth = (rightBound - leftBound);
//
//		b[2] = Math.round(frameWidth * (imR[2] - imR[0])); // width
//		b[3] = Math.round(frameHeight * (imR[1] - imR[3])); // height
//		b[0] = Math.round(leftBound + (frameWidth * imR[0])); // Xo
//		b[1] = Math.round((upperBound - (frameHeight * (1 - imR[1]))) - b[3]); // Yo
//
//		return b;
//	}
//	
///**
// * @deprecated use insertTextBoxYCentered instead
// */
//	public static PDPageContentStream insertTextBox(PDPageContentStream cts, int xStart, int upperStart, String text,
//			int textSize) {
//
//		try {
//			PDFont font = PDType1Font.HELVETICA_BOLD;
//
//			cts.beginText();
//			cts.setFont(font, textSize);
//			cts.newLineAtOffset(xStart, upperStart - textSize);
//			cts.showText(text);
//			cts.endText();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//		return cts;
//	}
//	
//	public static PDPageContentStream insertTextBoxToRightBoundNative(PDPageContentStream cts, float x, float y, String text, int textSize) {
//		try {
//			PDFont font = PDType1Font.HELVETICA_BOLD;			
//			float width = font.getStringWidth(text)/1000*textSize;
//			cts.beginText();
//			cts.setFont(font, textSize);			
//			cts.newLineAtOffset(x - Math.round(width), y);
//			cts.showText(text);
//			cts.endText();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//		return cts;
//	}
//
//	public static PDPageContentStream insertTextBoxMulipleLines(PDPageContentStream cts, int xStart, int upperStart, String text, int textSize) {
//		ArrayList<String> l = new ArrayList<String>(0);
//		loop: while (true) {
//			if (text.contains("\n")) {
//				l.add(text.substring(0, text.lastIndexOf("\n")));
//				text = text.substring(text.indexOf("\n"));
//			} else {
//				break loop;
//			}
//		}
//		try {
//			PDFont font = PDType1Font.HELVETICA_BOLD;
//			
//			cts.beginText();
//			cts.setFont(font, 5);
//			cts.newLineAtOffset(xStart, upperStart-textSize);
//			cts.showText(text);
//			cts.endText();
//		}
//		catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//		return cts;
//	}
//
//	/**
//	 * @deprecated
//	 */
//
//	public static PDPageContentStream drawFrame(PDPageContentStream cts, int[] bounds) {
//		if (bounds.length != 4) {
//			String msg = "Error: Array inserted into drawFrame() method in tools class does not contain excatly 4 params!!";
//			cts = insertTextBox(cts, 100, 500, msg, 8);
//			cts = insertTextBox(cts, 100, 800, msg, 8);
//			cts = insertTextBox(cts, 100, 300, msg, 8);
//			cts = insertTextBox(cts, 10, 10, msg, 8);
//		} else {
//			try {
//				cts.drawLine(bounds[0], bounds[1], bounds[0] + bounds[2], bounds[1]);
//				cts.drawLine(bounds[0] + bounds[2], bounds[1], bounds[0] + bounds[2], bounds[3] + bounds[1]);
//				cts.drawLine(bounds[0] + bounds[2], bounds[3] + bounds[1], bounds[0], bounds[3] + bounds[1]);
//				cts.drawLine(bounds[0], bounds[3] + bounds[1], bounds[0], bounds[1]);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return cts;
//	}
//
//	public static PDPageContentStream drawRect(PDPageContentStream cts, float x0, float y0, float width, float height) {
//		try {
//			cts.addRect(x0, y0, width, height);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return cts;
//	}
//	
//	/**
//	 * @param originalValue original RGB value
//	 * @param valueToAdd value needs to be added to the original RGB value
//	 * @return new RGB value. The methods corrects for overflowing values, meaning that each color cannot get >255 and, thus, affect the next channel
//	 */
//	
//
//	/**
//	 * @param rgb
//	 * @return array with [0] = red, [1] = green, [2] = blue;
//	 */
//	
//	
//	/**
//	 * pastes columns from .txt (separated by tabs) / .xls into ArrayList with the separated Strings representing the values
//	 * @param count of tabs to scan (eg. for 0 -> 1 -> 2 -> 3 -> 4 and index defined as "2" it would return 0,1)
//	 */
//	
//	
//	
//	public static Point2D.Double getMinMax (double[] input) {
//		double min = Double.MAX_VALUE;
//		double max = Double.MIN_VALUE;
//		
//		for(Double d : input) {
//			if(d < min) {
//				min = d;
//			}
//			if(d > max) {
//				max = d;
//			}
//		}
//		return new Point2D.Double(min, max);
//	}
//	
	
}
