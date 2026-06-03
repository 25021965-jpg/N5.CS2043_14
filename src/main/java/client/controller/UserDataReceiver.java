package client.controller;

import client.network.ClientSocket;
import model.Entity.User.User;

public interface UserDataReceiver {

    void setClient(ClientSocket client);

    void setUser(User user);
}