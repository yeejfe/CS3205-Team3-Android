package cs3205.subsystem3.health.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.base.Objects;
import java.util.UUID;

/**
 * Created by Yee on 09/18/17.
 */

public class Session {

    @NonNull
    private final String id;

    @Nullable
    private final String userId;

    @Nullable
    private final String timestamp;

    private final boolean haveUploaded;

    public Session(@Nullable String userId, @Nullable String timestamp) {
        this(userId, timestamp, UUID.randomUUID().toString(), false);
    }

    public Session(@Nullable String userId, @Nullable String timestamp, @NonNull String id) {
        this(userId, timestamp, id, false);
    }

    public Session(@Nullable String userId, @Nullable String timestamp, boolean haveUploaded) {
        this(userId, timestamp, UUID.randomUUID().toString(), haveUploaded);
    }

    public Session(@Nullable String userId, @Nullable String timestamp,
                @NonNull String id, boolean haveUploaded) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
        this.haveUploaded = haveUploaded;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    @Nullable
    public String getUserIdForList() {
        if (!Strings.isNullOrEmpty(userId)) {
            return userId;
        } else {
            return timestamp;
        }
    }

    @Nullable
    public String getTimestamp() {
        return timestamp;
    }

    public boolean isUploaded() {
        return haveUploaded;
    }

    public boolean isActive() {
        return !haveUploaded;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(userId) &&
                Strings.isNullOrEmpty(timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equal(id, session.id) &&
                Objects.equal(userId, session.userId) &&
                Objects.equal(timestamp, session.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, userId, timestamp);
    }

    @Override
    public String toString() {
        return "User id: " + userId;
    }
}
