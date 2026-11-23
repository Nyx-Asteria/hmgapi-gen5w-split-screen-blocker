package com.deflight.splitscreenblocker.hooks;

import hmgapi.hook.Hooks;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;

/** Suppresses split-screen OSD; optionally keeps the navigation arrow available. */
public final class Entry extends ModEntry {
    static final String PKG = "com.mobis.splitscreen.right";
    static final String BASE = "com.mobis.splitscreen.right.k.r";
    static final java.util.Set<String> BUILDS = new java.util.HashSet<String>(java.util.Arrays.asList(
            "cc1bd5131bee221ea3b5ed24a1c1d31a",   // Hyundai Ioniq 6 US 260507
            "7b84ed639e85a711c4a9f2c78e33acf4",   // Kia Sportage-family US 260507
            "bd97b438aeb3d9c77510b5e2da4234f7"    // Kia K5 DL322 US 260507
    ));
    static String odex = "/system/app/RightScreenHome.odex";
    static final int GONE = 8;

    @Override
    public void appLoaded(final ModContext mod, LoadedApp app) throws Throwable {
        if (!PKG.equals(app.packageName) || !PKG.equals(app.processName)) return;
        String md5 = md5(odex);
        if (!BUILDS.contains(md5)) {
            mod.log("split-screen app is a different build (" + md5 + "): nothing changed");
            return;
        }
        Class<?> c = Class.forName(BASE, false, app.classLoader);
        if (c.getDeclaredMethod("f", boolean.class, boolean.class).getReturnType() != boolean.class
                || c.getDeclaredMethod("ij").getReturnType() != boolean.class
                || c.getDeclaredMethod("canShowSSOsd", boolean.class, boolean.class, boolean.class)
                        .getReturnType() != boolean.class) {
            mod.log("split-screen methods look different: nothing changed");
            return;
        }
        final Field tab = c.getDeclaredField("Fw");
        if (!"android.widget.FrameLayout".equals(tab.getType().getName())) {
            mod.log("split-screen tab looks different: nothing changed");
            return;
        }
        tab.setAccessible(true);

        // Default: hide the slide-out arrow. Setting true preserves its native behavior.
        Hooks.findAndHookMethod(BASE, app.classLoader, "f", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (mod.enabled() && !navigationArrow(mod)) p.args[0] = Boolean.FALSE;
            }
        });
        // Keep the initial tab hidden too, unless the user wants the navigation arrow.
        Hooks.findAndHookMethod(BASE, app.classLoader, "ij", new MethodHook() {
            @Override
            protected void afterHookedMethod(Param p) throws Throwable {
                if (!mod.enabled() || navigationArrow(mod)) return;
                Object v = tab.get(p.thisObject);
                if (v != null) v.getClass().getMethod("setVisibility", int.class).invoke(v, GONE);
            }
        });
        // Always deny OSD permission while the mod is enabled.
        Hooks.findAndHookMethod(BASE, app.classLoader, "canShowSSOsd",
                boolean.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (mod.enabled()) p.setResult(Boolean.FALSE);
            }
        });
        mod.log("split-screen OSD blocked; navigation arrow setting: " + navigationArrow(mod));
    }

    static boolean navigationArrow(ModContext mod) {
        return Boolean.parseBoolean(mod.settingString("navigation_arrow", "false"));
    }

    static String md5(String path) throws Exception {
        MessageDigest d = MessageDigest.getInstance("MD5");
        InputStream in = new FileInputStream(path);
        try {
            byte[] b = new byte[65536];
            for (int n; (n = in.read(b)) > 0; ) d.update(b, 0, n);
        } finally {
            in.close();
        }
        StringBuilder s = new StringBuilder();
        for (byte x : d.digest()) s.append(String.format("%02x", x & 0xff));
        return s.toString();
    }
}
