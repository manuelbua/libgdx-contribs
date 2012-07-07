package com.bitfire.postprocessing.demo;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {

	public static void main( String[] argv ) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "PostProcessing Demo";
		config.width = 800;
		config.height = 480;
		config.samples = 0;
		config.depth = 16;
		config.vSyncEnabled = true;
		config.useCPUSynch = false;
		config.useGL20 = true;
		config.fullscreen = false;

		new LwjglApplication( new PostProcessingDemo(), config );
	}

}
