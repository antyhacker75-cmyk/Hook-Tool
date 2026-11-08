package com.astrotool.hook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.animation.ValueAnimator;

public final class ModernUi {
    private static final int BG = Color.rgb(7, 12, 25);
    private static final int PANEL = Color.rgb(15, 25, 43);
    private static final int PANEL2 = Color.rgb(20, 34, 55);
    private static final int CYAN = Color.rgb(55, 222, 231);
    private static final int PURPLE = Color.rgb(151, 116, 255);
    private static final int WHITE = Color.rgb(239, 245, 255);
    private static final int MUTED = Color.rgb(150, 168, 194);

    private ModernUi() {}

    private static int dp(Activity a, int v) {
        return (int)(v * a.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable shape(int color, int stroke, int radius, int strokeWidth) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        if (strokeWidth > 0) d.setStroke(strokeWidth, stroke);
        return d;
    }

    private static GradientDrawable gradient(int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{CYAN, PURPLE});
        d.setCornerRadius(radius);
        return d;
    }

    private static GradientDrawable backgroundGradient() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(6, 12, 27), Color.rgb(17, 20, 43), Color.rgb(8, 26, 39)});
        return d;
    }

    private static TextView text(Activity a, String value, int color, float size, boolean bold) {
        TextView t = new TextView(a);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return t;
    }

    private static Button button(Activity a, String value, boolean primary) {
        Button b = new Button(a);
        b.setText(value);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? Color.rgb(5, 16, 29) : WHITE);
        b.setBackground(primary ? gradient(dp(a, 24)) :
            shape(PANEL2, Color.rgb(48, 82, 111), dp(a, 16), dp(a, 1)));
        b.setMinHeight(dp(a, 46));
        return b;
    }

    private static void styleButton(Button b) {
        if (b == null) return;
        b.setAllCaps(false);
        b.setTextColor(Color.rgb(5, 14, 28));
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(20, 12, 20, 12);
        b.setMinHeight(48);
        b.setBackground(gradient(36));
        b.setElevation(4f);
    }

    private static void styleField(EditText field) {
        if (field == null) return;
        field.setTextColor(WHITE);
        field.setHintTextColor(MUTED);
        field.setTextSize(15);
        field.setSingleLine(true);
        field.setPadding(18, 14, 18, 14);
        field.setBackground(shape(Color.rgb(9, 17, 32), Color.rgb(44, 170, 195),  dpSafe(field, 16), dpSafe(field, 1)));
    }

    private static int dpSafe(View v, int n) {
        return (int)(n * v.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static void styleText(TextView v, int color, float size, boolean bold) {
        if (v == null) return;
        v.setTextColor(color);
        v.setTextSize(size);
        v.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
    }

    public static void applyLogin(Activity activity, LinearLayout root, LinearLayout card,
            LinearLayout fieldRow, LinearLayout buttonRow, TextView heading, TextView status,
            EditText keyInput, Button loginButton) {
        if (root != null) {
            root.setBackground(backgroundGradient());
            root.setPadding(dp(activity, 22), dp(activity, 18), dp(activity, 22), dp(activity, 18));
        }
        if (card != null) {
            card.setBackground(shape(PANEL, Color.rgb(38, 83, 112), dp(activity, 24), dp(activity, 1)));
            card.setPadding(dp(activity, 18), dp(activity, 20), dp(activity, 18), dp(activity, 18));
            card.setElevation(dp(activity, 8));
        }
        if (fieldRow != null) {
            fieldRow.setBackground(shape(Color.rgb(12, 20, 36), Color.rgb(34, 83, 111), dp(activity, 14), dp(activity, 1)));
            fieldRow.setPadding(dp(activity, 10), dp(activity, 6), dp(activity, 10), dp(activity, 6));
        }
        if (buttonRow != null) buttonRow.setBackgroundColor(Color.TRANSPARENT);
        styleText(heading, WHITE, 24f, true);
        if (heading != null) heading.setText("Welcome to Astro Hook");
        styleText(status, CYAN, 13f, true);
        if (status != null) {
            status.setGravity(Gravity.CENTER);
            status.setPadding(dp(activity, 10), dp(activity, 9), dp(activity, 10), dp(activity, 9));
            status.setBackground(shape(Color.rgb(12, 29, 43), Color.rgb(0, 170, 190), dp(activity, 13), dp(activity, 1)));
        }
        styleField(keyInput);
        if (activity.findViewById(R.id.rememberKey) instanceof CheckBox) {
            CheckBox remember = (CheckBox)activity.findViewById(R.id.rememberKey);
            remember.setTextColor(MUTED);
            remember.setTextSize(12);
            remember.setButtonTintList(android.content.res.ColorStateList.valueOf(CYAN));
        }
        styleButton(loginButton);
        if (activity.findViewById(R.id.webView1) != null) {
            View art = activity.findViewById(R.id.webView1);
            ViewGroup.LayoutParams lp = art.getLayoutParams();
            lp.height = dp(activity, 128);
            art.setLayoutParams(lp);
        }
    }

    public static void applyMain(Activity activity, LinearLayout root, LinearLayout panel,
            ScrollView scroll, TextView title, TextView targetLabel, TextView output,
            TextView copyLabel, TextView label7, TextView label8, TextView label9,
            EditText input, Button primary, Button secondary, Button tertiary) {
        if (root != null) root.setBackground(backgroundGradient());
        if (scroll != null) {
            scroll.setFillViewport(true);
            scroll.setBackground(shape(Color.rgb(11, 19, 34), Color.rgb(34, 75, 105),  dp(activity, 16), dp(activity, 1)));
        }
        if (panel != null) {
            panel.setBackground(shape(PANEL, Color.rgb(36, 86, 116), dp(activity, 20), dp(activity, 1)));
            panel.setPadding(dp(activity, 10), dp(activity, 8), dp(activity, 10), dp(activity, 8));
            panel.setElevation(dp(activity, 5));
        }
        styleText(title, WHITE, 22f, true);
        if (title != null) title.setText("ASTRO HOOK");
        styleText(targetLabel, CYAN, 13f, true);
        styleText(output, WHITE, 13f, false);
        if (output != null) {
            output.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12));
            output.setLineSpacing(dp(activity, 3), 1.05f);
        }
        styleText(copyLabel, CYAN, 13f, true);
        styleText(label7, CYAN, 13f, true);
        styleText(label8, MUTED, 12f, false);
        if (label8 != null) {
            label8.setText("Astro Hook  •  Authorized testing and read-only comparison");
            label8.setLineSpacing(dp(activity, 3), 1.08f);
        }
        styleText(label9, CYAN, 12f, true);
        styleField(input);
        styleButton(primary);
        styleButton(secondary);
        styleButton(tertiary);
        addAstroHeader(activity, root);
    }

    private static void addAstroHeader(final Activity a, LinearLayout root) {
        if (a == null || root == null || "astro_header_added".equals(root.getTag())) return;
        root.setTag("astro_header_added");
        LinearLayout header = new LinearLayout(a);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(a, 4), 0, dp(a, 10));

        LinearLayout brandBlock = new LinearLayout(a);
        brandBlock.setOrientation(LinearLayout.VERTICAL);
        brandBlock.addView(text(a, "ASTRO HOOK", WHITE, 21, true));
        TextView sub = text(a, "CONTROL CENTER  /  TOOLKIT", MUTED, 9, true);
        sub.setLetterSpacing(0.12f);
        brandBlock.addView(sub);
        header.addView(brandBlock, new LinearLayout.LayoutParams(0, dp(a, 52), 1f));

        Button menu = button(a, "☰", false);
        menu.setContentDescription("Open navigation drawer");
        header.addView(menu, new LinearLayout.LayoutParams(dp(a, 54), dp(a, 46)));
        menu.setOnClickListener(v -> showAstroDrawer(a));

        Button instructions = button(a, "VIEW INSTRUCTIONS", false);
        LinearLayout.LayoutParams guide = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 46));
        guide.bottomMargin = dp(a, 12);
        instructions.setOnClickListener(v -> showAstroInstructions(a));
        root.addView(header, 0);
        root.addView(instructions, 1, guide);
    }

    private static void showAstroDrawer(final Activity a) {
        final View decor = a.getWindow().getDecorView();
        if (!(decor instanceof FrameLayout)) return;
        final FrameLayout host = (FrameLayout)decor;
        final FrameLayout overlay = new FrameLayout(a);
        overlay.setBackgroundColor(0x99030A18);
        overlay.setAlpha(0f);

        final LinearLayout drawer = new LinearLayout(a);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setPadding(dp(a, 20), dp(a, 30), dp(a, 18), dp(a, 20));
        drawer.setBackground(shape(Color.rgb(10, 19, 36), Color.rgb(40, 111, 139), dp(a, 0), dp(a, 1)));
        drawer.setElevation(dp(a, 18));

        LinearLayout hero = new LinearLayout(a);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(a, 16), dp(a, 20), dp(a, 16), dp(a, 18));
        hero.setBackground(gradient(dp(a, 20)));
        hero.addView(text(a, "ASTRO HOOK", Color.rgb(5, 17, 30), 22, true));
        TextView tagline = text(a, "TOOLS • STATUS • INFORMATION", Color.rgb(12, 38, 52), 10, true);
        tagline.setPadding(0, dp(a, 6), 0, 0);
        hero.addView(tagline);
        drawer.addView(hero, new LinearLayout.LayoutParams(-1, dp(a, 106)));

        TextView section = text(a, "NAVIGATION", MUTED, 10, true);
        section.setLetterSpacing(0.15f);
        LinearLayout.LayoutParams sectionLp = new LinearLayout.LayoutParams(-1, -2);
        sectionLp.topMargin = dp(a, 26);
        sectionLp.bottomMargin = dp(a, 10);
        drawer.addView(section, sectionLp);

        addDrawerItem(a, drawer, "▤", "Step-by-step instructions", "Read the quick guide", () -> {
            closeDrawer(overlay, drawer);
            showAstroInstructions(a);
        });
        addDrawerItem(a, drawer, "◎", "Telegram channel", "@WashiWashi123", () -> {
            closeDrawer(overlay, drawer);
            a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/WashiWashi123")));
        });
        addDrawerItem(a, drawer, "ⓘ", "About Astro Hook", "Version and project information", () -> {
            closeDrawer(overlay, drawer);
            showAstroAbout(a);
        });

        View spacer = new View(a);
        drawer.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));
        View line = new View(a);
        line.setBackgroundColor(Color.rgb(37, 62, 86));
        drawer.addView(line, new LinearLayout.LayoutParams(-1, dp(a, 1)));
        TextView footer = text(a, "ASTRO HOOK  •  HELP & SUPPORT", MUTED, 9, true);
        footer.setPadding(0, dp(a, 14), 0, 0);
        drawer.addView(footer);

        overlay.addView(drawer, new FrameLayout.LayoutParams(dp(a, 304),
            ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        overlay.setOnClickListener(v -> closeDrawer(overlay, drawer));
        drawer.setOnClickListener(v -> { });
        host.addView(overlay, new FrameLayout.LayoutParams(-1, -1));

        drawer.setTranslationX(-dp(a, 304));
        drawer.animate().translationX(0).setDuration(260).setInterpolator(new DecelerateInterpolator()).start();
        overlay.animate().alpha(1f).setDuration(220).start();
    }

    private static void addDrawerItem(Activity a, LinearLayout drawer, String icon,
            String title, String subtitle, Runnable action) {
        LinearLayout item = new LinearLayout(a);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(a, 12), dp(a, 12), dp(a, 10), dp(a, 12));
        item.setBackground(shape(PANEL, Color.rgb(34, 62, 88), dp(a, 15), dp(a, 1)));
        TextView glyph = text(a, icon, CYAN, 21, true);
        glyph.setGravity(Gravity.CENTER);
        item.addView(glyph, new LinearLayout.LayoutParams(dp(a, 42), dp(a, 42)));
        LinearLayout labels = new LinearLayout(a);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(a, 10), 0, 0, 0);
        labels.addView(text(a, title, WHITE, 14, true));
        TextView detail = text(a, subtitle, MUTED, 11, false);
        LinearLayout.LayoutParams detailLp = new LinearLayout.LayoutParams(-1, -2);
        detailLp.topMargin = dp(a, 3);
        labels.addView(detail, detailLp);
        item.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(-1, -2);
        itemLp.bottomMargin = dp(a, 9);
        drawer.addView(item, itemLp);
        item.setOnClickListener(v -> action.run());
    }

    private static void closeDrawer(FrameLayout overlay, View drawer) {
        drawer.animate().translationX(-drawer.getWidth()).setDuration(200)
            .setInterpolator(new DecelerateInterpolator()).start();
        overlay.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            if (overlay.getParent() instanceof ViewGroup) ((ViewGroup)overlay.getParent()).removeView(overlay);
        }).start();
    }

    private static void showAstroInstructions(Activity a) {
        String guide = "1. Open Astro Hook and review the selected target library.\n\n" +
            "2. Use only an app or test build that you own or are authorized to assess.\n\n" +
            "3. Run the available status or comparison check and read each on-screen message.\n\n" +
            "4. A check mark means the selected item was reported as detected; it does not prove that a test is safe or authorized.\n\n" +
            "5. For a comparison, wait until progress reaches 100% and the completion message appears.\n\n" +
            "6. Review the plain-text result and save a copy if needed.\n\n" +
            "Do not use this tool to bypass protections, interfere with online games, or modify software without permission.";
        showModernDialog(a, "STEP-BY-STEP GUIDE", "A quick walkthrough for using the utility responsibly.", guide, "GOT IT", null);
    }

    private static void showAstroAbout(Activity a) {
        showModernDialog(a, "ABOUT ASTRO HOOK", "A clean control center for authorized testing.",
            "ASTRO HOOK\n\nUtility interface for authorized testing and read-only comparison tasks.\n\nTelegram support: @WashiWashi123",
            "CLOSE", "TELEGRAM");
    }

    private static void showModernDialog(Activity a, String title, String subtitle,
            String body, String positive, String secondary) {
        AlertDialog dialog = new AlertDialog.Builder(a).create();
        LinearLayout panel = new LinearLayout(a);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(a, 22), dp(a, 22), dp(a, 22), dp(a, 18));
        panel.setBackground(shape(Color.rgb(12, 22, 40), Color.rgb(51, 117, 145), dp(a, 24), dp(a, 1)));

        TextView eyebrow = text(a, "ASTRO HOOK  /  INFORMATION", CYAN, 9, true);
        eyebrow.setLetterSpacing(0.12f);
        panel.addView(eyebrow);

        TextView heading = text(a, title, WHITE, 19, true);
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(-1, -2);
        headLp.topMargin = dp(a, 10);
        panel.addView(heading, headLp);

        TextView desc = text(a, subtitle, MUTED, 12, false);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(-1, -2);
        descLp.topMargin = dp(a, 6);
        panel.addView(desc, descLp);

        View divider = new View(a);
        divider.setBackgroundColor(Color.rgb(39, 67, 91));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(-1, dp(a, 1));
        divLp.topMargin = dp(a, 16);
        divLp.bottomMargin = dp(a, 14);
        panel.addView(divider, divLp);

        ScrollView scroll = new ScrollView(a);
        TextView message = text(a, body, Color.rgb(210, 224, 242), 13, false);
        message.setLineSpacing(dp(a, 4), 1.08f);
        scroll.addView(message);
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, dp(a, 300)));

        LinearLayout actions = new LinearLayout(a);
        actions.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsLp = new LinearLayout.LayoutParams(-1, -2);
        actionsLp.topMargin = dp(a, 16);
        panel.addView(actions, actionsLp);

        if (secondary != null) {
            Button extra = button(a, secondary, false);
            actions.addView(extra, new LinearLayout.LayoutParams(-2, dp(a, 44)));
            extra.setOnClickListener(v -> {
                dialog.dismiss();
                a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/WashiWashi123")));
            });
        }
        Button ok = button(a, positive, true);
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(-2, dp(a, 44));
        okLp.leftMargin = dp(a, 8);
        actions.addView(ok, okLp);
        ok.setOnClickListener(v -> dialog.dismiss());

        dialog.setView(panel);
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setLayout(Math.min(a.getResources().getDisplayMetrics().widthPixels - dp(a, 36), dp(a, 420)),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.dimAmount = 0.72f;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().setLayout(Math.min(a.getResources().getDisplayMetrics().widthPixels - dp(a, 36), dp(a, 420)),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
