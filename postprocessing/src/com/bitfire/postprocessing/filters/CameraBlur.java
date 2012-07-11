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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.bitfire.utils.ShaderLoader;

/**
 * FIXME this effect is INCOMPLETE!
 * 
 * @author bmanuel
 */
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
