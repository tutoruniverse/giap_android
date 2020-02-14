package ai.gotit.giap.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import ai.gotit.giap.common.OfflineMode;
import ai.gotit.giap.exception.GIAPNetworkException;
import ai.gotit.giap.service.ConfigManager;

/**
 * An HTTP utility class for internal use in the GIAP library. Not thread-safe.
 */
public class Request {

    private static final int MIN_UNAVAILABLE_HTTP_RESPONSE_CODE = HttpURLConnection.HTTP_INTERNAL_ERROR;
    private static final int MAX_UNAVAILABLE_HTTP_RESPONSE_CODE = 599;

    @SuppressLint("MissingPermission")
    @SuppressWarnings("MissingPermission")
    public boolean isOnline(Context context, OfflineMode offlineMode) {
        if (onOfflineMode(offlineMode)) return false;

        boolean isOnline;
        try {
            final ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo == null) {
                isOnline = true;
                Logger.warn("A default network has not been set so we cannot be certain whether we are offline");
            } else {
                isOnline = netInfo.isConnectedOrConnecting();
                Logger.log("ConnectivityManager says we " + (isOnline ? "are" : "are not") + " online");
            }
        } catch (final SecurityException e) {
            isOnline = true;
            Logger.warn("Don't have permission to check connectivity, will assume we are online");
        }
        return isOnline;
    }

    private boolean onOfflineMode(OfflineMode offlineMode) {
        boolean onOfflineMode;

        try {
            onOfflineMode = offlineMode != null && offlineMode.isOffline();
        } catch (Exception e) {
            onOfflineMode = false;
            Logger.warn(e, "Client State should not throw exception, will assume is not on offline mode");
        }

        return onOfflineMode;
    }

    public byte[] performRequest(String endpointUrl, Map<String, Object> params, SSLSocketFactory socketFactory) throws GIAPNetworkException, IOException {
        Logger.log("Attempting request to " + endpointUrl);

        byte[] response = null;

        // the while(retries) loop is a workaround for a bug in some Android HttpURLConnection
        // libraries- The underlying library will attempt to reuse stale connections,
        // meaning the second (or every other) attempt to connect fails with an EOFException.
        // Apparently this nasty retry logic is the current state of the workaround art.
        int retries = 0;
        int requestRetryTimes = ConfigManager.getInstance().getRequestRetryTimes();
        boolean succeeded = false;
        while (retries < requestRetryTimes && !succeeded) {
            InputStream in = null;
            OutputStream out = null;
            BufferedOutputStream bout = null;
            HttpURLConnection connection = null;

            try {
                final URL url = new URL(endpointUrl);
                connection = (HttpURLConnection) url.openConnection();
                if (null != socketFactory && connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
                }

                connection.setConnectTimeout(2000);
                connection.setReadTimeout(30000);
                if (null != params) {
                    Uri.Builder builder = new Uri.Builder();
                    for (Map.Entry<String, Object> param : params.entrySet()) {
                        builder.appendQueryParameter(param.getKey(), param.getValue().toString());
                    }
                    String query = builder.build().getEncodedQuery();

                    connection.setFixedLengthStreamingMode(query.getBytes().length);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    out = connection.getOutputStream();
                    bout = new BufferedOutputStream(out);
                    bout.write(query.getBytes(StandardCharsets.UTF_8));
                    bout.flush();
                    bout.close();
                    bout = null;
                    out.close();
                    out = null;
                }
                in = connection.getInputStream();
                response = slurp(in);
                in.close();
                in = null;
                succeeded = true;
            } catch (final EOFException e) {
                Logger.error(e, "Failure to connect, likely caused by a known issue with Android lib. Retrying.");
                retries = retries + 1;
            } catch (final IOException e) {
                int responseCode = connection.getResponseCode();
                if (responseCode >= MIN_UNAVAILABLE_HTTP_RESPONSE_CODE && responseCode <= MAX_UNAVAILABLE_HTTP_RESPONSE_CODE) {
                    throw new GIAPNetworkException(responseCode);
                } else {
                    throw e;
                }
            } finally {
                if (null != bout)
                    try {
                        bout.close();
                    } catch (final IOException e) {
                        ;
                    }
                if (null != out)
                    try {
                        out.close();
                    } catch (final IOException e) {
                        ;
                    }
                if (null != in)
                    try {
                        in.close();
                    } catch (final IOException e) {
                        ;
                    }
                if (null != connection)
                    connection.disconnect();
            }
        }
        if (retries >= requestRetryTimes) {
            Logger.error("Could not connect to GIAP Core Platform after " + retries + " retries.");
        }
        return response;
    }

    private static byte[] slurp(final InputStream inputStream)
            throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[8192];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }
}