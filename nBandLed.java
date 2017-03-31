package jvst.defVstFft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.imageio.*;

//import jvst.vstFFT.util.fileIO.*;

public class nBandLed extends JPanel{
	private static final long serialVersionUID = 1L;

	public int NUM_BANDS = 10;
	
	int[] bandAmplitudeDbTarget; 
	int[] bandAmplitudeDbCurrent; 
	boolean[] bandAmplitudeTargetReached;
	public float MeterValue[]; //new data coming in
	public BufferedImage eqMeterImg;
	public int meterXOffset = 0;
	public int meterYOffset = 0;
	public int meterWidth = 0;
	public int meterHeight = 0;
	public int borderXOffset = 0; //x offset to account for border on the image
	public int borderYOffset = 0; //y offset to account for border on the image
	Timer timer;
	Timer timer2;
	
	/************************************************************************************************************
	 * Constructors
	 ************************************************************************************************************/
	public nBandLed(final String imagePath, int xOffset, int yOffset, int xBorderOffset, int yBorderOffset, int nBands) {
		super();
		
		//eqMeterImg = LoadBufferedImage.loadImage(imagePath);
		try{
			eqMeterImg = ImageIO.read(this.getClass().getResource(imagePath));
		} catch (Exception e){
			System.out.println("eqMeterImg error");
		}
		

		meterWidth = eqMeterImg.getWidth();
		meterHeight = eqMeterImg.getHeight();
		
		meterXOffset = xOffset;
		meterYOffset = yOffset;
		borderXOffset = xBorderOffset;
		borderYOffset = meterHeight - yBorderOffset; //y offset must also account for image height!!
		
		NUM_BANDS = nBands;
		
		bandAmplitudeDbTarget = new int[NUM_BANDS]; 
		bandAmplitudeDbCurrent = new int[NUM_BANDS]; 
		bandAmplitudeTargetReached = new boolean[NUM_BANDS];
		MeterValue = new float[NUM_BANDS]; //new data coming in
		
		this.add(new JLabel(new ImageIcon(eqMeterImg)));
		
		initializeEqMeter();     
	}
	
	private void initializeEqMeter() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setPreferredSize(new Dimension(meterWidth, meterHeight));
		this.setBounds(meterXOffset, meterYOffset, meterWidth, meterHeight);
		
		//Timer setup
		ActionListener taskPerformer = new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                //...Perform a task...
            	updateMeter();
            }
        };
		timer = new Timer(10, taskPerformer);
		timer.setRepeats(true);
        timer.start();
        
        //Timer2 setup
        ActionListener taskPerformer2 = new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                //...Perform a task...
            	for(int i = 0; i < NUM_BANDS; i++) {
            		//bandAmplitudeDbTarget[i] = (int) ((Math.random() * 20) + 1); //simulate an audio reading
            		//bandAmplitudeTargetReached[i] = false; // on new reading, reset all
            	}
            	
            }
        };
		timer2 = new Timer(100, taskPerformer2);
		timer2.setRepeats(true);
        timer2.start();
	}
	
	public void setBandBuffer(double[] inBuf, int len){
		if(len == 1024){
			bandAmplitudeDbTarget[0]    = (int) ((inBuf[1]   * 20) + 1);
			bandAmplitudeDbTarget[1]    = (int) ((inBuf[2]   * 20) + 1);
			bandAmplitudeDbTarget[2]    = (int) ((inBuf[4]   * 20) + 1);
			bandAmplitudeDbTarget[3]    = (int) ((inBuf[8]   * 20) + 1);
			bandAmplitudeDbTarget[4]    = (int) ((inBuf[16]  * 20) + 1);
			bandAmplitudeDbTarget[5]    = (int) ((inBuf[32]  * 20) + 1);
			bandAmplitudeDbTarget[6]    = (int) ((inBuf[64]  * 20) + 1);
			bandAmplitudeDbTarget[7]    = (int) ((inBuf[128] * 20) + 1);
			bandAmplitudeDbTarget[8]    = (int) ((inBuf[256] * 20) + 1);
			bandAmplitudeDbTarget[9]    = (int) ((inBuf[511] * 20) + 1);
		}                                       
		else if(len == 2048){                   
			bandAmplitudeDbTarget[0]    = (int) ((inBuf[2]    * 20) + 1);
			bandAmplitudeDbTarget[1]    = (int) ((inBuf[4]    * 20) + 1);
			bandAmplitudeDbTarget[2]    = (int) ((inBuf[8]    * 20) + 1);
			bandAmplitudeDbTarget[3]    = (int) ((inBuf[16]   * 20) + 1);
			bandAmplitudeDbTarget[4]    = (int) ((inBuf[32]   * 20) + 1);
			bandAmplitudeDbTarget[5]    = (int) ((inBuf[64]   * 20) + 1);
			bandAmplitudeDbTarget[6]    = (int) ((inBuf[128]  * 20) + 1);
			bandAmplitudeDbTarget[7]    = (int) ((inBuf[256]  * 20) + 1);
			bandAmplitudeDbTarget[8]    = (int) ((inBuf[511]  * 20) + 1);
			bandAmplitudeDbTarget[9]    = (int) ((inBuf[1023] * 20) + 1);
		}
		for(int i = 0; i < NUM_BANDS; i++) {
    		bandAmplitudeTargetReached[i] = false; // on new reading, reset all
    	}
	}
