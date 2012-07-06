package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PingPongBuffer;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.postprocessing.filters.Blur.BlurType;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.CrtScreen;

public final class CrtMonitor extends PostProcessorEffect {
	private PingPongBuffer pingPongBuffer = null;
	private FrameBuffer buffer = null;
	private CrtScreen crt;
	private Color tmptint = new Color();
	private Blur blur;
	private Combine combine;
	private boolean doblur;

	public CrtMonitor( boolean barrelDistortion, boolean performBlur ) {
		// the effect is designed to work on the whole screen area, no small/mid size tricks!
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		doblur = performBlur;

		if( doblur ) {
			pingPongBuffer = new PingPongBuffer( w, h, PostProcessor.getFramebufferFormat(), false );
			blur = new Blur( w, h );
			blur.setPasses( 1 );
			blur.setAmount( 1f );
			// blur.setType( BlurType.Gaussian3x3b ); // high defocus
			blur.setType( BlurType.Gaussian3x3 );	// modern machines defocus
		} else {
			buffer = new FrameBuffer( PostProcessor.getFramebufferFormat(), w, h, false );
		}

		combine = new Combine();
		combine.setParam( Combine.Param.Source1Intensity, barrelDistortion ? 0f : 0.15f );
		combine.setParam( Combine.Param.Source2Intensity, barrelDistortion ? 1.2f : 1.1f );
		combine.setParam( Combine.Param.Source1Saturation, 1f );
		combine.setParam( Combine.Param.Source2Saturation, 0.8f );

		crt = new CrtScreen( barrelDistortion );
	}

	@Override
	public void dispose() {
		crt.dispose();
		combine.dispose();
		if( doblur ) {
			blur.dispose();
		}

		if( buffer != null ) {
			buffer.dispose();
		}

		if( pingPongBuffer != null ) {
			pingPongBuffer.dispose();
		}
	}

	public void setTime( float time ) {
		crt.setTime( time );
	}

	public void setOffset( float offset ) {
		crt.setOffset( offset );
	}

	public void setTint( Color tint ) {
		crt.setTint( tint );
	}

	public void setTint( float r, float g, float b ) {
		tmptint.set( r, g, b, 1f );
		crt.setTint( tmptint );
	}

	public void setDistortion( float distortion ) {
		crt.setDistortion( distortion );
	}

	public void setZoom( float zoom ) {
		crt.setZoom( zoom );
	}

	@Override
	public void rebind() {
		crt.rebind();
	}

	@Override
	public void render( FrameBuffer src, FrameBuffer dest ) {
		// the original scene
		Texture in = src.getColorBufferTexture();

		Gdx.gl.glDisable( GL10.GL_BLEND );
		Gdx.gl.glDisable( GL10.GL_DEPTH_TEST );
		Gdx.gl.glDepthMask( false );

		Texture out = null;

		if( doblur ) {

			pingPongBuffer.begin();
			{
				// crt pass
				crt.setInput( in ).setOutput( pingPongBuffer.getSourceBuffer() ).render();

				// blur pass
				blur.render( pingPongBuffer );
			}
			pingPongBuffer.end();

			out = pingPongBuffer.getResultTexture();
		} else {
			// crt pass
			crt.setInput( in ).setOutput( buffer ).render();

			out = buffer.getColorBufferTexture();
		}

		// do combine pass
		combine.setOutput( dest ).setInput( in, out ).render();
	};
}
