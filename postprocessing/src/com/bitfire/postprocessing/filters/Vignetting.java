package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.bitfire.utils.ShaderLoader;

public final class Vignetting extends Filter<Vignetting> {

	private float x, y;
	private float intensity, saturation, saturationMul;

	private Texture texLut;
	private boolean dolut, dosat;
	private float lutintensity;
	private float lutindex;	// uses float to avoid casting in shader code
	private float centerX, centerY;

	public enum Param implements Parameter {
		// @formatter:off
		Texture0("u_texture0",0),
		TexLUT("u_texture1",0),
		VignetteIntensity("VignetteIntensity",0),
		VignetteX("VignetteX",0),
		VignetteY("VignetteY",0),
		Saturation("Saturation",0),
		SaturationMul("SaturationMul",0),
		LutIntensity("LutIntensity",0),
		LutIndex("LutIndex",0),
		CenterX("CenterX",0),
		CenterY("CenterY",0)
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

	public Vignetting( boolean controlSaturation ) {
		super( ShaderLoader.fromFile( "screenspace", "vignetting", (controlSaturation ? "#define CONTROL_SATURATION\n#define ENABLE_PIXEL_LUT"
				: "#define ENABLE_PIXEL_LUT") ) );
		dolut = false;
		dosat = controlSaturation;
		rebind();
		setCoords( 0.8f, 0.25f );
		setCenter( 0.5f, 0.5f );
		setIntensity( 1f );
	}

	public void setIntensity( float intensity ) {
		this.intensity = intensity;
		setParam( Param.VignetteIntensity, intensity );
	}

	public void setSaturation( float saturation ) {
		this.saturation = saturation;
		if( dosat ) {
			setParam( Param.Saturation, saturation );
		}
	}

	public void setSaturationMul( float saturationMul ) {
		this.saturationMul = saturationMul;
		if( dosat ) {
			setParam( Param.SaturationMul, saturationMul );
		}
	}

	public void setCoords( float x, float y ) {
		this.x = x;
		this.y = y;
		setParams( Param.VignetteX, x );
		setParams( Param.VignetteY, y );
		endParams();
	}

	public void setX( float x ) {
		this.x = x;
		setParam( Param.VignetteX, x );
	}

	public void setY( float y ) {
		this.y = y;
		setParam( Param.VignetteY, y );
	}

	public void setLut( Texture texture ) {
		texLut = texture;
		dolut = (texLut != null);

		if( dolut ) {
			setParam( Param.TexLUT, u_texture1 );
		}
	}

	public void setLutIntensity( float value ) {
		lutintensity = value;
		if( dolut ) {
			setParam( Param.LutIntensity, lutintensity );
		}
	}

	public void setLutIndex( int index ) {
		lutindex = index;
		if( dolut ) {
			setParam( Param.LutIndex, lutindex );
		}
	}

	/** Specify the center, in normalized screen coordinates. */
	public void setCenter( float x, float y ) {
		this.centerX = x;
		this.centerY = y;
		setParams( Param.CenterX, centerX );
		setParams( Param.CenterY, centerY ).endParams();
	}

	public float getCenterX() {
		return centerX;
	}

	public float getCenterY() {
		return centerY;
	}

	public int getLutIndex() {
		return (int)lutindex;
	}

	public float getLutIntensity() {
		return lutintensity;
	}

	public Texture getLut() {
		return texLut;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getIntensity() {
		return intensity;
	}

	public float getSaturation() {
		return saturation;
	}

	public float getSaturationMul() {
		return saturationMul;
	}

	@Override
	public void rebind() {
		setParams( Param.Texture0, u_texture0 );

		if( dolut ) {
			setParams( Param.LutIndex, lutindex );
			setParams( Param.TexLUT, u_texture1 );
			setParams( Param.LutIntensity, lutintensity );
		}

		if( dosat ) {
			setParams( Param.Saturation, saturation );
			setParams( Param.SaturationMul, saturationMul );
		}

		setParams( Param.VignetteIntensity, intensity );
		setParams( Param.VignetteX, x );
		setParams( Param.VignetteY, y );
		setParams( Param.CenterX, centerX );
		setParams( Param.CenterY, centerY );
		endParams();
	}

	@Override
	protected void onBeforeRender() {
		inputTexture.bind( u_texture0 );
		if( dolut ) {
			texLut.bind( u_texture1 );
		}
	}
}
