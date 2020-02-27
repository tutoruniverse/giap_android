package ai.gotit.giap.common;

/**
 * Implement this to allow GIAP behave in-sync with your current custom offline logic
 */
public interface OfflineMode {

    /**
     * Returns true if offline-mode is active on the client. When true GIAP will not start
     * new connections, but current active connections will not be interrupted.
     *
     * @return true if offline mode is active, false otherwise
     */
    boolean isOffline();

}