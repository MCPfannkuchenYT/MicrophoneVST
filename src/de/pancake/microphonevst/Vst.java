package de.pancake.microphonevst;

import java.io.File;
import java.io.FileNotFoundException;

import com.synthbot.audioplugin.vst.JVstLoadException;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**
 * Vst wrapper class
 * @author Pancake
 */
public class Vst {
	
	/**
	 * Vst file
	 */
	private File vstFile;
	
	/**
	 * Vst instance
	 */
	private JVstHost2 vst;
	
	/**
	 * Vst status
	 */
	private boolean isEnabled;
	
	/**
	 * Constructs the vst without initializing it
	 * @param vstFile Vst File
	 */
	public Vst(File vstFile) {
		this.vstFile = vstFile;
	}
	
	/**
	 * Initializes the vst
	 * @param sampleRate Sample rate
	 * @param bufferSize Buffer size
	 * @throws FileNotFoundException VST File not found
	 * @throws JVstLoadException VST Error
	 */
	public void initialize(float sampleRate, int bufferSize) throws FileNotFoundException, JVstLoadException {
		this.vst = JVstHost2.newInstance(this.vstFile, sampleRate, bufferSize);
		this.vst.turnOff();
	}
	
	/**
	 * Saves the vst settings into a byte array
	 * @return VST Settings
	 */
	public byte[] save() {
		return this.vst.getProgramChunk();
	}
	
	/**
	 * Loads the vst settings from a byte array
	 * @param VST Settings
	 */
	public void load(byte[] data) {
		this.vst.setProgramChunk(data);
	}
	
	/**
	 * Processes a chunk of audio data
	 * @param data Audio data
	 */
	void process(float[][] data) {
		if (this.vst.canReplacing())
			this.vst.processReplacing(data, data, data[0].length);
		else
			this.vst.process(data, data, data[0].length);
	}

	/**
	 * Enables the VST
	 */
	public void enable() {
		this.isEnabled = true;
		this.vst.turnOn();
	}
	
	/**
	 * Disables the VST
	 */
	public void disable() {
		this.isEnabled = false;
		this.vst.turnOff();
	}
	
	/**
	 * Opens the VST Editor
	 */
	public void open() {
		this.vst.openEditor(this.vst.getEffectName());
	}
	
	/**
	 * Closes the VST Editor
	 */
	public void close() {
		this.vst.turnOffAndUnloadPlugin();
	}

	/**
	 * Returns the vst name
	 * @return Vst name
	 */
	public String getName() {
		return this.vst.getEffectName();
	}

	/**
	 * Returns the vst file name
	 * @return Vst file name
	 */
	public String getFileName() {
		return this.vstFile.getName();
	}

	/**
	 * Returns the vst file
	 * @return Vst file
	 */
	public File getFile() {
		return this.vstFile;
	}
	
	/**
	 * Is vst enabled
	 * @return Vst
	 */
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
}