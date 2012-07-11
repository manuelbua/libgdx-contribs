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

	/**
	 * Resumes/continues tracking, without resetting the accumulated state
	 * (should be called "continue" but can't)
	 */
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
