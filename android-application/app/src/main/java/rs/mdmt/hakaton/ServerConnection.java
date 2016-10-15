package rs.mdmt.hakaton;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketOptions;

public class ServerConnection
{


    private static final String SERVER_IP = "ws://10.66.47.71:8181/";

    private static ServerConnection instance = new ServerConnection();
    private static WebSocketConnection socket;
    private static WebSocketOptions options;
    private static ServerHandler handler;

    public static class Request
    {
        public static final int DATA = 0;
        public static final int GET_ID = 1;
        public static final int INSERT_SHELTER = 2;
        public static final int GET_SHELTER = 3;
        public static final int GET_ROUTES = 4;
        public static final int UPLOAD_SHELTER = 7;
        public static final int UPDATE_SHELTER = 8;
        public static final int INSERT_ROUTE = 9;


        private Request() {}
    }

    public static class Response
    {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;

        private Response() {}
    }

    private ServerConnection()
    {
        socket = new WebSocketConnection();
        handler = new ServerHandler();
        options = new WebSocketOptions();
        options.setMaxFramePayloadSize(options.getMaxFramePayloadSize() * 10);
        options.setMaxMessagePayloadSize(options.getMaxMessagePayloadSize() * 10);
    }

    public static void open()
    {
        try
        {
            socket.connect(SERVER_IP, handler, options);
        }
        catch (WebSocketException e)
        {
            e.printStackTrace();
        }
    }

    public static void send(String text)
    {
        if (socket.isConnected())
            socket.sendTextMessage(text);
    }

    public static void send(byte[] bytes)
    {
        if (socket.isConnected())
            socket.sendBinaryMessage(bytes);
    }

    public static void sendRaw(byte[] bytes)
    {
        if (socket.isConnected())
            socket.sendRawTextMessage(bytes);
    }

    public static ServerHandler getHandler()
    {
        return handler;
    }

    public static boolean isOpen()
    {
        return socket.isConnected();
    }

    public static void getData()
    {
        JsonObject json = new JsonObject();
        json.addProperty("type", Request.DATA);
        Log.d("json", json.toString());

        send(json.toString());
    }

    public static void getUserId()
    {
        JsonObject json = new JsonObject();
        json.addProperty("type", Request.GET_ID);

        send(json.toString());
    }

    public static void getShelter(String id)
    {
        JsonObject json = new JsonObject();
        json.addProperty("type", Request.GET_SHELTER);
        json.addProperty("data", id);

        send(json.toString());
    }

    public static void getRoutes()
    {
        JsonObject json = new JsonObject();
        json.addProperty("type", Request.GET_ROUTES);

        send(json.toString());
    }
    public static void uploadShelter(JSONObject object)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("type", Request.INSERT_SHELTER);
            json.put("data", object);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        send(json.toString());

    }
    public static void updateShelter(JSONObject object)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("type", Request.UPDATE_SHELTER);
            json.put("data", object);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        send(json.toString());
    }
}
