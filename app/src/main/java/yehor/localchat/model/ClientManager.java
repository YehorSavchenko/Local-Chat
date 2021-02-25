package yehor.localchat.model;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import yehor.localchat.data_base.DBModel;
import yehor.localchat.data_base.DBProvider;
import yehor.localchat.presentation.MainPresenterRealize;
import yehor.localchat.presentation.presentations_interface.MainPresenter;

import static yehor.localchat.tags.Tags.CLIENT;
import static yehor.localchat.tags.Tags.SOCKET;

public class ClientManager implements Runnable {
    private ServerSocket serverSocket;
    private ClientHandlerCallBack clientHandlerCallBack;

    private Boolean isActive;


    private HashMap<String, ClientHandler> clients = new HashMap<>();

    public ClientManager(ClientHandlerCallBack callBack) {
        this.clientHandlerCallBack = callBack;
    }

    public Boolean getActive() {
        return isActive;
    }

    @Override
    public void run() {
        isActive = true;
        while (getActive()) {
            try {
                Log.i(CLIENT, "WAITING FOR CLIENT");

                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlerCallBack);
                clientHandler.start();
                clients.put(clientSocket.getInetAddress().toString().substring(1), clientHandler);

                Log.i(CLIENT, "ACCEPTED");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        isActive = false;
    }

    public void sendMessage(String receiveRedress, String message) {
        try {
            if (!clients.containsKey(receiveRedress)) {
                creatClient(receiveRedress);
            }
            clients.get(receiveRedress).sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void creatClient(String receiveRedress) {
        try {
            Socket clientSocket = new Socket(receiveRedress, 7777);
            ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlerCallBack);
            clientHandler.start();
            clients.put(receiveRedress, clientHandler);
            Log.i("CL", receiveRedress + " " + clientSocket.getLocalAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createServerSocket(ClientHandlerCallBack callBack) {
        try {
            this.serverSocket = new ServerSocket(7777);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServerConnection() {
        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                Log.e(SOCKET, "We can't close SOCKET");
            } finally {
                this.serverSocket = null;
            }
        }
        this.serverSocket = null;
    }

    public void closeClients() {
        clients.values().forEach(clientHandler -> clientHandler.closeSocket());
        stop();
    }
}
