package spqEval;

/** ===============================================================================
* SpermQEvaluator_.java Version 1.0.4
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
* 		research group Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany
* 		(http://www.iii.uni-bonn.de/en/wachten_lab/).
* 
* Funding: DFG priority program SPP 1726 “Microswimmers"
*    
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
*
* =============================================================================== */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import ij.IJ;
import ij.ImagePlus;
import spqEval.pdf.PDFPage;
import spqEval.pdf.PDFTools;
import spqEval.tools.constants;
import spqEval.tools.tools;

public class Main extends javax.swing.JFrame implements ActionListener {
	private static final String version = "1.0.4_preliminary";
	
	private static final long serialVersionUID = 1L;	
	
	private static String referenceLine = "This file was generated using SpermQ_Evaluator,"
			+ " a java application by Jan Niklas Hansen (\u00a9 2017 - 2019)"
			+ " (for credits see: https://github.com/IIIImaging/SpermQ_Evaluator).";
	
	public static final int ERROR = 0;
	public static final int NOTIF = 1;
	public static final int LOG = 2;
	
	static final double threshold = 0.70;
	
	static final SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
	static final SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
		
	static final String [] HEADRESULTS = {"angle theta", "head velocity (in 2D)", "max Intensity in head", "head coordinates"};
	static final String [] KYMORESULTS = {"Results Minimum:", "Results Maximum:", 
			"Results Median:", "Results Average:", "Results Amplitude:"};
	static final String [] KYMOFREQRESULTS = {"primary frequency", "peak height of primary frequency",
			"secondary frequency", "peak height of secondary frequency",
			"center-of-mass of frequency spectrum"};
	static final String [] HEADSAVE = {"Th2D", "HV", "HMaxI", "HCoord"};
	static final String [] KYMOSAVE = {"min", "max", "medi", "avg", "ampl"};
	static final String [] KYMOSAVEFREQ = {"f1", "a1", "f2","a2", "com"};
			
	LinkedList<File> filesToOpen = new LinkedList<File>();
	boolean done = false, dirSaved = false;
	File savedDir;// = new File(getClass().getResource(".").getFile());
	JMenuBar jMenuBar1;
	JMenu jMenu3, jMenu5;
	JSeparator jSeparator2;
	JPanel bgPanel, topPanel;
	JScrollPane jScrollPane1, logScrollPane;
	JList<Object> Liste1, logList;
	JButton loadButton, removeButton, goButton;
	
	private JProgressBar progressBar = new JProgressBar();
	
	private String [] notifications, log;
	private boolean notificationsAvailable = false, errorsAvailable = false;
	
	String desktopPath = "";
	String newLine = System.getProperty("line.separator");
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Main inst = new Main();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public Main() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
			String out = "";
			for(int err = 0; err < e.getStackTrace().length; err++){
				out += " \n " + e.getStackTrace()[err].toString();
			}
			this.logMessage("Exception while setting look and feel:" + out, NOTIF);			
		}
		
		File home = FileSystemView.getFileSystemView().getHomeDirectory(); 
		desktopPath = home.getAbsolutePath();
		
		int prefXSize = 600, prefYSize = 570;
		this.setMinimumSize(new java.awt.Dimension(prefXSize, prefYSize+40));
		this.setSize(prefXSize, prefYSize+40);			
		this.setTitle("SpermQ Evaluator " + version + " (\u00a9 2017 - 2018)");
