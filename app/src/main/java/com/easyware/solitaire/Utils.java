package com.easyware.solitaire;

import android.content.Context;
import android.media.MediaPlayer;

public class Utils {
	// Play the sound using android.media.MediaPlayer
	public static void playSound(Context context, int soundID){       
		MediaPlayer mp = MediaPlayer.create(context, soundID);  
		mp.start();
	}
}
