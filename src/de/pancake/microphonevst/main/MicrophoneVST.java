package de.pancake.microphonevst.main;

import de.pancake.microphonevst.VstController;
import de.pancake.microphonevst.VstProcessor;

/**
 * Microphone VST Main Class
 * @author Pancake
 */
public class MicrophoneVST {
	
	public static void main(String[] args) throws Exception { 
		VstProcessor processor = new VstProcessor("FlexASIO", 96000);
		new VstController(processor);
		processor.start();
	}

}
