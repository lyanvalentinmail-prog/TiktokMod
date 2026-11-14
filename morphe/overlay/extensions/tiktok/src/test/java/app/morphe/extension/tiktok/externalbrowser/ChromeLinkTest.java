package app.morphe.extension.tiktok.externalbrowser;

import static org.junit.Assert.*;
import android.content.ActivityNotFoundException;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ChromeLinkTest {
    @Test public void targetsChromeWithTheResolvedUrl() {
        final Intent[] started = { null };
        ContextWrapper context = new ContextWrapper(RuntimeEnvironment.getApplication()) {
            @Override public void startActivity(Intent intent) { started[0] = intent; }
        };
        Uri url = ExternalBrowserPatch.resolveTarget("example.com/profile");
        assertTrue(ExternalBrowserPatch.openInChrome(context, url));
        assertEquals(Intent.ACTION_VIEW, started[0].getAction());
        assertEquals("com.android.chrome", started[0].getPackage());
        assertEquals("https://example.com/profile", started[0].getDataString());
        assertTrue((started[0].getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0);
    }

    @Test public void missingOrDisabledChromeFallsBack() {
        ContextWrapper missing = new ContextWrapper(RuntimeEnvironment.getApplication()) {
            @Override public void startActivity(Intent intent) { throw new ActivityNotFoundException(); }
        };
        ContextWrapper blocked = new ContextWrapper(RuntimeEnvironment.getApplication()) {
            @Override public void startActivity(Intent intent) { throw new SecurityException(); }
        };
        Uri url = Uri.parse("https://example.com");
        assertFalse(ExternalBrowserPatch.openInChrome(missing, url));
        assertFalse(ExternalBrowserPatch.openInChrome(blocked, url));
        assertFalse(ExternalBrowserPatch.openInChrome(null, url));
        assertFalse(ExternalBrowserPatch.openInChrome(missing, null));
    }
}
