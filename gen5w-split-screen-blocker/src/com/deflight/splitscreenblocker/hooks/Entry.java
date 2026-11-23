package com.deflight.splitscreenblocker.hooks;

import android.content.Context;
import hmgapi.hook.Hooks;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;

/** Installs split-screen hooks only on recognized ODEX builds. */
public final class Entry extends ModEntry {
    static final String PKG = "com.mobis.splitscreen.right";
    static final String BASE = "com.mobis.splitscreen.right.k.r";
    static final String SERVICE = "com.mobis.splitscreen.right.SplitScreenHome";
    static final String HYUNDAI = "cc1bd5131bee221ea3b5ed24a1c1d31a";
    static final String KIA_SHARED = "7b84ed639e85a711c4a9f2c78e33acf4";
    static final String KIA_K5 = "bd97b438aeb3d9c77510b5e2da4234f7";
    static final String ODEX = "/system/app/RightScreenHome.odex";
    static final int GONE = 8;

    @Override public void appLoaded(final ModContext mod, LoadedApp app) throws Throwable {
        if (!PKG.equals(app.packageName) || !PKG.equals(app.processName)) return;
        String hash = md5(ODEX);
        final boolean known = HYUNDAI.equals(hash) || KIA_SHARED.equals(hash) || KIA_K5.equals(hash);
        mod.log("ODEX MD5: " + hash + " (" + (known ? "recognized" : "unrecognized") + ")");
        // The Binder path can work on other builds; obfuscated Java hooks cannot be assumed compatible.
        installServiceHook(mod, app, known);
        if (!known) {
            mod.log("Unknown ODEX: Java hooks skipped; OEM shutdown is best-effort only");
            return;
        }

        Class<?> c = Class.forName(BASE, false, app.classLoader);
        if (c.getDeclaredMethod("bh", boolean.class).getReturnType() != void.class
                || c.getDeclaredMethod("show").getReturnType() != void.class
                || c.getDeclaredMethod("f", boolean.class, boolean.class).getReturnType() != boolean.class
                || c.getDeclaredMethod("ij").getReturnType() != boolean.class) {
            mod.log("Base hook signatures differ: Java hooks skipped");
            return;
        }
        final Field tab = c.getDeclaredField("Fw");
        if (!"android.widget.FrameLayout".equals(tab.getType().getName())) {
            mod.log("Tab field type differs: Java hooks skipped");
            return;
        }
        tab.setAccessible(true);

        // Block requests to open the split-screen panel, but allow requests to close it.
        final AtomicBoolean panelHit = new AtomicBoolean();
        Hooks.findAndHookMethod(BASE, app.classLoader, "bh", boolean.class, new MethodHook() {
            @Override protected void beforeHookedMethod(Param p) {
                if (mod.enabled() && Boolean.TRUE.equals(p.args[0])) {
                    first(mod, panelHit, "bh(true): blocked panel opening");
                    p.setResult(null);
                }
            }
        });
        mod.log("Hook installed: bh(Z)V");

        // Prevent the split-screen panel from being made visible.
        final AtomicBoolean showHit = new AtomicBoolean();
        Hooks.findAndHookMethod(BASE, app.classLoader, "show", new MethodHook() {
            @Override protected void beforeHookedMethod(Param p) {
                if (mod.enabled()) {
                    first(mod, showHit, "show(): blocked panel visibility");
                    p.setResult(null);
                }
            }
        });
        mod.log("Hook installed: show()V");

        // Change requests to show the slide-out tab into requests to hide it.
        final AtomicBoolean tabHit = new AtomicBoolean();
        Hooks.findAndHookMethod(BASE, app.classLoader, "f", boolean.class, boolean.class, new MethodHook() {
            @Override protected void beforeHookedMethod(Param p) {
                if (mod.enabled() && Boolean.TRUE.equals(p.args[0])) {
                    first(mod, tabHit, "f(ZZ)Z: changed tab show to hide");
                    p.args[0] = Boolean.FALSE;
                }
            }
        });
        mod.log("Hook installed: f(ZZ)Z");

        // Hide the slide-out tab after its window is initialized.
        final AtomicBoolean initHit = new AtomicBoolean();
        Hooks.findAndHookMethod(BASE, app.classLoader, "ij", new MethodHook() {
            @Override protected void afterHookedMethod(Param p) throws Throwable {
                if (!mod.enabled()) return;
                Object v = tab.get(p.thisObject);
                if (v != null) {
                    v.getClass().getMethod("setVisibility", int.class).invoke(v, GONE);
                    first(mod, initHit, "ij(): initialized tab hidden");
                }
            }
        });
        mod.log("Hook installed: ij()Z");

        // These OSD methods are mapped on the recognized V015 Kia and Hyundai images.
        installOsdHooks(mod, app);
    }

