package com.tiktokmod;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/** The actual settings are stored in TikTok's process, not in this launcher. */
public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        TextView text = new TextView(this);
        text.setText("TikTok Mod\n\nActiva el módulo en LSPosed o inclúyelo con LSPatch. Después abre TikTok y entra en Ajustes y privacidad.\n\nLa integración es experimental y depende de la versión de TikTok. Consulta el README para conocer sus límites.");
        text.setTextSize(18);
        text.setPadding(40, 60, 40, 40);
        text.setGravity(Gravity.TOP);
        setContentView(text);
    }
}
