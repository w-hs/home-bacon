package de.whs.homebaconcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by pausf on 05.01.2016.
 *
 * Vorhersage-Modell für Messwerte von Bluetooth-Tags.
 */
public class PredictionModel implements Serializable {
    private static final long serialVersionUID = 4520404624482712866L;

    private float accuracy;
    private float minRssi = -106.0f;
    private float maxRssi = -40.0f;
    private float[][] W;
    private float[] b;
    private Map<String, Integer> rooms = new HashMap<>();
    private Map<String, Integer> tags = new HashMap<>();

    public static PredictionModel loadFromPreferences(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String modelBase64 = prefs.getString("Model", null);

            if (modelBase64 != null) {
                byte[] modelBytes =  Base64.decode(modelBase64, Base64.DEFAULT);
                Object modelObject = Serializer.deserialize(modelBytes);
                return (PredictionModel)modelObject;
            }
        }
        catch (Exception ex) {
            Log.e(Constants.DEBUG_TAG, "Could not load prediction model from preferences");
            Log.e(Constants.DEBUG_TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public void saveToPreferences(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            byte[] modelBytes = Serializer.serialize(this);
            String modelBase64 = Base64.encodeToString(modelBytes, Base64.DEFAULT);
            editor.putString("Model", modelBase64);
            editor.commit();
        }
        catch (Exception ex) {
            Log.e(Constants.DEBUG_TAG, "Could not save prediction model to preferences");
            Log.e(Constants.DEBUG_TAG, ex.getMessage());
        }
    }

    public static PredictionModel getPredictionModelFor(List<String> csvData) throws IOException, JSONException {
        URL url = new URL("http://87.106.16.104/cgi-bin/learn-scans.py");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/csv; charset=utf-8");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        for (String line : csvData) {
            byte[] utf8Bytes = line.getBytes("utf-8");
            outputStream.write(utf8Bytes);
        }
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = connection.getInputStream();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = inputReader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        inputReader.close();
        String responseString = response.toString();

        return parseJSON(responseString);
    }

    public static PredictionModel parseJSON(String jsonString) throws JSONException {
        PredictionModel result = new PredictionModel();

        JSONObject object = new JSONObject(jsonString);

        result.setAccuracy((float) object.getDouble("acc"));
        result.setMinRssi((float) object.getDouble("min_rssi"));
        result.setMaxRssi((float) object.getDouble("max_rssi"));

        JSONArray bArray = object.getJSONArray("b");
        float[] b = new float[bArray.length()];
        for (int i = 0; i < bArray.length(); ++i) {
            b[i] = (float)bArray.getDouble(i);
        }
        result.setB(b);

        JSONArray WArray = object.getJSONArray("W");
        int width = WArray.length();
        int height = WArray.getJSONArray(0).length();
        float[][] W = new float[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                W[y][x] = (float)WArray.getJSONArray(x).getDouble(y);
            }
        }
        result.setW(W);

        JSONObject roomsObject = object.getJSONObject("rooms");
        Iterator<String> roomKeys = roomsObject.keys();
        while(roomKeys.hasNext()) {
            String roomName = roomKeys.next();
            int roomIndex = roomsObject.getInt(roomName);
            result.getRooms().put(roomName, roomIndex);
        }

        JSONObject tagsObject = object.getJSONObject("tags");
        Iterator<String> tagKeys = tagsObject.keys();
        while(tagKeys.hasNext()) {
            String tagAddress = tagKeys.next();
            int tagIndex = tagsObject.getInt(tagAddress);
            result.getTags().put(tagAddress, tagIndex);
        }

        return result;
    }

    // Eingabe: Eine Messung aus n Paaren von Bluetooth-Adresse und Messwert
    // Ausgabe: Eine Vorhersage aus m Paaren von Raumname und W'keit
    public Map<String, Float> predict(Map<String, BeaconScan> scans) {
        // Eingabevektor x auf Basis der Scanwerte ermittlen und normalisieren
        float[] x = new float[tags.size()];
        for (String address : tags.keySet()) {
            BeaconScan scan = scans.get(address);
            if (scan != null) {
                Integer tagIndex = tags.get(address);
                float normalizedRssi = normalize(scan.getRssi());
                x[tagIndex] = normalizedRssi;
            }
        }

        float[] mulResult = multiply(W, x);
        float[] addResult = add(mulResult, b);
        float[] y = softmax(addResult);

        Map<String, Float> result = new HashMap<>();
        for (String room : rooms.keySet()) {
            Integer roomIndex = rooms.get(room);
            result.put(room, y[roomIndex]);
        }
        return result;
    }

    private float normalize(int rssi) {
        float rssiRange = maxRssi - minRssi;
        float rssiNormalizer = 1.0f / rssiRange;
        return rssiNormalizer * (rssi - minRssi);
    }

    private float[] multiply(float[][] W, float[] x_) {
        int height = W.length;
        int width = W[0].length;

        float[] result = new float[height];

        for (int y = 0; y < height; ++y) {
            float sum = 0.0f;
            for (int x = 0; x < width; ++x) {
                sum += W[y][x] * x_[x];
            }
            result[y] = sum;
        }

        return result;
    }

    private float[] add(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; ++i) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    private float[] softmax(float[] y) {
        float[] result = new float[y.length];

        float sum = 0.0f;
        for (int i = 0; i < y.length; ++i) {
            result[i] = (float) Math.exp(y[i]);
            sum += result[i];
        }
        float invSum = 1.0f / sum;
        for (int i = 0; i < y.length; ++i) {
            result[i] *= invSum;
        }
        return result;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public Map<String, Integer> getRooms() {
        return rooms;
    }

    public Map<String, Integer> getTags() {
        return tags;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setW(float[][] w) {
        W = w;
    }

    public void setB(float[] b) {
        this.b = b;
    }

    public void setMinRssi(float minRssi) {
        this.minRssi = minRssi;
    }

    public void setMaxRssi(float maxRssi) {
        this.maxRssi = maxRssi;
    }
}
