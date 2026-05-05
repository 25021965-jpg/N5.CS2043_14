package client.network;

import common.Command;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientSocket {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Consumer<String> handler;

    public ClientSocket() throws IOException {
        socket = new Socket("localhost", 9999);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        listen();
    }

    public void listen() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (handler != null) handler.accept(msg);
                }
            } catch (Exception e) {
                if (handler != null) handler.accept("DISCONNECTED");
            }
        }).start();
    }

    public void setHandler(Consumer<String> handler) {
        this.handler = handler;
    }

    private void send(Command cmd, String... args) {
        out.println(CommandBuilder.build(cmd, args));
    }

    // ===== COMMANDS =====

    public void login(String email, String password) {
        send(Command.LOGIN, email, password);
    }

    public void register(String username, String email, String password, String role) {
        send(Command.REGISTER, username, email, password, role);
    }

    public void list() {
        send(Command.LIST);
    }

    public void bid(String id, String amount) {
        send(Command.BID, id, amount);
    }
}
