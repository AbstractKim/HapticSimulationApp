package com.github.abstractkim.hapticsimulation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lukedeighton.wheelview.WheelView;
import com.lukedeighton.wheelview.adapter.WheelArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
   private static final int MY_PERMISSIONS_REQUEST_CODE = 100;
   private static final int ITEM_COUNT = 8;
   private static final String TAG = "MainActivity";
   MediaManager mediaManager;
   List<ViewSlotEntry> viewSlotEntries;
   List<JogWheelButtonAndImage> jogWheelButtonAndImages;
   String currentSlotKey;
   String currentSlotKeyForJog;
   SeekBar seekBar;



  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mediaManager = MediaManager.getInstance();
    setupPermissions();
  }

    private void init() {
        mediaManager.updateMediaMapFromContentResolver(this,
                MediaManager.MIME_TYPE_FILTER_WAV);

        viewSlotEntries = Arrays.asList(
                new ViewSlotEntry(findViewById(R.id.imageviewSlot1), getString(R.string.pref_slot1_key)),
                new ViewSlotEntry(findViewById(R.id.imageviewSlot2), getString(R.string.pref_slot2_key)),
                new ViewSlotEntry(findViewById(R.id.imageviewSlot3), getString(R.string.pref_slot3_key)),
                new ViewSlotEntry(findViewById(R.id.imageviewSlot4), getString(R.string.pref_slot4_key))
        );

        for(ViewSlotEntry viewSlotEntry : viewSlotEntries){
            viewSlotEntry.getImageView().setOnTouchListener( (view, motionEvent ) -> {
                String key = viewSlotEntry.getKey();
                Long id = mediaManager.getIdFromSharedPreference(this, key);
                if(id == -1) {
                    Toast.makeText(this, R.string.error_slot_not_assigned, Toast.LENGTH_LONG).show();
                    return false;
                }
                currentSlotKey = currentSlotKeyForJog = key;

                playMedia(id);
                setImage(viewSlotEntry);
                return false;
            });
        }


//        jogWheelButtonAndImages = Arrays.asList(
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_0), R.drawable.jog_0, false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_45), R.drawable.jog_45, false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_90), R.drawable.jog_90, false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_135), R.drawable.jog_135,false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_180), R.drawable.jog_180,false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_225), R.drawable.jog_225,false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_270), R.drawable.jog_270,false),
//                new JogWheelButtonAndImage(findViewById(R.id.imageButtonJog_315), R.drawable.jog_315,false)
//        );


        JogWheelButtonAndImage.init();
        StringBuilder degreesString = new StringBuilder("Degree:[");
        for(Double degree: JogWheelButtonAndImage.degrees){
            degreesString.append(degree).append(",");
        }
        StringBuilder radiansString = new StringBuilder("Radians:[");
        for(Double radians: JogWheelButtonAndImage.radians){
            radiansString.append(radians).append(",");
        }
        Log.d(TAG, degreesString.toString());
        Log.d(TAG, radiansString.toString());

        final ImageButton imageButtonForJog = findViewById(R.id.wheelview);
        Log.d(TAG, "Height:" + imageButtonForJog.getHeight() + ", Width: " + imageButtonForJog.getWidth());
        imageButtonForJog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Log.d(TAG, "Height:" + imageButtonForJog.getHeight() + ", Width: " + imageButtonForJog.getWidth());

                int centerX = imageButtonForJog.getWidth() / 2;
                int centerY = imageButtonForJog.getHeight() / 2;
                int touchX = (int)motionEvent.getX();
                int touchY = (int)motionEvent.getY();

                Log.d(TAG, "Center(" + centerX  + ", " + centerY + ")");
                Log.d(TAG, "Touch(" + touchX  + ", " + touchY + ")");

                int deltaX = - (touchX - centerX);
                int deltaY = -(touchY - centerY);

                double radian = Math.atan2(deltaX, deltaY);
                if (radian < 0)
                    radian = Math.abs(radian);
                else
                    radian = 2 * Math.PI - radian;
                Log.d(TAG, "Radian(" + radian + ")");

                int index = JogWheelButtonAndImage.getIndex(radian);
                Log.d(TAG, "Index(" + index + ")");

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        JogWheelButtonAndImage.setFocused(index);
                        imageButtonForJog.setImageResource(JogWheelButtonAndImage.resourceIDs.get(index));
                        if(currentSlotKeyForJog == null){
                            Toast.makeText(MainActivity.this, R.string.error_slot_not_assigned, Toast.LENGTH_LONG).show();
                            return false;
                        }
                        Long id = mediaManager.getIdFromSharedPreference(MainActivity.this, currentSlotKeyForJog);
                        playMedia(id);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!JogWheelButtonAndImage.focused[index]){
                            JogWheelButtonAndImage.setFocused(index);
                            imageButtonForJog.setImageResource(JogWheelButtonAndImage.resourceIDs.get(index));
                            if(currentSlotKeyForJog == null){
                                Toast.makeText(MainActivity.this, R.string.error_slot_not_assigned, Toast.LENGTH_LONG).show();
                                return false;
                            }
                            Long id_ = mediaManager.getIdFromSharedPreference(MainActivity.this, currentSlotKeyForJog);
                            playMedia(id_);
                        }
                        break;
                }

                return false;
            }
        });


