package com.bitfire.postprocessing.filters;

import com.bitfire.utils.ShaderLoader;

public final class Zoom extends Filter<Zoom> {
	private float x, y, zoom;

	public enum Param implements Parameter {
		// @formatter:off
		Texture( "u_texture0", 0 ),
		OffsetX( "offset_x", 0 ),
		OffsetY( "offset_y", 0 ),
		Zoom( "zoom", 0 ),
		;
		// @formatter:on

		private String mnemonic;
		private int elementSize;

		private Param( String mnemonic, int arrayElementSize ) {
			this.mnemonic = mnemonic;
			this.elementSize = arrayElementSize;
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

	public Zoom() {
		super( ShaderLoader.fromFile( "zoom", "zoom" ) );
		rebind();
		setOrigin( 0.5f, 0.5f );
		setZoom( 1f );
	}

	public void setOrigin( float x, float y ) {
		this.x = x;
		this.y = y;
		setParams( Param.OffsetX, this.x );
		setParams( Param.OffsetY, this.y );
		endParams();
	}

	public void setZoom( float zoom ) {
		this.zoom = zoom;
		setParam( Param.Zoom, this.zoom );
	}

	public float getZoom() {
		return zoom;
	}

	public float getOriginX() {
		return x;
	}

	public float getOriginY() {
		return y;
	}

	@Override
	public void rebind() {
		// reimplement super to batch every parameter
		setParams( Param.Texture, u_texture0 );
		setParams( Param.OffsetX, x );
		setParams( Param.OffsetY, y );
		setParams( Param.Zoom, zoom );
		endParams();
	}

	@Override
	protected void onBeforeRender() {
		inputTexture.bind( u_texture0 );
	}
}
