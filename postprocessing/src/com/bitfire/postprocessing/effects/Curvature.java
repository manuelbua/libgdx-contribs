package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.RadialDistortion;

public final class Curvature extends PostProcessorEffect {
	private RadialDistortion distort;

	public Curvature() {
		distort = new RadialDistortion();
	}

	@Override
	public void dispose() {
		distort.dispose();
	}

	public void setDistortion( float distortion ) {
		distort.setDistortion( distortion );
	}

	public void setZoom( float zoom ) {
		distort.setZoom( zoom );
	}

	public float getDistortion() {
		return distort.getDistortion();
	}

	public float getZoom() {
		return distort.getZoom();
	}

	@Override
	public void rebind() {
		distort.rebind();
	}

	@Override
	public void render( FrameBuffer src, FrameBuffer dest ) {
		distort.setInput( src ).setOutput( dest ).render();
	};

}
