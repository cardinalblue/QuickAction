package com.cardinalblue.quickaction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cardinalblue.quickaction.app.R;

import org.tensorflow.demo.TensorFlowClassifier;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import io.bugtags.library.Bugtags;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_CLASSES = 1001;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    // private static final String MODEL_FILE = "file:///android_asset/dots.pb";
    private static final String MODEL_FILE = "file:///android_asset/newspaper.pb";

    private static final String LABEL_FILE =
        "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private final TensorFlowClassifier tensorflow = new TensorFlowClassifier();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.root).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("QA", ">>>>> on touch " + motionEvent);
                return true;
            }
        });
        final ImageView preview = (ImageView) findViewById(R.id.preview);

        findViewById(R.id.btn_01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createQuickAction().showAtLocation(300,300);
                tensorflow.initializeTensorFlow(
                    getAssets(), MODEL_FILE, LABEL_FILE, NUM_CLASSES, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD,
                    INPUT_NAME, OUTPUT_NAME);
            }
        });
        findViewById(R.id.btn_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createQuickAction().showAtView(view);
                Task.callInBackground(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        Bitmap bm = BitmapFactory.decodeStream(getAssets().open("test_selfie.jpg"));
                        return tensorflow.styleTransfer(Bitmap.createScaledBitmap(bm, INPUT_SIZE, INPUT_SIZE, false));
                    }
                }).continueWith(new Continuation<Bitmap, Void>() {
                    @Override
                    public Void then(Task<Bitmap> task) throws Exception {
                        if (task.isFaulted()) {
                            Log.d("TEST", task.getError().getMessage());
                            return null;
                        }
                        preview.setImageBitmap(task.getResult());
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            }
        });
    }

    private QuickAction createQuickAction() {
        TableQuickAction qa = new TableQuickAction(MainActivity.this, findViewById(R.id.root), true);
        qa.addActionItem(new ActionItem(0, "test", getResources().getDrawable(R.drawable.popup_arrow_down)));
        qa.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId, ImageView img, TextView text) {
                Log.d("QA", "onItemClick : " + pos);
            }
        });
        qa.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Log.d("QA", "onDismiss");
            }
        });
        return qa;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //Callback 3
        Bugtags.onDispatchTouchEvent(this, event);
        return super.dispatchTouchEvent(event);
    }
}
