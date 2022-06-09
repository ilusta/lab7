package lab7.UserInput;

import java.io.*;

public class BufferedFileReader extends BufferedReader {

    private File file;

    public BufferedFileReader(File file) throws FileNotFoundException {
        super(new InputStreamReader(new FileInputStream(file)));
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }
}
