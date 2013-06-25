package com.sun.tracker;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

class CustomItem extends OverlayItem {
	
		Drawable marker=null;
	
		CustomItem(GeoPoint pt, String name, String snippet,
							 Drawable marker) {
			super(pt, name, snippet);
			this.marker=marker;
		}

		@Override
		public Drawable getMarker(int stateBitset) {
			Drawable result=this.marker;

			setState(result, stateBitset);
			return(result);
		}

	}