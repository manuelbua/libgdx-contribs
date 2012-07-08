package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.RadialBlur;
import com.bitfire.postprocessing.filters.Zoom;

/** Implements a zooming effect: either a radial blur filter or a zoom filter is used. */
public final class Zoomer extends PostProcessorEffect {
	private boolean doRadial = false;
	private RadialBlur radialBlur = null;
	private Zoom zoom = null;
	private float oneOnW, oneOnH;
	private float userOriginX, userOriginY;

	/** Creating a Zoomer specifying the radial blur quality will enable radial blur */
	public Zoomer( RadialBlur.Quality quality ) {
		radialBlur = new RadialBlur( quality );
		zoom = null;
		oneOnW = 1f / (float)Gdx.graphics.getWidth();
		oneOnH = 1f / (float)Gdx.graphics.getHeight();

		doRadial = true;
	}

	/** Creating a Zoomer without any parameter will use plain simple zooming */
	public Zoomer() {
		radialBlur = null;
		zoom = new Zoom();

		doRadial = false;
	}

	/** Specify the zoom origin, in screen coordinates. */
	public void setOrigin( Vector2 o ) {
		userOriginX = o.x;
		userOriginY = o.y;

		if( doRadial ) {
			radialBlur.setOrigin( o.x * oneOnW, 1f - o.y * oneOnH );
		} else {
			zoom.setOrigin( o.x * oneOnW, 1f - o.y * oneOnH );
		}
	}

	/** Specify the zoom origin, in screen coordinates. */
	public void setOrigin( float x, float y ) {
		userOriginX = x;
		userOriginY = y;

		if( doRadial ) {
			radialBlur.setOrigin( x * oneOnW, 1f - y * oneOnH );
		} else {
			zoom.setOrigin( x * oneOnW, 1f - y * oneOnH );
		}
	}

	public void setBlurStrength( float strength ) {
		if( doRadial ) {
			radialBlur.setStrength( strength );
		}
	}

	public void setZoom( float zoom ) {
		if( doRadial ) {
			radialBlur.setZoom( 1f / zoom );
		} else {
			this.zoom.setZoom( 1f / zoom );
		}
	}

	public float getZoom() {
		if( doRadial ) {
			return 1f / radialBlur.getZoom();
		} else {
			return 1f / zoom.getZoom();
		}
	}

	public float getBlurStrength() {
		if( doRadial ) {
			return radialBlur.getStrength();
		}

		return -1;
	}

	public float getOriginX() {
		return userOriginX;
	}

	public float getOriginY() {
		return userOriginY;
	}

	@Override
	public void dispose() {
		if( radialBlur != null ) {
			radialBlur.dispose();
			radialBlur = null;
		}

		if( zoom != null ) {
			zoom.dispose();
			zoom = null;
		}
	}

	@Override
	public void rebind() {
		radialBlur.rebind();
	}

	@Override
	public void render( FrameBuffer src, FrameBuffer dest ) {
		if( doRadial ) {
			radialBlur.setInput( src ).setOutput( dest ).render();
		} else {
			zoom.setInput( src ).setOutput( dest ).render();
		}
	}
}