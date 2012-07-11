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

package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.bitfire.utils.ShaderLoader;

public final class CrtScreen extends Filter<CrtScreen> {
	private float elapsedSecs, offset, zoom;
	private final Vector3 vtint;
	private final Color tint;
	private float distortion;
	private boolean dodistortion;

	public enum Param implements Parameter {
		// @formatter:off
		Texture0("u_texture0",0),
		Time("time",0),
		Tint("tint",3),
		ColorOffset("offset",0),
		Distortion("Distortion",0),
		Zoom("zoom",0)
		;
		// @formatter:on

		private final String mnemonic;
		private int elementSize;

		private Param( String m, int elementSize ) {
			this.mnemonic = m;
			this.elementSize = elementSize;
		}

		@Override
		public String mnemonic() {
			return this.mnemonic;
		}

		@Override
		public int arrayElementSize() {
			return this.elementSize;
		}
	}

	public CrtScreen( boolean barrelDistortion ) {
		super( ShaderLoader.fromFile( "screenspace", "crt-screen", barrelDistortion ? "#define ENABLE_BARREL_DISTORTION" : "" ) );
		dodistortion = barrelDistortion;

		vtint = new Vector3();
		tint = new Color();

		rebind();

		setTime( 0f );
		setTint( 0.8f, 1.0f, 0.7f );
		setDistortion( 0.3f );
		setZoom( 1f );
		setColorOffset( 0.003f );
	}

	public void setTime( float elapsedSecs ) {
		this.elapsedSecs = elapsedSecs;
		setParam( Param.Time, elapsedSecs );
	}

	public void setColorOffset( float offset ) {
		this.offset = offset;
		setParam( Param.ColorOffset, this.offset );
	}

	public void setTint( Color color ) {
		tint.set( color );
		vtint.set( tint.r, tint.g, tint.b );
		setParam( Param.Tint, vtint );
	}

	public void setTint( float r, float g, float b ) {
		tint.set( r, g, b, 1f );
		vtint.set( tint.r, tint.g, tint.b );
		setParam( Param.Tint, vtint );
	}

	public void setDistortion( float distortion ) {
		this.distortion = distortion;
		if( dodistortion ) {
			setParam( Param.Distortion, this.distortion );
		}
	}

	public void setZoom( float zoom ) {
		this.zoom = zoom;
		if( dodistortion ) {
			setParam( Param.Zoom, this.zoom );
		}
	}

	public float getOffset() {
		return offset;
	}

	public float getZoom() {
		return zoom;
	}

	public Color getTint() {
		return tint;
	}

	@Override
	protected void onBeforeRender() {
		inputTexture.bind( u_texture0 );
	}

	@Override
	public void rebind() {
		setParams( Param.Texture0, u_texture0 );
		setParams( Param.Time, elapsedSecs );
		setParams( Param.ColorOffset, offset );

		vtint.set( tint.r, tint.g, tint.b );
		setParams( Param.Tint, vtint );

		if( dodistortion ) {
			setParams( Param.Distortion, distortion );
			setParams( Param.Zoom, zoom );
		}

		endParams();
	}
}
