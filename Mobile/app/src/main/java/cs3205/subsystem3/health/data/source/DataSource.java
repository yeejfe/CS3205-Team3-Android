package cs3205.subsystem3.health.data.source;

import android.support.annotation.NonNull;

import java.util.List;

import cs3205.subsystem3.health.model.Session;

/**
 * Created by Yee on 09/17/17.
 */

public interface DataSource {

    interface LoadSessionsCallback {

        void onSessionsLoaded(List<Session> sessions);

        void onDataNotAvailable();
    }

    interface GetSessionCallback {

        void onSessionLoaded(Session session);

        void onDataNotAvailable();
    }

    void getSessions(@NonNull LoadSessionsCallback callback);

    void getSession(@NonNull String sessionId, @NonNull GetSessionCallback callback);

    void saveSession(@NonNull Session session);

    void completeSession(@NonNull Session session);

    void completeSession(@NonNull String sessionId);

    void activateSession(@NonNull Session session);

    void activateSession(@NonNull String sessionId);

    void clearCompletedSessions();

    void refreshSessions();

    void deleteAllSessions();

    void deleteSession(@NonNull String sessionId);
}
