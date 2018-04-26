package med.kamili.rachid.practiceapp.utils;

import timber.log.Timber;

public class FakeCrashLibrary {
    public static void log(int priority, String tag, String message) {
        //add log entry to circular buffer.
        Timber.tag(tag).d(message);
    }

    public static void logWarning(Throwable t) {
        //report non-fatal warning.
        Timber.tag("non-fatal warning").d(t);
    }

    public static void logError(Throwable t) {
        //report non-fatal error.
        Timber.tag("non-fatal error").d(t);
    }

    private FakeCrashLibrary() {
        throw new AssertionError("No instances.");
    }

}
