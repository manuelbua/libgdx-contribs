package com.bitfire.postprocessing.demo;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.utils.ShaderLoader;

public class PostProcessingDemo implements ApplicationListener, InputProcessor {

	Stage ui;
	Skin skin;
	SpriteBatch batch;
	Sprite badlogic;
	OrthographicCamera camera;
	float angle;
	float width, height, halfWidth, halfHeight;
	PostProcessor postProcessor;

	@Override
	public void create() {
		ShaderLoader.BasePath = "../shaders/";
		Gdx.input.setInputProcessor( this );
		postProcessor = new PostProcessor( false, false, (Gdx.app.getType() == ApplicationType.Desktop) );
		createScene();
	}

	private void createScene() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		halfWidth = width / 2;
		halfHeight = height / 2;

		ui = new Stage();
		skin = new Skin( Gdx.files.internal( "data/uiskin.json" ) );
		camera = new OrthographicCamera( width, height );
		camera.setToOrtho( true );

		batch = new SpriteBatch();
		batch.setProjectionMatrix( camera.projection );
		batch.setTransformMatrix( camera.view );

		badlogic = ResourceFactory.newSprite( "badlogic.jpg" );
		angle = 0;

//		postProcessor.addEffect( new CrtMonitor( true, false ) );
		postProcessor.addEffect( new Bloom( (int)(halfWidth*1f), (int)(halfHeight*1f) ) );
//		postProcessor.addEffect( new Vignette( false ) );
	}

	@Override
	public void dispose() {
		ResourceFactory.dispose();
		postProcessor.dispose();
	}

	@Override
	public void render() {
//		Gdx.gl20.glClearColor( 0, 0, 0, 1 );
//		Gdx.gl20.glClear( GL20.GL_COLOR_BUFFER_BIT );

		angle += 50 * Gdx.graphics.getDeltaTime();
		if( angle > 360 ) {
			angle -= 360;
		}

		postProcessor.capture();

		Gdx.gl20.glActiveTexture( GL20.GL_TEXTURE0 );
		batch.begin();
		{
			badlogic.setPosition( halfWidth - badlogic.getWidth() / 2 + 50 * MathUtils.sin( angle * MathUtils.degreesToRadians ),
					halfHeight - badlogic.getHeight() / 2 + 50 * MathUtils.cos( angle * MathUtils.degreesToRadians ) );
			badlogic.draw( batch );
		}
		batch.end();

		postProcessor.render( null );

		ui.draw();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void resize( int width, int height ) {
	}

	@Override
	public boolean keyDown( int keycode ) {
		return false;
	}

	@Override
	public boolean keyUp( int keycode ) {
		return false;
	}

	@Override
	public boolean keyTyped( char character ) {
		return false;
	}

	@Override
	public boolean touchDown( int x, int y, int pointer, int button ) {
		return false;
	}

	@Override
	public boolean touchUp( int x, int y, int pointer, int button ) {
		return false;
	}

	@Override
	public boolean touchDragged( int x, int y, int pointer ) {
		return false;
	}

	@Override
	public boolean mouseMoved( int x, int y ) {
		return false;
	}

	@Override
	public boolean scrolled( int amount ) {
		return false;
	}

}
