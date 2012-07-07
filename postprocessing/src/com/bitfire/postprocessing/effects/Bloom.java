package com.bitfire.postprocessing.effects;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.BufferUtils;
import com.bitfire.postprocessing.PingPongBuffer;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.postprocessing.filters.Blur.BlurType;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.Combine.Param;
import com.bitfire.postprocessing.filters.Threshold;

public final class Bloom extends PostProcessorEffect {
	public static class Settings {
		public final String name;

		public final BlurType blurType;
		public final int blurPasses;	// simple blur
		public final float blurAmount;	// normal blur (1 pass)
		public final float bloomThreshold;

		public final float bloomIntensity;
		public final float bloomSaturation;
		public final float baseIntensity;
		public final float baseSaturation;

		public Settings( String name, BlurType blurType, int blurPasses, float blurAmount, float bloomThreshold, float baseIntensity, float baseSaturation,
				float bloomIntensity, float bloomSaturation ) {
			this.name = name;
			this.blurType = blurType;
			this.blurPasses = blurPasses;
			this.blurAmount = blurAmount;

			this.bloomThreshold = bloomThreshold;
			this.baseIntensity = baseIntensity;
			this.baseSaturation = baseSaturation;
			this.bloomIntensity = bloomIntensity;
			this.bloomSaturation = bloomSaturation;
		}

		// simple blur
		public Settings( String name, int blurPasses, float bloomThreshold, float baseIntensity, float baseSaturation, float bloomIntensity, float bloomSaturation ) {
			this( name, BlurType.Gaussian5x5b, blurPasses, 0, bloomThreshold, baseIntensity, baseSaturation, bloomIntensity, bloomSaturation );
		}

		public Settings( Settings other ) {
			this.name = other.name;
			this.blurType = other.blurType;
			this.blurPasses = other.blurPasses;
			this.blurAmount = other.blurAmount;

			this.bloomThreshold = other.bloomThreshold;
			this.baseIntensity = other.baseIntensity;
			this.baseSaturation = other.baseSaturation;
			this.bloomIntensity = other.bloomIntensity;
			this.bloomSaturation = other.bloomSaturation;
		}
	}

	public static boolean useAlphaChannelAsMask = false;

	private PingPongBuffer pingPongBuffer;

	private Blur blur;
	private Threshold threshold;
	private Combine combine;

	private Settings settings;

	private ByteBuffer prevBlendState;
	private boolean blending = false;
	private int sfactor, dfactor;

	public Bloom( int fboWidth, int fboHeight ) {
		pingPongBuffer = new PingPongBuffer( fboWidth, fboHeight, PostProcessor.getFramebufferFormat(), false );

		blur = new Blur( fboWidth, fboHeight );
		threshold = new Threshold();
		combine = new Combine();
		prevBlendState = BufferUtils.newByteBuffer(32);

		setSettings( new Settings( "default", 2, 0.277f, 1f, .85f, 1.1f, .85f ) );
	}

	@Override
	public void dispose() {
		combine.dispose();
		threshold.dispose();
		blur.dispose();
		pingPongBuffer.dispose();
	}

	public void setBaseIntesity( float intensity ) {
		combine.setParam( Param.Source1Intensity, intensity );
	}

	public void setBaseSaturation( float saturation ) {
		combine.setParam( Param.Source1Saturation, saturation );
	}

	public void setBloomIntesity( float intensity ) {
		combine.setParam( Param.Source2Intensity, intensity );
	}

	public void setBloomSaturation( float saturation ) {
		combine.setParam( Param.Source2Saturation, saturation );
	}

	public void setThreshold( float gamma ) {
		threshold.setTreshold( gamma );
	}

	public void enableBlending( int sfactor, int dfactor) {
		this.blending = true;
		this.sfactor = sfactor;
		this.dfactor = dfactor;
	}

	public void disableBlending() {
		this.blending = false;
	}

	public void setBlurType( BlurType type ) {
		blur.setType( type );
	}

	public void setSettings( Settings settings ) {
		this.settings = settings;

		// setup threshold filter
		setThreshold( settings.bloomThreshold );

		// setup combine filter
		setBaseIntesity( settings.baseIntensity );
		setBaseSaturation( settings.baseSaturation );
		setBloomIntesity( settings.bloomIntensity );
		setBloomSaturation( settings.bloomSaturation );

		// setup blur filter
		setBlurPasses( settings.blurPasses );
		setBlurAmount( settings.blurAmount );
		setBlurType( settings.blurType );
	}

	public void setBlurPasses( int passes ) {
		blur.setPasses( passes );
	}

	public void setBlurAmount( float amount ) {
		blur.setAmount( amount );
	}

	@Override
	public void render( final FrameBuffer src, final FrameBuffer dest ) {
		Texture texsrc = src.getColorBufferTexture();

		Gdx.gl20.glGetBooleanv( GL20.GL_BLEND, prevBlendState );
		boolean blendingWasEnabled = (prevBlendState.get() == 1);
		prevBlendState.clear();

		Gdx.gl.glDisable( GL20.GL_BLEND );

		pingPongBuffer.begin();
		{
			// threshold pass
			// cut bright areas of the picture and blit to smaller fbo
			threshold.setInput( texsrc ).setOutput( pingPongBuffer.getSourceBuffer() ).render();

			// blur pass
			blur.render( pingPongBuffer );
		}
		pingPongBuffer.end();

		if( blendingWasEnabled ) {
			Gdx.gl.glEnable( GL20.GL_BLEND );
		}

		if( blending ) {
			Gdx.gl.glBlendFunc( sfactor, dfactor );
		}

		// mix original scene and blurred threshold, modulate via
		// set(Base|Bloom)(Saturation|Intensity)
		combine.setOutput( dest ).setInput( texsrc, pingPongBuffer.getResultTexture() ).render();
	}

	@Override
	public void rebind() {
		blur.rebind();
		threshold.rebind();
		combine.rebind();
		pingPongBuffer.rebind();

		setSettings( settings );
	}
}