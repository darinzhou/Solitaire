package com.easyware.solitaire;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DeleteGameDialogFragment extends DialogFragment {
	private boolean mIsSelectAll;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	
		// get the host activity
		Activity activity = getActivity();
		// Get the layout inflater
		LayoutInflater inflater = activity.getLayoutInflater();

		// inflate the view
		// Inflate and set the layout for the dialog    
		// Pass null as the parent view because its going in the dialog layout    
		View view = inflater.inflate(R.layout.delete_game, null);
		
		// manage saved games
		
		// get all internal saved game files
		String[] filenames = activity.fileList();
		final ArrayList<String> fileList = new ArrayList<String>();
		for (String fn : filenames)
			fileList.add(fn);
		
		final TextView tvNoSaveGames = (TextView)view.findViewById(R.id.tvNoSavedGames);
		final LinearLayout llSavedGames = (LinearLayout)view.findViewById(R.id.llSavedGames);
		
		final Button btnRemove = (Button)view.findViewById(R.id.btnRemove);
		final Button btnSelectAll = (Button)view.findViewById(R.id.btnSelectAll);
		mIsSelectAll = true;
		
		final ListView lvSavedGames = (ListView)view.findViewById(R.id.lvSavedGames);
		
		if (fileList.size() == 0) {
			llSavedGames.setVisibility(View.GONE);
			tvNoSaveGames.setVisibility(View.VISIBLE);
		}
		else {
			llSavedGames.setVisibility(View.VISIBLE);
			tvNoSaveGames.setVisibility(View.GONE);
			// list events
			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_multiple_choice, fileList);
			lvSavedGames.setAdapter(adapter);
	        lvSavedGames.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        
	        // button remove events
	        btnRemove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					ArrayList<String> deleteList = new ArrayList<String>();
					int count = lvSavedGames.getAdapter().getCount();
					for (int i=0; i<count; ++i) {
						if (lvSavedGames.isItemChecked(i)) {
							deleteList.add(fileList.get(i));
						}
					}
					for (String fn : deleteList) {
						removeFile(fn);
						fileList.remove(fn);
					}
					adapter.notifyDataSetChanged();

					if (fileList.size() == 0) {
						llSavedGames.setVisibility(View.GONE);
						tvNoSaveGames.setVisibility(View.VISIBLE);
					}
				}
	        });
	        
	        // button select all
	        btnSelectAll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int count = lvSavedGames.getAdapter().getCount();
					for (int i=0; i<count; ++i) {
						lvSavedGames.setItemChecked(i, mIsSelectAll);
					}
					
					mIsSelectAll = !mIsSelectAll;
					btnSelectAll.setText(mIsSelectAll ? R.string.button_select_all : R.string.button_deselect_all);
				}
	        });
		}
		
		// dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(view)
			   .setTitle(R.string.delete_game);

		Dialog dlg = builder.create();
		dlg.setCanceledOnTouchOutside(true);
		
		return dlg;
    }
	
	public void removeFile(String filename) {
		File file = new File(getActivity().getFilesDir(), filename);
		file.delete();
	}
}
