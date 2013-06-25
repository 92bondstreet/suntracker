package com.sun.tracker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class SolMessageManager{

	private Context currentContext;
	private Activity myCurrentActivity;

	// PROGRESS DIALOG
	private ProgressDialog progressCurrentState;
	private ProgressDialog downloadCurrentState;
	private String currentMessage;
	
	private Toast toastCurrentState;

	public SolMessageManager(Activity context)
	{
		myCurrentActivity = context;
		downloadCurrentState= new ProgressDialog(myCurrentActivity);
	}

	/*
	 *    PROGRESS
	 */
	
	public void showProgressDownload(String message){
		
		downloadCurrentState.setProgress(0);
		downloadCurrentState.setSecondaryProgress(50);
		downloadCurrentState.setMax(100);
		downloadCurrentState.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadCurrentState.setMessage(message);
		downloadCurrentState.setCancelable(false);
		downloadCurrentState.show();
	}
	
	public void updateProgressDownload(int delta){
		
		downloadCurrentState.incrementProgressBy(delta);
	}
	
	public void showProgress(String message){

		currentMessage = message;
		progressCurrentState = ProgressDialog.show(myCurrentActivity,
				"", message, true);
	}
	
	public void updateProgress(String message){

		currentMessage = message;
		if(progressCurrentState!=null){
		
			progressCurrentState.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressCurrentState.setMessage(message);
		}
		else
			progressCurrentState = ProgressDialog.show(myCurrentActivity,
				"", message, true);
	}
	
	public void hideProgress(){
		if(progressCurrentState!=null)
			progressCurrentState.hide();
	}
	
	public void hideProgressDownload(){
		if(downloadCurrentState!=null)
			downloadCurrentState.hide();
	}

	/*
	 *    TOAST
	 */

	public void showToast(String message, int length){
		
		if(progressCurrentState!=null)
			progressCurrentState.cancel();
		if(toastCurrentState!=null)
			toastCurrentState.cancel(); 
		toastCurrentState = Toast.makeText(myCurrentActivity, message, length);
		toastCurrentState.show();
	}
	
	public void hide(){
		if(progressCurrentState!=null)
			progressCurrentState.hide();
		if(toastCurrentState!=null)
			toastCurrentState.cancel();
	}
	
	
	 /**
	   * Shows a dialog with the given message.
	   * Does it on the UI thread.
	   *
	   * @param success if true, displays an info icon/title, otherwise an error
	   *        icon/title
	   * @param message resource string id
	   */
	  public void showCloseApp(final String message, final String title, final String button) {
		  myCurrentActivity.runOnUiThread(new Runnable() {
	      public void run() {
	    	  if(progressCurrentState!=null)
					progressCurrentState.cancel();
	    	  
	        AlertDialog dialog = null;
	        AlertDialog.Builder builder = new AlertDialog.Builder(myCurrentActivity);
	        builder.setMessage(message);
	        builder.setTitle(title);
	        builder.setNeutralButton(button,new OnClickListener() {
				
				//@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					myCurrentActivity.finish();
				}
			});
			
	        dialog = builder.create();
	        dialog.show();
	      }
	    });
	  }
}