//	public void setBandBuffer(float[] inBuf, int len){
//		
//		if(len == 1024){
//			bandAmplitudeDbTarget[0]    = (int) ((inBuf[0]   * 20) + 1);
//			bandAmplitudeDbTarget[1]    = (int) ((inBuf[1]   * 20) + 1);
//			bandAmplitudeDbTarget[2]    = (int) ((inBuf[2]   * 20) + 1);
//			bandAmplitudeDbTarget[3]    = (int) ((inBuf[3]   * 20) + 1);
//			bandAmplitudeDbTarget[4]    = (int) ((inBuf[4]  * 20) + 1);
//			bandAmplitudeDbTarget[5]    = (int) ((inBuf[5]  * 20) + 1);
//			bandAmplitudeDbTarget[6]    = (int) ((inBuf[6]  * 20) + 1);
//			bandAmplitudeDbTarget[7]    = (int) ((inBuf[7] * 20) + 1);
//			bandAmplitudeDbTarget[8]    = (int) ((inBuf[8] * 20) + 1);
//			bandAmplitudeDbTarget[9]    = (int) ((inBuf[9] * 20) + 1);
//		}                                        
//		else if(len == 2048){                    
//			bandAmplitudeDbTarget[0]    = (int) ((inBuf[2]    * 20) + 1);
//			bandAmplitudeDbTarget[1]    = (int) ((inBuf[4]    * 20) + 1);
//			bandAmplitudeDbTarget[2]    = (int) ((inBuf[8]    * 20) + 1);
//			bandAmplitudeDbTarget[3]    = (int) ((inBuf[16]   * 20) + 1);
//			bandAmplitudeDbTarget[4]    = (int) ((inBuf[32]   * 20) + 1);
//			bandAmplitudeDbTarget[5]    = (int) ((inBuf[64]   * 20) + 1);
//			bandAmplitudeDbTarget[6]    = (int) ((inBuf[128]  * 20) + 1);
//			bandAmplitudeDbTarget[7]    = (int) ((inBuf[256]  * 20) + 1);
//			bandAmplitudeDbTarget[8]    = (int) ((inBuf[511]  * 20) + 1);
//			bandAmplitudeDbTarget[9]    = (int) ((inBuf[1023] * 20) + 1);
//		}
//		else{//testing...
//			for (int i = 0; i < len; i++){
//				bandAmplitudeDbTarget[i] = (int) ((inBuf[i]   * 20) + 1);
//			}
//		}
//		
//		for(int i = 0; i < NUM_BANDS; i++) {
//    		bandAmplitudeTargetReached[i] = false; // on new reading, reset all
//    	}
//		//System.out.println("from setBandBuffer: " + Arrays.toString(bandAmplitudeDbTarget));
//	}
	
	private void updateMeter() {
    	repaint();
	}
	
	
	/************************************************************************************************************
	 * 
	 * Override Paint
	 *
	************************************************************************************************************/
	//class EqPanel extends JPanel {
		//private static final long serialVersionUID = 1L;

		@Override
		public void paint(final Graphics g){
			super.paint(g);
			final Graphics2D g2 = (Graphics2D)g;
			int barSize = 18;
			int redAmt = 0;
			int greenAmt = 0;
			float barMax = 20; //for testing
			float barYSpacing = 0;
			float bandXSpacing = 0;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			
		
			
			g2.setStroke(new BasicStroke(1));
			
			for(int bandNum = 0; bandNum < NUM_BANDS; bandNum++) {
				//adjust the current bars visible to reach the last reading
				if ((bandAmplitudeDbCurrent[bandNum] < bandAmplitudeDbTarget[bandNum]) && !(bandAmplitudeTargetReached[bandNum])) {
					bandAmplitudeDbCurrent[bandNum]++;
				}
				else if (bandAmplitudeDbCurrent[bandNum] > 0) {
					if (bandAmplitudeDbCurrent[bandNum] > bandAmplitudeDbTarget[bandNum]) {
						bandAmplitudeDbCurrent[bandNum]--;
					}
					else if ((bandAmplitudeDbCurrent[bandNum] == bandAmplitudeDbTarget[bandNum]) || bandAmplitudeTargetReached[bandNum]) {
						bandAmplitudeTargetReached[bandNum] = true;
						bandAmplitudeDbCurrent[bandNum]--;
					}
				}
				
				//now have the update value for the band. Draw the bars on the eq meter
				for (int barNum = 0; barNum <= bandAmplitudeDbCurrent[bandNum]; barNum++) {
					redAmt = (int) ((barNum / barMax) * 255);
					greenAmt = (int) (255* (1 -(barNum / barMax)));
					g2.setColor(new Color(redAmt, greenAmt, 0));
					//g2.setColor(Color.green);
					bandXSpacing = ((bandNum+1) * (borderXOffset - 5)) + (bandNum * barSize);
					barYSpacing = barNum * 2;
					final Line2D line = new Line2D.Float(borderXOffset + bandXSpacing, borderYOffset - barYSpacing, borderXOffset+ bandXSpacing + barSize, borderYOffset - barYSpacing);
					g2.draw(line);
				}
			}
				
		}
}
