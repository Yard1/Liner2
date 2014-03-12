package liner2.chunker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.Main;
import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;
import liner2.structure.Paragraph;
import liner2.structure.Document;
import liner2.structure.Sentence;
import liner2.structure.Token;

import org.chasen.crfpp.Tagger;

public class CrfppChunker extends Chunker 
	implements TrainableChunkerInterface, DeserializableChunkerInterface, SerializableChunkerInterface {
	
	private File trainingFile = null;
	private PrintWriter trainingFileWriter = null;
	private Tagger tagger = null;
	private Pattern p = Pattern.compile("([IB])-(.*)");
	private String template_filename = null;
	private String model_filename = null;
	private int threads = 1;
	private static final int MAX_TOKENS = 1000;
	private HashSet<String> types = null;
	
    public CrfppChunker() {
		this.types = new HashSet<String>();    	
    }
    
    public CrfppChunker(int threads, HashSet<String> types){
		this.threads = threads;
		this.types = types;    	
    }

    /**
     * Reads output from the external CRF tagger. 
     * Transforms the result from IOB format into a list of annotations.
     * @param cSeq --- text to tag
     * @return chunking with annotations
     */

	private synchronized AnnotationSet chunkSentence(Sentence sentence){
		if (sentence.getTokenNumber()>MAX_TOKENS)
			return new AnnotationSet(sentence);
		this.sendDataToTagger(sentence);
		return this.readTaggerOutput(sentence);
	}
	
	private void sendDataToTagger(Sentence sentence){
		tagger.clear();
		int numAttrs = sentence.getAttributeIndexLength();
		String val = null;
		for (Token token : sentence.getTokens()) {
			StringBuilder oStr = new StringBuilder();
			for (int i = 0; i < numAttrs; i++){
				oStr.append(" ");
				val = token.getAttributeValue(i);
				if ( val != null){
					val = val.replaceAll("\\s+", "_");
                    val = val.length()==0 ? "NULL" : val;
                                }
				oStr.append(val);
			}
			tagger.add(oStr.toString().trim());
		}
		tagger.parse();		
	}
	
	private AnnotationSet readTaggerOutput(Sentence sentence){
        AnnotationSet chunking = new AnnotationSet(sentence);
        String type = null;
        int from = 0;

        for (int i = 0; i < tagger.size(); ++i) {
            Matcher m = p.matcher(tagger.y2(i));
                                  
            if ( type != null && ( !m.matches() || m.group(1).equals("B") ) ){
            	chunking.addChunk(new Annotation(from, i-1, type, sentence));
            	type = null;
            	from = 0;
            }
            
            if ( m.matches() && m.group(1).toString().equals("B") ){
            	from = i;
            	type = m.group(2);
            }
        }    

        if (type != null)
        	chunking.addChunk(new Annotation(from, (int)tagger.size()-1, type, sentence));

        return chunking;
    }
	
	            
    @Override
	public void train() throws Exception {
    	this.trainingFileWriter.close();
		this.compileTagger();
    }

    @Override
    public void addTrainingData(Document paragraphSet) {

    	// Utwórz tymczasowy plik do zapisu danych treningowych
    	if ( this.trainingFileWriter == null ){
    		try {
    			this.trainingFile = new File("crf_iob.txt");
				this.trainingFileWriter = new PrintWriter(this.trainingFile);
			} catch (IOException e) {
				e.printStackTrace();
			}			
    	}
    	
    	String val = null;
    	for (Paragraph paragraph : paragraphSet.getParagraphs())
    		for (Sentence sentence : paragraph.getSentences()) {
    			int numAttrs = sentence.getAttributeIndexLength();
    			ArrayList<Token> tokens = sentence.getTokens();    			
    			for (int i = 0; i < tokens.size(); i++) {
    				String oStr = "";
    				
    				for (int j = 0; j < numAttrs; j++){
    					val = tokens.get(i).getAttributeValue(j);
    					if ( val != null){
    						val = val.replaceAll("\\s+", "_");
    							val = val.length()==0 ? "NULL" : val;
    					}
    					oStr += " " + val;
    				}
    				
    				Annotation chunk = this.types.size() == 0 
    						? sentence.getChunkAt(i)
    						: sentence.getChunkAt(i, this.types);
    				
    				if (chunk == null)
    					oStr += " O";
    				else {
    					if (chunk.getBegin() == i)
    						oStr += " B-";
    					else
    						oStr += " I-";
    					oStr += chunk.getType();
    				}
    				
    				this.trainingFileWriter.write(oStr.trim() + "\n");
    			}
    			this.trainingFileWriter.write("\n");
    		}
    		
    	this.trainingFileWriter.flush();
    		
    }
    
    /**
     * Kompilacja chunkera.
     * W przypadku CRF zostaje zamknięty tymczasowy plik z danymi treningowymi, po czym
     * zostaje uruchomiony crf_learn. Wynikiem przetwarzania jest plik z modelem.
     */
    private void compileTagger() {
    	this.trainingFileWriter.close();
    	
    	CRFcmd cmd = new CRFcmd();
    	cmd.file_template = this.template_filename;
    	cmd.file_model = this.model_filename;
    	cmd.file_iob = this.trainingFile.getAbsolutePath();
    	cmd.threads = this.threads;
    	
        try {
			Process p = Runtime.getRuntime().exec(cmd.get_crf_learn());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
            while ((line = input.readLine()) != null) {
                Main.log(line);
            }
            
            boolean wasError = false;
            while ((line = error.readLine()) != null) {
                System.out.println(">> Error: " + line);
                wasError = true;
            }
            if (wasError)
            	throw new Error("There was a problem with training CRF");
            
            this.deserialize(cmd.file_model);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Wczytuje chunker z modelu binarnego.
     * @param model_filename
     */
	@Override
    public void deserialize(String model_filename){
    	String parameters = String.format("-m %s -v 3 -n 64", model_filename);
    	this.tagger = new Tagger(parameters);
    }

	@Override
	public void serialize(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		this.tagger.delete();
		
	}
		
	
	/**
	 * Load external library.
	 */
	static {
		try {
            String linerJarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            System.load(linerJarPath.replace("liner2.jar","") + "lib/libCRFPP.so");
        } catch (UnsatisfiedLinkError e) {
		    System.err.println("Cannot load the libCRFPP.so native code.\nRun: java -Djava.library.path=./lib -jar liner2.jar ..." + e);
		    System.exit(1);
        }

    }
	
	
	/**
	 * Klasa pomocnicza reprezentuje obiekt do generowania wywołania komendy crf
	 */
	class CRFcmd{
		public String file_template = "";
		public String file_iob = "";
		public String file_model = "";
		public int threads = 1;
		
		public String get_crf_learn(){
			String cmd = String.format("crf_learn %s %s %s -f 5 -c 1 -p %d", this.file_template, this.file_iob, this.file_model, this.threads);
			//cmd += " -m 20";
			return cmd;
		}
	}
	
	public void setTemplateFilename(String templateFilename) {
		this.template_filename = templateFilename;		
	}

	public void setModelFilename(String modelFilename) {
		this.model_filename = modelFilename;		
	}

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}

}
