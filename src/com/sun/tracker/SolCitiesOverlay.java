package com.sun.tracker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.sun.tracker.parser.City;
import com.sun.tracker.utils.SolUtils;

public class SolCitiesOverlay extends ItemizedOverlay<OverlayItem>{

	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private Context context = null;
	
	private Drawable marker_code_1 = null;
	private Drawable marker_code_2 = null;
	private Drawable marker_code_3 = null;
	
	private Drawable marker_my_position = null;
	
	public SolCitiesOverlay(Context context,Drawable defaultMarker) {
		
		super(defaultMarker);
		// TODO Auto-generated constructor stub
		
		this.context = context;
		
		// init marker
		marker_code_1 = getMarker(R.drawable.sol_local_code_1);
		marker_code_2 = getMarker(R.drawable.sol_local_code_2);
		marker_code_3 = getMarker(R.drawable.sol_local_code_3);
		
		marker_my_position = getMarker(android.R.drawable.ic_menu_myplaces);
	}
	
	
	public void addItem(GeoPoint onecity ) {
		//super(defaultMarker);
		// TODO Auto-generated constructor stub
		
		OverlayItem currentItem = new OverlayItem(onecity,"current_position",null);
		currentItem.setMarker(marker_my_position);
		items.add(currentItem);
		
		populate();
	}
	
	public void addItems(ArrayList<City> cities ) {
		//super(defaultMarker);
		// TODO Auto-generated constructor stub
		
		for(City current:cities){
			
			OverlayItem currentItem = new OverlayItem(SolUtils.makeGeoPoint(Double.parseDouble(current.latitude),Double.parseDouble(current.longitude)),
					current.name ,current.name);
			switch(current.code){
			case 1:
				currentItem.setMarker(marker_code_1);
				break;
			case 2:
				currentItem.setMarker(marker_code_2);
				break;
			case 3:
				currentItem.setMarker(marker_code_3);
				break;
			default:
				currentItem.setMarker(marker_code_3);
				break;
			}
			items.add(currentItem);
		}
		
		populate();
	} 
	
	public void clearItems(){
		items.clear();
	}

	
	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return items.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
		//boundCenter(marker);
	}

	private Drawable getMarker(int resource) {
		Drawable marker=this.context.getResources().getDrawable(resource);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
											marker.getIntrinsicHeight());
		boundCenter(marker);

		return(marker);
	}
	
	@Override
	protected boolean onTap(int index) {
		// TODO Auto-generated method stub
		String snippet = items.get(index).getSnippet();
		if(snippet!=null)
			Toast.makeText(context, items.get(index).getSnippet(),Toast.LENGTH_SHORT).show();
		return true;
	}
	
	
	
	
}