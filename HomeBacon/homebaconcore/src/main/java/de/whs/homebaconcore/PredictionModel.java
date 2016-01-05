package de.whs.homebaconcore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pausf on 05.01.2016.
 */
public class PredictionModel {

    private float accuracy;
    private float[][] W;
    private float[] b;
    private Map<String, Integer> rooms = new HashMap<>();
    private Map<String, Integer> tags = new HashMap<>();

    public static PredictionModel getPredictionModelFor(String csvData) throws IOException, JSONException {
        URL url = new URL("http://87.106.16.104/cgi-bin/learn-scans.py");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/csv; charset=utf-8");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeUTF(csvData);
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = connection.getInputStream();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuffer response = new StringBuffer();
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
        JSONArray bArray = object.getJSONArray("b");
        float[] b = new float[bArray.length()];
        for (int i = 0; i < bArray.length(); ++i) {
            b[i] = (float)bArray.getDouble(i);
        }

        JSONArray WArray = object.getJSONArray("W");
        int height = WArray.length();
        int width = WArray.getJSONArray(0).length();
        float[][] W = new float[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                W[y][x] = (float)WArray.getJSONArray(y).getDouble(x);
            }
        }

        JSONObject roomsObject = object.getJSONObject("rooms");
        Iterator<String> roomKeys = roomsObject.keys();
        while(roomKeys.hasNext()) {
            String roomName = roomKeys.next();
            int roomIndex = roomsObject.getInt(roomName);
            result.getRooms().put(roomName, roomIndex);
        }

        JSONObject tagsObject = object.getJSONObject("rooms");
        Iterator<String> tagKeys = tagsObject.keys();
        while(tagKeys.hasNext()) {
            String tagAddress = tagKeys.next();
            int tagIndex = tagsObject.getInt(tagAddress);
            result.getRooms().put(tagAddress, tagIndex);
        }

        return result;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float[][] getW() {
        return W;
    }

    public float[] getB() {
        return b;
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

    public void setRooms(Map<String, Integer> rooms) {
        this.rooms = rooms;
    }

    public void setTags(Map<String, Integer> tags) {
        this.tags = tags;
    }
}
