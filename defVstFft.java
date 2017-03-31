package jvst.defVstFft;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class defVstFft extends VSTPluginAdapter {
	public final static int FFT_LENGTH = 1024; //either 1024 or 2048
	
	//TEST!!!!
	float[] tstBuf = new float[10]; 
	
	public float[] fftBuffer = new float[FFT_LENGTH * 2]; //double length to set up double-buffering system
	double fftMagnitude[] = new double[FFT_LENGTH/2];
	
	public int startIdx = 0;
	public int endIdx = 511; 
	    
	public LED10Band gui; //reference to gui, initialized in vstFFTGUI's main()
	
	public FFT_Started fftStartSignal_bufA;
	public FFT_Finished fftDoneSignal_bufA;
	public FFT_Started fftStartSignal_bufB;
	public FFT_Finished fftDoneSignal_bufB;
	
	List<FFT_Started> listeners = new ArrayList<FFT_Started>();
	
	private float sampleRate = 44100;
			
	//NOTE: "Program" refers to the preset that is applied to the effect.
	//Current Program is set to 0 which is the only program (or preset) in this plugin.  
	private int currentProgram = 0;
	
 
	//Constructor
	public defVstFft(long wrapper) {
		super(wrapper);
		log("Construktor vstFFT() START!");
		currentProgram = 0;

		//communicate with the host
		this.setNumInputs(1);// mono input
		this.setNumOutputs(1);// mono output
		//this.hasVu(false); //deprecated as of vst2.4
		this.canProcessReplacing(true);//mandatory for vst 2.4!
		this.setUniqueID(9876543);//random unique number registered at steinberg (4 byte)

		this.canMono(true); 	
		
		

		//add listeners
		fftStartSignal_bufA = new FFT_Started();
		fftDoneSignal_bufA = new FFT_Finished();
		fftStartSignal_bufB = new FFT_Started();
		fftDoneSignal_bufB = new FFT_Finished();
		//this.addListener(fftStartSignal);
		//this.addListener(fftDoneSignal);
				
		//start thread for FFT calculation
		Runnable fftThreadTask = new ProcFFT();
		Thread fftThread = new Thread(fftThreadTask);
		fftThread.start();
		
		//start thread for GUI
		Runnable guiThreadTask = new ProcGUI();
		Thread guiThread = new Thread(guiThreadTask);
		guiThread.start();
		
		log("Construktor vstFFT() INVOKED!");
	}
	
	public static void main(String[] args) throws Throwable {
		//initialize the gui
		gui = new LED10Band(null, null);
	}
	
	
	
	@Override
	public float getParameter(int index) {return 0.0f;}
	@Override
	public void setParameter(int index, float value) {}

	// Generate / Process the sound
	@Override
	public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
		float[] inBuffer = inputs[0];
		float[] outBuffer = outputs[0];
		
		//Read a new sample into the FFT buffer if the last one has been processed
		if(fftStartSignal_bufA.isDone && fftDoneSignal_bufA.isDone){
			//write the sample into buffer A
			for (int i = 0; i < sampleFrames; i++) {
				outBuffer[i] = inBuffer[i];
				fftBuffer[i] = inBuffer[i];
			}
			fftStartSignal_bufA.isDone = false;
		}
		else if(fftStartSignal_bufB.isDone && fftDoneSignal_bufB.isDone){
			//write buffer B
			for (int i = 0; i < sampleFrames; i++) {
				outBuffer[i] = inBuffer[i];
				fftBuffer[i + (FFT_LENGTH-1)] = inBuffer[i];
			}
			fftStartSignal_bufB.isDone = false;
		}
		else{
			//just copy the input to the output
			for (int i = 0; i < sampleFrames; i++) {
				outBuffer[i] = inBuffer[i];
				//fftBuffer[i] = inBuffer[i];
			}
		}
		//TEST!!
//		for(int i = 0; i < 10; i++) {
//			tstBuf[i] = (float) Math.random(); //simulate an audio reading
//    	}
//		try{
//			//System.out.println("from ProcessReplacing: " + Arrays.toString(tstBuf));
//			gui.updateGuiBuf(fftBuffer, 10);
//		} catch (Exception e) {
//			System.out.println("Failed to write GUI" + e);
//		}
		
	} 
	
	@Override
	public int canDo(String feature) {
		// the host asks us here what we are able to do
		int ret = CANDO_NO;
		if (feature.equals(CANDO_PLUG_1_IN_1_OUT)) ret = CANDO_YES;
		if (feature.equals(CANDO_PLUG_PLUG_AS_CHANNEL_INSERT)) ret = CANDO_YES;
		if (feature.equals(CANDO_PLUG_PLUG_AS_SEND)) ret = CANDO_YES;

		log("canDo: " + feature + " = " + ret);
		return ret;
	}

	@Override
	public String getParameterDisplay(int index) {return "";}
	@Override
	public String getParameterLabel(int index) {return "";}
	@Override
	public String getParameterName(int index) {return "";}
  	//Other methods that need to be re-defined from the base class
  	@Override
	public String getProductString() {return "vstFFT";}
	@Override
	public String getEffectName() {return "vstFFT";}
	@Override
	public int getProgram() {return currentProgram;}
	@Override
	public String getProgramName() {return "program " + currentProgram;}
	@Override
	public String getProgramNameIndexed(int category, int index) {return "program: cat: " + category + ", " + index;}
	@Override
	public String getVendorString() { return "jvstwrapper"; }
	@Override
	public int getNumParams() {return 0;}
	@Override
	public int getNumPrograms() {return 1;} //return the number of preset programs (or presets) the plugin supports
	@Override
	public boolean setBypass(boolean value) {return false;}//do not support soft bypass!
  	@Override
	public void setSampleRate(float sampleRate) { this.sampleRate = sampleRate;}
  	@Override
	public void setProgram(int index) {}
	@Override
	public void setProgramName(String name) {}
	
	@Override
	public int getPlugCategory() {
		  log("getPlugCategory");
		  return PLUG_CATEG_EFFECT;
	}
	
	@Override
	public boolean string2Parameter(int index, String value) {
		  try {
		    if (value != null) this.setParameter(index, Float.parseFloat(value));
		    return true;
		  }
		  catch(Exception e) {   //ignore
		    return false;
		  }
	}
	
	public void addListener(FFT_Started toAdd) {
        listeners.add(toAdd);
    }
	
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * class ProcGUI - the task for the gui thread
	 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	class ProcGUI implements Runnable{
		@Override 
		public void run(){
			while(true){
				try{
					if (gui != null){ //Don't run before gui initialized
						if(!fftDoneSignal_bufA.isDone){
							//System.out.println("from fftDoneSignal_bufA: " + Arrays.toString(fftMagnitude));
							gui.updateGuiBuf(fftMagnitude, FFT_LENGTH);
							
							//reset the fft done flag -- can now process signal again in this buffer
							fftDoneSignal_bufA.isDone = true; 
							
						}
						else if (!fftDoneSignal_bufB.isDone){
							//System.out.println("from fftDoneSignal_bufB: " + Arrays.toString(fftMagnitude));
							gui.updateGuiBuf(fftMagnitude, FFT_LENGTH);

							//reset the fft done flag -- can now process signal again in this buffer
							fftDoneSignal_bufB.isDone = true; 
							
						}

					}
				}catch (Exception e){
					System.out.println("Exception in ProcGUI" + e);
				}
				
				try{
					Thread.sleep(1); //only 1ms but required!!
				} catch(Exception e){
					System.out.println(e);
				}
			}
		}
	}
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * class ProcFFT - the task for the FFT calculation
	 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	class ProcFFT implements Runnable{ 
		
		@Override
		public void run(){
			while(true){
				//wait forever until the audio in buffer changes
				//System.out.println("attempting fft calc....");
				try{
					//Check if a new sample is available
					if(!fftStartSignal_bufA.isDone){
						//Apply the FFT to the incoming signal
						try{
							//System.out.println("Entering calculateFFT (0)...");
							this.calculateFFT(0);
						} catch (Exception e) {
							System.out.println("Failed to calculate FFT (0)");
						}
						//reset the FFT start flag -- buffer processed, can now read new samples into bufA
						fftStartSignal_bufA.isDone = true; 
						//set the FFT done signal -- flag so that the ProcGui thread can send to the GUI & no new samples will be written to the fftBuffer
						fftDoneSignal_bufA.isDone = false;
					}
					if(!fftStartSignal_bufB.isDone){
						//Apply the FFT to the incoming signal
						try {
						//System.out.println("Entering calculateFFT (FFT_LENGTH)...");	
						this.calculateFFT(FFT_LENGTH);
						} catch (Exception e) {
							System.out.println("Failed to calculate FFT (FFT_LENGTH - 1)");
						}
						//reset the FFT start flag -- buffer processed, can now read new samples into bufB
						fftStartSignal_bufB.isDone = true; 
						//set the FFT done signal -- flag so that the ProcGui thread can send to the GUI & no new samples will be written to the fftBuffer
						fftDoneSignal_bufB.isDone = false;
					}
				} catch (Exception e){
					System.out.println("Exception in ProcFFT");
				}
				try{
					Thread.sleep(1); //only 1ms but required!!
				} catch(Exception e){
					System.out.println(e);
				}
			}
//				
		}
		/******************************************
		 * calculateFFT() - apply FFT to a buffer
		 ******************************************/
		
		public void calculateFFT(int startIdx){
			int NumBits = 0;
			int i = startIdx; 
			int j, k, n = 0;
			int BlockSize, BlockEnd = 0;
			double angle_numerator = 2.0f * Math.PI;
			double tr, ti = 0;
			double Real[] = new double[FFT_LENGTH];
			double Imaginary[] = new double[FFT_LENGTH];
			
			//System.out.println("Check if power of 2...");
			//Return if not power of two
			if(!isPowOfTwo(FFT_LENGTH)){
				return;
			}
			
//			int ii = 0;
//			while(NumBits == 0 && ii < 32){
//				if((FFT_LENGTH & (1 << ii)) == 1){
//					NumBits = ii;
//				}
//				ii++;
//			}
			NumBits = FFT_LENGTH;

			
			for(i=0;i < FFT_LENGTH; i++){
				j = ReverseBits(i, NumBits);
				Real[j] = fftBuffer[i];
				Imaginary[j] = 0.0f; //should look at the input, but it's 0 anyway
			}
			//System.out.println("in Buffer: " + Arrays.toString(fftBuffer));
			
			BlockEnd = 1;
			for(BlockSize=2; BlockSize <= FFT_LENGTH; BlockSize <<= 1){
				double delta_angle = angle_numerator / BlockSize;
				double sm2 = Math.sin(-2 * delta_angle);
				double sm1 = Math.sin(-delta_angle);
				double cm2 = Math.cos(-2 * delta_angle);
				double cm1 = Math.cos(-delta_angle);
				double w = 2 * cm1;
				double[] ar = new double[3];
				double[] ai = new double[3];
				
				for(i=0; i < FFT_LENGTH; i+= BlockSize){
					ar[2] = cm2;
					ar[1] = cm1;
					ai[2] = sm2;
					ai[1] = sm1;
					
					for(j=i, n=0; n < BlockEnd; j++, n++){
						ar[0] = w*ar[1] - ar[2];
						ar[2] = ar[1];
						ar[1] = ar[0];

						ai[0] = w*ai[1] - ai[2];
						ai[2] = ai[1];
						ai[1] = ai[0];

						k = j + BlockEnd;
						tr = ar[0]*Real[k] - ai[0]*Imaginary[k];
						ti = ar[0]*Imaginary[k] + ai[0]*Real[k];
						
						
						Real[k] = Real[j] - tr;
						Imaginary[k] = Imaginary[j] - ti;

						Real[j] += tr;
						Imaginary[j] += ti;

					}
				}
				BlockEnd = BlockSize;
			}

			//Get the magnitude
			for(int idx=0;idx < FFT_LENGTH/2; idx++){//Use FFT_LEN/2 since the data is mirrored within the array.
				fftMagnitude[idx] = Math.sqrt(Real[idx]*Real[idx] + Imaginary[idx]*Imaginary[idx] );
			}
			
			fftMagnitude = normalizeBuffer(fftMagnitude, FFT_LENGTH/2);	
		}

		public boolean isPowOfTwo(int sampleSize){
			if( sampleSize < 2 ) {
				return false; 
				}
//			if((sampleSize & (sampleSize - 1)) == 0) {
//				return false;
//				}
			
			return true;
		}
		
		public int ReverseBits(int nIdx, int numBits){
			int i, rev;
			
			for(i=rev=0; i < numBits; i++){
				rev = (rev << 1) | (nIdx & 1);
				nIdx >>=1;
			}
		
			return rev;
		}
		
		public double[] normalizeBuffer(double[] inBuffer, int bufSize){
			double fMax = 0;
			//System.out.println("normalizeBuffer inBuffer: " + Arrays.toString(inBuffer));
			
			for(int j = 0; j < bufSize; j++){
				if(Math.abs(inBuffer[j]) > fMax){
					fMax = Math.abs(inBuffer[j]);
				}
			}
			
			if(fMax > 0){
				for(int j=0; j < bufSize; j++){
					
						inBuffer[j] = inBuffer[j]/fMax;
					}
			}
			return inBuffer;
		}
	}
	
	
	/********************************************************************
	 * Class FFT_Started - triggered when FFT event is started
	 * 	
	 ********************************************************************/
	class FFT_Started{
		public boolean isDone = true;
		
		public void setDoneFlag(){
			isDone = false;
		}
		public void resetDoneFlag(){
			isDone = true;
		}
	}
	
	/********************************************************************
	 * Class FFT_Finished - triggered when FFT process event is complete
	 * 	initializes to isDone = false
	 *  isDone must manually be reset
	 ********************************************************************/
	class FFT_Finished{
		public boolean isDone = true;
		
		public void resetDoneFlag(){
			isDone = true;
		}
	}
	
}







