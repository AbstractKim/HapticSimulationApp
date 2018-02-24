package com.github.abstractkim.hapticsimulation;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;


import java.util.HashMap;

import lombok.Data;
import lombok.NonNull;


/**
 * Created by peter on 2/22/18.
 *    - get Media we want using filter (ex. wav)
 *    - update hashmap for media list
 */

@Data
//Singleton with static factory
public class MediaManager {
    public static final String TAG = "MediaManager";
    public static final String MIME_TYPE_FILTER_WAV = "wav";
    private static final MediaManager INSTANCE = new MediaManager();

    //Map<Key, Value> will help us to find id to play...
    HashMap<String, Media> mediaMap = new HashMap<String, Media>();
    MediaPlayer mediaPlayer;

    private MediaManager(){}
    public static MediaManager getInstance(){
        return INSTANCE;
    }
    //just put data.. because each key in a HashMap must be unique. the old value is replaced..
    public MediaManager updateMediaMapFromContentResolver(Context context, String mimeTypeFilter){
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if(cursor == null){
            Log.d(TAG, "cursor is null. query error");
        }else if(!cursor.moveToFirst()){
            Log.d(TAG, "nod media on the device");
        } else {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);

            do {
                String title = cursor.getString(titleColumn);
                Long id = cursor.getLong(idColumn);
                String mimeType = cursor.getString(mimeTypeColumn);
                if(mimeTypeFilter.equals("none") || mimeType.toLowerCase().contains(mimeTypeFilter.toLowerCase())){

                    mediaMap.put(title, new Media(title, id, mimeType));
                    StringBuilder stringBuilder =
                            new StringBuilder("Title : ").append(title)
                                    .append("\nID : ").append(id)
                                    .append("\nMimeType : ").append(mimeType);
                    Log.d(TAG,stringBuilder.toString());
                    //Log.d("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
                }


            } while(cursor.moveToNext());
        }
        return this;
    }

    public void playMedia(@NonNull final Context context, final Long id){
        if(null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(context, ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
        if(null != mediaPlayer) {
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
        }else
            throw new NullPointerException("mediaPlayer is null");

    }

    public Long getIdFromSharedPreference(@NonNull final Context context, final String key) {
        String title = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, context.getString(R.string.pref_default_value));
        if(title.equals(context.getString(R.string.pref_default_value)))
            return new Long(-1);

        Log.d(TAG, new StringBuffer("on getIdFromSharedPreference: get title:" )
                .append(title).append("from key(").append(key).append(")").toString());
        Long id = mediaMap.get(title).getId();
        Log.d(TAG, new StringBuffer("on getIdFromSharedPreference: get id:" )
                .append(id).append("from title(").append(title).append(")").toString());
        return id;
    }

    public void setAudioStreamVolume(@NonNull AudioManager audioManager, int setIndex){
        final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float ratio = maxVolume / 10;
        int adjustedIndex = (int)(setIndex * ratio);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustedIndex, 0);
        Log.d(TAG, new StringBuffer("setAudioStreamVolume()-").append("max:").append(maxVolume)
                .append(", setIndex:").append(setIndex)
                .append(", adjustedIndex:").append(adjustedIndex).toString());
    }

}
