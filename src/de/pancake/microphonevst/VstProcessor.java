package de.pancake.microphonevst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

/**
 * Processes audio received from asio in and sends it to an asio out
 * @author Pancake
 */
public class VstProcessor implements AsioDriverListener {
	
	/**
	 * Sample Rate of the processor
	 */
	private float sampleRate;
	
	/**
	 * Buffer Size of the processor
	 */
	private int bufferSize;
	
	/**
	 * Asio driver
	 */
	private AsioDriver asio;
	
	/**
	 * Asio input and output channels
	 */
	private AsioChannel input, output;
	
	/**
	 * Audio data array
	 */
	private float[][] data;
	
	/**
	 * List of VSTs before starting
	 */
	volatile List<Vst> vsts = new ArrayList<>();
	
	/**
	 * Initializes the processor
	 * @param deviceName Device Name
	 * @param sampleRate Sample rate
	 * @throws Exception VST Controller Exception
	 */
	public VstProcessor(String deviceName, float sampleRate) throws Exception {
		this.asio = AsioDriver.getDriver(deviceName);
		this.asio.setSampleRate(this.sampleRate = sampleRate);
		this.asio.addAsioDriverListener(this);
		this.data = new float[2][this.bufferSize = this.asio.getBufferPreferredSize()];
		this.asio.createBuffers(new HashSet<>(Set.of(this.input = this.asio.getChannelInput(0), this.output = this.asio.getChannelOutput(0))));
	}
	
	/**
	 * Launches the processor after initializing the vsts
	 * @throws Exception Exception
	 */
	public void start() throws Exception {
		this.asio.start();

		while (AsioDriver.isDriverLoaded())
			Thread.yield();
	}
	
	/**
	 * Closes the processor
	 */
	public void close() {
		this.asio.shutdownAndUnloadDriver();
	}
	
	@Override
	public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) {
		this.input.read(this.data[0]);
		for (Vst vst : this.vsts)
			if (vst.isEnabled()) vst.process(this.data);
		this.output.write(this.data[0]);
	}
	
	/**
	 * Returns the Sample Rate
	 * @return Sample Rate
	 */
	public float getSampleRate() {
		return this.sampleRate;
	}
	
	/**
	 * Returns the Buffer Size
	 * @return
	 */
	public int getBufferSize() {
		return this.bufferSize;
	}
	
	@Override public void resyncRequest() {}
	@Override public void resetRequest() {}
	@Override public void sampleRateDidChange(double sampleRate) {}
	@Override public void latenciesChanged(int inputLatency, int outputLatency) {}
	@Override public void bufferSizeChanged(int newBufferSize) {}

}
