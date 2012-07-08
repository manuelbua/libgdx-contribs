package com.bitfire.postprocessing.filters;

import com.bitfire.utils.ShaderLoader;

public final class Threshold extends Filter<Threshold> {

	public enum Param implements Parameter {
		// @formatter:off
		Texture( "u_texture0", 0 ),
		Threshold( "treshold", 0 ),
		ThresholdInvTx( "tresholdInvTx", 0 );
		// @formatter:on

		private String mnemonic;
		private int elementSize;

		private Param( String mnemonic, int elementSize ) {
			this.mnemonic = mnemonic;
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

	public Threshold() {
		super( ShaderLoader.fromFile( "screenspace", "threshold" ) );
		rebind();
	}

	private float gamma = 0;

	public void setTreshold( float gamma ) {
		this.gamma = gamma;
		setParams( Param.Threshold, gamma );
		setParams( Param.ThresholdInvTx, 1f / (1 - gamma) ).endParams();
	}

	public float getThreshold() {
		return gamma;
	}

	@Override
	protected void onBeforeRender() {
		inputTexture.bind( u_texture0 );
	}

	@Override
	public void rebind() {
		setParams( Param.Texture, u_texture0 );
		setTreshold( this.gamma );
	}
}
