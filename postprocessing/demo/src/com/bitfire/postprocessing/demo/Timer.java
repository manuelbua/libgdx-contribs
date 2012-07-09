package com.bitfire.postprocessing.demo;

import com.badlogic.gdx.utils.TimeUtils;

public final class Timer {

	private static final float oneOnOneBillion = 1.0f / 1000000000.0f;
	private long nsStartTime;
	private boolean stopped;
	private long nsStopTime;
	private float elapsedSecs;

	/** Constructs a new Time object */
	public Timer() {
		reset();
	}

	/** Stops tracking */
	public void pause() {
		stopped = true;
		nsStopTime = TimeUtils.nanoTime();
	}

	/** Resumes/continues tracking, without resetting the accumulated state
	 * (should be called "continue" but can't */
	public void resume() {
		stopped = false;
	}

	/** Resets the internal state */
	public void reset() {
		stopped = false;

		// abs
		nsStartTime = TimeUtils.nanoTime();
		nsStopTime = 0;
	}

	public void update() {
		long now = (stopped ? nsStopTime : TimeUtils.nanoTime());
		elapsedSecs = (float)(now - nsStartTime) * oneOnOneBillion;
	};

	/** Returns the elapsed time in seconds. */
	public float elapsed() {
		return elapsedSecs;
	}
}
