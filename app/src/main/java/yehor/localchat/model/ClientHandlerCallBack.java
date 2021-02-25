package yehor.localchat.model;

public interface ClientHandlerCallBack {
    void handleMessage(String message, String from, String to);
}
