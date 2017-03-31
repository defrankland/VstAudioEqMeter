package jvst.defVstFft;

import java.awt.Dimension;
import javax.swing.*;
import java.awt.*;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.VSTPluginGUIAdapter;
import jvst.wrapper.gui.VSTPluginGUIRunner;
import java.util.*;

public class LED10Band extends VSTPluginGUIAdapter implements ChangeListener{
	
	
	private static final long serialVersionUID = -8641024370578430211L;

	//no VST controls to declare!
	
	//Declare elements for the gui
	public JPanel guiPanel1 = new JPanel();
	public JPanel eqMeter;
		
		
	private VSTPluginAdapter pPlugin;
	public static boolean DEBUG = false;
	
	/********************************************************************************
	 * Constructor:
	 * @param r
	 * @param plug
	 * @throws Exception
	 *********************************************************************************/
	public LED10Band(VSTPluginGUIRunner r, VSTPluginAdapter plug) throws Exception {
		super(r,plug);
	    log("vstFFTGUI <init>");
	    ((defVstFft)plugin).gui=this; //tell the plug that it has a gui!
	    
	    this.setTitle("def VST FFT");
	    this.setSize(500, 500);
	    this.setResizable(false);
	    
	    this.pPlugin = plug;
	    
	    //load the gui window background & set up content pane
	    ImageIcon bgImg = new ImageIcon(getClass().getResource("resources/bg01.png"));
	    this.setContentPane(new JLabel(bgImg)); //load the background
	    //this.getContentPane().setLayout(null);
	  
	    //create the LED meter
	    eqMeter = new nBandLed("resources/display01.png", 25, 25, 10, 12, 10);
	  	this.guiPanel1.setLayout(new BoxLayout(guiPanel1, BoxLayout.X_AXIS));
	  	this.guiPanel1.setOpaque(false);
	  	this.guiPanel1.setPreferredSize(new Dimension(200,200));
	  	this.guiPanel1.setBounds(25, 100, 250, 200);

	  	//add controls to the panel and finish
	  	this.add(guiPanel1);
	  	this.add(eqMeter);
	  	this.setResizable(false);
	  	//this.pack();
	  	
	  	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  	this.setVisible(true);
	  	
	    //this is needed on the mac only, 
	    //java guis are handled there in a pretty different way than on win/linux
	    //XXX
	    if (RUNNING_MAC_X) this.show();  
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		//no controls, so no state changes
	}
	
	public void updateGuiBuf(double[] inBuf, int fftLen){
		((nBandLed) eqMeter).setBandBuffer(inBuf, fftLen);
		if(inBuf[1] > 0){
			//System.out.println("from updateGuiBuf: " + Arrays.toString(inBuf));
		}
	}
//	public void updateGuiBuf(float[] inBuf, int fftLen){
//		//System.out.println("from updateGuiBuf: " + Arrays.toString(inBuf));
//		((nBandLed) eqMeter).setBandBuffer(inBuf, fftLen);
//		
//	}
	
}
