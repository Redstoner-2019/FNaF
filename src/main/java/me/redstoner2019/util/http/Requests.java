package me.redstoner2019.util.http;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Requests {
    public static JSONObject request(Method method, String address, JSONObject data){
        return request(method,address,data,new HashMap<>());
    }
    public static JSONObject request(Method method, String address, JSONObject data, HashMap<String, String> headers){
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(address))
                    .header("Content-Type","application/json");

            for(String header : headers.keySet()){
                requestBuilder.header(header,headers.get(header));
            }

            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(data.toString())).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.toString());
            json.put("code", response.statusCode());
            json.put("body", response.body());
            return json;
        } catch (Exception e) {
            e.printStackTrace();

            JSONObject json = new JSONObject();
            json.put("code", -1);
            return json;
        }
    }
}

