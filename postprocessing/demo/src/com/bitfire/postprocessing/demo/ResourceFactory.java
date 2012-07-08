package com.bitfire.postprocessing.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.LongMap;
import com.bitfire.utils.Hash;
import com.bitfire.utils.ItemsManager;

public final class ResourceFactory {
	public static boolean DebugUI = true;
	private static Skin UISkin = new Skin( Gdx.files.internal( "data/uiskin.json" ) );
	private static ItemsManager<Texture> textures = new ItemsManager<Texture>();
	private static LongMap<Texture> textureCache = new LongMap<Texture>();

	// graphics

	public static Texture newTexture( String name, boolean mipMap ) {
		long hash = Hash.APHash( name );
		Texture t = textureCache.get( hash );

		if( t != null ) {
			Gdx.app.log( "ResourceFactory", "Cache hit for \"" + name + "\"" );
			// cache hit
			return t;
		}

		t = new Texture( Gdx.files.internal( "data/" + name ), Format.RGBA8888, mipMap );

		if( mipMap ) {
			t.setFilter( TextureFilter.MipMapLinearNearest, TextureFilter.Nearest );
		} else {
			t.setFilter( TextureFilter.Nearest, TextureFilter.Nearest );
		}

		textures.add( t );
		textureCache.put( hash, t );

		return t;
	}

	public static Sprite newSprite( String textureName ) {
		Sprite s = new Sprite( newTexture( textureName, false ) );
		s.flip( false, true );
		return s;
	}

	// ui

	public static Slider newSlider( float min, float max, float step, float value, ChangeListener listener ) {
		Slider s = new Slider( min, max, step, UISkin );
		s.setValue( value );
		s.addListener( listener );
		return s;
	}

	public static CheckBox newCheckBox( String text, boolean checked, ClickListener listener ) {
		CheckBox cb = new CheckBox( text, UISkin );
		cb.setChecked( checked );
		cb.addListener( listener );
		return cb;
	}

	public static SelectBox newSelectBox( Object[] items, ChangeListener listener ) {
		SelectBox sb = new SelectBox( items, UISkin );
		sb.addListener( listener );
		return sb;
	}

	public static Label newLabel( String text ) {
		Label l = new Label( text, UISkin );
		return l;
	}

	public static Table newTable() {
		Table t = new Table();
		if( DebugUI ) {
			t.debug();
		}
		return t;
	}

	public static void dispose() {
		textures.dispose();
		textureCache.clear();
	}

	private ResourceFactory() {
	}
}
