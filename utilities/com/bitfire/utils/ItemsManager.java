package com.bitfire.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class ItemsManager<T extends Disposable> implements Disposable {
	private static final int ItemNotFound = -1;

	public final Array<T> items = new Array<T>();
	protected final Array<Boolean> owned = new Array<Boolean>();

	@Override
	public void dispose() {
		for( int i = 0; i < items.size; i++ ) {
			if( owned.get( i ) ) {
				items.get( i ).dispose();
			}
		}

		items.clear();
		owned.clear();
	}

	/** Add an item to the manager, if own is true the manager will manage the resource's lifecycle */
	public void add( T item, boolean own ) {
		if( item == null ) {
			return;
		}

		items.add( item );
		owned.add( own );
	}

	/** Add an item to the manager and transfer ownership to it */
	public void add( T item ) {
		add( item, true );
	}

	/** Removes a previously added resource */
	public void remove( T item ) {
		int index = items.indexOf( item, true );
		if( index == ItemNotFound ) {
			return;
		}

		if( owned.get(index) ) {
			items.get( index ).dispose();
		}

		items.removeIndex( index );
		owned.removeIndex( index );
		items.removeValue( item, true );
	}
}
