import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bitfire.utils.ItemsManager;

public class PostProcessingDemo implements ApplicationListener, InputProcessor {

	ItemsManager<Texture> textures = new ItemsManager<Texture>();
	Stage ui;
	Skin skin;

	@Override
	public void create() {
		Gdx.input.setInputProcessor( this );
		createScene();
	}

	private void createScene() {
		ui = new Stage();
		skin = new Skin( Gdx.files.internal( "data/uiskin.json" ) );

		Image bg = new Image( newTexture( "badlogic.jpg", false ) );
		bg.setFillParent( true );
		ui.addActor( bg );
	}

	private Texture newTexture( String name, boolean mipMap ) {
		Texture t = new Texture( Gdx.files.internal( "data/" + name ), Format.RGBA8888, mipMap );

		if( mipMap ) {
			t.setFilter( TextureFilter.MipMapLinearNearest, TextureFilter.Nearest );
		} else {
			t.setFilter( TextureFilter.Nearest, TextureFilter.Nearest );
		}

		textures.add( t );
		return t;
	}

	@Override
	public void dispose() {
		textures.dispose();
	}

	@Override
	public void render() {
		Gdx.gl20.glClearColor( 0, 0, 0, 1 );
		Gdx.gl20.glClear( GL20.GL_COLOR_BUFFER_BIT );

		ui.draw();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void resize( int width, int height ) {
	}

	@Override
	public boolean keyDown( int keycode ) {
		return false;
	}

	@Override
	public boolean keyUp( int keycode ) {
		return false;
	}

	@Override
	public boolean keyTyped( char character ) {
		return false;
	}

	@Override
	public boolean touchDown( int x, int y, int pointer, int button ) {
		return false;
	}

	@Override
	public boolean touchUp( int x, int y, int pointer, int button ) {
		return false;
	}

	@Override
	public boolean touchDragged( int x, int y, int pointer ) {
		return false;
	}

	@Override
	public boolean mouseMoved( int x, int y ) {
		return false;
	}

	@Override
	public boolean scrolled( int amount ) {
		return false;
	}

}
