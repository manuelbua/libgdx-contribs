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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Implements an automatic top panel animator with the only role do make it appear when
 * the user approach the top panel zone and make it disappear whenever the user is leaving
 * the area.
 * 
 * @author bmanuel
 */
public final class TopPanelAnimator {
	enum State {
		ShowingPanel, HidingPanel, Idle
	}

	static final float InHotZoneSecondsBeforeShowing = 0.25f;
	static final float OutHotZoneSecondsBeforeHiding = 0.8f;
	Actor panel;
	Timer timer;
	State state;
	HotZone hotZone;
	float closedHotZoneHeight, openedHotZoneHeight;
	boolean suspended;
	float yShow, yHidden;

	public TopPanelAnimator( Actor panel, Rectangle hotZone, float yWhenShown, float yWhenHidden ) {
		this.state = State.Idle;
		this.panel = panel;
		this.timer = new Timer();
		this.hotZone = new HotZone( hotZone );
		this.yShow = yWhenShown;
		this.yHidden = yWhenHidden;
		this.suspended = false;

		this.closedHotZoneHeight = hotZone.height - 25;
		this.openedHotZoneHeight = panel.getHeight() - 30;
	}

	public void update() {
		if( suspended ) {
			return;
		}

		hotZone.update();

		switch( state ) {
		case Idle:
			idling();
			break;
		case ShowingPanel:
			timer.update();
			showing( timer.elapsed() );
			break;
		case HidingPanel:
			timer.update();
			hiding( timer.elapsed() );
			break;
		}
	}

	public void suspend() {
		suspended = true;
	}

	public void resume() {
		if( suspended ) {
			suspended = false;
			timer.reset();
		}
	}

	private void idling() {
		if( hotZone.justIn ) {
			setState( State.ShowingPanel );
			// Gdx.app.log( "PanelAnimator", "Waiting to show..." );
		} else if( hotZone.justOut ) {
			setState( State.HidingPanel );
			// Gdx.app.log( "PanelAnimator", "Waiting to hide..." );
		}
	}

	private void showing( float elapsed ) {
		if( hotZone.isIn ) {
			if( elapsed > InHotZoneSecondsBeforeShowing ) {
				setState( State.Idle );

				panel.addAction( Actions.moveTo( panel.getX(), yShow, 0.5f, Interpolation.exp10 ) );
				panel.addAction( Actions.alpha( 1f, 0.5f, Interpolation.exp10 ) );

				// resize hotzone rect to let the user move in the whole area
				Rectangle hz = hotZone.getHotZone();
				hz.setHeight( openedHotZoneHeight );
				hotZone.setHotZone( hz );

				// Gdx.app.log( "PanelAnimator", "Start showing" );
			}
		} else {
			setState( State.Idle );
			// Gdx.app.log( "PanelAnimator", "Show canceled." );
		}
	}

	private void hiding( float elapsed ) {
		if( !hotZone.isIn ) {
			if( elapsed > OutHotZoneSecondsBeforeHiding ) {
				setState( State.Idle );

				panel.addAction( Actions.moveTo( panel.getX(), yHidden, 0.5f, Interpolation.exp10 ) );
				panel.addAction( Actions.alpha( 0.5f, 0.5f, Interpolation.exp10 ) );

				// restore original hotzone height
				Rectangle hz = hotZone.getHotZone();
				hz.setHeight( closedHotZoneHeight );
				hotZone.setHotZone( hz );

				// Gdx.app.log( "PanelAnimator", "Start hiding" );
			}
		} else {
			setState( State.Idle );
			// Gdx.app.log( "PanelAnimator", "Hide canceled." );
		}
	}

	private void setState( State state ) {
		timer.reset();
		this.state = state;
	}

	public void mouseMoved( int x, int y ) {
		hotZone.mouseMoved( x, y );
	}

	private final class HotZone {
		public boolean isIn;
		public boolean wasIn;
		public boolean justIn;
		public boolean justOut;
		private Vector2 mouse;
		Rectangle rectHotZone;

		public HotZone( Rectangle hotZone ) {
			mouse = new Vector2();
			rectHotZone = new Rectangle();
			setHotZone( hotZone );
			reset();
		}

		public void reset() {
			isIn = false;
			wasIn = false;
			justIn = false;
			justOut = false;
			mouse.set( -1, -1 );
		}

		public void setHotZone( Rectangle rectangle ) {
			rectHotZone.set( rectangle );
		}

		public Rectangle getHotZone() {
			return rectHotZone;
		}

		public void update() {
			if( mouse.x > -1 && mouse.y > -1 ) {
				wasIn = isIn;
				isIn = rectHotZone.contains( mouse.x, mouse.y );
				justIn = isIn && !wasIn;
				justOut = !isIn && wasIn;
			}
		}

		public void mouseMoved( int x, int y ) {
			mouse.set( x, y );
		}
	}
}
