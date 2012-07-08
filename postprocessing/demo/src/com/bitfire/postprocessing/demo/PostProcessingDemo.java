package com.bitfire.postprocessing.demo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.bitfire.utils.ShaderLoader;

public class PostProcessingDemo implements ApplicationListener, InputProcessor {
	private static final int DefaultBackground = 2;
	private static final int DefaultGradientMap = 0;
	private static final boolean DebugUI = false;

	Stage ui;
	SpriteBatch batch;
	Sprite badlogic;
	OrthographicCamera camera;
	float angle;
	float width, height, halfWidth, halfHeight;
	boolean animateBadSmile;
	float zoomAmount, zoomFactor;
	long startMs;
	boolean drawBackground, backgroundAffected, drawSprite, zoomRadialBlur;
	Sprite background;
	Texture gradientMapping;
	Vector2 circleOffset = new Vector2();
	PostProcessing post;
	InputMultiplexer plex;
	Table uiContainer;
	Label singleMessage, fps;
	Window wndSettings;

	@Override
	public void create() {
		ResourceFactory.DebugUI = DebugUI;
		ShaderLoader.BasePath = "../shaders/";
		plex = new InputMultiplexer();
		plex.addProcessor( this );
		Gdx.input.setInputProcessor( plex );
		post = new PostProcessing();

		createScene();
		createUI();
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

		animateBadSmile = !post.zoomer.isEnabled();

		badlogic = ResourceFactory.newSprite( "badlogic.jpg" );
		gradientMapping = ResourceFactory.newTexture( "gradient-mapping.png", false );
		background = ResourceFactory.newSprite( "bgnd.jpg" );
		background.setSize( width, height );

		angle = 0;
		startMs = TimeUtils.millis();

		drawBackground = true;
		drawSprite = false;
		backgroundAffected = true;
		zoomRadialBlur = true;
	}

