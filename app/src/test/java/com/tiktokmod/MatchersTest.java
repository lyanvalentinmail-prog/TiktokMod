package com.tiktokmod;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchersTest {
    @Test public void onlyRecognizesExplicitIds() {
        assertTrue(Matchers.isBio("profile_bio"));
        assertTrue(Matchers.isComment("comment_text"));
        assertTrue(Matchers.isLink("profile_link"));
        assertFalse(Matchers.isBio("bio_button"));
        assertFalse(Matchers.isComment("comment_username"));
        assertFalse(Matchers.isLink("profile_button"));
    }
    @Test public void exactLocalizedSettingsTitles() {
        assertTrue(Matchers.isSettingsTitle(" Ajustes y privacidad "));
        assertTrue(Matchers.isSettingsTitle("Settings and privacy"));
        assertFalse(Matchers.isSettingsTitle("Ir a ajustes y privacidad"));
        assertFalse(Matchers.isSettingsTitle(null));
    }
    @Test public void onlySafeWebUrls() {
        assertEquals("https://example.com/path", Matchers.webUrl("Visita https://example.com/path."));
        assertEquals("http://example.com", Matchers.webUrl("http://example.com"));
        assertNull(Matchers.webUrl("javascript:alert(1)"));
        assertNull(Matchers.webUrl("https://user@example.com"));
        assertNull(Matchers.webUrl("https://"));
        assertNull(Matchers.webUrl(null));
    }
}
