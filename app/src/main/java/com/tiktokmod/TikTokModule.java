package com.tiktokmod;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Legacy Xposed entry point. Never hook system services or other applications. */
public final class TikTokModule implements IXposedHookLoadPackage {
    private static final Map<Activity, SettingsEntry> ENTRIES = new WeakHashMap<>();

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!p.packageName.equals("com.zhiliaoapp.musically") && !p.packageName.equals("com.ss.android.ugc.trill")) return;
        if (!p.packageName.equals(p.processName)) return;
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    Activity a = (Activity) param.thisObject;
                    SettingsEntry old = ENTRIES.remove(a);
                    if (old != null) old.detach();
                    SettingsEntry entry = new SettingsEntry(a);
                    ENTRIES.put(a, entry);
                    entry.attach();
                }
            });
            XposedHelpers.findAndHookMethod(Activity.class, "onPause", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    SettingsEntry entry = ENTRIES.remove((Activity) param.thisObject);
                    if (entry != null) entry.detach();
                }
            });
            XposedHelpers.findAndHookMethod(View.class, "performLongClick", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    View v = (View) param.thisObject;
                    Activity a = activity(v.getContext());
                    if (a == null || !(v instanceof TextView) || !v.isShown()) return;
                    String id = resourceName(v);
                    boolean bio = Matchers.isBio(id) && ModSettings.enabled(a, ModSettings.BIO);
                    boolean comment = Matchers.isComment(id) && ModSettings.enabled(a, ModSettings.COMMENT);
                    if (!bio && !comment) return;
                    CharSequence text = ((TextView) v).getText();
                    if (text == null || text.toString().trim().isEmpty()) return;
                    // Only copy the text view itself; never join it with a separate author view.
                    ClipboardManager clipboard = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard == null) return;
                    clipboard.setPrimaryClip(ClipData.newPlainText(comment ? "Comentario" : "Biografía", text.toString()));
                    Toast.makeText(a, comment ? "Comentario copiado" : "Biografía copiada", Toast.LENGTH_SHORT).show();
                    param.setResult(true);
                }
            });
            XposedHelpers.findAndHookMethod(View.class, "performClick", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    View v = (View) param.thisObject;
                    Activity a = activity(v.getContext());
                    if (a == null || !(v instanceof TextView) || !v.isShown() || !Matchers.isLink(resourceName(v)) || !ModSettings.enabled(a, ModSettings.LINK)) return;
                    CharSequence text = ((TextView) v).getText();
                    String url = Matchers.webUrl(text == null ? null : text.toString());
                    if (url == null) return;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.android.chrome");
                    try {
                        a.startActivity(intent);
                        param.setResult(true);
                    } catch (android.content.ActivityNotFoundException | SecurityException ignored) {
                        // Leave TikTok's normal click intact if Chrome cannot handle it.
                        Toast.makeText(a, "Chrome no está disponible", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Throwable error) { XposedBridge.log(error); }
    }
    private static String resourceName(View view) {
        try { return view.getResources().getResourceEntryName(view.getId()); }
        catch (Exception ignored) { return ""; }
    }
    private static Activity activity(Context context) {
        for (int i = 0; i < 10 && context != null; i++) {
            if (context instanceof Activity) return (Activity) context;
            if (!(context instanceof ContextWrapper)) return null;
            Context next = ((ContextWrapper) context).getBaseContext();
            if (next == context) return null;
            context = next;
        }
        return null;
    }

    private static final class SettingsEntry implements ViewTreeObserver.OnGlobalLayoutListener {
        final Activity activity;
        final FrameLayout content;
        Button button;
        boolean updating;
        final Runnable refresh = this::refreshEntry;
        SettingsEntry(Activity activity) {
            this.activity = activity;
            View v = activity.findViewById(android.R.id.content);
            content = v instanceof FrameLayout ? (FrameLayout) v : null;
        }
        void attach() {
            if (content == null) return;
            content.getViewTreeObserver().addOnGlobalLayoutListener(this);
            onGlobalLayout();
        }
        void detach() {
            if (content == null) return;
            content.removeCallbacks(refresh);
            if (content.getViewTreeObserver().isAlive()) content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (button != null) content.removeView(button);
            button = null;
        }
        @Override public void onGlobalLayout() {
            if (updating || content == null) return;
            // Coalesce repeated layout events during animations and scrolling.
            content.removeCallbacks(refresh);
            content.postDelayed(refresh, 150);
        }
        private void refreshEntry() {
            if (updating || content == null || activity.isFinishing()) return;
            updating = true;
            try {
                boolean settings = hasSettingsTitle(content);
                if (settings && button == null) {
                    button = new Button(activity);
                    button.setText("TikTok Mod");
                    button.setAllCaps(false);
                    button.setTextColor(ModSettings.accent(activity));
                    button.setOnClickListener(v -> ModSettings.show(activity));
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
                    content.addView(button, lp);
                } else if (!settings && button != null) {
                    content.removeView(button);
                    button = null;
                }
            } finally { updating = false; }
        }
        private boolean hasSettingsTitle(View v) {
            if (v == button || !v.isShown()) return false;
            if (v instanceof TextView && Matchers.isSettingsTitle(((TextView) v).getText().toString())) return true;
            if (v instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) v;
                for (int i = 0; i < group.getChildCount(); i++) if (hasSettingsTitle(group.getChildAt(i))) return true;
            }
            return false;
        }
    }
}
