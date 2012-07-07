package com.bitfire.postprocessing.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.bitfire.utils.ItemsManager;

public final class ResourceFactory {

	private static ItemsManager<Texture> textures = new ItemsManager<Texture>();

	public static Texture newTexture( String name, boolean mipMap ) {
		Texture t = new Texture( Gdx.files.internal( "data/" + name ), Format.RGBA8888, mipMap );

		if( mipMap ) {
			t.setFilter( TextureFilter.MipMapLinearNearest, TextureFilter.Nearest );
		} else {
			t.setFilter( TextureFilter.Nearest, TextureFilter.Nearest );
		}

		textures.add( t );
		return t;
	}

	public static Sprite newSprite( String textureName ) {
		Sprite s = new Sprite( newTexture( textureName, false ) );
		s.flip( false, true );
		return s;
	}

	public static void dispose() {
		textures.dispose();
	}

	private ResourceFactory() {
	}
}
