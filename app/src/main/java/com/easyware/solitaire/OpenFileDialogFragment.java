package com.easyware.solitaire;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class OpenFileDialogFragment extends DialogFragment {

	/* The activity that creates an instance of this dialog fragment must     
	* implement this interface in order to receive event callbacks.     
	* Each method passes the DialogFragment in case the host needs to query it. 
	*/    
	public interface OpenFileDialogListener {        
		public void onOpenFileItemClicked(final String filename);        
	}        
	
	// Use this instance of the interface to deliver action events    
	private OpenFileDialogListener mListener;       
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);        
		// Verify that the host activity implements the callback interface        
		try {            
			// Instantiate the NoticeDialogListener so we can send events to the host            
			mListener = (OpenFileDialogListener) activity;        
		} 
		catch (ClassCastException e) {            
			// The activity doesn't implement the interface, throw exception            
			throw new ClassCastException(activity.toString() + " must implement OpenFileDialogListener");        
		}    
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// get activity
		final Activity activity = getActivity();
		// dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.load_game);
		
		// get all internal saved game files
		final String[] filenames = activity.fileList();
		
		if (filenames == null || filenames.length == 0) {
			builder.setMessage(R.string.no_saved_game)
			       .setNeutralButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
			       });
		}
		else {
  		    // show file list           
			builder.setItems(filenames, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String filename = filenames[which];
						mListener.onOpenFileItemClicked(filename);
					}
				});          
		}

		Dialog dlg = builder.create();
		dlg.setCanceledOnTouchOutside(true);
		
		return dlg;
	}	
}
