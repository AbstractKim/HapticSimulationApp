package com.github.abstractkim.hapticsimulation;

import android.widget.ImageButton;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by peter on 3/5/18.
 */

@AllArgsConstructor
@Data
public class JogWheelButtonAndImage {
    ImageButton imageButton;
    int imageResourceId;
}
