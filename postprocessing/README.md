## libgdx-contribs-postprocessing
A lightweight, GLES2-only library to ease development and inclusion of post-processing effects in libgdx applications and games.

## Usage

It's usually simple to add post-processing effects to your own application but, depending on the complexity of your rendering stage and the rendering states you are using, you may be required to track state changes in some cases (see how the Bloom effect [saves](https://github.com/manuelbua/libgdx-contribs/blob/master/postprocessing/src/com/bitfire/postprocessing/effects/Bloom.java#L221) and [restores](https://github.com/manuelbua/libgdx-contribs/blob/master/postprocessing/src/com/bitfire/postprocessing/effects/Bloom.java#L235-237) the OpenGL blending state).

## Basic example
Suppose you want to add a *bloom* effect to your libgdx application and that the following is your original source code (this is just an example, it will not compile!):

``` java
public class YourApplication implements ApplicationListener, ... {

   private static final boolean isDesktop = (Gdx.app.getType() == ApplicationType.Desktop);

    @Override
    public void create() {
        yourCreateCode();
    }

    @Override
    public void dispose() {
        yourDisposeCode();
    }

    @Override
    public void resume() {
        yourResumeCode();
    }

    @Override
    public void render() {
        yourUpdateScene();
        yourRenderScene();
    }

    private void yourUpdateScene() {
        yourGameLogic.update();
    }

    private void yourRenderScene() {
        // draw game sprites
        batch.begin();
        yourBatchRenderer( batch );
        batch.end();

        // draw ui
        ui.draw();
    }
}
```
First thing first, we should point the ShaderLoader to where you are going to put the accompaining shader code from the *shaders/* folder in this repository, else it will not find any shader support code: it's usually a good habit to put your resources in the **data** folder of your project, so just copy or soft-link the *shaders* folder right there.

Then, the first thing we are going to create is an instance of the *PostProcessor* object, its role will be to manage one or more effects for us: also create the *Bloom* effect itself so that we can add it to the post-processor, note that in doing so we are also *transferring the ownership* of the effect object to the post-processor itself.

```java
    @Override
    public void create() {
        yourCreateCode();
        ShaderLoader.BasePath = "data/shaders/";
        postProcessor = new PostProcessor( false, false, isDesktop );
        Bloom bloom = new Bloom( (int)(Gdx.graphics.getWidth() * 0.25f), (int)(Gdx.graphics.getHeight() * 0.25f) );
        postProcessor.addEffect( bloom );
    }
```

Basically, we are using the *PostProcessor* default constructor to create a color buffer with the same size as the one of your application screen, this is where the original scene will be rendered to, so that the effects will be able to access a full-resolution copy of the scene: next, since we are not going to use the depth buffer nor alpha/blending, we specify false for both of these flags and finally, we use 32 bits-per-pixel precision only on the desktop.

The default constructor will assume the viewport size to be the same as your window: should you require a custom viewport, you can specify one either in the overloaded constructor or by invoking the *setViewport* method.

Then we are creating a *Bloom* effect instance: this object will create an internal buffer for storing intermediate image computations and here we are specifying the pixel dimensions of this internal buffer to be &frac14; of the original application size. This size choice has a double effect: *it's beneficial to the performance*, since the kernel filter will run on a lot less pixels, *and the result will be much smoother*, since the bundled Blur *filter* is going to exploit the hardware bilinear filtering capabilities by using some specially pre-computed weights and offsets, taken from a binomial distribution, that will play nice with texture lookups (*i wrote a tool to generate those magic numbers so i may push it to the repo whenever i'll get the time to clean it up :*).

Remember to be nice on allocated resources, so it's goot to release them as soon as the application gives us the chance: now, do you remember the previously added effect and its ownership being transferred? The post-processor is now the *owner* of that instance and it will manage the cleanup for you by just invoking `dispose()` on it:

```java
    @Override
    public void dispose() {
        yourDisposeCode();
        postProcessor.dispose();
    }
```

We are not quite ready yet, due to something called *OpenGL context loss*: although this is something that does not affect desktop applications but only Android, it's always a good thing to know the problem and how to handle it, so if you have some spare time, invest it wisely and read [this excellent write up by Mario](http://www.badlogicgames.com/wordpress/?p=1073) at Badlogic Games.

Once you read it, you'll understand that the PostProcessor needs then a way to recreate the resources and possibly rebind shaders' parameters as well, since they get invalidated and need to be reloaded.
The PostProcessor object can handle all of this for you by invoking the `rebind()` method whenever the OpenGL context is recreated (in Android terms, the Activity gets resumed):

```java
    @Override
    public void resume() {
        yourResumeCode();
        postProcessor.rebind();
    }

```

Finally, we can now tell the post-processor when the actual scene drawing occurs, and it will take care of the rest:

```java
    @Override
    public void render() {
        yourUpdateScene();

        postProcessor.capture();
        yourRenderScene();
        postProcessor.render();
    }
```

Easy, uh? Just one more note: in case you didn't want to have your UI post-processed as well, you may want to alter your rendering code a bit differently, just exclude from the postprocessor's capture/render block and render it after everything has been put on screen, so instead of modifying the `render()` method we are going to act on the real meat:

```java
    private void yourRenderScene() {

        postProcessor.capture();

        // draw game sprites
        batch.begin();
        yourBatchRenderer( batch );
        batch.end();

        postProcessor.render();

        // draw ui
        ui.draw();
    }
```

**Now go and create awesome effects!**

## Known issues

* **Error: Java.Lang.NoClassDefFoundError: com.bitfire.utils.ShaderLoader. (Android)**
* **More obscure Dex-related errors (Android)**

Thanks to CatalystNZ for figuring this out.
It looks like the fix is pretty simple: one should track dependency usage in the projects and the projects it depends on, and carefully avoid to have both projects reference the same source.

So referencing *gdx.jar* is enough and **DO NOT** link any gdx source in the contribs project (else, reference the sources and DO NOT use the .jar files anywhere).

## Notes
Probably quite a few other bugs live in there :)
Please [report them](https://github.com/manuelbua/libgdx-contribs/issues) on github!
