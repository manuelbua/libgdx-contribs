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

package com.bitfire.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class ShaderLoader {
	public static String BasePath = "";
	public static boolean Pedantic = true;

	public static ShaderProgram fromFile( String vertexFileName, String fragmentFileName ) {
		return ShaderLoader.fromFile( vertexFileName, fragmentFileName, "" );
	}

	public static ShaderProgram fromFile( String vertexFileName, String fragmentFileName, String defines ) {
		Gdx.app.log( "ShaderLoader", "Compiling " + vertexFileName + " | " + fragmentFileName + "..." );
		String vertexShaderSrc = Gdx.files.internal( BasePath + vertexFileName + ".vertex" ).readString();
		String fragmentShaderSrc = Gdx.files.internal( BasePath + fragmentFileName + ".fragment" ).readString();
		return ShaderLoader.fromString( vertexShaderSrc, fragmentShaderSrc, vertexFileName, fragmentFileName, defines );
	}

	public static ShaderProgram fromString( String vertex, String fragment, String vertexName, String fragmentName ) {
		return ShaderLoader.fromString( vertex, fragment, vertexName, fragmentName, "" );
	}

	public static ShaderProgram fromString( String vertex, String fragment, String vertexName, String fragmentName, String defines ) {
		ShaderProgram.pedantic = ShaderLoader.Pedantic;
		ShaderProgram shader = new ShaderProgram( defines + "\n" + vertex, defines + "\n" + fragment );
		if( !shader.isCompiled() ) {
			Gdx.app.error( "ShaderLoader", shader.getLog() );
			Gdx.app.exit();
		} else {
			if( defines != null && defines.length() > 0 ) {
				Gdx.app.log( "ShaderLoader", vertexName + "/" + fragmentName + " compiled w/ (" + defines.replace( "\n", ", " )
						+ ")" );
			} else {
				Gdx.app.log( "ShaderLoader", vertexName + "/" + fragmentName + " compiled!" );
			}
		}

		return shader;
	}

	private ShaderLoader() {
	}
}
