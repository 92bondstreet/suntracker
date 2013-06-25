package com.sun.tracker;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SolAbout extends Dialog implements OnClickListener{

	private Button AboutOkButton;
	private Button AboutMarketButton;
	private TextView SolVersionTextView;
	
	private String versionName = "1.0";

	public SolAbout(Context context, String versionName) {
		super(context);
		// TODO Auto-generated constructor stub
		this.versionName = versionName;
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		AboutOkButton = (Button) findViewById(R.id.AboutOk);
		AboutOkButton.setOnClickListener(this);
		
		SolVersionTextView = (TextView) findViewById(R.id.SolVersion);
		SolVersionTextView.setText(versionName);
		
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		switch(arg0.getId()){

		case R.id.AboutOk:{
			dismiss();
			return;
		}
		}
	}
}