	private void createUI() {
		Array<SelectBox> forceUpdateDefaultSelection = new Array<SelectBox>();

		ui = new Stage();
		plex.addProcessor( ui );

		// ui background
		NinePatch np = new NinePatch( ResourceFactory.newTexture( "brushed.png", false ), 0, 0, 0, 0 );
		np.setColor( new Color( 0.3f, 0.3f, 0.3f, 1f ) );
		NinePatchDrawable tback = new NinePatchDrawable( np );

		uiContainer = ResourceFactory.newTable();
		// uiContainer.setSize( width, 150 );
		uiContainer.setFillParent( true );
		uiContainer.defaults().pad( 5, 5, 5, 5 ).align( Align.top );
		// uiContainer.setY( height - uiContainer.getHeight() + 13 );
		uiContainer.left();
		// uiContainer.setBackground( tback );

		uiContainer.padTop( 20 );

		wndSettings = ResourceFactory.newWindow( "Settings" );
		wndSettings.setWidth( 520 );
		wndSettings.setHeight( 380 );
		wndSettings.setPosition( 10, height - wndSettings.getHeight() - 10 );
		wndSettings.add( uiContainer );
		wndSettings.setColor( 0.3f, 0.3f, 0.3f, 1f );
		wndSettings.addListener( new ActorListener() {
			@Override
			public void enter( ActorEvent event, float x, float y, int pointer, Actor fromActor ) {
				wndSettings.setColor( 0.3f, 0.3f, 0.3f, 1f );
			}

			@Override
			public void exit( ActorEvent event, float x, float y, int pointer, Actor toActor ) {
				wndSettings.setColor( 0.3f, 0.3f, 0.3f, 0.5f );
			}
		} );
		ui.addActor( wndSettings );

		//
		// global
		//

		// post-processing
		final CheckBox cbPost = ResourceFactory.newCheckBox( " Post-processing", post.isEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				post.setEnabled( source.isChecked() );
			}
		} );

		final SelectBox sbBackground = ResourceFactory.newSelectBox( new String[] { "None ", "Scratches ", "Mountains ", "Lake " }, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				SelectBox source = (SelectBox)event.getTarget();
				drawBackground = true;

				switch( source.getSelectionIndex() ) {
				case 0:
					drawBackground = false;
					break;
				case 1:
					background.setTexture( ResourceFactory.newTexture( "bgnd.jpg", false ) );
					break;
				case 2:
					background.setTexture( ResourceFactory.newTexture( "bgnd2.jpg", false ) );
					break;
				case 3:
					background.setTexture( ResourceFactory.newTexture( "bgnd3.jpg", false ) );
					break;
				}
			}
		} );

		// background affected by post-processing
		final CheckBox cbBackgroundAffected = ResourceFactory.newCheckBox( " Background affected by post-processing", backgroundAffected, new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				backgroundAffected = source.isChecked();
			}
		} );

		// sprite
		final CheckBox cbSprite = ResourceFactory.newCheckBox( " Show sprite", drawSprite, new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				drawSprite = source.isChecked();
			}
		} );

		sbBackground.setSelection( DefaultBackground );
		forceUpdateDefaultSelection.add( sbBackground );

		Table tGlobal = ResourceFactory.newTable();
		tGlobal.padRight( 10 );
		tGlobal.add( cbPost ).colspan( 2 ).center();
		tGlobal.row();
		tGlobal.add( ResourceFactory.newLabel( "Choose background " ) );
		tGlobal.add( sbBackground );
		tGlobal.row();
		tGlobal.add( cbBackgroundAffected ).colspan( 2 ).left();
		tGlobal.row();
		tGlobal.add( cbSprite ).colspan( 2 ).left();

		//
		// bloom
		//
		final CheckBox cbBloom = ResourceFactory.newCheckBox( " Bloom", post.bloom.isEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				post.bloom.setEnabled( source.isChecked() );
			}
		} );

		final Slider slBloomThreshold = ResourceFactory.newSlider( 0, 1, 0.01f, post.bloom.getThreshold(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.bloom.setThreshold( source.getValue() );
			}
		} );

		final Slider slBloomBaseI = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBaseIntensity(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.bloom.setBaseIntesity( source.getValue() );
			}
		} );

		final Slider slBloomBaseS = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBaseSaturation(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.bloom.setBaseSaturation( source.getValue() );
			}
		} );

		final Slider slBloomBloomI = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBloomIntensity(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.bloom.setBloomIntesity( source.getValue() );
			}
		} );

		final Slider slBloomBloomS = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBloomSaturation(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.bloom.setBloomSaturation( source.getValue() );
			}
		} );

		Table tBloom = ResourceFactory.newTable();
		tBloom.add( cbBloom ).colspan( 2 ).center();
		tBloom.row();
		tBloom.add( ResourceFactory.newLabel( "threshold " ) ).left();
		tBloom.add( slBloomThreshold );
		tBloom.row();
		tBloom.add( ResourceFactory.newLabel( "base int " ) ).left();
		tBloom.add( slBloomBaseI );
		tBloom.row();
		tBloom.add( ResourceFactory.newLabel( "base sat " ) ).left();
		tBloom.add( slBloomBaseS );
		tBloom.row();
		tBloom.add( ResourceFactory.newLabel( "bloom int " ) ).left();
		tBloom.add( slBloomBloomI );
		tBloom.row();
		tBloom.add( ResourceFactory.newLabel( "bloom sat " ) ).left();
		tBloom.add( slBloomBloomS );

		//
		// curvature
		//
		final CheckBox cbCurvature = ResourceFactory.newCheckBox( " Curvature", post.curvature.isEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				post.curvature.setEnabled( source.isChecked() );
			}
		} );

		final Slider slCurvatureDist = ResourceFactory.newSlider( 0, 2, 0.01f, post.curvature.getDistortion(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.curvature.setDistortion( source.getValue() );
			}
		} );

		final Slider slCurvatureZoom = ResourceFactory.newSlider( 0, 2, 0.01f, 2f - post.curvature.getZoom(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.curvature.setZoom( 2f - source.getValue() );
			}
		} );

		Table tCurvature = ResourceFactory.newTable();
		tCurvature.add( cbCurvature ).colspan( 2 ).center();
		tCurvature.row();
		tCurvature.add( ResourceFactory.newLabel( "Distortion " ) ).left();
		tCurvature.add( slCurvatureDist );
		tCurvature.row();
		tCurvature.add( ResourceFactory.newLabel( "Zoom " ) ).left();
		tCurvature.add( slCurvatureZoom );

		//
		// crt emulation
		//
		final CheckBox cbCrt = ResourceFactory.newCheckBox( " Old CRT emulation", post.crt.isEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				post.crt.setEnabled( source.isChecked() );
			}
		} );

		final Slider slCrtColorOffset = ResourceFactory.newSlider( 0, 0.01f, 0.001f, post.crt.getOffset(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.crt.setOffset( source.getValue() );
			}
		} );

		final Slider slCrtTintR = ResourceFactory.newSlider( 0, 1f, 0.01f, post.crt.getTint().r, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				Color tint = post.crt.getTint();
				tint.r = source.getValue();
				post.crt.setTint( tint );
			}
		} );

		final Slider slCrtTintG = ResourceFactory.newSlider( 0, 1f, 0.01f, post.crt.getTint().g, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				Color tint = post.crt.getTint();
				tint.g = source.getValue();
				post.crt.setTint( tint );
			}
		} );

		final Slider slCrtTintB = ResourceFactory.newSlider( 0, 1f, 0.01f, post.crt.getTint().b, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				Color tint = post.crt.getTint();
				tint.b = source.getValue();
				post.crt.setTint( tint );
			}
		} );

		Table tCrt = ResourceFactory.newTable();
		tCrt.add( cbCrt ).colspan( 2 ).center();
		tCrt.row();
		tCrt.add( ResourceFactory.newLabel( "Color offset " ) ).left();
		tCrt.add( slCrtColorOffset );
		tCrt.row();
		tCrt.add( ResourceFactory.newLabel( "Tint (R) " ) ).left();
		tCrt.add( slCrtTintR );
		tCrt.row();
		tCrt.add( ResourceFactory.newLabel( "Tint (G) " ) ).left();
		tCrt.add( slCrtTintG );
		tCrt.row();
		tCrt.add( ResourceFactory.newLabel( "Tint (B) " ) ).left();
		tCrt.add( slCrtTintB );

		//
		// vignetting + gradient mapping
		//
		final CheckBox cbVignette = ResourceFactory.newCheckBox( " Vignetting", post.vignette.isEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				post.vignette.setEnabled( source.isChecked() );
			}
		} );

		final Slider slVignetteI = ResourceFactory.newSlider( 0, 1f, 0.01f, post.vignette.getIntensity(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getTarget();
				post.vignette.setIntensity( source.getValue() );
			}
		} );

		// @formatter:off
		final SelectBox sbGradientMap = ResourceFactory.newSelectBox(
				new String[] {
					"Cross processing ", "Sunset ", "Mars", "Vivid ", "Greenland ", "Cloudy ", "Muddy "
				},
				// @formatter:on
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						if( post.vignette.isGradientMappingEnabled() ) {
							SelectBox source = (SelectBox)event.getTarget();
							switch( source.getSelectionIndex() ) {
							case 0:
								post.vignette.setLutIndex( 16 );
								break;
							case 1:
								post.vignette.setLutIndex( 5 );
								break;
							case 2:
								post.vignette.setLutIndex( 7 );
								break;
							case 3:
								post.vignette.setLutIndex( 6 );
								break;
							case 4:
								post.vignette.setLutIndex( 8 );
								break;
							case 5:
								post.vignette.setLutIndex( 3 );
								break;
							case 6:
								post.vignette.setLutIndex( 0 );
								break;
							}
						}
					}
				} );

		sbGradientMap.setSelection( DefaultGradientMap );
		forceUpdateDefaultSelection.add( sbGradientMap );

		final CheckBox cbGradientMapping = ResourceFactory.newCheckBox( " Perform gradient mapping", post.vignette.isGradientMappingEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				if( source.isChecked() ) {
					post.vignette.setLut( gradientMapping );
					sbGradientMap.fire( new ChangeListener.ChangeEvent() );
				} else {
					post.vignette.setLut( null );
					post.vignette.setLutIndex( -1 );
				}
			}
		} );

		Table tVignette = ResourceFactory.newTable();
		tVignette.add( cbVignette ).colspan( 2 ).center();
		tVignette.row();
		tVignette.add( ResourceFactory.newLabel( "Intensity " ) ).left();
		tVignette.add( slVignetteI );
		tVignette.row();
		tVignette.add( cbGradientMapping ).colspan( 2 ).center();
		tVignette.row();
		tVignette.add( ResourceFactory.newLabel( "Choose map " ) ).left();
		tVignette.add( sbGradientMap );

		//
		// zoomer
		//
		final CheckBox cbZoomer = ResourceFactory.newCheckBox( " Zoomer", post.zoomer.isEnabled(), new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				post.zoomer.setEnabled( source.isChecked() );
				if( post.isEnabled() ) {
					animateBadSmile = !post.zoomer.isEnabled();
					if( post.zoomer.isEnabled() ) {
						zoomAmount = 0;
						zoomFactor = 0;
						singleMessage.setText( "Use the mousewheel to zoom in/out" );
					} else {
						singleMessage.setText( "" );
					}
				}
			}
		} );

		final CheckBox cbZoomerDoBlur = ResourceFactory.newCheckBox( " Radial blur", zoomRadialBlur, new ClickListener() {
			@Override
			public void clicked( ActorEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getTarget();
				if( source.isChecked() ) {
					zoomRadialBlur = true;
				} else {
					post.zoomer.setBlurStrength( 0 );
					zoomRadialBlur = false;
				}
			}
		} );

		Table tZoomer = ResourceFactory.newTable();
		tZoomer.add( cbZoomer );
		tZoomer.row();
		tZoomer.add( cbZoomerDoBlur );

		// lay out tables
		uiContainer.add( tGlobal );
		uiContainer.add( tBloom );
		uiContainer.row();
		uiContainer.add( tCurvature );
		uiContainer.add( tCrt );
		uiContainer.row();
		uiContainer.add( tVignette );
		uiContainer.add( tZoomer );

		// ui.addActor( uiContainer );

		// fire a change event on selected SelectBoxes to
		// update the default selection and initialize with
		// the default values
		for( int i = 0; i < forceUpdateDefaultSelection.size; i++ ) {
			forceUpdateDefaultSelection.get( i ).fire( new ChangeListener.ChangeEvent() );
		}

		Table messages = ResourceFactory.newTable();
		messages.setSize( width, 130 );
		messages.defaults().pad( 10, 15, 0, 15 ).align( Align.top ).expandY();
		messages.setY( -98 );
		messages.left();
		messages.setBackground( tback );

		messages.add( ResourceFactory.newLabel( "Press \"Q\" or \"Esc\" to quit" ) );
		fps = ResourceFactory.newLabel( "fps: " );
		messages.add( fps ).width( 200 ).padLeft( 50 );

		// general-purpose single message
		singleMessage = ResourceFactory.newLabel( "" );
		messages.add( singleMessage ).expandX().right();

		ui.addActor( messages );
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
		circleOffset.y = halfHeight + (angleAmplitude * 5) * MathUtils.cos( angle * MathUtils.degreesToRadians );

		// angle
		angle += angleSpeed * delta;
		if( angle > 360 ) {
			angle -= 360;
		}

		// info label
		fps.setText( "fps: " + Gdx.graphics.getFramesPerSecond() );

		// animate post-processing effects
		if( !animateBadSmile ) {
			float smoothing = 1.5f;
			zoomFactor = Utils.lerp( zoomFactor * smoothing, zoomAmount / smoothing, 0.1f ) * 0.5f;
			post.zoomer.setZoom( 1f + 4.0f * zoomFactor );
			if( zoomRadialBlur ) {
				post.zoomer.setBlurStrength( -0.1f * zoomFactor );
			}
		}

		post.crt.setTime( elapsedSecs );
		ui.act( Gdx.graphics.getDeltaTime() );
	}

	private void draw() {

		boolean willPostProcess = post.isReady();
		boolean backgroundFirst = (willPostProcess && !backgroundAffected && drawBackground) || (!willPostProcess && drawBackground);
		post.blending = backgroundFirst && willPostProcess;

		if( backgroundFirst || !willPostProcess ) {
			Gdx.gl20.glClearColor( 0, 0, 0, 1 );
			Gdx.gl20.glClear( GL20.GL_COLOR_BUFFER_BIT );
			if( backgroundFirst ) {
				batch.begin();
				background.draw( batch );
				batch.end();
			}
		}

		post.begin();
		batch.begin();
		{
			if( drawBackground && backgroundAffected ) {
				background.draw( batch );
			}

			if( drawSprite ) {
				if( animateBadSmile ) {
					badlogic.setPosition( circleOffset.x - badlogic.getWidth() / 2, circleOffset.y - badlogic.getHeight() / 2 );
				} else {
					badlogic.setPosition( halfWidth - badlogic.getWidth() / 2, halfHeight - badlogic.getHeight() / 2 );
				}

				badlogic.draw( batch );
			}
		}
		batch.end();
		post.end();

		ui.draw();

		if( DebugUI ) {
			Table.drawDebug( ui );
		}
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
		post.zoomer.setOrigin( x, y );
		return false;
	}

	@Override
	public boolean scrolled( int amount ) {
		zoomAmount += amount * -1;
		zoomAmount = MathUtils.clamp( zoomAmount, 0, 15 );
		return false;
	}

	private static final class Utils {
		public static float lerp( float prev, float curr, float alpha ) {
			return curr * alpha + prev * (1f - alpha);
		}

		private Utils() {
		}
	}
}
