package spqEval.plot;

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;

public class Plot {
	
	double xFrame, yFrame;
	int width, height;
	
	boolean setRange = false;
	int xLower, xHigher, yLower, yHigher;
	
	String name, imagePath;	
	XYSeriesCollection dataSet;
	XYLineAndShapeRenderer renderer;
	NumberAxis xax, yax;
		
	boolean showPane = true, saveImage = true;
	
	public Plot(int width, int height, String targetPath, String name, XYSeries... data){
		imagePath = targetPath;
		this.width = width;
		this.height = height;
		this.name = name;
		dataSet = new XYSeriesCollection();
		
		for(XYSeries serie : data) {
			dataSet.addSeries(serie);
		}
		
		renderer = new XYLineAndShapeRenderer(true, false);
				
		xax = new NumberAxis("x");
		yax = new NumberAxis("y");
		xax.setVisible(false);
		yax.setVisible(false);
	}

	public void setLineColor(java.awt.Color... colors) {
		for(int i = 0; i < colors.length; i++) {
			renderer.setSeriesPaint(i, colors[i]);
		}
//		BasicStroke stroke = new BasicStroke(100f);
//		renderer.setBaseStroke(stroke);
	}
	
	public void setLineThickness(float thickness) {
		renderer.setBaseStroke(new BasicStroke(thickness));
	}
	
	public void setRanges(double xMax, double yMax) {
		xax.setRange(0, xMax);
		yax.setRange(0, yMax);
	}
	
	public void setRanges(double xMin, double yMin, double xMax, double yMax) {
		xax.setRange(xMin, xMax);
		yax.setRange(yMin, yMax);
	}
	
	@SuppressWarnings("deprecation")
	public void renderPlot(boolean showPane, boolean saveImage, float lineThickness) {

		XYPlot plot = new XYPlot(dataSet, xax, yax, renderer);
		plot.setRangeGridlinesVisible(false);
		plot.setDomainGridlinesVisible(false);
		plot.setAxisOffset(new RectangleInsets(-5, -9, -5, -8));				//changes the position of the frame containing the plot inside of the window, the grey background of the window will appear inside of the image of the plot
		plot.getRenderer().setStroke(new BasicStroke(lineThickness));
		
		JFreeChart chart = new JFreeChart(plot);
		chart.getPlot().setBackgroundPaint(Color.white);
		chart.removeLegend();
		
		if(showPane) showPane(chart);

		if(saveImage) saveAsImage(chart);
		
	}

	private boolean saveAsImage(JFreeChart chart) {
		BufferedImage objBufferedImage = chart.createBufferedImage(width,height);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		        try {
		            ImageIO.write(objBufferedImage, "png", bas);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }

		byte[] byteArray = bas.toByteArray();
		
		InputStream in = new ByteArrayInputStream(byteArray);
		BufferedImage image;
		try {
			image = ImageIO.read(in);
			File outputfile = new File(imagePath);
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void showPane(JFreeChart chart) {
		ChartPanel chartPanel = new ChartPanel(chart);
		ApplicationFrame punkteframe = new ApplicationFrame(name);
		punkteframe.setContentPane(chartPanel);
		punkteframe.pack();
		punkteframe.setVisible(true);		
	}
}
