package spqEval.lut;

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

public class PhaseLUT extends LUT {
	
	final static int [] red2 = {0,0,0,4,4,4,8,8,12,12,12,16,16,20,20,20,24,24,28,28,28,32,32,36,36,36,40,40,44,44,44,48,48,52,52,52,56,56,60,60,60,64,64,68,68,68,72,72,76,76,76,80,80,84,84,84,88,88,92,92,92,96,96,96,100,100,104,104,104,108,108,112,112,112,116,116,120,120,120,124,124,128,128,128,132,132,136,136,136,140,140,144,144,144,148,148,152,152,152,156,156,160,160,160,164,164,168,168,168,172,172,176,176,176,180,180,184,184,184,188,188,192,192,192,196,196,200,200,200,200,200,200,200,200,200,200,204,204,204,204,204,204,204,204,204,204,208,208,208,208,208,208,208,208,208,208,212,212,212,212,212,212,212,212,212,212,216,216,216,216,216,216,216,216,216,216,220,220,220,220,220,220,220,220,220,220,224,224,224,224,224,224,224,224,224,228,228,228,228,228,228,228,228,228,228,232,232,232,232,232,232,232,232,232,232,236,236,236,236,236,236,236,236,236,236,240,240,240,240,240,240,240,240,240,240,244,244,244,244,244,244,244,244,244,244,248,248,248,248,248,248,248,248,248,252,252};
	final static int [] green2 = {0,0,0,4,4,4,8,8,12,12,12,16,16,20,20,20,24,24,28,28,28,32,32,36,36,36,40,40,44,44,44,48,48,52,52,52,56,56,60,60,60,64,64,68,68,68,72,72,76,76,76,80,80,84,84,84,88,88,92,92,92,96,96,96,100,100,104,104,104,108,108,112,112,112,116,116,120,120,120,124,124,128,128,128,132,132,136,136,136,140,140,144,144,144,148,148,152,152,152,156,156,160,160,160,164,164,168,168,168,172,172,176,176,176,180,180,184,184,184,188,188,192,192,192,196,196,200,200,200,196,196,196,192,192,188,188,188,184,184,180,180,180,176,176,172,172,172,168,168,168,164,164,160,160,160,156,156,152,152,152,148,148,144,144,144,140,140,136,136,136,132,132,132,128,128,124,124,124,120,120,116,116,116,112,112,108,108,108,104,104,100,100,100,96,96,96,92,92,88,88,88,84,84,80,80,80,76,76,72,72,72,68,68,68,64,64,60,60,60,56,56,52,52,52,48,48,44,44,44,40,40,36,36,36,32,32,32,28,28,24,24,24,20,20,16,16,16,12,12,8,8,8,4,4,0,0};
	final static int [] blue2 = {252,252,252,252,252,252,252,252,252,252,248,248,248,248,248,248,248,248,248,248,244,244,244,244,244,244,244,244,244,244,240,240,240,240,240,240,240,240,240,236,236,236,236,236,236,236,236,236,236,232,232,232,232,232,232,232,232,232,232,228,228,228,228,228,228,228,228,228,224,224,224,224,224,224,224,224,224,224,220,220,220,220,220,220,220,220,220,220,216,216,216,216,216,216,216,216,216,212,212,212,212,212,212,212,212,212,212,208,208,208,208,208,208,208,208,208,208,204,204,204,204,204,204,204,204,204,200,200,200,196,196,196,192,192,188,188,188,184,184,180,180,180,176,176,172,172,172,168,168,168,164,164,160,160,160,156,156,152,152,152,148,148,144,144,144,140,140,136,136,136,132,132,132,128,128,124,124,124,120,120,116,116,116,112,112,108,108,108,104,104,100,100,100,96,96,96,92,92,88,88,88,84,84,80,80,80,76,76,72,72,72,68,68,68,64,64,60,60,60,56,56,52,52,52,48,48,44,44,44,40,40,36,36,36,32,32,32,28,28,24,24,24,20,20,16,16,16,12,12,8,8,8,4,4,0,0};
	
	public PhaseLUT() {	
		super(red2, green2, blue2);				
	}

}
