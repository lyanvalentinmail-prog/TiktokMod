package com.tiktokmod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

final class ModSettings {
    static final String PREFS = "tiktok_mod_settings";
    static final String BIO = "copy_bio", COMMENT = "copy_comment", LINKS = "chrome_links", ACCENT = "accent";
    private static final int[] COLORS = { 0xfffe2c55, 0xff20b8c0, 0xff8b5cf6, 0xff2979ff };
    private static final String[] NAMES = { "Rosa TikTok", "Turquesa", "Violeta", "Azul" };

    private ModSettings() { }
    static SharedPreferences prefs(Activity activity) { return activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE); }
    static int accent(Activity activity) { return prefs(activity).getInt(ACCENT, COLORS[0]); }
    static boolean enabled(Activity activity, String key) { return prefs(activity).getBoolean(key, true); }

    static void show(Activity activity) {
        LinearLayout menu = column(activity);
        TextView appearance = row(activity, "Apariencia  ›");
        menu.addView(appearance);
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("TikTok Mod").setView(menu)
                .setNegativeButton("Cerrar", null).create();
        appearance.setOnClickListener(v -> { dialog.dismiss(); showAppearance(activity); });
        // Extensions are part of Appearance so that the main menu contains one button.
        dialog.show();
    }
    private static void showAppearance(Activity activity) {
        LinearLayout layout = column(activity);
        TextView color = row(activity, "Color de acento  ›");
        layout.addView(color);
        color.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Color de acento")
                .setItems(NAMES, (d, which) -> {
                    prefs(activity).edit().putInt(ACCENT, COLORS[which]).apply();
                    color.setTextColor(COLORS[which]);
                }).setNegativeButton("Cancelar", null).show());
        TextView note = row(activity, "Perfil modo Instagram · Próximamente\nRequiere adaptar el perfil a una versión concreta de TikTok.");
        note.setTextColor(Color.DKGRAY);
        layout.addView(note);
        TextView heading = row(activity, "Extensiones experimentales");
        heading.setTextSize(17);
        layout.addView(heading);
        addSwitch(activity, layout, "Mantener pulsada la biografía para copiarla", BIO);
        addSwitch(activity, layout, "Mantener pulsado el texto del comentario para copiarlo", COMMENT);
        addSwitch(activity, layout, "Abrir enlaces del perfil en Chrome", LINKS);
        TextView warning = row(activity, "Solo funciona si TikTok expone los elementos compatibles. El acento se aplica a este menú, no a toda la aplicación.");
        warning.setTextColor(Color.DKGRAY);
        warning.setTextSize(13);
        layout.addView(warning);
        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        scroll.addView(layout);
        new AlertDialog.Builder(activity).setTitle("Apariencia").setView(scroll).setPositiveButton("Listo", null).show();
    }
    private static LinearLayout column(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = dp(activity, 16);
        layout.setPadding(p, p, p, p);
        return layout;
    }
    private static TextView row(Activity activity, String text) {
        TextView row = new TextView(activity);
        row.setText(text);
        row.setTextSize(16);
        row.setTextColor(accent(activity));
        row.setPadding(0, dp(activity, 12), 0, dp(activity, 12));
        return row;
    }
    private static void addSwitch(Activity activity, LinearLayout layout, String title, String key) {
        Switch toggle = new Switch(activity);
        toggle.setText(title);
        toggle.setTextSize(15);
        toggle.setPadding(0, dp(activity, 10), 0, dp(activity, 10));
        toggle.setChecked(enabled(activity, key));
        toggle.setOnCheckedChangeListener((button, checked) -> prefs(activity).edit().putBoolean(key, checked).apply());
        layout.addView(toggle);
    }
    static int dp(Activity a, int value) { return Math.round(value * a.getResources().getDisplayMetrics().density); }
}
