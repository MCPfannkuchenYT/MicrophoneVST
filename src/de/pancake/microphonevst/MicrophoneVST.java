package de.pancake.microphonevst;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import com.synthbot.audioplugin.vst.vst2.JVstHost2;
import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;

/**
 * Microphone VST Class
 * @author Pancake
 */
public class MicrophoneVST implements AsioDriverListener {
	public static void main(String[] args) throws Exception { new MicrophoneVST(); }
	
	private static final String ASIO = "FlexASIO";
	private static final int SAMPLERATE = 48000;

	private int blockSize;
	private float data[][];
	private AsioChannel input, output;
	private JVstHost2 vsts[];
	
	/**
	 * Initializes the Microphone VST
	 * @throws Exception Vst Exception
	 */
	public MicrophoneVST() throws Exception {
		// Prepare ASIO Device
		var asioDriver = AsioDriver.getDriver(ASIO);
		asioDriver.setSampleRate(SAMPLERATE);
		asioDriver.addAsioDriverListener(this);
		var activeChannels = new HashSet<AsioChannel>();
		activeChannels.add(this.input = asioDriver.getChannelInput(0));
		activeChannels.add(this.output = asioDriver.getChannelOutput(0));
		asioDriver.createBuffers(activeChannels);
		
		this.data = new float[2][this.blockSize = asioDriver.getBufferPreferredSize()];

		// Prepare VST Chain
		this.vsts = new JVstHost2[1];
		this.vsts[0] = this.newDenoiserVst();
		
		// Launch ASIO
		asioDriver.start();
		while (true)
			Thread.yield();
	}
	
	/**
	 * Applies the Vst Plugins to the Asio Input
	 */
	@Override
	public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) {
		input.read(data[0]);
		for (var vst : vsts)
			vst.processReplacing(data, data, data[0].length);
		output.write(data[0]);
	}
	
	/**
	 * Creates a trained denoising vst for my microphone.
	 * @throws Exception Vst Exception
	 */
	public JVstHost2 newDenoiserVst() throws Exception {
		JVstHost2 vst = JVstHost2.newInstance(new File("BL-Denoiser.dll"), SAMPLERATE, blockSize);
		vst.setProgramChunk(Files.readAllBytes(new File("noise.dat").toPath()));
		vst.turnOn();
		return vst;
	}
	
	@Override public void resyncRequest() {}
	@Override public void resetRequest() {}
	@Override public void sampleRateDidChange(double sampleRate) {}
	@Override public void latenciesChanged(int inputLatency, int outputLatency) {}
	@Override public void bufferSizeChanged(int newBufferSize) {}

}
