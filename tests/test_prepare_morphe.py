"""Integration tests for the pinned-source preparation script (no Android SDK needed)."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / 'scripts/prepare-morphe.sh'


class PreparationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.dest = Path(self.temp.name) / 'source'
        self.env = os.environ.copy()  # METRA_ARCHIVE optionally supplies the pinned archive offline.

    def run_script(self):
        return subprocess.run([str(SCRIPT), str(self.dest)], env=self.env,
                              text=True, capture_output=True)

    def test_pinned_source_is_patched_and_overlay_is_installed(self):
        result = self.run_script()
        self.assertEqual(0, result.returncode, result.stderr)
        base = self.dest / 'extensions/tiktok/src/main/java/app/morphe/extension/tiktok'
        self.assertIn('new ModGatewayFragment()', (base / 'settings/TikTokActivityHook.java').read_text())
        self.assertIn('com.android.chrome', (base / 'externalbrowser/ExternalBrowserPatch.java').read_text())
        self.assertTrue((base / 'settings/preference/ModGatewayFragment.java').is_file())
        self.assertTrue((self.dest / 'LICENSE').is_file())
        kotlin = self.dest / 'patches/src/main/kotlin/app/morphe/patches/tiktok/misc/settings/SettingsPatch.kt'
        self.assertIn('TikTok Mod', kotlin.read_text())

    def test_existing_directory_is_not_touched(self):
        self.dest.mkdir()
        sentinel = self.dest / 'sentinel'
        sentinel.write_text('keep me')
        result = self.run_script()
        self.assertNotEqual(0, result.returncode)
        self.assertEqual('keep me', sentinel.read_text())

    def test_invalid_archive_is_rejected(self):
        bad = Path(self.temp.name) / 'bad.tar.gz'
        bad.write_bytes(b'not the pinned source')
        self.env['METRA_ARCHIVE'] = str(bad)
        result = self.run_script()
        self.assertNotEqual(0, result.returncode)
        self.assertIn('hash', result.stderr.lower())
        self.assertFalse(self.dest.exists())


if __name__ == '__main__':
    unittest.main()
