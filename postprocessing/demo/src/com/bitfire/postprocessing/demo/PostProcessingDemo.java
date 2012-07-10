/*******************************************************************************
 * Copyright 2012 bmanuel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.bitfire.postprocessing.demo;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.bitfire.utils.ShaderLoader;

public class PostProcessingDemo implements ApplicationListener, InputProcessor {
	private static final boolean UsePanelAnimator = true;
	private static final boolean UseRightScreen = true;

	SpriteBatch batch;
	Sprite badlogic;
	OrthographicCamera camera;
	float angle;
	float width, height, halfWidth, halfHeight;
	long startMs;
	Vector2 circleOffset = new Vector2();
	PostProcessing post;
	InputMultiplexer plex;

	UI ui;

	public static void main( String[] argv ) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "libgdx's post-processing demo";
		config.width = 1280;
		config.height = 720;
		config.samples = 0;
		config.depth = 0;
		config.vSyncEnabled = true;

		// Note: this is usually set to false, but i'm noticing a 3fps improvement over
		// a cpuSync=false setting. Also, in case of grabbing the window via ffmpeg/qx11grab
		// to make a video, artifacts may appear at the top such as flashing white bars: if
		// this is the case, using cpuSync=true may solve this.
		//
		// ffmpeg -xerror -loglevel info -f x11grab -framerate 25 -video_size 1280x720 -i :0.0+2240,200 -dcodec copy -vcodec
		// libx264 -preset ultrafast -y /tmp/out.mkv
		config.useCPUSynch = true;
		config.useGL20 = true;
		config.fullscreen = false;

		new LwjglApplication( new PostProcessingDemo(), config );

		if( UseRightScreen ) {
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

				Display.setLocation( pmode.getWidth() + (tmode.getWidth() - config.width) / 2,
						(tmode.getHeight() - config.height) / 2 );
			}
		}
	}

	@Override
	public void create() {
		ShaderLoader.BasePath = "../shaders/";
		plex = new InputMultiplexer();
		plex.addProcessor( this );
		Gdx.input.setInputProcessor( plex );
		post = new PostProcessing();

		createScene();
		ui = new UI( plex, post, UsePanelAnimator );
	}

	private void createScene() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		halfWidth = width / 2;
		halfHeight = height / 2;

		camera = new OrthographicCamera( width, height );
		camera.setToOrtho( true );

		batch = new SpriteBatch();
		batch.setProjectionMatrix( camera.projection );
		batch.setTransformMatrix( camera.view );

		badlogic = ResourceFactory.newSprite( "badlogic.jpg" );

		angle = 0;
		startMs = TimeUtils.millis();
	}

	@Override
	public void dispose() {
		ResourceFactory.dispose();
		post.dispose();
	}

	@Override
	public void render() {
		update();
		draw();
	}

	private void update() {
		float delta = Gdx.graphics.getDeltaTime();
		float angleSpeed = 50;
		float angleAmplitude = 50;
		float elapsedSecs = (float)(TimeUtils.millis() - startMs) / 1000;

		// circling offset
		circleOffset.x = halfWidth + (angleAmplitude * 8) * MathUtils.sin( angle * MathUtils.degreesToRadians );
		circleOffset.y = halfHeight + (angleAmplitude * 4.5f) * MathUtils.cos( angle * MathUtils.degreesToRadians );

		// angle
		angle += angleSpeed * delta;
		if( angle > 360 ) {
			angle -= 360;
		}

		// UI
		ui.update( Gdx.graphics.getDeltaTime() );

		// post-processing
		post.update( elapsedSecs );
	}

	private void draw() {

		boolean willPostProcess = post.isReady();
		boolean backgroundFirst = (willPostProcess && !ui.backgroundAffected && ui.drawBackground)
				|| (!willPostProcess && ui.drawBackground);
		post.blending = backgroundFirst && willPostProcess;

		if( backgroundFirst || !willPostProcess ) {
			Gdx.gl20.glClearColor( 0, 0, 0, 1 );
			Gdx.gl20.glClear( GL20.GL_COLOR_BUFFER_BIT );
			if( backgroundFirst ) {
				batch.begin();
				ui.background.draw( batch );
				batch.end();
			}
		}

		post.begin();
		batch.begin();
		{
			if( ui.drawBackground && ui.backgroundAffected ) {
				ui.background.draw( batch );
			}

			if( ui.drawSprite ) {
				badlogic.setPosition( circleOffset.x - badlogic.getWidth() / 2, circleOffset.y - badlogic.getHeight() / 2 );
				badlogic.draw( batch );
			}
		}
		batch.end();
		post.end();

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
		// check for quitting in keyUp, avoid multiple re-entrant keydown events
		if( keycode == Keys.Q || keycode == Keys.ESCAPE ) {
			Gdx.app.exit();
		}
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
		if( post.zoomer.isEnabled() ) {
			post.zoomer.setOrigin( x, y );
		}

		ui.mouseMoved( x, y );
		return false;
	}

	@Override
	public boolean scrolled( int amount ) {
		post.zoomAmount += amount * -1;
		post.zoomAmount = MathUtils.clamp( post.zoomAmount, 0, 15 );
		return false;
	}
}
