package client.network;

import common.Command;

public class MessageParser {

    private Command command;
    private String[] args;

    public MessageParser(String message) {
        String[] parts = message.split("\\|");

        command = Command.from(parts[0]);

        args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
    }

    public Command getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }
}
