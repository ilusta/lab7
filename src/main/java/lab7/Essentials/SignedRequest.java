package lab7.Essentials;


import java.net.SocketAddress;

public class SignedRequest{
    SocketAddress clientID = null;
    Object request = null;

    public SignedRequest(SocketAddress clientAddr, Object object) {
        this.clientID = clientAddr;
        this.request = object;
    }

    public SocketAddress getClientID(){
        return this.clientID;
    }

    public Object getRequest(){
        return this.request;
    }
}
