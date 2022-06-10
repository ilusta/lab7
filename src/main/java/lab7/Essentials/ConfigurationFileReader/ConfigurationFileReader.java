package lab7.Essentials.ConfigurationFileReader;

import lab7.Exceptions.InputException;

import java.io.*;

public class ConfigurationFileReader {

    private File confFile;

    public ConfigurationFileReader(String file){
        this.confFile = new File(file);
    }

    public String readParameter(String parameterName) throws InputException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(confFile)));
            while(true) {
                String line = reader.readLine();
                if (line == null) throw new InputException("parameter not found");
                String[] words = line.split("[ ]");
                if (words[0].equals(parameterName)) return words[1];
            }

        }
        catch (Exception e){
            throw new InputException("Unable to read configuration file: " + e);
        }
    }
}
