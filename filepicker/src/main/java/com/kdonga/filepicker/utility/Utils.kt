package com.kdonga.filepicker.utility

import android.animation.ValueAnimator
import android.os.StatFs
import android.view.View
import android.view.animation.DecelerateInterpolator


object Utils {

    fun getRootSubtitle(path: String): String {
        val stat = StatFs(path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return if (total == 0L) {
            ""
        } else SizeUnit.formatFileSize(free) + " / " + SizeUnit.formatFileSize(total)
    }

    fun compareExtension(extTypes: List<String>, desired: String): Boolean {
        for (str in extTypes) {
            if (desired == str) {
                return true
            }
        }
        return false
    }

    fun expandView(v: View): ValueAnimator {
        val trHeight = 60.toPx
        v.visibility = View.VISIBLE
        val anim = ValueAnimator.ofInt(0, trHeight)
        anim.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 500
        anim.start()
        return anim
    }

    fun collapseView(v: View) {
        val trHeight = 60.toPx
        val anim = ValueAnimator.ofInt(trHeight, 0)
        anim.addUpdateListener { animation ->
            val animValue = animation.animatedValue as Int
            v.layoutParams.height = animValue
            v.requestLayout()
            if (animValue == 0)
                v.visibility = View.GONE
        }
        anim.interpolator = DecelerateInterpolator()
        anim.duration = 500
        anim.start()
    }
}
