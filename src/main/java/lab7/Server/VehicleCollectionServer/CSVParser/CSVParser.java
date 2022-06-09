package lab7.Server.VehicleCollectionServer.CSVParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CSVParser {

    CSVParser(){};

    private static String convertToValue(String str)
    {
        StringBuilder outStr = new StringBuilder("\"");

        for(char c : str.toCharArray())
        {
            if(c == '\"')
                outStr.append('\"');
            outStr.append(c);
        }

        outStr.append("\"");

        return outStr.toString();
    }

    public static String convertToLine(String[] inputStr)
    {
        StringBuilder outStr = new StringBuilder();
        for (String str : inputStr) {
            outStr.append(convertToValue(str)).append(";");
        }
        outStr.append("\n");
        return outStr.toString();
    }

    public static ArrayList<String> readLine(InputStreamReader input) throws IOException
    {
        ArrayList<String> wordArray = new ArrayList<String>();
        String word = "";
        boolean prevSymbolIsQuotes = false;
        int quotesCounter = 0;
        boolean ignoreCh = false;

        int i = input.read();
        while((char)i == '\n')
            i = input.read();

        while(i != -1 && (char)i != '\n')
        {
            char ch = (char)i;
            ignoreCh = false;

            if(ch == ';' && (quotesCounter % 2) == 0){
                if(word.equals("null") || word.equals("NULL")) word = "";
                wordArray.add(word);
                word = "";
                prevSymbolIsQuotes = false;
            }
            else {
                if (ch == '\"')
                {
                    quotesCounter++;
                    if(!prevSymbolIsQuotes) {
                        prevSymbolIsQuotes = true;
                        ignoreCh = true;
                    }
                    else
                        prevSymbolIsQuotes = false;

                }
                else
                    prevSymbolIsQuotes = false;

                if(!ignoreCh) {
                    word += String.valueOf(ch);
                }
            }
            i = input.read();

        }
        return wordArray;
    }
}
