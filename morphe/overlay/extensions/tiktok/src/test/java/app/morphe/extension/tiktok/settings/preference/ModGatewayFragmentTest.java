package app.morphe.extension.tiktok.settings.preference;

import static org.junit.Assert.*;

import android.app.Activity;
import android.preference.Preference;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.tiktok.SettingsContextRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
@SuppressWarnings("deprecation")
public class ModGatewayFragmentTest {
    @Rule public final SettingsContextRule settingsContext = new SettingsContextRule();

    @Test public void gatewayHasOneActionAndBackReturnsToIt() {
        try (var owner = Robolectric.buildActivity(Activity.class).setup()) {
            Activity activity = owner.get();
            Utils.setContext(activity);
            ModGatewayFragment gateway = new ModGatewayFragment();
            activity.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, gateway).commit();
            activity.getFragmentManager().executePendingTransactions();
            assertEquals(1, gateway.getPreferenceScreen().getPreferenceCount());
            Preference appearance = gateway.findPreference(ModGatewayFragment.APPEARANCE_KEY);
            assertEquals("Apariencia", appearance.getTitle());
            assertTrue(appearance.getOnPreferenceClickListener().onPreferenceClick(appearance));
            activity.getFragmentManager().executePendingTransactions();
            assertTrue(activity.getFragmentManager().findFragmentById(android.R.id.content)
                    instanceof TikTokPreferenceFragment);
            TikTokPreferenceFragment appearancePage = (TikTokPreferenceFragment)
                    activity.getFragmentManager().findFragmentById(android.R.id.content);
            assertNotNull(appearancePage.findPreference("tiktok_mod_accent_info"));
            assertNotNull(appearancePage.findPreference("tiktok_mod_instagram_info"));
            assertFalse(appearancePage.findPreference("tiktok_mod_instagram_info").isSelectable());
            activity.getFragmentManager().popBackStackImmediate();
            assertTrue(activity.getFragmentManager().findFragmentById(android.R.id.content)
                    instanceof ModGatewayFragment);
        }
    }
}
