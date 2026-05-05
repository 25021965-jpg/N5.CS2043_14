package client.network;

import common.Command;

public class CommandBuilder {

    public static String build(Command cmd, String... args) {
        StringBuilder sb = new StringBuilder(cmd.name());

        for (String arg : args) {
            sb.append(" ").append(arg);
        }

        return sb.toString();
    }
}
