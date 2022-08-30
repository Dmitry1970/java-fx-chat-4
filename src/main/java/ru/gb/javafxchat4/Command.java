package ru.gb.javafxchat4;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {   // [0]    [1]   [2]  // enum не можем унаследовать от класса
    AUTH("/auth") {    // /auth login1 pass1  - входящее сообщение
        @Override
        public String[] parse(String commandText) {     // имплементируем метод
            final String[] split = commandText.split(TOKEN_DELIMiTER);   // разбиваем на части(токены) по пробелам
            return new String[]{split[1], split[2]};  // [1] - логин, [2] - пароль
        }
    },
    AUTHOK("/authok") {
        @Override                                   //      [0]  [1]
        public String[] parse(String commandText) { //   /authok nick1
            final String[] split = commandText.split(TOKEN_DELIMiTER);
            return new String[]{split[1]};   // [1] - nick
        }
    },
    END("/end") {
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },                       // [0] [1]      [2]
    PRIVATE_MESSAGE("/w") {  // /w nick1 long long message
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER, 3);  // делим строку на 3 части(limit: 3)
            return new String[]{split[1], split[2]};  // [1] - nick    [2] - всё сообщение
        }
    },
    CLIENTS("/clients") {   //   /clients nick1 nick2 nick3 -  список клиентов
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER);
            final String[] nicks = new String[split.length - 1]; // массив с никами
            for (int i = 0; i < nicks.length; i++) {
                nicks[i] = split[i + 1];
            }
            return nicks;   // список ников
        }
    },                   //     [0]       [1]
    ERROR("/error") {    //   /error error message
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER, 2);
            return new String[]{split[1]};  // [1] - сообщение об ошибке
        }
    },
    MESSAGE("/message") {   // нет простого сообщения, сообщение начинается со "/message",

        @Override           // чтобы избежать проверок на команду
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMiTER, 2);
            return new String[]{split[1]};
        }
    },
    STOP("/stop") {
         @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
     CHANGE_USERNAME("/change-username") {
         @Override
         public String[] parse(String commandText) {
             final String[] split = commandText.split(TOKEN_DELIMiTER, 2);
             return new String[]{split[1]};
     }
    };



    private final String command;   // хранит в себе текст команды из списка enum Command
    static final String TOKEN_DELIMiTER = "\\p{Blank}+";  //разбивает команду на составляющие по пробелу
    static final Map<String, Command> commandMap = Arrays.stream(values())  // values возвращает команды(enum Command)
            .collect(Collectors.toMap(Command::getCommand, Function.identity()));  // stream превращает массив в Map


    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;         // возвращает значение поля command
    }

    public static boolean isCommand(String message) {       // распознавание команда или нет
        return message.startsWith("/");     // если сообщение начинается со "/", то это команда
    }

    public static Command getCommand(String message) {       // передаем сообщение, которое к нам пришло: /w n ndjfkfg
        if (!isCommand(message)) {                           // проверяем команда это или нет(проверяем "/")
            throw new RuntimeException("'" + message + "' is not a command");   // бросаем исключение
        }
        final String cmd = message.split(TOKEN_DELIMiTER, 2)[0];    // делим на 2 токена, в индексе [0] лежит команда
        final Command command = commandMap.get(cmd);  // если текст совпал с командой (Мар - быстрый поиск от "1")
        if (command == null) {                       // если текст не совпал с командой
            throw new RuntimeException("Unknown command '" + cmd + "'"); // генерируем сообщение об ошибке
        }
        return command;  // возвращаем команду(если текст совпал с командой)


    }

    public abstract String[] parse(String commandText);  // текст сообщения превращаем строку в команду и парсим

    public String collectMessage(String... params) {    //params заменяет маcсив (String[] {})
        return this.command + " " + String.join(" ", params);  // соединение параметров
    }
}
