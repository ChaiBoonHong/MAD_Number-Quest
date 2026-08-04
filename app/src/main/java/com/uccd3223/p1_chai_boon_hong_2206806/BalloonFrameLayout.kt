package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class BalloonFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
