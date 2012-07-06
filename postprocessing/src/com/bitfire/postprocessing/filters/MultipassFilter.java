package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.PingPongBuffer;

/** The base class for any multi-pass filter. */
public abstract class MultipassFilter {
	public abstract void rebind();
	public abstract void render( PingPongBuffer srcdest );
}
