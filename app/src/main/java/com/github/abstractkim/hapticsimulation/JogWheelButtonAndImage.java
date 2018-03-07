package com.github.abstractkim.hapticsimulation;

import android.support.v4.app.INotificationSideChannel;
import android.widget.ImageButton;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by peter on 3/5/18.
 */

@AllArgsConstructor
@Data
public class JogWheelButtonAndImage {
    static public int POINTS = 8;
    static public Double degrees[] = new Double[POINTS];
    static public Double radians[] = new Double[POINTS];
    static public List<Integer> resourceIDs;
    static public Boolean focused[] = new Boolean[POINTS];

    static public void init(){
        double intervalDegree = 360 / POINTS;
        double degree = -22.5;
        for(int i = 0; i < POINTS; i++){
            degree += intervalDegree;
            degrees[i] = new Double(degree);
            radians[i] = new Double(degree * Math.PI / 180);
            focused[i] = Boolean.valueOf(false);
        }

        resourceIDs = Arrays.asList(R.drawable.jog_0,
                                    R.drawable.jog_45,
                                    R.drawable.jog_90,
                                    R.drawable.jog_135,
                                    R.drawable.jog_180,
                                    R.drawable.jog_225,
                                    R.drawable.jog_270,
                                    R.drawable.jog_315);
    }
    static public int getIndex(double rad){
        int index = 0;
        for(; index < POINTS; index++){
            if(rad > radians[(index+ POINTS - 1) % POINTS] && rad <= radians[index])
                return index;
        }

        return 0;
//        if(rad > radians[7] && rad <= radians[0])
//            return 0;
//        if(rad > radians[0] && rad <= radians[1])
//            return 1;
//        if(rad > radians[1] && rad <= radians[2])
//            return 2;
//        if(rad > radians[2] && rad <= radians[3])
//            return 3;
//        if(rad > radians[3] && rad <= radians[4])
//            return 4;
//        if(rad > radians[4] && rad <= radians[5])
//            return 5;
//        if(rad > radians[5] && rad <= radians[6])
//            return 6;
//        if(rad > radians[6] && rad <= radians[7])
//            return 7;
//        return 0;
    }
    public static void setFocused(int index){
        for(int i = 0; i < POINTS; i++){
            if(index == i)
                focused[i] = true;
            else
                focused[i] = false;
        }
    }
}
