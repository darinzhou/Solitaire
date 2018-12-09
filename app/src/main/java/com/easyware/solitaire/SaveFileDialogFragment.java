package com.easyware.solitaire;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SaveFileDialogFragment extends DialogFragment {

	private EditText input;
	
	/* The activity that creates an instance of this dialog fragment must     
	* implement this interface in order to receive event callbacks.     
	* Each method passes the DialogFragment in case the host needs to query it. 
	*/    
	public interface SaveFileDialogListener {        
		public void onSaveFilePositiveClick(final String filename);        
		public void onSaveFileNegativeClick(DialogFragment dialog);    
	}        
	
	// Use this instance of the interface to deliver action events    
	private SaveFileDialogListener mListener;       
	
	public Editable getInput() {
		return input.getText();
	}
		
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);        
		// Verify that the host activity implements the callback interface        
		try {            
			// Instantiate the NoticeDialogListener so we can send events to the host            
			mListener = (SaveFileDialogListener) activity;        
		} 
		catch (ClassCastException e) {            
			// The activity doesn't implement the interface, throw exception            
			throw new ClassCastException(activity.toString() + " must implement SaveFileDialogListener");        
		}    
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// get activity
		final Activity activity = getActivity();
		// create the EditText control 
		input = new EditText(activity);  
		// dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.save_game)
			   .setMessage(R.string.save_game_msg)
			   .setView(input)
			   
			   // set action buttons
			   // positive button onClickListener will be override in setOnShowListener later
			   // here only set negative button behavior
			    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// behavior is defined in the override OnClickListener
					}
				})
		    	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {               

		    		@Override               
		    		public void onClick(DialogInterface dialog, int id) {  
		    			// Send the negative button event back to the host activity
			    		mListener.onSaveFileNegativeClick(SaveFileDialogFragment.this);
		    		}           
	    		});          
    
		final Dialog dlg =  builder.create();
		dlg.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				Button positiveButton = ((AlertDialog)dlg).getButton(AlertDialog.BUTTON_POSITIVE);
				positiveButton.setEnabled(input.getText().length() != 0);
				
				// override the behavior of positive button to NOT dismissing dialog by default
				positiveButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

			    		String filename = getInput().toString();
			    		File file = new File(activity.getFilesDir(),filename);
			    		if(file.exists()) {
			    			String msg = getResources().getString(R.string.file_exists, filename);
			    	        new AlertDialog.Builder(activity)
				            	.setMessage(msg)
				            	.setCancelable(false)
				            	.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				    				@Override
				    				public void onClick(DialogInterface dialog, int id) {
							    		// Send the positive button event back to the host activity
							    		mListener.onSaveFilePositiveClick(getInput().toString());
						    			// now dismiss the dialog by positive button
				    					dlg.dismiss();
				    				}
				    			})
				    			.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
				    				@Override
				    				public void onClick(DialogInterface dialog, int which) {
				    				}
				    			})
				    			.show();
			    		}
			    		else {
				    		// Send the positive button event back to the host activity
				    		mListener.onSaveFilePositiveClick(getInput().toString());
			    			// now dismiss the dialog by positive button
	    					dlg.dismiss();
			    		}
					}
				});
			}
			
		});

		// add text change listener for input
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				((AlertDialog)dlg).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() != 0);
			}
		});
		
		return dlg;
	}

}
