package lab7.Essentials;

import java.io.Serializable;

public class Request implements Serializable {

    private String user = null;
    private String password = null;
    private Serializable info = null;

    public Request(String user, String password, Serializable info){
        this.user = user;
        this.password = password;
        this.info = info;
    }

    public Serializable getInfo() {
        return info;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
