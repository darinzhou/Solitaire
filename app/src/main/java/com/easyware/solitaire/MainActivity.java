package com.easyware.solitaire;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

public class MainActivity extends FragmentActivity
							implements PopupMenu.OnMenuItemClickListener,
									SettingsDialogFragment.SettingsDialogListener,
									OpenFileDialogFragment.OpenFileDialogListener,
									SaveFileDialogFragment.SaveFileDialogListener
{
	public final static String APP_SETTINGS_PLAYSOUND = "APP_SETTINGS_PLAYSOUND";
	public final static String APP_SETTINGS_DRAWONE = "APP_SETTINGS_DRAWONE";
	public final static String APP_SETTINGS_GREEN = "APP_SETTINGS_GREEN";
	
	private SceneView mSceneView;
	private ImageButton mIbNewGame;
	private boolean mIsPlaySound = true;
	private boolean mIsDrawOne = false;;
	private boolean mIsBackgroundGreen = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		// app settings
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		mIsPlaySound = preferences.getBoolean(APP_SETTINGS_PLAYSOUND, false);
		mIsDrawOne = preferences.getBoolean(APP_SETTINGS_DRAWONE, false);
		mIsBackgroundGreen = preferences.getBoolean(APP_SETTINGS_GREEN, false);
		
		mSceneView = (SceneView)findViewById(R.id.sceneView);
		mSceneView.setMainActivity(this);
		mIbNewGame = (ImageButton)findViewById(R.id.ibNewGame);
		mIbNewGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNewGameButton(false);
				mSceneView.newGame();
			}
		});
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.miGame:
		    View menuGame = findViewById(R.id.miGame); // SAME ID AS MENU ID
		    PopupMenu popupMenu = new PopupMenu(this, menuGame); 
		    popupMenu.inflate(R.menu.game);
		    popupMenu.setOnMenuItemClickListener(this);
		    popupMenu.show();
		    break;
		case R.id.miSettings:
			SettingsDialogFragment settings = SettingsDialogFragment.newInstance(mIsPlaySound, 
					mIsDrawOne, mIsBackgroundGreen);
			settings.setCancelable(false);
			settings.show(getSupportFragmentManager(), "SOLITAIRE_SETTINGS_DIALOG");
			break;
		}
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		switch(arg0.getItemId()) {
		case R.id.miNewGame:
			showNewGameButton(false);
			mSceneView.newGame();
			break;
		case R.id.miLoad:
			OpenFileDialogFragment openFile = new OpenFileDialogFragment();
			openFile.setCancelable(true);
			openFile.show(getSupportFragmentManager(), "SOLITAIRE_OPEN_FILE_DIALOG");
			break;
		case R.id.miSave:
			SaveFileDialogFragment saveFile = new SaveFileDialogFragment();
			saveFile.setCancelable(false);
			saveFile.show(getSupportFragmentManager(), "SOLITAIRE_SAVE_FILE_DIALOG");
			break;
		case R.id.miDelete:
			DeleteGameDialogFragment deleteGame = new DeleteGameDialogFragment();
			deleteGame.setCancelable(true);
			deleteGame.show(getSupportFragmentManager(), "SOLITAIRE_DELETE_GAME_DIALOG");
			break;
		}
		return true;
	}

	public SceneView getSceneView() {
		return mSceneView;
	}
	
	public void showNewGameButton(boolean show) {
		mIbNewGame.setVisibility(show?View.VISIBLE:View.GONE);
	}

	public boolean isPlaySound() {
		return mIsPlaySound;
	}
	public boolean isDrawOne() {
		return mIsDrawOne;
	}
	public boolean isBackgroundGreen() {
		return mIsBackgroundGreen;
	}
	
	@Override
	public void onSettingsPositiveClick(DialogFragment dialog) {
		// backup old value
		boolean isBackgroundGreen = mIsBackgroundGreen;
		
		SettingsDialogFragment settings = (SettingsDialogFragment)dialog;
		mIsPlaySound = settings.isPlaySound();
		mIsDrawOne = settings.isDrawOne();
		mIsBackgroundGreen = settings.isBackgroundGreen();
		
		// save settings
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(APP_SETTINGS_PLAYSOUND, mIsPlaySound);
		editor.putBoolean(APP_SETTINGS_DRAWONE, mIsDrawOne);
		editor.putBoolean(APP_SETTINGS_GREEN, mIsBackgroundGreen);
		editor.commit();
		
		// if background need to be changed, change it 
		if (isBackgroundGreen != mIsBackgroundGreen) {
			getSceneView().changeAppearance(mIsBackgroundGreen);
		}
		getSceneView().setDrawOne(mIsDrawOne);
	}
	@Override
	public void onSettingsNegativeClick(DialogFragment dialog) {
	}
	
	@Override
	public void onSaveFilePositiveClick(String filename) {
		OutputStream outputStream = null;
		
		try {  
			outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			getSceneView().getSolitaire().save(outputStream);
		} 
		catch (Exception e) {  
			e.printStackTrace();
		}
	}
	@Override
	public void onSaveFileNegativeClick(DialogFragment dialog) {
	}

	@Override
	public void onOpenFileItemClicked(final String filename) {
		// need the user confirm to quit current game
		if (getSceneView().getSolitaire() != null && !getSceneView().getSolitaire().isEmpty()) {
			String msg = getResources().getString(R.string.sure_to_load_game);
	        new AlertDialog.Builder(this)
	        	.setMessage(msg)
	        	.setCancelable(false)
	        	.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// opening
						InputStream inputStream = null;
						
						try {  
							inputStream = openFileInput(filename);
							getSceneView().getSolitaire().load(inputStream, MainActivity.this);
							getSceneView().invalidate();
						} 
						catch (Exception e) {  
							e.printStackTrace();
						}
						
						if (getSceneView().getSolitaire() == null || getSceneView().getSolitaire().isEmpty()) {
							String msg = getResources().getString(R.string.invalid_game, filename);
					        new AlertDialog.Builder(MainActivity.this)
					        	.setMessage(msg)
					        	.setCancelable(false)
					        	.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										File file = new File(getFilesDir(), filename);
										file.delete();
									}
								})
								.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								})
								.show();
						}
					}
				})
				.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
		}				
	}

}
