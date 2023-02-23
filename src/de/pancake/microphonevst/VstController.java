package de.pancake.microphonevst;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.synthbot.audioplugin.vst.JVstLoadException;

/**
 * Vst Controller System Tray
 * @author Pancake
 */
public class VstController {
	
	public static final File VST_DIR = new File("C:\\Program Files\\VstPlugins");
	
	/**
	 * Vst Processor
	 */
	private VstProcessor processor;
	
	/**
	 * Various UI Menues
	 */
	private Menu unregisterItem, enableItem, disableItem, configureItem;
	
	/**
	 * Popup menu
	 */
	private PopupMenu popup;
	
	/**
	 * Prepares the system tray
	 * @param processor Vst Processor
	 * @throws Exception System Tray Exception
	 */
	public VstController(VstProcessor processor) throws Exception {
		this.processor = processor;

		// Prepare elements for popup
		this.popup = new PopupMenu();
		var registerItem = (Menu) popup.add(new Menu("Register VST"));
		this.unregisterItem = (Menu) popup.add(new Menu("Unregister VST"));
		this.popup.add(new MenuItem("Clear VSTs")).addActionListener(e -> this.clearVsts());
		this.popup.addSeparator();
		this.enableItem = (Menu) popup.add(new Menu("Enable VST"));
		this.disableItem = (Menu) popup.add(new Menu("Disable VST"));
		this.configureItem = (Menu) popup.add(new Menu("Configure VST"));
		this.popup.addSeparator();
		this.popup.add(new MenuItem("Save")).addActionListener(e -> this.save());
		this.popup.add(new MenuItem("Quit")).addActionListener(e -> this.quit());
		
		// Prepare all registerable vsts
		for (File vstFile : VST_DIR.listFiles()) {
			var vstItem = new MenuItem(vstFile.getName());
			vstItem.addActionListener(e -> this.registerVst(vstFile));
			registerItem.add(vstItem);
		}
		
		// Prepare other submenus
		this.updateMenus();
		
		SystemTray.getSystemTray().add(new TrayIcon(ImageIO.read(VstController.class.getResourceAsStream("/vst-icon.gif")), "MicrophoneVST Settings", popup));
		this.load();
	}
	
	/**
	 * Updates the VST Menues
	 * @param enableItem Enable Item
	 * @param disableItem Disable Item
	 * @param configureItem Configure Item
	 */
	private void updateMenus() {
		this.unregisterItem.removeAll();
		this.enableItem.removeAll();
		this.disableItem.removeAll();
		this.configureItem.removeAll();
		
		for (Vst vst : this.processor.vsts) {
			this.unregisterItem.add(new MenuItem(vst.getName())).addActionListener(e -> this.unregisterVst(vst));
			this.enableItem.add(new MenuItem(vst.getName())).addActionListener(e -> this.enableVst(vst));
			this.disableItem.add(new MenuItem(vst.getName())).addActionListener(e -> this.disableVst(vst));
			this.configureItem.add(new MenuItem(vst.getName())).addActionListener(e -> this.configureVst(vst));
		}
	}

	
	/**
	 * Saves MicrophoneVST Data
	 */
	private void save() {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream("microphonevst.conf"));
			out.writeInt(this.processor.vsts.size());
			for (Vst vst : this.processor.vsts) {
				byte[] data = vst.save();
				out.writeUTF(vst.getFile().getAbsolutePath());
				out.writeBoolean(vst.isEnabled());
				out.writeInt(data.length);
				out.write(data);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads MicrophoneVST Data
	 */
	private void load() {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream("microphonevst.conf"));
			int count = in.readInt();
			for (int i = 0; i < count; i++) {			
				Vst vst = new Vst(new File(in.readUTF()));
				vst.initialize(this.processor.getSampleRate(), this.processor.getBufferSize());
				if (in.readBoolean())
					vst.enable();
				
				byte[] data = new byte[in.readInt()];
				in.read(data);
				vst.load(data);
				
				this.processor.vsts.add(vst);
				
			}
			in.close();
			this.updateMenus();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JVstLoadException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers a VST
	 * @param vstFile Vst file
	 */
	private void registerVst(File vstFile) {
		try {
			Vst vst = new Vst(vstFile);
			vst.initialize(this.processor.getSampleRate(), this.processor.getBufferSize());
			this.processor.vsts.add(vst);
			this.updateMenus();
		} catch (FileNotFoundException | JVstLoadException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unregisters a VST
	 * @param vst Vst
	 */
	private void unregisterVst(Vst vst) {
		vst.close();
		this.processor.vsts.remove(vst);
		this.updateMenus();
	}
	
	/**
	 * Clears all registered VSTs
	 */
	private void clearVsts() {
		for (Vst vst : this.processor.vsts)
			vst.close();
		this.processor.vsts.clear();
		this.updateMenus();
	}
	
	/**
	 * Quits MicrophoneVST
	 */
	private void quit() {
		this.processor.close();
		System.exit(0);
	}
	
	/**
	 * Enables a vst
	 * @param vst Vst
	 */
	private void enableVst(Vst vst) {
		vst.enable();
	}
	
	/**
	 * Disables a vst
	 * @param vst Vst
	 */
	private void disableVst(Vst vst) {
		vst.disable();
	}
	
	/**
	 * Configures a vst
	 * @param vst Vst
	 */
	private void configureVst(Vst vst) {
		vst.open();
	}

}
