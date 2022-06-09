package lab7.UserInput;

import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;

import java.io.*;
import java.util.Stack;

public class UserInput
{
    static private BufferedReader defaultReader = new BufferedReader(new InputStreamReader(System.in));
    static private BufferedReader activeReader = defaultReader;
    static private final Stack<BufferedFileReader> readersStack = new Stack<>();


    public static void setDefaultReader(BufferedReader reader){
        defaultReader = reader;
        if (readersStack.empty()) activeReader = defaultReader;
    }

    public static void addFileReader(File file) throws RuntimeException, FileNotFoundException {
        System.out.println("Switching to new file");

        boolean flag = false;
        for(BufferedFileReader bfr : readersStack)
            if (bfr.getFile().equals(file)) {
                flag = true;
                break;
            }
        if(flag) throw new RuntimeException("Recursion detected");

        BufferedFileReader reader = new BufferedFileReader(file);
        readersStack.push(reader);
        activeReader = readersStack.peek();
    }

    public static int getFilesStackSize(){
        return readersStack.size();
    }

    public static void removeReader() {
        try {
            activeReader.close();
            if (!readersStack.empty()) {
                readersStack.pop();
                if (!readersStack.empty()) {
                    System.out.println("Switching to previous reader");
                    activeReader = readersStack.peek();
                } else {
                    activeReader = defaultReader;
                }
            }
        }
        catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    public static boolean available() throws IOException {
        return activeReader.ready();
    }

    public static String readLine() throws IOException, EOFInputException {
        String str = UserInput.activeReader.readLine();
        if(str == null){
            throw new EOFInputException("End of file");
        }
        return str;
    }

    public static String getString(String inputName) throws IOException, EOFInputException {
        System.out.print("\tEnter " + inputName + ": ");
        return readLine();
    }

    public static String getWord(String inputName) throws IOException, EOFInputException {
        return getString(inputName).split(" +")[0];
    }

    public static Integer getInteger(String inputName) throws IOException, InputException, EOFInputException {
        String input = getWord(inputName);
        if (input.equals("")) return null;

        Integer i = null;
        try {
            i = Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            throw new InputException("Input is not integer");
        }

        return i;
    }

    public static Double getDouble(String inputName) throws IOException, InputException, EOFInputException {
        String input = getWord(inputName);
        if (input.equals("")) return null;

        Double d = null;
        try {
            d = Double.parseDouble(input);
        }
        catch (NumberFormatException e) {
            throw new InputException("Input is not double");
        }

        return d;
    }

    public static Long getLong(String inputName) throws IOException, InputException, EOFInputException {
        String input = getWord(inputName);
        if (input.equals("")) return null;

        Long l = null;
        try {
            l = Long.parseLong(input);
        }
        catch (NumberFormatException e) {
            throw new InputException("Input is not long");
        }

        return l;
    }
}