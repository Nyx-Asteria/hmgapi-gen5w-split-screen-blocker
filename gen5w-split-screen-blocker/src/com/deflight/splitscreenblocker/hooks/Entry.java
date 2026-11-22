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

/**
 * Runs in the car's split-screen app (com.mobis.splitscreen.right). Its base class,
 * SplitScreenBase (obfuscated to k.r), does everything through these methods:
 *   bh(boolean)     setSplitSeenShow: opens or closes the panel
 *   show()          makes the panel's view visible
 *   f(boolean, boolean)  setButtonShow: shows or hides the slide-out tab (window SS_Button_Window)
 *   ij()            initButton: creates the tab's window at start
 * The names are obfuscated and differ between software builds, so nothing is hooked unless
 * the app is one of the exact builds they were checked on.
 */
public final class Entry extends ModEntry {
    static final String PKG = "com.mobis.splitscreen.right";
    static final String BASE = "com.mobis.splitscreen.right.k.r";
    /** RightScreenHome.odex builds checked to have these methods and the tab field, with the same code */
    static final java.util.Set<String> BUILDS = new java.util.HashSet<String>(java.util.Arrays.asList(
            "cc1bd5131bee221ea3b5ed24a1c1d31a",   // Hyundai US 260507: Ioniq 6 (CEEV)
            "7b84ed639e85a711c4a9f2c78e33acf4",   // Kia US 260507: Sportage, Niro, Telluride, Carnival and others
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
        // all or nothing: every method and the tab's field must be what this build has
        if (c.getDeclaredMethod("bh", boolean.class).getReturnType() != void.class
                || c.getDeclaredMethod("show").getReturnType() != void.class
                || c.getDeclaredMethod("f", boolean.class, boolean.class).getReturnType() != boolean.class
                || c.getDeclaredMethod("ij").getReturnType() != boolean.class) {
            mod.log("split-screen app looks different: nothing changed");
            return;
        }
        final Field tab = c.getDeclaredField("Fw");
        if (!"android.widget.FrameLayout".equals(tab.getType().getName())) {
            mod.log("split-screen tab looks different: nothing changed");
            return;
        }
        tab.setAccessible(true);

        // the panel never opens; closing still runs as stock
        Hooks.findAndHookMethod(BASE, app.classLoader, "bh", boolean.class, new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (mod.enabled() && Boolean.TRUE.equals(p.args[0])) p.setResult(null);
            }
        });
        Hooks.findAndHookMethod(BASE, app.classLoader, "show", new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (mod.enabled()) p.setResult(null);
            }
        });
        // every "show the tab" becomes the stock "hide the tab"
        Hooks.findAndHookMethod(BASE, app.classLoader, "f", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (mod.enabled()) p.args[0] = Boolean.FALSE;
            }
        });
        // initButton can return without deciding (a popup on screen, no resumed activity): the tab starts hidden
        Hooks.findAndHookMethod(BASE, app.classLoader, "ij", new MethodHook() {
            @Override
            protected void afterHookedMethod(Param p) throws Throwable {
                if (!mod.enabled()) return;
                Object v = tab.get(p.thisObject);
                if (v != null) v.getClass().getMethod("setVisibility", int.class).invoke(v, GONE);
            }
        });
        mod.log("split screen and its tab are off");
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
