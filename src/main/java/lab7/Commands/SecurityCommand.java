package lab7.Commands;


public abstract class SecurityCommand extends Command
{
    protected String user, password;

    public String getUser(){
        return this.user;
    }

    public String getPassword(){
        return this.password;
    }
}