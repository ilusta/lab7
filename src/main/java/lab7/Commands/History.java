package lab7.Commands;

import java.util.ArrayList;


public class History extends Command
{
    private static ArrayList<String> commandHistory = new ArrayList<>();

    @Override
    public String getName() {
        return "history";
    }
    @Override
    public CommandType getType() {
        return CommandType.ALL;
    }
    @Override
    public String getHelp() {
        return "Prints last 11 executed commands.";
    }

    public static void add(String commandName){
        History.commandHistory.add(commandName);
    }

    @Override
    public Command build(String[] param){
        return this;
    }

    @Override
    public String execute(){
        StringBuilder message = new StringBuilder();
        int x = History.commandHistory.size();

        if (x == 0) message.append("History is empty.");
        else {
            if (x > 11) x = 11;
            for (int i = 0; i < x; ++i) {
                message.append(History.commandHistory.get(i)).append("\n");
            }
        }
        return message.toString();
    }
}
