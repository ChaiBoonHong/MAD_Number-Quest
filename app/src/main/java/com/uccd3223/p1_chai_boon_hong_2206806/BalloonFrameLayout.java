package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class BalloonFrameLayout extends FrameLayout {
    public BalloonFrameLayout(Context context) {
        super(context);
    }

    public BalloonFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BalloonFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
