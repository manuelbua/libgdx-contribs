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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;

/**
 * Implements the UI creation and event handling.
 * 
 * Notes on the panel animator: some very simple expedients help in determining whenever the
 * user is voluntarily leaving the panel area to make it to hide itself or it's just due to
 * the fact combobox widgets are higher than the panel, thus the user *needs* to move out of
 * the panel to make a selection: the latter case is being tracked by
 * 
 * @author bmanuel
 */
public final class UI {
	public boolean drawBackground, backgroundAffected, drawSprite;
	public Sprite background;

	private Stage stage;
	private Label singleMessage, fps;

	// panel animator
	private boolean comboBoxFlag, panelShown, usePanelAnimator;
	private TopPanelAnimator panelAnimator;

	private static final boolean DebugUI = false;
	private static final int DefaultBackground = 2;
	private static final int DefaultGradientMap = 0;
	private PostProcessing post;
	private Array<SelectBox> selectBoxes = new Array<SelectBox>();

	public UI( InputMultiplexer inputMultiplexer, PostProcessing postProcessing, boolean panelAutoShow ) {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		ResourceFactory.DebugUI = DebugUI;

		post = postProcessing;
		drawBackground = true;
		drawSprite = false;
		backgroundAffected = true;
		comboBoxFlag = false;
		usePanelAnimator = panelAutoShow;
		post.zoomRadialBlur = true;

		stage = new Stage();
		inputMultiplexer.addProcessor( stage );

		// selectable screen background
		background = ResourceFactory.newSprite( "bgnd.jpg" );
		background.setSize( width, height );

		// panel background
		NinePatch np = new NinePatch( ResourceFactory.newTexture( "brushed.png", false ), 0, 0, 0, 0 );
		np.setColor( new Color( 0.3f, 0.3f, 0.3f, 1f ) );
		NinePatchDrawable npBack = new NinePatchDrawable( np );

		// build the top panel and add all of its widgets
		Table topPanel = buildTopPanel( npBack, width, height );
		topPanel.add( buildGlobalSettingsWidgets() );
		topPanel.add( buildBloomWidgets() );
		topPanel.add( buildCurvatureWidgets() );
		topPanel.add( buildCrtEmulationWidgets() );
		topPanel.add( buildVignettingWidgets() );

		// the zoomer widgets group is somewhat "special": if we are going
		// to NOT use a panel animator then one more button will be added
		// to permit the user to show/hide the panel manually
		Table tZoomer = buildZoomerWidgets();

		// compute the panel's opened/closed position
		final float yWhenShown = height - topPanel.getHeight() + 13;
		final float yWhenHidden = height - 60 + 13;

		if( usePanelAnimator ) {
			panelShown = false;
			panelAnimator = new TopPanelAnimator( topPanel, new Rectangle( 10, 5, width - 20, 60 ), yWhenShown, yWhenHidden );
			topPanel.setY( yWhenHidden );
			topPanel.add( tZoomer ).expandX();
			topPanel.setColor( 1f, 1f, 1f, 0.5f );
		} else {
			panelShown = true;
			topPanel.setY( yWhenShown );
			topPanel.add( tZoomer ).expandX();
			tZoomer.row();
			tZoomer.add( buildPanelActionButtons( topPanel, yWhenShown, yWhenHidden ) ).align( Align.right );
		}

		// build the bottom panel
		Table bottomPanel = buildBottomPanel( npBack, width, height );
		bottomPanel.add( ResourceFactory.newLabel( "Press \"Q\" or \"Esc\" to quit" ) );

		// fps label
		fps = ResourceFactory.newLabel( "fps: " );
		bottomPanel.add( fps ).width( 200 ).padLeft( 50 );

		// general-purpose single message
		singleMessage = ResourceFactory.newLabel( "" );
		bottomPanel.add( singleMessage ).expandX().right();

		// UI is quite ready at this point, just add the containers to the stage
		stage.addActor( topPanel );
		stage.addActor( bottomPanel );

		// perform some processing on the SelectBox widgets
		for( int i = 0; i < selectBoxes.size; i++ ) {
			// fire a change event on selected SelectBoxes to
			// update the default selection and initialize accordingly
			selectBoxes.get( i ).fire( new ChangeListener.ChangeEvent() );

			if( usePanelAnimator ) {
				// track clicks on the comboboxes, this imply the widget is
				// opening giving the user some choices
				selectBoxes.get( i ).addListener( new ClickListener() {
					@Override
					public void clicked( InputEvent event, float x, float y ) {
						comboBoxFlag = true;
						panelAnimator.suspend();
					}
				} );

				// track changes, user performed a selection
				selectBoxes.get( i ).addListener( new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						comboBoxFlag = false;
					}
				} );
			}
		}

		// finally, track clicks on the stage, whenever the user cancel an
		// opened combobox selection by clicking away it will cause the widgets
		// to close (no ChangeListener will be notified)
		stage.addListener( new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				if( !comboBoxFlag ) {
					panelAnimator.resume();
				}

				comboBoxFlag = false;
			}
		} );
	}

	private Table buildTopPanel( NinePatchDrawable back, float width, float height ) {

		Table p = ResourceFactory.newTable();
		p.setSize( width, 155 );
		p.defaults().pad( 5, 25, 5, 0 ).align( Align.top );
		p.left();
		p.setBackground( back );

		return p;
	}

	private Table buildGlobalSettingsWidgets() {
		// post-processing
		final CheckBox cbPost = ResourceFactory.newCheckBox( " Post-processing", post.isEnabled(), new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				post.setEnabled( source.isChecked() );
			}
		} );

		final SelectBox<String> sbBackground = ResourceFactory.newSelectBox( new String[] { "None ", "Scratches ", "Mountains ",
				"Lake ", "Checker board " }, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				@SuppressWarnings( "unchecked" )
				SelectBox<String> source = (SelectBox<String>)actor;
				drawBackground = true;

				switch( source.getSelectedIndex() ) {
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
				case 4:
					background.setTexture( ResourceFactory.newTexture( "bgnd4.jpg", false ) );
					break;
				}
			}
		} );

		// background affected by post-processing
		final CheckBox cbBackgroundAffected = ResourceFactory.newCheckBox( " Background affected\n by post-processing",
				backgroundAffected, new ClickListener() {
					@Override
					public void clicked( InputEvent event, float x, float y ) {
						CheckBox source = (CheckBox)event.getListenerActor();
						backgroundAffected = source.isChecked();
						// if( backgroundAffected ) {
						// post.enableBlending();
						// } else {
						// post.disableBlending();
						// }
					}
				} );

		// sprite
		final CheckBox cbSprite = ResourceFactory.newCheckBox( " Show sprite", drawSprite, new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				drawSprite = source.isChecked();
			}
		} );

		sbBackground.setSelectedIndex( DefaultBackground );
		selectBoxes.add( sbBackground );

		Table t = ResourceFactory.newTable();
		t.add( cbPost ).colspan( 2 ).left();
		t.row();
		t.add( ResourceFactory.newLabel( "Background " ) );
		t.add( sbBackground );
		t.row();
		t.add( cbBackgroundAffected ).colspan( 2 ).left();
		t.row();
		t.add( cbSprite ).colspan( 2 ).left();

		return t;
	}

	private Table buildBloomWidgets() {
		final CheckBox cbBloom = ResourceFactory.newCheckBox( " Bloom", post.bloom.isEnabled(), new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				post.bloom.setEnabled( source.isChecked() );
			}
		} );

		final Slider slBloomThreshold = ResourceFactory.newSlider( 0, 1, 0.01f, post.bloom.getThreshold(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				post.bloom.setThreshold( source.getValue() );
			}
		} );

		final Slider slBloomBaseI = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBaseIntensity(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				post.bloom.setBaseIntesity( source.getValue() );
			}
		} );

		final Slider slBloomBaseS = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBaseSaturation(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				post.bloom.setBaseSaturation( source.getValue() );
			}
		} );

		final Slider slBloomBloomI = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBloomIntensity(),
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						Slider source = (Slider)event.getListenerActor();
						post.bloom.setBloomIntesity( source.getValue() );
					}
				} );

		final Slider slBloomBloomS = ResourceFactory.newSlider( 0, 2, 0.01f, post.bloom.getBloomSaturation(),
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						Slider source = (Slider)event.getListenerActor();
						post.bloom.setBloomSaturation( source.getValue() );
					}
				} );

		Table t = ResourceFactory.newTable();
		t.add( cbBloom ).colspan( 2 ).center();
		t.row();
		t.add( ResourceFactory.newLabel( "threshold " ) ).left();
		t.add( slBloomThreshold );
		t.row();
		t.add( ResourceFactory.newLabel( "base int " ) ).left();
		t.add( slBloomBaseI );
		t.row();
		t.add( ResourceFactory.newLabel( "base sat " ) ).left();
		t.add( slBloomBaseS );
		t.row();
		t.add( ResourceFactory.newLabel( "bloom int " ) ).left();
		t.add( slBloomBloomI );
		t.row();
		t.add( ResourceFactory.newLabel( "bloom sat " ) ).left();
		t.add( slBloomBloomS );

		return t;
	}

	private Table buildCurvatureWidgets() {
		final CheckBox cbCurvature = ResourceFactory.newCheckBox( " Curvature", post.curvature.isEnabled(), new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				post.curvature.setEnabled( source.isChecked() );

			}
		} );

		final Slider slCurvatureDist = ResourceFactory.newSlider( 0, 2, 0.01f, post.curvature.getDistortion(),
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						Slider source = (Slider)event.getListenerActor();
						post.curvature.setDistortion( source.getValue() );
					}
				} );

		final Slider slCurvatureZoom = ResourceFactory.newSlider( 0, 2, 0.01f, 2f - post.curvature.getZoom(),
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						Slider source = (Slider)event.getListenerActor();
						post.curvature.setZoom( 2f - source.getValue() );
					}
				} );

		Table t = ResourceFactory.newTable();
		t.add( cbCurvature ).colspan( 2 ).center();
		t.row();
		t.add( ResourceFactory.newLabel( "Distortion " ) ).left();
		t.add( slCurvatureDist );
		t.row();
		t.add( ResourceFactory.newLabel( "Zoom " ) ).left();
		t.add( slCurvatureZoom );

		return t;
	}

	private Table buildCrtEmulationWidgets() {
		final CheckBox cbCrt = ResourceFactory.newCheckBox( " Old CRT emulation", post.crt.isEnabled(), new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				post.crt.setEnabled( source.isChecked() );
			}
		} );

		final Slider slCrtDispersionRC = ResourceFactory.newSlider( -1f, 1f, 0.001f, post.crt.getChromaticDispersion().x,
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						Slider source = (Slider)event.getListenerActor();
						post.crt.setChromaticDispersionRC( source.getValue() );
					}
				} );

		final Slider slCrtDispersionBY = ResourceFactory.newSlider( -1f, 1f, 0.001f, post.crt.getChromaticDispersion().y,
				new ChangeListener() {
					@Override
					public void changed( ChangeEvent event, Actor actor ) {
						Slider source = (Slider)event.getListenerActor();
						post.crt.setChromaticDispersionBY( source.getValue() );
					}
				} );

		slCrtDispersionRC.setSnapToValues( new float[] { 0 }, 0.05f );
		slCrtDispersionBY.setSnapToValues( new float[] { 0 }, 0.05f );

		final Slider slCrtTintR = ResourceFactory.newSlider( 0, 1f, 0.01f, post.crt.getTint().r, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				Color tint = post.crt.getTint();
				tint.r = source.getValue();
				post.crt.setTint( tint );
			}
		} );

		final Slider slCrtTintG = ResourceFactory.newSlider( 0, 1f, 0.01f, post.crt.getTint().g, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				Color tint = post.crt.getTint();
				tint.g = source.getValue();
				post.crt.setTint( tint );
			}
		} );

		final Slider slCrtTintB = ResourceFactory.newSlider( 0, 1f, 0.01f, post.crt.getTint().b, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				Color tint = post.crt.getTint();
				tint.b = source.getValue();
				post.crt.setTint( tint );
			}
		} );

		Table t = ResourceFactory.newTable();
		t.add( cbCrt ).colspan( 2 ).center();
		t.row();
		t.add( ResourceFactory.newLabel( "Chromatic dispersion R/C " ) ).left();
		t.add( slCrtDispersionRC );
		t.row();
		t.add( ResourceFactory.newLabel( "Chromatic dispersion B/Y " ) ).left();
		t.add( slCrtDispersionBY );
		t.row();
		t.add( ResourceFactory.newLabel( "Tint (R) " ) ).left();
		t.add( slCrtTintR );
		t.row();
		t.add( ResourceFactory.newLabel( "Tint (G) " ) ).left();
		t.add( slCrtTintG );
		t.row();
		t.add( ResourceFactory.newLabel( "Tint (B) " ) ).left();
		t.add( slCrtTintB );

		return t;
	}

	private Table buildVignettingWidgets() {
		final CheckBox cbVignette = ResourceFactory.newCheckBox( " Vignetting", post.vignette.isEnabled(), new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				post.vignette.setEnabled( source.isChecked() );

			}
		} );

		final Slider slVignetteI = ResourceFactory.newSlider( 0, 1f, 0.01f, post.vignette.getIntensity(), new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				Slider source = (Slider)event.getListenerActor();
				post.vignette.setIntensity( source.getValue() );
			}
		} );

		final SelectBox<String> sbGradientMap = ResourceFactory.newSelectBox( new String[] { "Cross processing ", "Sunset ",
				"Mars", "Vivid ", "Greenland ", "Cloudy ", "Muddy " }, new ChangeListener() {
			@Override
			public void changed( ChangeEvent event, Actor actor ) {
				if( post.vignette.isGradientMappingEnabled() ) {
					SelectBox<String> source = (SelectBox)actor;
					switch( source.getSelectedIndex() ) {
					case 0:
						post.vignette.setLutIndexVal( 0, 16 );
						break;
					case 1:
						post.vignette.setLutIndexVal( 0, 5 );
						break;
					case 2:
						post.vignette.setLutIndexVal( 0, 7 );
						break;
					case 3:
						post.vignette.setLutIndexVal( 0, 6 );
						break;
					case 4:
						post.vignette.setLutIndexVal( 0, 8 );
						break;
					case 5:
						post.vignette.setLutIndexVal( 0, 3 );
						break;
					case 6:
						post.vignette.setLutIndexVal( 0, 0 );
						break;
					}
				}
			}
		} );

		sbGradientMap.setSelectedIndex( DefaultGradientMap );
		selectBoxes.add( sbGradientMap );

		final CheckBox cbGradientMapping = ResourceFactory.newCheckBox( " Perform gradient mapping",
				post.vignette.isGradientMappingEnabled(), new ClickListener() {
					@Override
					public void clicked( InputEvent event, float x, float y ) {
						CheckBox source = (CheckBox)event.getListenerActor();
						if( source.isChecked() ) {
							post.vignette.setLutTexture( ResourceFactory.newTexture( "gradient-mapping.png", false ) );
							sbGradientMap.fire( new ChangeListener.ChangeEvent() );
						} else {
							post.vignette.setLutTexture( null );
							post.vignette.setLutIndexVal( 0, -1 );
						}
					}
				} );

		Table t = ResourceFactory.newTable();
		t.add( cbVignette ).colspan( 2 ).center();
		t.row();
		t.add( ResourceFactory.newLabel( "Intensity " ) ).left();
		t.add( slVignetteI );
		t.row();
		t.add( cbGradientMapping ).padTop( 10 ).colspan( 2 ).center();
		t.row();
		t.add( ResourceFactory.newLabel( "Gradient " ) ).center().padTop( 10 );
		t.add( sbGradientMap ).padTop( 10 );

		return t;
	}

	private Table buildZoomerWidgets() {
		final CheckBox cbZoomer = ResourceFactory.newCheckBox( " Zoomer", post.zoomer.isEnabled(), new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				post.zoomer.setEnabled( source.isChecked() );
				if( post.isEnabled() ) {
					if( post.zoomer.isEnabled() ) {
						post.zoomAmount = 0;
						post.zoomFactor = 0;
						singleMessage.setText( "Use the mousewheel to zoom in/out" );
					} else {
						singleMessage.setText( "" );
					}
				}

			}
		} );

		final CheckBox cbZoomerDoBlur = ResourceFactory.newCheckBox( " Radial blur", post.zoomRadialBlur, new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				CheckBox source = (CheckBox)event.getListenerActor();
				if( source.isChecked() ) {
					post.zoomRadialBlur = true;
				} else {
					post.zoomer.setBlurStrength( 0 );
					post.zoomRadialBlur = false;
				}
			}
		} );

		Table t = ResourceFactory.newTable();
		t.add( cbZoomer );
		t.row();
		t.add( cbZoomerDoBlur );

		return t;
	}

	private Table buildPanelActionButtons( final Table topPanel, final float yWhenShown, final float yWhenHidden ) {
		TextButton btnShowHide = ResourceFactory.newButton( "Show/hide panel", new ClickListener() {
			@Override
			public void clicked( InputEvent event, float x, float y ) {
				if( !panelShown ) {
					topPanel.addAction( Actions.moveTo( topPanel.getX(), yWhenShown, 0.5f, Interpolation.exp10 ) );
					topPanel.addAction( Actions.alpha( 1f, 0.5f, Interpolation.exp10 ) );
					panelShown = true;
				} else {
					topPanel.addAction( Actions.moveTo( topPanel.getX(), yWhenHidden, 0.5f, Interpolation.exp10 ) );
					topPanel.addAction( Actions.alpha( 0.5f, 0.5f, Interpolation.exp10 ) );
					panelShown = false;
				}
			}
		} );

		Table t = ResourceFactory.newTable();
		t.row().padTop( 55 );
		t.add( btnShowHide );

		return t;
	}

	private Table buildBottomPanel( NinePatchDrawable back, float width, float height ) {
		Table t = ResourceFactory.newTable();
		t.setSize( width, 130 );
		t.defaults().pad( 10, 15, 0, 15 ).align( Align.top ).expandY();
		t.setY( -98 );
		t.left();
		t.setBackground( back );

		return t;
	}

	public void update( float deltaTimeSecs ) {
		stage.act( deltaTimeSecs );
		fps.setText( "fps: " + Gdx.graphics.getFramesPerSecond() );
		if( usePanelAnimator ) {
			panelAnimator.update();
		}
	}

	public void draw() {
		stage.draw();

		if( DebugUI ) {
			Table.drawDebug( stage );
		}
	}

	public void mouseMoved( int x, int y ) {
		if( usePanelAnimator ) {
			panelAnimator.mouseMoved( x, y );
		}
	}
}
