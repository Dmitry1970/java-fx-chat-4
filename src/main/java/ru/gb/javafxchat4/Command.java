package ru.gb.javafxchat4;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {
    AUTH("/auth") {    // /auth login1 pass1

        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER);
            return new String[]{split[1], split[2]};
        }
    },
    AUTHOK("/authok") {
        @Override
        public String[] parse(String commandText) { //   /authok nick1
            final String[] split = commandText.split(TOKEN_DELIMiTER);
            return new String[]{split[1]};
        }
    },
    END("/end") {
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    PRIVATE_MESSAGE("/w") {  // /w nick1 long lond message

        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER, 3);
            return new String[]{split[1], split[2]};
        }
    },
    CLIENTS("/clients") {   //   /clients nick1 nick2 nick3 -  список ников

        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER);
            final String[] nicks = new String[split.length - 1];
            for (int i = 0; i < nicks.length; i++) {
                nicks[i] = split[i + 1];
            }
            return nicks;
        }
    },
    ERROR("/error") {    //   /error error message

        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER, 2);
            return new String[]{split[1]};
        }
    },
    MESSAGE("/message") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER, 2);
            return new String[]{split[1]};
        }
    };

    private final String command;   // хранит в себе текст команды enum Command
    static final String TOKEN_DELIMiTER = "\\p{Blank}+";
    static final Map<String, Command> commandMap = Arrays.stream(values())
            .collect(Collectors.toMap(Command::getCommand, Function.identity()));  // stream, кот. превращает массив в Map


    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isCommand(String message) {       // распознавание команда или нет
        return message.startsWith("/");
    }

    public static Command getCommand(String message) {       // передаем сообщение, которое к нам пришло: /w n ndjfkfg
        if (!isCommand(message)) {                           // проверяем команда это или нет
            throw new RuntimeException("'" + message + "' is not a command");
        }
        final String cmd = message.split(TOKEN_DELIMiTER, 2)[0];    // делим на 2 токена, в нулевом индексе лежит /w
        final Command command = commandMap.get(cmd);
        if (command == null) {
            throw new RuntimeException("Unknown command '" + cmd + "'");
        }
        return command;


    }

    public abstract String[] parse(String commandText);  // текст сообщения преврашем в команду и парсим

    public String collectMessage(String... params) {    //params заменяет маcсив (String[] {})
        return this.command + " " + String.join(" ", params);
    }
}
