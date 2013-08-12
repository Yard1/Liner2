package liner2.tools;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import java.io.File;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/5/13
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class Maltparser {

    private static HashMap<String, MaltParserService> parsers = new HashMap<String, MaltParserService>();

    private Maltparser(){
    }

    public static MaltParserService addParser(String modelPath){
        try {
            MaltParserService parser =  new MaltParserService();
            File modelFile = new File(modelPath);
            parser.initializeParserModel(String.format("-c %s -m parse -w %s", modelFile.getName(), modelFile.getParent()));
            parsers.put(modelPath, parser);
            return parser;
        } catch (MaltChainedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MaltParserService getParser(String modelPath){
        return parsers.get(modelPath);
    }

    public static boolean isInitialized(String modelPath){
        return parsers.containsKey(modelPath);
    }
}