    private static void installOsdHooks(final ModContext mod, LoadedApp app) {
        final String osd = "com.mobis.splitscreen.right.r.i";
        try {
            Class<?> base = Class.forName(BASE, false, app.classLoader);
            if (base.getDeclaredMethod("canShowSSOsd", boolean.class, boolean.class, boolean.class).getReturnType() == boolean.class) {
                // Deny standard requests to display the right-side on-screen overlay.
                final AtomicBoolean hit = new AtomicBoolean();
                Hooks.findAndHookMethod(BASE, app.classLoader, "canShowSSOsd", boolean.class, boolean.class, boolean.class, new MethodHook() {
                    @Override protected void beforeHookedMethod(Param p) {
                        if (mod.enabled()) {
                            first(mod, hit, "canShowSSOsd(): denied OSD");
                            p.setResult(Boolean.FALSE);
                        }
                    }
                });
                mod.log("Hook installed: canShowSSOsd(ZZZ)Z");
            }
        } catch (Throwable e) { mod.log("OSD hook unavailable: canShowSSOsd: " + e); }
        try {
            Class<?> c = Class.forName(osd, false, app.classLoader);
            if (c.getDeclaredMethod("d", int.class, int.class, boolean.class).getReturnType() == void.class) {
                // Stop overlay requests that reach the shared entry point.
                final AtomicBoolean hit = new AtomicBoolean();
                Hooks.findAndHookMethod(osd, app.classLoader, "d", int.class, int.class, boolean.class, new MethodHook() {
                    @Override protected void beforeHookedMethod(Param p) {
                        if (mod.enabled()) {
                            first(mod, hit, "r.i.d(): blocked OSD request");
                            p.setResult(null);
                        }
                    }
                });
                mod.log("Hook installed: r.i.d(IIZ)V");
            }
        } catch (Throwable e) { mod.log("OSD hook unavailable: r.i.d: " + e); }
        try {
            Class<?> c = Class.forName(osd, false, app.classLoader);
            if (c.getDeclaredMethod("handleShow").getReturnType() == void.class) {
                // Prevent the overlay renderer from displaying the right-side pop-over.
                final AtomicBoolean hit = new AtomicBoolean();
                Hooks.findAndHookMethod(osd, app.classLoader, "handleShow", new MethodHook() {
                    @Override protected void beforeHookedMethod(Param p) {
                        if (mod.enabled()) {
                            first(mod, hit, "r.i.handleShow(): blocked OSD renderer");
                            p.setResult(null);
                        }
                    }
                });
                mod.log("Hook installed: r.i.handleShow()V");
            }
        } catch (Throwable e) { mod.log("OSD hook unavailable: r.i.handleShow: " + e); }
    }

    private static void installServiceHook(final ModContext mod, LoadedApp app, final boolean known) {
        try {
            Class<?> c = Class.forName(SERVICE, false, app.classLoader);
            if (!Context.class.isAssignableFrom(c)) throw new IllegalStateException("Not an Android Context");
            c.getDeclaredMethod("onCreate");
            // Start the shutdown scheduler when the OEM split-screen service is created.
            Hooks.findAndHookMethod(SERVICE, app.classLoader, "onCreate", new MethodHook() {
                @Override protected void afterHookedMethod(Param p) {
                    if (p.thisObject instanceof Context)
                        SplitScreenShutdown.start(mod, (Context) p.thisObject, known);
                }
            });
            mod.log("Hook installed: SplitScreenHome.onCreate() (OEM shutdown " + (known ? "verified build" : "fallback") + ")");
        } catch (Throwable e) {
            mod.log("OEM shutdown lifecycle hook unavailable: " + e);
        }
    }

    private static void first(ModContext mod, AtomicBoolean once, String message) {
        if (once.compareAndSet(false, true)) mod.log("Intercepted: " + message);
    }

    static String md5(String path) throws Exception {
        MessageDigest d = MessageDigest.getInstance("MD5");
        InputStream in = new FileInputStream(path);
        try {
            byte[] b = new byte[65536];
            for (int n; (n = in.read(b)) > 0; ) d.update(b, 0, n);
        } finally { in.close(); }
        StringBuilder s = new StringBuilder();
        for (byte x : d.digest()) s.append(String.format("%02x", x & 0xff));
        return s.toString();
    }
}
