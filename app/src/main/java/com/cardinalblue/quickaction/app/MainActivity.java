package com.cardinalblue.quickaction.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cardinalblue.quickaction.ActionItem;
import com.cardinalblue.quickaction.QuickAction;
import com.cardinalblue.quickaction.TableQuickAction;


public class MainActivity extends AppCompatActivity {

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
            }
        });
        findViewById(R.id.btn_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createQuickAction().showAtView(view);
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
}
