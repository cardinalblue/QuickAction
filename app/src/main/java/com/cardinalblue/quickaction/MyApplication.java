package com.cardinalblue.quickaction;

import android.app.Application;

import io.bugtags.library.Bugtags;

/**
 * Created by prada on 8/31/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Start
        Bugtags.start("e6323a2516411e775c55f0847033390d", this, Bugtags.BTGInvocationEventBubble);
    }
}
