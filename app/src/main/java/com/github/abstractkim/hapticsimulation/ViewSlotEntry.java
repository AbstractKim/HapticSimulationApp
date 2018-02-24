package com.github.abstractkim.hapticsimulation;

import android.media.Image;
import android.widget.ImageView;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by peter on 2/23/18.
 */

@AllArgsConstructor
@Data
public class ViewSlotEntry {
    ImageView imageView;
    String key;
}
