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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.bitfire.utils.ShaderLoader;

public final class CrtScreen extends Filter<CrtScreen> {
	private float elapsedSecs, offset, zoom;
	private float cdRedCyan, cdBlueYellow;
	private Vector2 chromaticDispersion;
	private final Vector3 vtint;
	private final Color tint;
	private float distortion;
	private boolean dodistortion;
	private RgbMode mode;

	public enum RgbMode {
		None, RgbShift, ChromaticAberrations
	}

	public enum Param implements Parameter {
		// @formatter:off
		Texture0("u_texture0",0),
		Time("time",0),
		Tint("tint",3),
		ColorOffset("offset",0),
		ChromaticDispersion("chromaticDispersion",2),
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

	public CrtScreen( boolean barrelDistortion, RgbMode mode ) {
		// @off
		super( ShaderLoader.fromFile( "screenspace", "crt-screen", (barrelDistortion ? "#define ENABLE_BARREL_DISTORTION\n" : "")
				+ (mode == RgbMode.RgbShift ? "#define ENABLE_RGB_SHIFT\n" : "")
				+ (mode == RgbMode.ChromaticAberrations ? "#define ENABLE_CHROMATIC_ABERRATIONS\n" : "") ) );
		// @on

		dodistortion = barrelDistortion;
		this.mode = mode;

		vtint = new Vector3();
		tint = new Color();
		chromaticDispersion = new Vector2();

		rebind();

		setTime( 0f );
		setTint( 1.0f, 1.0f, 0.85f );
		setDistortion( 0.3f );
		setZoom( 1f );
		switch( mode ) {
		case ChromaticAberrations:
			setChromaticDispersion( -0.1f, -0.1f );
			break;
		case RgbShift:
			setColorOffset( 0.003f );
			break;
		default:
			throw new GdxRuntimeException( "Unsupported RGB mode" );
		}
	}

	public void setTime( float elapsedSecs ) {
		this.elapsedSecs = elapsedSecs;
		setParam( Param.Time, elapsedSecs );
	}

	public void setColorOffset( float offset ) {
		this.offset = offset;
		if( mode == RgbMode.RgbShift ) {
			setParam( Param.ColorOffset, this.offset );
		}
	}

	public void setChromaticDispersion( Vector2 dispersion ) {
		setChromaticDispersion( dispersion.x, dispersion.y );
	}

	public void setChromaticDispersion( float redCyan, float blueYellow ) {

		this.cdRedCyan = redCyan;
		this.cdBlueYellow = blueYellow;
		chromaticDispersion.x = cdRedCyan;
		chromaticDispersion.y = cdBlueYellow;
		if( mode == RgbMode.ChromaticAberrations ) {
			setParam( Param.ChromaticDispersion, chromaticDispersion );
		}
	}

	public void setChromaticDispersionRC( float redCyan ) {
		this.cdRedCyan = redCyan;
		chromaticDispersion.x = cdRedCyan;
		if( mode == RgbMode.ChromaticAberrations ) {
			setParam( Param.ChromaticDispersion, chromaticDispersion );
		}
	}

	public void setChromaticDispersionBY( float blueYellow ) {
		this.cdBlueYellow = blueYellow;
		chromaticDispersion.y = cdBlueYellow;
		if( mode == RgbMode.ChromaticAberrations ) {
			setParam( Param.ChromaticDispersion, chromaticDispersion );
		}
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

	public Vector2 getChromaticDispersion() {
		return chromaticDispersion;
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
		if( mode == RgbMode.RgbShift ) {
			setParams( Param.ColorOffset, offset );
		} else if( mode == RgbMode.ChromaticAberrations ) {
			setParams( Param.ChromaticDispersion, chromaticDispersion );
		}

		setParams( Param.Tint, vtint );

		if( dodistortion ) {
			setParams( Param.Distortion, distortion );
			setParams( Param.Zoom, zoom );
		}

		endParams();
	}
}
