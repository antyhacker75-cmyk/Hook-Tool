package com.astrotool.hook;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class BorderStylers {
    public static void applyRainbowBorder(Activity activity, int viewId, int cornerRadiusDp, int strokeWidthDp) {
        final View view = activity.findViewById(viewId);
        if (view == null) {
            return;
        }
        final GradientDrawable shape = new GradientDrawable();
        shape.setColor(0);
        shape.setCornerRadius(dpToPx(activity, cornerRadiusDp));
        final int strokeWidthPx = dpToPx(activity, strokeWidthDp);
        shape.setStroke(strokeWidthPx, -65536);
        view.setBackground(shape);
        Integer[] rainbowColors = {-16711681, -16711936, -256, -16711681};
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), rainbowColors);
        animator.setDuration(4000L);
        animator.setRepeatMode(1);
        animator.setRepeatCount(-1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                BorderStylers.lambda$applyRainbowBorder$0(shape, strokeWidthPx, view, valueAnimator);
            }
        });
        animator.start();
    }

    static  void lambda$applyRainbowBorder$0(GradientDrawable shape, int strokeWidthPx, View view, ValueAnimator animation) {
        int animatedColor = ((Integer) animation.getAnimatedValue()).intValue();
        shape.setStroke(strokeWidthPx, animatedColor);
        view.invalidate();
    }

    private static int dpToPx(Activity activity, int dp) {
        float scale = activity.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }
}
