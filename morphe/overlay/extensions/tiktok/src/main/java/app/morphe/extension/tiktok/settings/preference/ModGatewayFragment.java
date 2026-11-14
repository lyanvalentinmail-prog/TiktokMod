/* TikTok Mod additions, derived for the GPL-3.0 Metra patch source. */
package app.morphe.extension.tiktok.settings.preference;

import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/** A small entrance in front of Metra's existing settings; direct section links remain intact. */
@SuppressWarnings("deprecation")
public final class ModGatewayFragment extends PreferenceFragment {
    public static final String APPEARANCE_KEY = "tiktok_mod_appearance";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        screen.setTitle("TikTok Mod");
        setPreferenceScreen(screen);
        Preference appearance = new Preference(getActivity());
        appearance.setKey(APPEARANCE_KEY);
        appearance.setTitle("Apariencia");
        appearance.setOnPreferenceClickListener(preference -> openAppearance());
        screen.addPreference(appearance);
    }

    private boolean openAppearance() {
        FragmentManager manager = getFragmentManager();
        if (manager == null || getId() == 0 || manager.isDestroyed()) return false;
        manager.beginTransaction()
                .replace(getId(), TikTokPreferenceFragment.appearance())
                .addToBackStack("tiktok_mod_appearance")
                .commit();
        return true;
    }
}
