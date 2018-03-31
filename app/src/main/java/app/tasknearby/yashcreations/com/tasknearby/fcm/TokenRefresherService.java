package app.tasknearby.yashcreations.com.tasknearby.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

/**
 * @author vermayash8
 */
public final class TokenRefresherService extends IntentService {

    private static final String TAG = TokenRefresherService.class.getSimpleName();

    public TokenRefresherService() {
        super(TokenRefresherService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "FCM token:" + fcmToken);
        if (fcmToken == null) {
            try {
                Log.d(TAG, "Deleting instance id");
                FirebaseInstanceId.getInstance().deleteInstanceId();
                fcmToken = FirebaseInstanceId.getInstance().getToken();
                if (fcmToken != null) {
                    // Ideally we should save it to shared preferences here. But since we're not
                    // using the token at all in our app, we are not doing that.
                    Log.i(TAG, "Token generated successfully." + fcmToken);
                } else {
                    FirebaseAnalytics.getInstance(this).logEvent("fcm_token_failed", new Bundle());
                    Log.e(TAG, "Unable to generate token");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
