package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.bitfire.utils.ShaderLoader;

public final class CameraBlur extends Filter<CameraBlur> {

	private Texture depthMap = null;

	public enum Param implements Parameter {
		// @formatter:off
		InputScene("u_texture0",0),
		DepthMap("u_texture1",0),
		InvMVP("invMVP", 0),
		PrevMVP("prevMVP", 0),
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

	public CameraBlur() {
		super( ShaderLoader.fromFile( "screenspace", "camerablur" ) );
		rebind();
		// dolut = false;
	}

	public void setDepthMap( Texture texture ) {
		this.depthMap = texture;
	}

	public void setMatrices( Matrix4 invViewProj, Matrix4 prevViewProj ) {
		setParams( Param.InvMVP, invViewProj );
		setParams( Param.PrevMVP, prevViewProj );
		endParams();
	}

	@Override
	public void rebind() {
		setParams( Param.InputScene, u_texture0 );
		setParams( Param.DepthMap, u_texture1 );
		endParams();
	}

	@Override
	protected void onBeforeRender() {
		inputTexture.bind( u_texture0 );
		depthMap.bind( u_texture1 );
	}
}
