package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Vignetting;

public final class Vignette extends PostProcessorEffect {
	private Vignetting vignetting;
	public boolean controlSaturation;

	public Vignette( boolean controlSaturation ) {
		this.controlSaturation = controlSaturation;
		vignetting = new Vignetting( controlSaturation );
	}

	@Override
	public void dispose() {
		vignetting.dispose();
	}

	public void setIntensity( float intensity ) {
		vignetting.setIntensity( intensity );
	}

	public void setCoords( float x, float y ) {
		vignetting.setCoords( x, y );
	}

	public void setX( float x ) {
		vignetting.setX( x );
	}

	public void setY( float y ) {
		vignetting.setY( y );
	}

	public void setSaturation( float saturation ) {
		vignetting.setSaturation( saturation );
	}

	public void setSaturationMul( float saturationMul ) {
		vignetting.setSaturationMul( saturationMul );
	}

	public void setLut( Texture texture ) {
		vignetting.setLut( texture );
	}

	public void setLutIntensity( float value ) {
		vignetting.setLutIntensity( value );
	}

	public void setLutIndex( int value ) {
		vignetting.setLutIndex( value );
	}

	public void setCenter( float x, float y ) {
		vignetting.setCenter( x, y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
	}

	@Override
	public void rebind() {
		vignetting.rebind();
	}

	@Override
	public void render( FrameBuffer src, FrameBuffer dest ) {
		vignetting.setInput( src ).setOutput( dest ).render();
	};
}
