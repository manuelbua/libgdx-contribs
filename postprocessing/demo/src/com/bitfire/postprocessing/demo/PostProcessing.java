package com.bitfire.postprocessing.demo;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.postprocessing.PostProcessListener;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.CrtMonitor;
import com.bitfire.postprocessing.effects.Curvature;
import com.bitfire.postprocessing.effects.Vignette;
import com.bitfire.postprocessing.effects.Zoomer;
import com.bitfire.postprocessing.filters.RadialBlur;

public final class PostProcessing implements Disposable, PostProcessListener {

	public PostProcessor postProcessor;
	public Bloom bloom;
	public Curvature curvature;
	public Zoomer zoomer;
	public CrtMonitor crt;
	public Vignette vignette;
	public boolean blending;

	public PostProcessing() {
		boolean isDesktop = (Gdx.app.getType() == ApplicationType.Desktop);
		blending = false;

		postProcessor = new PostProcessor( false, false, isDesktop );
		postProcessor.setListener( this );

		bloom = new Bloom( (int)(Gdx.graphics.getWidth() * 0.25f), (int)(Gdx.graphics.getHeight() * 0.25f) );
		curvature = new Curvature();
		zoomer = new Zoomer( isDesktop ? RadialBlur.Quality.VeryHigh : RadialBlur.Quality.Low );
		crt = new CrtMonitor( false, false );
		vignette = new Vignette( false );

		postProcessor.setEnabled( true );
		postProcessor.addEffect( curvature );
		postProcessor.addEffect( zoomer );
		postProcessor.addEffect( vignette );
		postProcessor.addEffect( crt );
		postProcessor.addEffect( bloom );

		// specify a negative value to blur inside-to-outside,
		// so that to avoid artifacts at borders
		zoomer.setBlurStrength( -0.1f );
		zoomer.setOrigin( Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2 );
		curvature.setZoom( 1f );
		crt.setOffset( 0.002f );
		vignette.setIntensity( 0.85f );

		crt.setEnabled( false );
		vignette.setEnabled( false );
		curvature.setEnabled( false );
		zoomer.setEnabled( false );
	}

	@Override
	public void dispose() {
		postProcessor.dispose();
	}

	public boolean begin() {
		return postProcessor.capture();
	}

	public void end() {
		postProcessor.render( null );
	}

	public boolean isEnabled() {
		return postProcessor.isEnabled();
	}

	public boolean isReady() {
		return postProcessor.isReady();
	}

	public void setEnabled( boolean enabled ) {
		postProcessor.setEnabled( enabled );
	}

	@Override
	public void beforeRenderToScreen() {
		if( blending ) {
			Gdx.gl20.glEnable( GL20.GL_BLEND );
			Gdx.gl20.glBlendFunc( GL20.GL_SRC_COLOR, GL20.GL_SRC_ALPHA );
		}
	}
}
