package com.bitfire.postprocessing.demo;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {

	public static void main( String[] argv ) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "PostProcessing Demo";
		config.width = 1280;
		config.height = 800;
		config.samples = 0;
		config.depth = 0;
		config.vSyncEnabled = true;
		config.useCPUSynch = false;
		config.useGL20 = true;
		config.fullscreen = false;

		new LwjglApplication( new PostProcessingDemo(), config );

		// move the window to the right screen
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice primary = env.getDefaultScreenDevice();
		GraphicsDevice[] devices = env.getScreenDevices();
		GraphicsDevice target = null;

		// search for the first target screen
		for( int i = 0; i < devices.length; i++ ) {
			boolean isPrimary = (primary == devices[i]);
			if( !isPrimary ) {
				target = devices[i];
				break;
			}
		}

		if( target != null ) {
			DisplayMode pmode = primary.getDisplayMode();
			DisplayMode tmode = target.getDisplayMode();

			Display.setLocation( pmode.getWidth() + (tmode.getWidth() - config.width) / 2, (tmode.getHeight() - config.height) / 2 );
		}
	}

}
