package util;

import android.content.Context;

import com.android.sensorbergVolley.ServerError;
import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.model.BeaconId;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import static java.lang.System.arraycopy;
import static junit.framework.Assert.fail;

public class Utils {

    static Random random = new Random();

    public static final long ONE_ADVERTISEMENT_INTERVAL = 100;
    public static final long ONE_SECOND = 1000;
    public static final long TEN_SECONDS = 10 * ONE_SECOND;
    public static final long THIRTY_SECONDS = 30 * ONE_SECOND;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    public static final long FIVE_MINUTES = 5 * ONE_MINUTE;
    public static final long TEN_MINUTES = 10 * ONE_MINUTE;
    public static final long THIRTY_MINUTES = 30 * ONE_MINUTE;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long VERY_LONG_TIME = Long.MAX_VALUE / 2L;

    public static final long EXIT_TIME = 9 * ONE_SECOND;
    public static final long EXIT_TIME_HAS_PASSED = EXIT_TIME + 1;
    public static final long EXIT_TIME_NOT_YET = EXIT_TIME -1;



    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static JSONObject getRawResourceAsJSON(int resourceID, Context context) throws IOException, JSONException {
        String theString = getRawResourceAsString(resourceID, context);
        return new JSONObject(theString);
    }

    public static String getRawResourceAsString(int resourceID, Context context) throws IOException {
        return IOUtils.toString(context.getResources().openRawResource(resourceID));
    }

    public static BeaconId getRandomBeaconId() {
        return new BeaconId(TestConstants.BEACON_PROXIMITY_ID, random.nextInt() % 65535, random.nextInt() % 65535 );
    }

    public static byte[] wrapWithZeroBytes(byte[] bytesForFakeScan, int length) {
        byte[] value = new byte[length];
        arraycopy(bytesForFakeScan, 0 , value, 0, bytesForFakeScan.length);
        return value;
    }

    public static void failWithVolleyError(VolleyError volleyError, String s){
        Throwable error = volleyError;
        while (error.getCause() != null){
            error = error.getCause();
            if (error instanceof ServerError){
                ServerError serverError = (ServerError) error;
                if (serverError.networkResponse != null) {
                    fail(String.format("%1$s:\nStatus:%2$d\nData:%3$s",s , serverError.networkResponse.statusCode, new String(serverError.networkResponse.data)));
                    return;
                }
            }
        }
        fail(volleyError.getMessage());
    }
}
