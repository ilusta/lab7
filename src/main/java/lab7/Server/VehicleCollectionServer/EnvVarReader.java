package lab7.Server.VehicleCollectionServer;

import java.util.Map;
import lab7.Exceptions.InputException;

public class EnvVarReader
{
    public static String getValue(String varName) throws SecurityException, InputException {

        Map<String, String> env = System.getenv();

        if (!env.containsKey(varName)) throw new InputException("Environment variable " + varName + " does not exist.");

        return env.get(varName);
    }
}