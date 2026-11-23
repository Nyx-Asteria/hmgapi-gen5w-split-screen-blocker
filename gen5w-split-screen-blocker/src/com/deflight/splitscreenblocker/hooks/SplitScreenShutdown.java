package com.deflight.splitscreenblocker.hooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcel;
import hmgapi.hook.ModContext;
import java.io.IOException;

/** Closes the OEM panel after startup and driver login without modifying system files. */
final class SplitScreenShutdown {
    private static final String SERVICE = Entry.SERVICE;
    private static final String DESCRIPTOR = "com.hkmc.split.manager.ISplitScreenInterfaces";
    private static final String LOGIN = "com.hkmc.intent.action.USER_LOGGED_IN";
    private static final int READY = 0x28;
    private static final int OFF = 0x0a;
    private static boolean started;
    private static Handler worker;
    private static Runnable pending;
    private static int attempts;

    private SplitScreenShutdown() {}

    static synchronized void start(final ModContext mod, Context context, final boolean known) {
        if (started) {
            mod.log("OEM shutdown scheduler already registered");
            return;
        }
        HandlerThread thread = new HandlerThread("split-screen-shutdown");
        thread.start();
        worker = new Handler(thread.getLooper());
        pending = new Runnable() {
            @Override public void run() {
                if (!mod.enabled()) {
                    mod.log("OEM shutdown skipped: mod disabled");
                    return;
                }
                int number = ++attempts;
                try {
                    mod.log("OEM shutdown attempt " + number + "/15" + (known ? "" : " (unknown firmware fallback)"));
                    int ready = transact(READY);
                    if (ready == 0) throw new IOException("service not ready");
                    mod.log("OEM service ready; requesting split-screen OFF");
                    int response = transact(OFF, 0, 1, 0);
                    mod.log("OEM split-screen OFF transaction returned " + response);
                    attempts = 0;
                } catch (Throwable e) {
                    mod.log("OEM shutdown attempt " + number + " failed: " + e);
                    if (number < 15) worker.postDelayed(pending, 2000);
                    else mod.log("OEM shutdown stopped after 15 attempts");
                }
            }
        };
        try {
            context.registerReceiver(new BroadcastReceiver() {
                @Override public void onReceive(Context c, Intent intent) {
                    mod.log("Driver login detected; scheduling OEM shutdown");
                    schedule(3000);
                }
            }, new IntentFilter(LOGIN));
            started = true;
            mod.log("OEM driver-login receiver registered");
            schedule(10000);
            mod.log("OEM startup shutdown scheduled in 10 seconds");
        } catch (Throwable e) {
            mod.log("OEM shutdown receiver registration failed: " + e);
            thread.quit();
            worker = null;
            pending = null;
        }
    }

    private static synchronized void schedule(long delay) {
        if (worker == null || pending == null) return;
        worker.removeCallbacks(pending);
        attempts = 0;
        worker.postDelayed(pending, delay);
    }

    /** Check that the split-screen service is ready, then ask it to close the panel. */
    private static int transact(int code, int... args) throws Exception {
        IBinder binder = (IBinder) Class.forName("android.os.ServiceManager")
                .getMethod("getService", String.class).invoke(null, SERVICE);
        if (binder == null || !binder.isBinderAlive()
                || !DESCRIPTOR.equals(binder.getInterfaceDescriptor()))
            throw new IOException("OEM split-screen service/descriptor unavailable");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            for (int arg : args) data.writeInt(arg);
            if (!binder.transact(code, data, reply, 0)) throw new IOException("OEM transaction refused");
            reply.readException();
            if (reply.dataAvail() != 4) throw new IOException("Unexpected OEM reply");
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