//            jogWheelButtonAndImage.getImageButton().setOnClickListener(view -> {
//                imageViewForJog.setImageResource(jogWheelButtonAndImage.getImageResourceId());
//                if(currentSlotKeyForJog == null){
//                    Toast.makeText(this, R.string.error_slot_not_assigned, Toast.LENGTH_LONG).show();
//                    return;
//                }
//                Long id = mediaManager.getIdFromSharedPreference(this, currentSlotKeyForJog);
//                playMedia(id);
//        });

        /*
        findViewById(R.id.imagebuttonJog).setOnClickListener(view -> {
                if(null != currentSlotKey)
                    playMedia(mediaManager.getIdFromSharedPreference(this, currentSlotKey));
                else
                    Toast.makeText(this,getString(R.string.error_not_selected), Toast.LENGTH_LONG).show();
        });
        */



        seekBar = (SeekBar)findViewById(R.id.seekbarVolume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int progressChangedValue;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeVolume(progressChangedValue);
                if(null != currentSlotKey)
                    playMedia(mediaManager.getIdFromSharedPreference(MainActivity.this, currentSlotKey));
                else
                    Toast.makeText(MainActivity.this,getString(R.string.error_not_selected), Toast.LENGTH_LONG).show();
            }


        });

        //switch - radio button
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(R.id.radio_off == checkedId){
                Long id = mediaManager.getIdFromSharedPreference(this, "slot5");
                if(id == -1) {
                    Toast.makeText(this, R.string.error_slot_not_assigned, Toast.LENGTH_LONG).show();
                    return;
                }
                currentSlotKey = "slot5";

                playMedia(id);
                findViewById(R.id.radio_off).setBackgroundResource(R.drawable.switch_off_off);
                findViewById(R.id.radio_on).setBackgroundResource(R.drawable.switch_off_on);
            }else if(R.id.radio_on == checkedId){
                Long id = mediaManager.getIdFromSharedPreference(this, "slot6");
                if(id == -1) {
                    Toast.makeText(this, R.string.error_slot_not_assigned, Toast.LENGTH_LONG).show();
                    return;
                }
                currentSlotKey = "slot6";

                playMedia(id);
                findViewById(R.id.radio_off).setBackgroundResource(R.drawable.switch_on_off);
                findViewById(R.id.radio_on).setBackgroundResource(R.drawable.switch_on_on);
            }

        });
        RadioButton radioButtonOff = findViewById(R.id.radio_off);
        radioButtonOff.setOnClickListener((view)-> {
            Toast.makeText(MainActivity.this, "off", Toast.LENGTH_SHORT);
            Log.d(TAG, "off");
        });
        RadioButton radioButtonOn = findViewById(R.id.radio_on);
        radioButtonOn.setOnClickListener((view)-> {
            Toast.makeText(MainActivity.this, "on", Toast.LENGTH_SHORT);
            Log.d(TAG, "on");
        });

    }


    private void changeVolume(int index) {
            mediaManager.setAudioStreamVolume(
                    (AudioManager)getSystemService(Context.AUDIO_SERVICE), index);
        }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(null == seekBar)
                        return true;
                    int index = seekBar.getProgress();
                    if(index < 10){
                        index++;
                        seekBar.setProgress(index);
                        changeVolume(index);
                        if(null != currentSlotKey)
                            playMedia(mediaManager.getIdFromSharedPreference(MainActivity.this, currentSlotKey));
                        else
                            Toast.makeText(MainActivity.this,getString(R.string.error_not_selected), Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG, "KEYCODE_VOLUME_UP");
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(null == seekBar)
                        return true;
                    int index = seekBar.getProgress();
                    if(index > 0){
                        index--;
                        seekBar.setProgress(index);
                        seekBar.setProgress(index);
                        changeVolume(index);
                        if(null != currentSlotKey)
                            playMedia(mediaManager.getIdFromSharedPreference(MainActivity.this, currentSlotKey));
                        else
                            Toast.makeText(MainActivity.this,getString(R.string.error_not_selected), Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG, "KEYCODE_VOLUME_DOWN");
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void setImage(ViewSlotEntry myViewSlotEntry) {
      for(ViewSlotEntry viewSlotEntry: viewSlotEntries){
          if(myViewSlotEntry == viewSlotEntry){
              viewSlotEntry.getImageView().setImageResource((isTablet())
                      ? R.drawable.button_on : R.drawable.button_on );
          }else{
              viewSlotEntry.getImageView().setImageResource((isTablet())
                      ? R.drawable.button_off : R.drawable.button_off );
          }
      }
    }

    private void playMedia(Long id) {
      try {
          mediaManager
                  .playMedia(this, id);
      }catch (NullPointerException e){
          Toast.makeText(this, getString(R.string.error_not_play), Toast.LENGTH_LONG).show();
      }
  }

    public boolean isTablet() {
        boolean xlarge = ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
    @Override
  protected void onDestroy() {
    super.onDestroy();

  }

  /**
   * App permission for Audio, read to external storage
   */
  private void setupPermissions() {
    //If we don't hav the record audio or read external storage permission..
    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
      // And if we're on SDK M or later...
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        //Ask again, nicely, for the permissions.
        String[] permissionsWeNeed = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permissionsWeNeed, MY_PERMISSIONS_REQUEST_CODE );
      }

    }else{
      // Otherwise, permissions were granted and we are ready to go!
      // our logic will be here
        init();
    }

  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch(requestCode){
      case MY_PERMISSIONS_REQUEST_CODE:{
        //If request is cancelled, the result arrays are empty
        if( grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
          // we are ready to go, our logic will be here
            init();
        } else{
          Toast.makeText(this,"Permission for audio or read external storage not granted. Haptic simulator can't run", Toast.LENGTH_LONG).show();
          finish();
          //The permission was denied, so we can show a message why we can't run the app
          //and then close the app
        }
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.haptic_simulation_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if(id == R.id.action_setting){
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
