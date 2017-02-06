package g419.liner2.api.features.annotations;

import g419.corpus.structure.Annotation;
import g419.liner2.api.tools.parser.MaltParser;

import java.util.HashMap;
import java.util.HashSet;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 7/30/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationFeatureMalt extends AnnotationFeature{

    private MaltParser malt;
    private String type;
    private int distance;


    public AnnotationFeatureMalt(String modelPath, int distance, String type) {
        this.type = type;
        this.distance = distance;
        malt = new MaltParser(modelPath);
    }

    public HashMap<Annotation, String> generate(String[] dataForMalt, HashSet<Annotation> annotations) {

        HashMap<Annotation, String> features = new HashMap<Annotation, String>();
        try {
            String [] parsedTokens =  malt.parseTokens(dataForMalt);

            for(Annotation ann: annotations){
                String annData = parsedTokens[ann.getBegin()];
                features.put(ann, getFeature(annData, parsedTokens));

            }
        } catch (MaltChainedException e) {
            e.printStackTrace();
        }
        return features;
    }

    public String getFeature(String annotation, String[] maltData){
        if(this.distance > 1){
            int i=1;
            int parentIdx = Integer.parseInt(annotation.split("\t")[8]) - 1;
            while(i<distance){
                if(parentIdx<0)
                    return "NULL";
                annotation =  maltData[parentIdx];
                parentIdx = Integer.parseInt(annotation.split("\t")[8]) - 1;
                i++;
            }
        }
        if(this.type.equals("base"))
            return getParentBase(annotation, maltData);
        else if(this.type.equals("relation"))
            return getRelation(annotation);
        return null;
    }

    public String getRelation(String annotation){
        return annotation.split("\t")[9];
    }

    public String getParentBase(String annotation, String[] maltData){
        int parentIdx = Integer.parseInt(annotation.split("\t")[8]) - 1;
        if(parentIdx < 0)
            return "NULL";
        return  maltData[parentIdx].split("\t")[2];
    }
}