//		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		//Surface
			bgPanel = new JPanel();
			bgPanel.setLayout(new BoxLayout(bgPanel, BoxLayout.Y_AXIS));
			bgPanel.setVisible(true);
			bgPanel.setPreferredSize(new java.awt.Dimension(prefXSize,prefYSize-20));
			{
				topPanel = new JPanel();
//				topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
				topPanel.setVisible(true);
				topPanel.setPreferredSize(new java.awt.Dimension(prefXSize,60));
				{
					JTextPane text = new JTextPane();
					text.setText("*** Credits and copyright information: https://github.com/IIIImaging/SpermQ_Evaluator ***");
					text.setBackground(this.getBackground());
					text.setForeground(Color.DARK_GRAY);
					text.setEditable(false);
					text.setVisible(true);
					topPanel.add(text);//, BorderLayout.NORTH);
				}
				{
					JTextPane text = new JTextPane();
					text.setText("Add SpermQ results (SpermQ output-folders) to the list below and start analysis.");
					text.setBackground(this.getBackground());
					text.setEditable(false);
					text.setVisible(true);
					topPanel.add(text);
				}
				bgPanel.add(topPanel);
			}
			{
				jScrollPane1 = new JScrollPane();
				jScrollPane1.setHorizontalScrollBarPolicy(30);
				jScrollPane1.setVerticalScrollBarPolicy(20);
				jScrollPane1.setPreferredSize(new java.awt.Dimension(prefXSize-10, 340));
				bgPanel.add(jScrollPane1);
				{
					Liste1 = new JList<Object>();
					jScrollPane1.setViewportView(Liste1);
					Liste1.setModel(new DefaultComboBoxModel(new String[] { "" }));
				}
				{
					JPanel spacer = new JPanel();
					spacer.setMaximumSize(new java.awt.Dimension(prefXSize,10));
					spacer.setVisible(true);
					bgPanel.add(spacer);
				}
				{
					JPanel bottom = new JPanel();
					bottom.setMaximumSize(new java.awt.Dimension(prefXSize,10));
					bottom.setVisible(true);
					bgPanel.add(bottom);
					int locHeight = 30;
					int locWidth3 = prefXSize/4-60;
					{
						loadButton = new JButton();
						loadButton.addActionListener(this);
						loadButton.setText("add SpermQ results folder(s) to list");
						loadButton.setMinimumSize(new java.awt.Dimension(locWidth3,locHeight));
						loadButton.setVisible(true);
						loadButton.setVerticalAlignment(SwingConstants.BOTTOM);
						bottom.add(loadButton);
					}
					{
						removeButton = new JButton();
						removeButton.addActionListener(this);
						removeButton.setText("remove folder(s) from list");
						removeButton.setMinimumSize(new java.awt.Dimension(locWidth3,locHeight));
						removeButton.setVisible(true);
						removeButton.setVerticalAlignment(SwingConstants.BOTTOM);
						bottom.add(removeButton);
					}	
					{
						goButton = new JButton();
						goButton.addActionListener(this);
						goButton.setText("start analysis");
						goButton.setMinimumSize(new java.awt.Dimension(locWidth3,locHeight));
						goButton.setVisible(true);
						goButton.setVerticalAlignment(SwingConstants.BOTTOM);
						bottom.add(goButton);
					}	
				}	
			}
			{
				progressBar = new JProgressBar();
				progressBar = new JProgressBar(0, 0);
				progressBar.setPreferredSize(new java.awt.Dimension(prefXSize,40));
				progressBar.setStringPainted(true);
				progressBar.setValue(0);
				progressBar.setMaximum(100);
				progressBar.setString("no analysis started!");
				bgPanel.add(progressBar);	
			}
			{
				JPanel spacer = new JPanel();
//				spacer.setBackground(Color.black);
				spacer.setMaximumSize(new java.awt.Dimension(prefXSize,10));
				spacer.setVisible(true);
				bgPanel.add(spacer);
			}
			{
				JPanel imPanel = new JPanel();
				imPanel.setLayout(new BorderLayout());
				imPanel.setVisible(true);
				imPanel.setPreferredSize(new java.awt.Dimension(prefXSize,60));
				{
					JLabel spacer = new JLabel("Log:", SwingConstants.LEFT);
					spacer.setMinimumSize(new java.awt.Dimension(prefXSize,20));
					spacer.setVisible(true);
					imPanel.add(spacer, BorderLayout.NORTH);
				}
				{	
					logScrollPane = new JScrollPane();
					logScrollPane.setHorizontalScrollBarPolicy(30);
					logScrollPane.setVerticalScrollBarPolicy(20);
					logScrollPane.setPreferredSize(new java.awt.Dimension(prefXSize, 40));
					imPanel.add(logScrollPane, BorderLayout.CENTER);
					{
						ListModel ListeModel = new DefaultComboBoxModel(new String[] { "" });
						logList = new JList();
						logScrollPane.setViewportView(logList);
						logList.setModel(ListeModel);
					}
				}
				bgPanel.add(imPanel);
			}
			
			getContentPane().add(bgPanel);		
			
			this.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
