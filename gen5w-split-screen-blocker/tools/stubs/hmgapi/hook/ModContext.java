package hmgapi.hook;
/** Compile-only stand-in for HMG-API's hook API; never packaged. */
public final class ModContext {
    public void log(String message) {}
    public boolean enabled() { return true; }
    public String settingString(String key, String fallback) { return fallback; }
}
