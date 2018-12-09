package com.easyware.solitaire;

import java.io.File;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class SettingsDialogFragment extends DialogFragment {
	
    /* The activity that creates an instance of this dialog fragment must     
	* implement this interface in order to receive event callbacks.     
	* Each method passes the DialogFragment in case the host needs to query it. 
	*/    
	public interface SettingsDialogListener {        
		public void onSettingsPositiveClick(DialogFragment dialog);        
		public void onSettingsNegativeClick(DialogFragment dialog);    
	}        
	
	// Use this instance of the interface to deliver action events    
	private SettingsDialogListener mListener;        
	
	private CheckBox mCheckBoxPlaySound;
	private RadioButton mRadioButtonDrawOne;
	private RadioButton mRadioButtonGreen;
	private boolean mIsPlaySound;
	private boolean mIsDrawOne;
	private boolean mIsBackgroundGreen;
	
	public boolean isPlaySound() {
		return mCheckBoxPlaySound.isChecked();
	}
	public boolean isDrawOne() {
		return mRadioButtonDrawOne.isChecked();
	}
	public boolean isBackgroundGreen() {
		return mRadioButtonGreen.isChecked();
	}

   /**     
    * Create a new instance of MyDialogFragment, providing "autoCheck" and "autoHelp" as an argument.     
    **/    
	public static SettingsDialogFragment newInstance(boolean playSound, boolean drawOne, 
			boolean backgroundGreen) {        
		SettingsDialogFragment f = new SettingsDialogFragment();        
		// Supply "autoCheck" and "autoHelp" input as arguments       
		Bundle args = new Bundle();        
		args.putBoolean("play_sound", playSound);        
		args.putBoolean("draw_one", drawOne);        
		args.putBoolean("background_green", backgroundGreen);        
		f.setArguments(args);        
		return f;    
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get arguments
		mIsPlaySound = getArguments().getBoolean("play_sound");
		mIsDrawOne = getArguments().getBoolean("draw_one");
		mIsBackgroundGreen = getArguments().getBoolean("background_green");
	}
	
	// Override the Fragment.onAttach() method to instantiate the SettingsDialogListener    
	@Override    
	public void onAttach(Activity activity) {        
		super.onAttach(activity);        
		// Verify that the host activity implements the callback interface        
		try {            
			// Instantiate the NoticeDialogListener so we can send events to the host            
			mListener = (SettingsDialogListener) activity;        
		} 
		catch (ClassCastException e) {            
			// The activity doesn't implement the interface, throw exception            
			throw new ClassCastException(activity.toString() + " must implement SettingsDialogListener");        
		}    
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	
		// get the host activity
		Activity activity = getActivity();
		// Get the layout inflater
		LayoutInflater inflater = activity.getLayoutInflater();

		// inflate the view
		// Inflate and set the layout for the dialog    
		// Pass null as the parent view because its going in the dialog layout    
		View view = inflater.inflate(R.layout.settings, null);
		
		// dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(view)
			   .setTitle(R.string.settings)
				
			   // Add action buttons           
				
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {               
			    	
			    	@Override               
			    	public void onClick(DialogInterface dialog, int id) {     
			    		// Send the positive button event back to the host activity
			    		mListener.onSettingsPositiveClick(SettingsDialogFragment.this);
			    	}           
		    	})           
		    	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {               

		    		@Override               
		    		public void onClick(DialogInterface dialog, int id) {  
		    			// Send the negative button event back to the host activity
			    		mListener.onSettingsNegativeClick(SettingsDialogFragment.this);
		    		}           
	    		});          
    
		mCheckBoxPlaySound = (CheckBox)view.findViewById(R.id.cbPlaySound);
		mRadioButtonDrawOne = (RadioButton)view.findViewById(R.id.rbDrawOne);
		final RadioButton radioButtonDrawThree = (RadioButton)view.findViewById(R.id.rbDrawThree);
		mRadioButtonGreen = (RadioButton)view.findViewById(R.id.rbGreen);
		final RadioButton radioButtonBlue = (RadioButton)view.findViewById(R.id.rbBlue);
		mCheckBoxPlaySound.setChecked(mIsPlaySound);
		if (mIsDrawOne)
			mRadioButtonDrawOne.setChecked(true);
		else
			radioButtonDrawThree.setChecked(true);
		if (mIsBackgroundGreen)
			mRadioButtonGreen.setChecked(true);
		else
			radioButtonBlue.setChecked(true);
		
		return builder.create();
    }
	
	public void removeFile(String filename) {
		File file = new File(getActivity().getFilesDir(), filename);
		file.delete();
	}
}
