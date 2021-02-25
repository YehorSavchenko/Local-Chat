package yehor.localchat.model;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static yehor.localchat.tags.Tags.CLIENT;

public class ClientHandler extends Thread {

    //Final
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket clientSocket;
    private ClientHandlerCallBack callBack;
    private final String from;

    private Boolean isActive;


    public ClientHandler(Socket clientSocket, ClientHandlerCallBack clientHandlerCallBack) throws IOException {
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
        this.clientSocket = clientSocket;
        this.callBack = clientHandlerCallBack;
        this.from = clientSocket.getInetAddress().toString().substring(1);
    }

    public Boolean getActive() {
        return isActive;
    }

    public void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        dos.writeUTF(message);
    }

    @Override
    public void run() {
        isActive = true;
        try {
            while (getActive()) {
                String s = dis.readUTF();
                callBack.handleMessage(s, this.from, "me");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
