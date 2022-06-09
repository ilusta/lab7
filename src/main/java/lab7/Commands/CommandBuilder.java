package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.UserInput.UserInput;

import java.io.IOException;
import java.util.ArrayList;

public class CommandBuilder {

    private static ArrayList<Command> commandList;

    public static void setCommandList(ArrayList<Command> commandList) {
        CommandBuilder.commandList = commandList;
    }

    public Command build() throws EOFInputException, IOException, InputException, CommandExecutionException {
        String[] words;
        try {
            words = UserInput.readLine().split(" +");
        }catch(EOFInputException e){
            throw new EOFInputException("");
        }

        Command command = null;
        for (Command c : commandList)
            if(c.getName().equals(words[0])) command = c;

        if (command == null)
            throw new InputException("Unknown command. Use 'help' command to get list of supported commands.");

        return command.build(words);

    }
}
