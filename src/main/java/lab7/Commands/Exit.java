package lab7.Commands;


public class Exit extends Command
{
    private static boolean runFlag = true;

    public static boolean getRunFlag() {
        return Exit.runFlag;
    }

    @Override
    public String getName() {
        return "exit";
    }
    @Override
    public CommandType getType() {
        return CommandType.ALL;
    }
    @Override
    public String getHelp() {
        return "Exits app.";
    }

    @Override
    public Command build(String[] param){
        return this;
    }

    @Override
    public String execute(){
        Exit.runFlag = false;
        return "Exiting...\n";
    }

}