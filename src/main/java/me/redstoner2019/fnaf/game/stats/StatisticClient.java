package me.redstoner2019.fnaf.game.stats;

import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatisticClient {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private List<JSONObject> requests = new ArrayList<>();
    private JSONObject response = new JSONObject();
    private boolean isConnected = false;

    public StatisticClient(String ip, int port) {
        Object WAIT_FOR_INIT = new Object();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread pinger = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Connecting to " + ip + ":" + port);
                                socket = new Socket(ip,port);
                                oos = new ObjectOutputStream(socket.getOutputStream());
                                ois = new ObjectInputStream(socket.getInputStream());
                            } catch (IOException e) {
                                System.err.println("Connection failed to " + ip + ":" + port);
                                throw new RuntimeException(e);
                            }
                            isConnected = true;
                            while (isConnected) {
                                try {
                                    Thread.sleep(2000);
                                    oos.writeObject(new String("Ping"));
                                } catch (SocketException e) {
                                    isConnected = false;
                                } catch (Exception e){

                                }
                            }
                        }
                    });
                    pinger.start();

                    boolean running = true;

                    synchronized (WAIT_FOR_INIT){
                        WAIT_FOR_INIT.notifyAll();
                    }

                    while (running){
                        try{
                            if(!requests.isEmpty()){
                                JSONObject toSend = requests.get(0);
                                oos.writeObject(toSend.toString());
                                JSONObject responseObject = new JSONObject((String) ois.readObject());
                                response = responseObject;
                                synchronized (toSend) {
                                    toSend.notifyAll();
                                }
                                synchronized (response){
                                    response.wait();
                                }
                                requests.remove(0);
                            } else Thread.sleep(0);
                        }catch (Exception e){
                            running = false;
                            e.printStackTrace();
                            System.err.println("Disconnected");
                            System.out.println(e.getLocalizedMessage());
                            isConnected = false;
                            synchronized (WAIT_FOR_INIT){
                                WAIT_FOR_INIT.notifyAll();
                            }
                        }
                    }
                    isConnected = false;
                    System.err.println("Disconnected");
                }catch (Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),"An error occured connecting to the Stat Server",JOptionPane.ERROR_MESSAGE);
                    isConnected = false;
                    System.err.println("Disconnected");
                    synchronized (WAIT_FOR_INIT){
                        WAIT_FOR_INIT.notifyAll();
                    }
                }
            }
        });
        t.start();
        synchronized (WAIT_FOR_INIT){
            try {
                WAIT_FOR_INIT.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public JSONObject sendRequest(JSONObject request) throws InterruptedException {
        this.requests.add(request);
        System.out.println(this.requests.size());
        synchronized (request){
            request.wait();
        }
        JSONObject localResponse = new JSONObject(response.toString());
        synchronized (response) {
            response.notifyAll();
        }
        return localResponse;
    }

    public JSONObject createEntry(String game, String challenge, long time, String username, float powerLeft, long timeLasted) throws Exception {
        JSONObject header = new JSONObject();
        JSONObject entry = new JSONObject();

        entry.put("time",time);
        entry.put("username",username);
        entry.put("power-left",powerLeft);
        entry.put("time-lasted",timeLasted);

        header.put("data",entry);
        header.put("game",game);
        header.put("challenge",challenge);
        header.put("header","add-entry");
        return sendRequest(header);
    }

    public JSONObject createEntry(String game, String challenge, JSONObject data) throws Exception {
        JSONObject header = new JSONObject();

        header.put("data",data);
        header.put("game",game);
        header.put("challenge",challenge);
        header.put("header","add-entry");
        return sendRequest(header);
    }

    public JSONObject createChallenge(String game, String challenge) throws Exception{
        JSONObject packet = new JSONObject();
        packet.put("header","create-challenge");
        packet.put("game",game);
        packet.put("challenge",challenge);
        return sendRequest(packet);
    }

    public JSONObject createGame(String game) throws Exception{
        JSONObject packet = new JSONObject();
        packet.put("header","create-game");
        packet.put("game",game);
        return sendRequest(packet);
    }
    public JSONObject getEntry(String uuid) throws Exception{
        JSONObject packet = new JSONObject();
        packet.put("header","get-entry");
        packet.put("uuid",uuid);
        return sendRequest(packet);
    }
    public JSONObject getEntries(String game, String challenge) throws Exception {
        JSONObject packet = new JSONObject();
        packet.put("header", "get-challenge");
        packet.put("game", game);
        packet.put("challenge", challenge);
        return sendRequest(packet);
    }
}