//		        	System.out.println("disposed");
		        	dispose();		        	
		        }
		    });
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object eventQuelle = ae.getSource();
		if (eventQuelle == loadButton){
			JFileChooser chooser = new JFileChooser();
			chooser.setPreferredSize(new Dimension(600,400));
			if(dirSaved){				
				chooser.setCurrentDirectory(savedDir);
			}			
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			Component frame = null;
			chooser.showOpenDialog(frame);
			
			File[] files = chooser.getSelectedFiles();
			for(int i = 0; i < files.length; i++){
				filesToOpen.add(files[i]);
				savedDir = files[i];
				dirSaved=true;
			}			
			updateDisplay();
		}
		if (eventQuelle == removeButton){
			int[] indices = Liste1.getSelectedIndices();
			for(int i = indices.length-1; i >=0; i--){
				filesToOpen.remove(indices[i]);
			}
			updateDisplay();
		}
		if (eventQuelle == goButton){
			{
				Thread analyze = new Thread(){
					public void run(){		
						analyze();
						Thread.currentThread().interrupt();
					}
				};
				analyze.start();
			}
			System.gc();
		}		
		
	}
	
	private void analyze(){
		goButton.setEnabled(false);
		removeButton.setEnabled(false);
		loadButton.setEnabled(false);
		progressBar.setString("analyzing");
		bgPanel.updateUI();
		
		this.clearLog();
		//create new file
		Date now = new Date();
		
		File outputFolder = new File(desktopPath + System.getProperty("file.separator") 
			+ "SPQEV " + NameDateFormatter.format(now) + System.getProperty("file.separator"));
		PDFPage page;

		//create Result objects
		ArrayList<Result> results = new ArrayList<Result>(filesToOpen.size());
		try {
			// if file doesnt exists, then create it
			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}			
			int slicesPerCycle [] = new int [filesToOpen.size()];
			Arrays.fill(slicesPerCycle, 0);
			for(int i = 0; i < filesToOpen.size(); i++){
				Result r = new Result(filesToOpen.get(i).getPath(),threshold);
				results.add(r);
				if(r.valid == false){
					this.logMessage("The metadata for task " + (i + 1) + " could not be retrieved!", NOTIF);
//				}else{
//					System.out.println("valid");
				}
				progressBar.setString("Producing PDF for file " + (i+1) + " of " + filesToOpen.size());		
				try{
					page = new PDFPage(r.directory, filesToOpen.get(i).getName(), outputFolder.getPath() + System.getProperty("file.separator"),r.threshold);
					slicesPerCycle [i] = (int) page.getSlicesPerCycle();
				}catch(Exception e){
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					this.logMessage("Failed to produce PDF for file " + (i+1) + " \n Error message:\n" 
							+ e.getCause() + "\n" + out, ERROR);
				}				
				progressBar.setValue((int)Math.round((double)(i+1.0)*(10.0/(double)filesToOpen.size())));
			}
			//save list of results data
			this.saveResultsList(outputFolder, now);				
			progressBar.setValue(15);
//			progressBar.updateUI();
			
			//get kymograph results curvature Angle
			String [] kymoTypes = {"cAng", "Curv", "X", "Y", "Z"};
			for(int t = 0; t < kymoTypes.length; t++){
				progressBar.setString("Determining " + kymoTypes [t] + " results");
				this.saveKymographResults(outputFolder, now, results, kymoTypes [t], slicesPerCycle);
				progressBar.setString("Determining " + kymoTypes [t] + " - frequency results");
				this.saveFrequencyResults(outputFolder, now, results, kymoTypes [t] + "_f");
				progressBar.setValue(15+(t+1)*5);
			}
			
			//theta, hri
			progressBar.setString("Determining head results");
			this.saveHeadResults(outputFolder, now, results);
			progressBar.setValue(45);
			
			//theta frequency, hri frequency
			progressBar.setString("Determining head frequency results");
			this.saveThetaFreqResults(outputFolder, now, results);
			this.saveHRIFreqResults(outputFolder, now, results);
			progressBar.setValue(65);
			
			//Save log file
			this.logMessage("Processing done. A file (" + outputFolder.getName() + ") was created on the desktop.", LOG);
			this.saveLog(outputFolder.getPath() + System.getProperty("file.separator") + "Log.txt");			
		}catch (Exception e) {
			outputFolder.deleteOnExit();
			progressBar.setString("Sorry .. no file could be generated. An error occured during file reading / writing.");
			progressBar.setValue(100); 		
			progressBar.setStringPainted(true);
			progressBar.setForeground(Color.red);
			String out = "";
			for(int err = 0; err < e.getStackTrace().length; err++){
				out += " \n " + e.getStackTrace()[err].toString();
			}
			this.logMessage("Sorry .. no file could be generated. An error occured during file reading / writing:\n" 
					+ e.getCause() + "\n" + out, ERROR);
		}			
		results.clear();
		System.gc();
		if(errorsAvailable){
			progressBar.setString("processing done but some tasks failed (see log)!");
			progressBar.setValue(100); 		
			progressBar.setStringPainted(true);
			progressBar.setForeground(Color.red);
			progressBar.updateUI();
		}else if(notificationsAvailable){
			progressBar.setString("processing done, but some notifications are available (see log)!");
			progressBar.setValue(100); 
			progressBar.setStringPainted(true);
			progressBar.setForeground(new Color(255,130,0));
			progressBar.updateUI();
		}else{				
			progressBar.setString("analysis done!");
			progressBar.setStringPainted(true);
			progressBar.setValue(100); 
			progressBar.setForeground(new Color(0,140,0));
			progressBar.updateUI();
		}
		
		goButton.setEnabled(true);
		removeButton.setEnabled(true);
		loadButton.setEnabled(true);
		bgPanel.updateUI();
	}
	
	private void saveResultsList(File file, Date d){
		File fileRes;
		FileWriter fw;
		BufferedWriter bw;
		{
			//save list of results
			fileRes = new File(file.getPath() + System.getProperty("file.separator")
			+	"List.txt");
			
			try {
				if (!fileRes.exists()) {
					fileRes.createNewFile();
				}	
				fw = new FileWriter(fileRes.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				
				bw.write(referenceLine + newLine);
				bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
				bw.write(newLine);
				bw.write("This analyze contains results for the following " + filesToOpen.size() + " SpermQ-analysis results: " + newLine);
				for(int i = 0; i < filesToOpen.size(); i++){
					bw.write(filesToOpen.get(i).getPath().substring(0, 
							filesToOpen.get(i).getPath().lastIndexOf(System.getProperty("file.separator")))
							+ "	" + filesToOpen.get(i).getName() + newLine);
				}
				bw.close();
				fw.close();	
			} catch (IOException e) {
				String out = "";
				for(int err = 0; err < e.getStackTrace().length; err++){
					out += " \n " + e.getStackTrace()[err].toString();
				}
				this.logMessage("no results list generated - IOException:\n" + out, NOTIF);				
			}		
		}
	}
	
	private void saveThetaFreqResults(File file, Date d, ArrayList<Result> results){
		//save Theta Frequency Results File		
		double [][][] resultsArray = new double [results.size()][5][4];	// results, freqResultsType, min/max/avg/medi
		{
			double [][] headFreqResult;
			for(int i = 0; i < results.size(); i++){
				headFreqResult = results.get(i).getHeadFrequencyResults("Th2D");
				for(int k = 0; k < 5; k++){
					if(headFreqResult [k].length > 1){
						resultsArray [i][k][0] = tools.getMinimumWithinRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						resultsArray [i][k][1] = tools.getMaximumWithinRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						resultsArray [i][k][2] = tools.getAverageOfRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						resultsArray [i][k][3] = tools.getMedianOfRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						
					}else if(headFreqResult [k].length == 1){
						resultsArray [i][k][0] = headFreqResult[k][0];
						resultsArray [i][k][1] = headFreqResult[k][0];
						resultsArray [i][k][2] = headFreqResult[k][0];
						resultsArray [i][k][3] = headFreqResult[k][0];					
					}
					else{
						for(int l = 0; l < 4; l++){
							resultsArray [i][k][l] = Double.NEGATIVE_INFINITY;
						}
					}
				}
			}
		}
		System.gc();
		
		
		File fileRes;
		FileWriter fw;
		BufferedWriter bw;
		{
			//save results
			for(int k = 0; k < 5; k++){
				fileRes = new File(file.getPath() + System.getProperty("file.separator")
				+ HEADSAVE [0] + "_f_" + KYMOSAVEFREQ[k] + ".txt");
				
				try {
					if (!fileRes.exists()) {
						fileRes.createNewFile();
					}	
					fw = new FileWriter(fileRes.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					
					bw.write(referenceLine + newLine);
					bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
					bw.write(newLine);
					bw.write("This file contains the frequency results for the parameter " + HEADRESULTS [0] + " - " + KYMOFREQRESULTS [k] + ":" + newLine);
					String appendTxt = "";
					for(int i = 0; i < results.size(); i++){
						appendTxt += "	" + results.get(i).directory.substring(
								results.get(i).directory.lastIndexOf(
										System.getProperty("file.separator"))+1);
					}
					bw.write(appendTxt + newLine);
					
					for(int l = 0; l < 4; l++){
						appendTxt = KYMORESULTS [l];	
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	";
							if(resultsArray[i][k][l] != Double.NEGATIVE_INFINITY){
								appendTxt += constants.df6US.format(resultsArray[i][k][l]);
							}
						}
						bw.write(appendTxt + newLine);	
					}					
										
					bw.close();
					fw.close();	
				} catch (IOException e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
				}
			}
		}
		System.gc();
	}
	
	private void saveHRIFreqResults(File file, Date d, ArrayList<Result> results){
		//save Theta Frequency Results File		
		double [][][] resultsArray = new double [results.size()][5][4];	// results, freqResultsType, min/max/avg/medi
		{
			double [][] headFreqResult;
			for(int i = 0; i < results.size(); i++){
				headFreqResult = results.get(i).getHeadFrequencyResults("HRMaxInt");
				for(int k = 0; k < 5; k++){
					if(headFreqResult [k].length > 1){
						resultsArray [i][k][0] = tools.getMinimumWithinRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						resultsArray [i][k][1] = tools.getMaximumWithinRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						resultsArray [i][k][2] = tools.getAverageOfRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						resultsArray [i][k][3] = tools.getMedianOfRange(headFreqResult[k], 0, headFreqResult[k].length-1);
						
					}
					else if(headFreqResult [k].length == 1){
						resultsArray [i][k][0] = headFreqResult[k][0];
						resultsArray [i][k][1] = headFreqResult[k][0];
						resultsArray [i][k][2] = headFreqResult[k][0];
						resultsArray [i][k][3] = headFreqResult[k][0];					
					}
					else{
						for(int l = 0; l < 4; l++){
							resultsArray [i][k][l] = Double.NEGATIVE_INFINITY;
						}
					}
				}
			}
		}
		System.gc();
		
		
		File fileRes;
		FileWriter fw;
		BufferedWriter bw;
		{
			//save results
			for(int k = 0; k < 5; k++){
				fileRes = new File(file.getPath() + System.getProperty("file.separator")
				+ HEADSAVE [2] + "_f_" + KYMOSAVEFREQ[k] + ".txt");
				
				try {
					if (!fileRes.exists()) {
						fileRes.createNewFile();
					}	
					fw = new FileWriter(fileRes.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					
					bw.write(referenceLine + newLine);
					bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
					bw.write(newLine);
					bw.write("This file contains the frequency results for the parameter " + HEADRESULTS [2] + " - " + KYMOFREQRESULTS [k] + ":" + newLine);
					String appendTxt = "";
					for(int i = 0; i < results.size(); i++){
						appendTxt += "	" + results.get(i).directory.substring(
								results.get(i).directory.lastIndexOf(
										System.getProperty("file.separator"))+1);
					}
					bw.write(appendTxt + newLine);
					
					for(int l = 0; l < 4; l++){
						appendTxt = KYMORESULTS [l];	
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	";
							if(resultsArray[i][k][l] != Double.NEGATIVE_INFINITY){
								appendTxt += constants.df6US.format(resultsArray[i][k][l]);
							}
						}
						bw.write(appendTxt + newLine);	
					}					
										
					bw.close();
					fw.close();	
				} catch (IOException e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
				}
			}
		}
		System.gc();
	}
	
	
	private void saveHeadResults(File file, Date d, ArrayList<Result> results){
		//Retrieve results
		ArrayList<float[][]> kymoData = new ArrayList<float[][]>(results.size());
		float maxFrameNr = 0.0f;
		double maxTimePerFrame = 0.0;
//		double minCal = Double.MAX_VALUE;
		for(int i = 0; i < results.size(); i++){
//			if(results.get(i).valid){
//				System.out.println("valid1");
//			}else{
//				System.out.println("invalid");
//			}
			float kymoRes [][] = results.get(i).getHeadResults();
			if(results.get(i).valid){
//				System.out.println("valid2");
				if(maxFrameNr < kymoRes [0].length * results.get(i).timePerFrame){
					maxFrameNr = (float)(kymoRes [0].length * results.get(i).timePerFrame);
				}
				if(maxTimePerFrame < results.get(i).timePerFrame){
					maxTimePerFrame = results.get(i).timePerFrame;
				}
//				if(minCal > results.get(i).calibration){
//					minCal = results.get(i).calibration;
//				}
			}
			kymoData.add(kymoRes);
		}
		
		int maxPos = (int)Math.round(maxFrameNr/maxTimePerFrame)+1;		
//		System.out.println("" + maxAl + " / " + maxCal + " = " + maxPos);
		float [][][][] kymoDataArray = new float [6][kymoData.size()][maxPos][2];
		{
			for(int i = 0; i < kymoData.size(); i++){
				for(int j = 0; j < maxPos; j++){
					for(int k = 0; k < 6; k++){
						kymoDataArray [k][i][j][0] = 0.0f;
						kymoDataArray [k][i][j][1] = 0.0f;
					}
				}
			}
			float kymoRes [][];
			int pos;
			for(int i = 0; i < kymoData.size(); i++){
				if(results.get(i).valid){
					kymoRes = kymoData.get(i);
					for(int j = 0; j < kymoRes [0].length; j++){
						for(int k = 0; k < 6; k++){
							if(kymoRes[k][j] != Float.NEGATIVE_INFINITY
									&& !Float.isNaN(kymoRes[k][j])){
								pos = (int)Math.round(j * results.get(i).timePerFrame / maxTimePerFrame);
								kymoDataArray [k][i][pos][0] = kymoDataArray [k][i][pos][0] + kymoRes [k][j];
								kymoDataArray [k][i][pos][1] = kymoDataArray [k][i][pos][1] + 1.0f;
							}
						}
					}
				}
			}
		}		
		kymoData.clear();
		kymoData = null;
		System.gc();
		
		//find last > 0
		int lastPos = maxPos;
		searching: for(int j = maxPos-1; j >=0 ; j--){
			for(int i = 0; i < kymoDataArray [0].length; i++){
				for(int k = 0; k < 6; k++){
					if(kymoDataArray [k][i][j][1] != 0.0f){
						lastPos = j+1;
						break searching;
					}
				}
			}
		}
		
		saveHeadCoordinates(file, d, kymoDataArray, results, lastPos, maxTimePerFrame);	//TODO
				
		File fileRes;
		FileWriter fw;
		BufferedWriter bw;
		{
			//save results
			for(int k = 0; k < 3; k++){
				fileRes = new File(file.getPath() + System.getProperty("file.separator")
				+ HEADSAVE [k] + ".txt");
				
				try {
					if (!fileRes.exists()) {
						fileRes.createNewFile();
					}	
					fw = new FileWriter(fileRes.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					
					bw.write(referenceLine + newLine);
					bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
					bw.write(newLine);
					bw.write("This file contains the kymograph results for the parameter " + HEADRESULTS [k] + ":" + newLine);
					String appendTxt = "";
					for(int i = 0; i < results.size(); i++){
						appendTxt += "	" + results.get(i).directory.substring(
								results.get(i).directory.lastIndexOf(
										System.getProperty("file.separator"))+1);
					}
					bw.write(appendTxt + newLine);
					
					//save min
					appendTxt = "minimum";
					float collArray [] = new float [lastPos];
					int ct;
					for(int i = 0; i < results.size(); i++){
						Arrays.fill(collArray, Float.MAX_VALUE);
						ct = 0;
						for(int j = 0; j < lastPos; j++){
							if(kymoDataArray[k][i][j][1] != 0.0f){
								collArray[ct] = kymoDataArray[k][i][j][0] / kymoDataArray[k][i][j][1];
								ct++;
							}	
						}												
						appendTxt += "	";
						
						if(ct > 1){
							appendTxt += constants.df6US.format(tools.getMinimumWithinRange(collArray, 0, ct-1));
						}else if(ct == 1){
							appendTxt += constants.df6US.format(collArray[0]);
						}
					}
					bw.write(appendTxt + newLine);
					
					//save min
					appendTxt = "maximum";
					for(int i = 0; i < results.size(); i++){
						Arrays.fill(collArray, Float.MAX_VALUE);
						ct = 0;
						for(int j = 0; j < lastPos; j++){
							if(kymoDataArray[k][i][j][1] != 0.0f){
								collArray[ct] = kymoDataArray[k][i][j][0] / kymoDataArray[k][i][j][1];
								ct++;
							}	
						}												
						appendTxt += "	";
						
						if(ct > 1){
							appendTxt += constants.df6US.format(tools.getMaximumWithinRange(collArray, 0, ct-1));
						}else if(ct == 1){
							appendTxt += constants.df6US.format(collArray[0]);
						}
					}
					bw.write(appendTxt + newLine);	
					
					//save average
					appendTxt = "average";
					for(int i = 0; i < results.size(); i++){
						Arrays.fill(collArray, Float.MAX_VALUE);
						ct = 0;
						for(int j = 0; j < lastPos; j++){
							if(kymoDataArray[k][i][j][1] != 0.0f){
								collArray[ct] = kymoDataArray[k][i][j][0] / kymoDataArray[k][i][j][1];
//								if(Float.isNaN(collArray[ct])){
//									System.out.println(" " + collArray[ct] + ":" + k + "." + i + "." + j + ":" + kymoDataArray[k][i][j][0] + ":" + kymoDataArray[k][i][j][1]);
//								}
								ct++;
							}	
						}												
						appendTxt += "	";
						
						if(ct > 1){
							appendTxt += constants.df6US.format(tools.getAverageOfRange(collArray, 0, ct-1));
						}else if(ct == 1){
							appendTxt += constants.df6US.format(collArray[0]);
						}
					}
					bw.write(appendTxt + newLine);	
					
					//save median
					appendTxt = "median";					
					for(int i = 0; i < results.size(); i++){
						Arrays.fill(collArray, Float.MAX_VALUE);
						ct = 0;
						for(int j = 0; j < lastPos; j++){
							if(kymoDataArray[k][i][j][1] != 0.0f){
								collArray[ct] = kymoDataArray[k][i][j][0] / kymoDataArray[k][i][j][1];
								ct++;
							}	
						}												
						appendTxt += "	";
						
						if(ct > 1){
							appendTxt += constants.df6US.format(tools.getMedianOfRange(collArray, 0, ct-1));
						}else if(ct == 1){
							appendTxt += constants.df6US.format(collArray[0]);
						}
					}
					bw.write(appendTxt + newLine);	
					
					bw.write("time" + newLine);						
					for(int j = 0; j < lastPos; j++){
						appendTxt = "" + constants.df6US.format(j*maxTimePerFrame);
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	";
							if(kymoDataArray[k][i][j][1] != 0.0f){
								appendTxt += constants.df6US.format(kymoDataArray[k][i][j][0] / kymoDataArray[k][i][j][1]);
							}								
						}
						bw.write(appendTxt + newLine);					
					}
					
					bw.close();
					fw.close();	
				} catch (IOException e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
				}
			}
		}
		System.gc();
	}
	
	private void saveHeadCoordinates(File file, Date d, float [][][][] kymoDataArray, ArrayList<Result> results, int lastPos, double maxTimePerFrame){
		File fileRes;
		FileWriter fw;
		BufferedWriter bw;
		
		fileRes = new File(file.getPath() + System.getProperty("file.separator")
		+ HEADSAVE [3] + ".txt");
		
		try {
			if (!fileRes.exists()) {
				fileRes.createNewFile();
			}	
			fw = new FileWriter(fileRes.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			
			bw.write(referenceLine + newLine);
			bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
			bw.write(newLine);
			bw.write("This file contains the kymograph results for the parameter " + HEADRESULTS [3] + ":" + newLine);
			String appendTxt = "";
			for(int i = 0; i < results.size(); i++){
				appendTxt += "	" + results.get(i).directory.substring(
						results.get(i).directory.lastIndexOf(
								System.getProperty("file.separator"))+1)
						+ "		";
			}
			bw.write(appendTxt + newLine);
			
			appendTxt = "time";
			for(int i = 0; i < results.size(); i++){
				appendTxt += "	X	Y	Z (fit width)";
			}
			bw.write(appendTxt + newLine);
			
			for(int j = 0; j < lastPos; j++){
				appendTxt = "" + constants.df6US.format(j*maxTimePerFrame);
				for(int i = 0; i < results.size(); i++){
					appendTxt += "	";
					if(kymoDataArray[3][i][j][1] != 0.0f){
						appendTxt += constants.df6US.format(kymoDataArray[3][i][j][0] / kymoDataArray[3][i][j][1]);
					}
					appendTxt += "	";
					if(kymoDataArray[4][i][j][1] != 0.0f){
						appendTxt += constants.df6US.format(kymoDataArray[4][i][j][0] / kymoDataArray[4][i][j][1]);
					}
					appendTxt += "	";
					if(kymoDataArray[5][i][j][1] != 0.0f){
						appendTxt += constants.df6US.format(kymoDataArray[5][i][j][0] / kymoDataArray[5][i][j][1]);
					}
				}
				bw.write(appendTxt + newLine);					
			}
			bw.close();
			fw.close();	
		} catch (IOException e) {
			String out = "";
			for(int err = 0; err < e.getStackTrace().length; err++){
				out += " \n " + e.getStackTrace()[err].toString();
			}
			this.logMessage("no head coordinate file generated - IOException:\n" + out, NOTIF);			
		}
		 
	}
	
	private void saveKymographResults(File file, Date d, ArrayList<Result> results, String keySeq, int [] slicesPerCycle){
		/**
		 * From version v1.0.4: use method "getFlagellarParameterResult" instead of "getKymoResult", which reads the corresponding text file using BufferedReader and FileReader -> higher precision + method relying on double and not float
		 * ***/
		
		//Retrieve results
		ArrayList<double[][]> kymoData = new ArrayList<double[][]>(results.size());
		double maxAl = 0.0;
		double maxCal = 0.0;
//		double minCal = Double.MAX_VALUE;
		for(int i = 0; i < results.size(); i++){
			double kymoRes [][] = results.get(i).getFlagellarParameterResult(keySeq, slicesPerCycle[i]);
			if(results.get(i).valid){
				if(maxAl < kymoRes.length * results.get(i).calibration){
					maxAl = (double)(kymoRes.length * results.get(i).calibration);
				}
				if(maxCal < results.get(i).calibration){
					maxCal = results.get(i).calibration;
				}
//				if(minCal > results.get(i).calibration){
//					minCal = results.get(i).calibration;
//				}
			}
			kymoData.add(kymoRes);
		}
//		System.out.println("maxCal" + maxCal + " cal of 0: " + results.get(0).calibration);
				
		int maxPos = (int)Math.round(maxAl/maxCal)+1;
		double [][][][] kymoDataArray = new double [kymoData.size()][maxPos][4][2];
		{
			for(int i = 0; i < kymoData.size(); i++){
				for(int j = 0; j < maxPos; j++){
					for(int k = 0; k < 4; k++){
						kymoDataArray [i][j][k][0] = 0.0f;
						kymoDataArray [i][j][k][1] = 0.0f;
					}
				}
			}
			double kymoRes [][];
			int pos;
			for(int i = 0; i < kymoData.size(); i++){
				if(results.get(i).valid){
					kymoRes = kymoData.get(i);
					for(int j = 0; j < kymoRes.length; j++){
						for(int k = 0; k < 4; k++){
							if(kymoRes[j][k] != Double.NEGATIVE_INFINITY
									&& !Double.isNaN(kymoRes[j][k])){
								pos = (int)Math.round(j * results.get(i).calibration/maxCal);
//								if(keySeq == "cAng" && k==3) System.out.println("other " + k + ": " + pos + ":	" + kymoRes [j][k]);	
								kymoDataArray [i][pos][k][0] = kymoDataArray [i][pos][k][0] + kymoRes [j][k];
								kymoDataArray [i][pos][k][1] = kymoDataArray [i][pos][k][1] + 1.0f;
							}							
						}
					}
				}
			}
		}
		
		//find last > 0
		int lastPos = maxPos;
		searching: for(int j = maxPos-1; j >=0 ; j--){
			for(int i = 0; i < kymoData.size(); i++){
				for(int k = 0; k < 4; k++){
					if(kymoDataArray [i][j][k][1] != 0.0f){
						lastPos = j+1;
						break searching;
					}
				}
			}
		}
		
		kymoData.clear();
		kymoData = null;
		System.gc();
		
		File fileRes;
		FileWriter fw;
		BufferedWriter bw;
		{
			//save results
			for(int k = 0; k < 4; k++){
				fileRes = new File(file.getPath() + System.getProperty("file.separator")
				+ keySeq + "_" + KYMOSAVE [k] + ".txt");
				
				try {
					if (!fileRes.exists()) {
						fileRes.createNewFile();
					}	
					fw = new FileWriter(fileRes.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					
					bw.write(referenceLine + newLine);
					bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
					bw.write(newLine);
					bw.write("This file contains the kymograph results for the parameter " + keySeq + ":" + newLine);
					bw.write(KYMORESULTS [k] + newLine);
					String appendTxt = "arc length";
					for(int i = 0; i < results.size(); i++){
						appendTxt += "	" + results.get(i).directory.substring(
								results.get(i).directory.lastIndexOf(
										System.getProperty("file.separator"))+1);
					}
					bw.write(appendTxt + newLine);
					
					for(int j = 0; j < lastPos; j++){
						appendTxt = "" + constants.df6US.format(j*maxCal);
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	";
							if(kymoDataArray[i][j][k][1] != 0.0f){
//								if(keySeq == "cAng" && k==3) System.out.println("out " + constants.df6US.format(kymoDataArray[i][j][k][0] / kymoDataArray[i][j][k][1]));
								appendTxt += constants.df6US.format(kymoDataArray[i][j][k][0] / kymoDataArray[i][j][k][1]);
							}								
						}
						bw.write(appendTxt + newLine);					
					}
					
					bw.close();
					fw.close();	
				} catch (IOException e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
				}
			}
			
			//save amplitude text file
			/**
			 * Method changed from version 1.0.3 on
			 * Until version 1.0.3: Amplitude = Max - Min
			 * From version 1.0.3 on: make chunks of 3 main beat cycles and determine min + max in each of them.
			 * Take the median max and the median min value of all chunks.
			 * */
			{				
				fileRes = new File(file.getPath() + System.getProperty("file.separator")
				+ keySeq + "_" + KYMOSAVE [4] + ".txt");
				
				try {
					if (!fileRes.exists()) {
						fileRes.createNewFile();
					}	
					fw = new FileWriter(fileRes.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					
					bw.write(referenceLine + newLine);
					bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
					bw.write(newLine);
					bw.write("This file contains the kymograph results for the parameter " + keySeq + ":" + newLine);
					bw.write(KYMORESULTS [4] + newLine);
					String appendTxt = "arc length";
					for(int i = 0; i < results.size(); i++){
						appendTxt += "	" + results.get(i).directory.substring(
								results.get(i).directory.lastIndexOf(
										System.getProperty("file.separator"))+1);
					}
					bw.write(appendTxt + newLine);
					
					for(int j = 0; j < lastPos; j++){
						appendTxt = "" + constants.df6US.format(j*maxCal);
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	";
							if(kymoDataArray[i][j][1][1] != 0.0f && kymoDataArray[i][j][0][1] != 0.0f){
								appendTxt += constants.df6US.format(
										(kymoDataArray[i][j][1][0] / kymoDataArray[i][j][1][1])
										- (kymoDataArray[i][j][0][0] / kymoDataArray[i][j][0][1]));
							}								
						}
						bw.write(appendTxt + newLine);					
					}
					
					bw.close();
					fw.close();	
				}catch (IOException e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}
					this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
				}
			}
			
			//save abs text files
			{
				for(int k = 2; k < 4; k++){
					fileRes = new File(file.getPath() + System.getProperty("file.separator")
					+ keySeq + "_" + KYMOSAVE [k] + "_abs.txt");
					
					try {
						if (!fileRes.exists()) {
							fileRes.createNewFile();
						}	
						fw = new FileWriter(fileRes.getAbsoluteFile());
						bw = new BufferedWriter(fw);
						
						bw.write(referenceLine + newLine);
						bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
						bw.write(newLine);
						bw.write("This file contains the kymograph results for the parameter " + keySeq + ":" + newLine);
						bw.write(KYMORESULTS [k] + " Absolute values (without + or -):" + newLine);
						String appendTxt = "arc length";
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	" + results.get(i).directory.substring(
									results.get(i).directory.lastIndexOf(
											System.getProperty("file.separator"))+1);
						}
						bw.write(appendTxt + newLine);
						
						for(int j = 0; j < lastPos; j++){
							appendTxt = "" + constants.df6US.format(j*maxCal);
							for(int i = 0; i < results.size(); i++){
								appendTxt += "	";
								if(kymoDataArray[i][j][k][1] != 0.0f){
									appendTxt += constants.df6US.format(Math.sqrt(Math.pow((double) kymoDataArray[i][j][k][0] 
											/ (double)kymoDataArray[i][j][k][1], 2.0)));
								}								
							}
							bw.write(appendTxt + newLine);					
						}
						
						bw.close();
						fw.close();	
					} catch (IOException e) {
						String out = "";
						for(int err = 0; err < e.getStackTrace().length; err++){
							out += " \n " + e.getStackTrace()[err].toString();
						}
						this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
					}
				}
			}
		}
		System.gc();
	}
	
	private void saveFrequencyResults(File file, Date d, ArrayList<Result> results, String keySeq){
		//Retrieve results
		ArrayList<float[][][]> kymoData = new ArrayList<float[][][]>(results.size());
		float maxAl = 0.0f;
		double maxCal = 0.0;
//		double minCal = Double.MAX_VALUE;
		for(int i = 0; i < results.size(); i++){
			float kymoRes [][][] = results.get(i).getFreqResults(keySeq);
			if(results.get(i).valid){
				if(maxAl < kymoRes [0].length * results.get(i).calibration){
					maxAl = (float)(kymoRes [0].length * results.get(i).calibration);
				}
				if(maxCal < results.get(i).calibration){
					maxCal = results.get(i).calibration;
				}
//				if(minCal > results.get(i).calibration){
//					minCal = results.get(i).calibration;
//				}
			}
			kymoData.add(kymoRes);
		}
		
		
		int maxPos = (int)Math.round(maxAl/maxCal)+1;
		for(int f = 0; f < 5; f++){
			float [][][][] kymoDataArray = new float [kymoData.size()][maxPos][4][2];
			{
				for(int i = 0; i < kymoData.size(); i++){
					for(int j = 0; j < maxPos; j++){
						for(int k = 0; k < 4; k++){
							kymoDataArray [i][j][k][0] = 0.0f;
							kymoDataArray [i][j][k][1] = 0.0f;
						}
					}
				}
				float kymoRes [][];
				int pos;
				for(int i = 0; i < kymoData.size(); i++){
					if(results.get(i).valid){
						kymoRes = kymoData.get(i) [f];
						for(int j = 0; j < kymoRes.length; j++){
							for(int k = 0; k < 4; k++){
								if(kymoRes[j][k] != Float.NEGATIVE_INFINITY){
									pos = (int)Math.round(j * results.get(i).calibration/maxCal);
									kymoDataArray [i][pos][k][0] = kymoDataArray [i][pos][k][0] + kymoRes [j][k];
									kymoDataArray [i][pos][k][1] = kymoDataArray [i][pos][k][1] + 1.0f;
								}							
							}
						}
					}
				}
			}
						
			//find last > 0
			int lastPos = maxPos;
			searching: for(int j = maxPos-1; j >=0 ; j--){
				for(int i = 0; i < kymoDataArray.length; i++){
					for(int k = 0; k < 4; k++){
						if(kymoDataArray [i][j][k][1] != 0.0f){
							lastPos = j+1;
							break searching;
						}
					}
				}
			}
			
			File fileRes;
			FileWriter fw;
			BufferedWriter bw;
			{
				//save results
				for(int k = 0; k < 4; k++){
					fileRes = new File(file.getPath() + System.getProperty("file.separator")
					+ keySeq + "_" + KYMOSAVE [k] + "_" + KYMOSAVEFREQ [f] + ".txt");
					
					try {
						if (!fileRes.exists()) {
							fileRes.createNewFile();
						}	
						fw = new FileWriter(fileRes.getAbsoluteFile());
						bw = new BufferedWriter(fw);
						
						bw.write(referenceLine + newLine);
						bw.write("Date of processing:	" + FullDateFormatter.format(d) + newLine);
						bw.write(newLine);
						bw.write("This file contains the kymograph results for the parameter " + keySeq + ":" + newLine);
						bw.write(KYMOFREQRESULTS [f] + "	" + KYMORESULTS [k] + newLine);
						String appendTxt = "arc length";
						for(int i = 0; i < results.size(); i++){
							appendTxt += "	" + results.get(i).directory.substring(
									results.get(i).directory.lastIndexOf(
											System.getProperty("file.separator"))+1);
						}
						bw.write(appendTxt + newLine);
						
						for(int j = 0; j < lastPos; j++){
							appendTxt = "" + constants.df6US.format(j*maxCal);
							for(int i = 0; i < results.size(); i++){
								appendTxt += "	";
								if(kymoDataArray[i][j][k][1] != 0.0f){
									appendTxt += constants.df6US.format(kymoDataArray[i][j][k][0] / kymoDataArray[i][j][k][1]);
								}								
							}
							bw.write(appendTxt + newLine);					
						}
						
						bw.close();
						fw.close();	
					}catch (IOException e) {
						String out = "";
						for(int err = 0; err < e.getStackTrace().length; err++){
							out += " \n " + e.getStackTrace()[err].toString();
						}
						this.logMessage("no results list generated - IOException:\n" + out, NOTIF);			
					}
				}
			}
			System.gc();
		}
		kymoData.clear();
		kymoData = null;
		System.gc();
	}
	
	private void updateDisplay(){
		String resultsString [] = new String [filesToOpen.size()];
		for(int i = 0; i < filesToOpen.size(); i++){
			resultsString [i] = (i+1) + ": " + filesToOpen.get(i).getName();
		}
		Liste1.setListData(resultsString);
	}
	
	private void clearLog(){
		log = null;
		if(notifications != null){
			logList.setListData(notifications);
		}else{
			logList.setListData(new String []{""});
		}
		bgPanel.updateUI();
	}
	
	private void saveLog(String path){
		if(log!=null){
			final SimpleDateFormat fullDate = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
			File file = new File(path);
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				PrintWriter pw = new PrintWriter(fw);
				pw.println(referenceLine);
				pw.println("Logfile created on:	" + fullDate.format(new Date()));
				pw.println("");
				for(int i = 0; i < log.length; i++){
					pw.println(log[i]);
				}			
				pw.close();
			}catch (IOException e) {
				String out = "";
				for(int err = 0; err < e.getStackTrace().length; err++){
					out += " \n " + e.getStackTrace()[err].toString();
				}				
				this.logMessage("Error while saving log: " + e.getCause() + out,ERROR);
			}
		}		
	}

	void logMessage(String message, int type) {
		if(type == ERROR){
			errorsAvailable = true;
		}else if(type == NOTIF){
			notificationsAvailable = true;
		}
		
		if(type == ERROR || type == NOTIF){
			if(notifications==null ){
				notifications = new String [1];
				notifications [0] = message;
			}else{
				String [] notificationsCopy = notifications.clone();
				notifications = new String [notifications.length+1];
				for(int j = 0; j < notificationsCopy.length; j++){
					notifications[j+1] = notificationsCopy[j];
				}
				notifications [0] = message;
			}
		}else{
			if(log==null ){
				log = new String [1];
				log [0] = message;
			}else{
				String [] logCopy = log;
				log = new String [log.length+1];
				for(int j = 0; j < logCopy.length; j++){
					log[j+1] = logCopy[j];
				}
				log [0] = message;
			}
		}
		
		if(notifications != null && log == null){
			logList.setListData(notifications);
		}else if(notifications == null && log != null){
			logList.setListData(log);
		}else if(notifications != null && log != null){
			String [] listData = new String [notifications.length + log.length];
			for(int i = 0; i < notifications.length; i++){
				listData [i] = notifications [i];
			}
			for(int i = 0; i < log.length; i++){
				listData [notifications.length + i] = log [i];
			}
			logList.setListData(listData);
		}		
		bgPanel.updateUI();
	}
}
