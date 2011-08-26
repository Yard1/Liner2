package liner2.chunker.factory;

import java.util.regex.Matcher;

import liner2.LinerOptions;
import liner2.Main;

import liner2.chunker.CRFPPChunker;
import liner2.chunker.Chunker;
import liner2.chunker.TrainableChunkerInterface;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;

import liner2.tools.CorpusFactory;

public class ChunkerFactoryItemCRFPPTrain extends ChunkerFactoryItem {

	public ChunkerFactoryItemCRFPPTrain() {
		super("crfpp-train:p=([1-6]):template=(.*):(ccl|iob|data)=(.*?)(:model=(.*))?");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {

       	Matcher matcherCRFPP = this.pattern.matcher(description);
		if (matcherCRFPP.find()){
            Main.log("--> CRFPP Chunker train");

            int threads = Integer.parseInt(matcherCRFPP.group(1));
            String template_filename = matcherCRFPP.group(2) + ".tpl";
            String inputFormat = matcherCRFPP.group(3);
            String inputFile = matcherCRFPP.group(4);
            
            String model_filename = "crf_model.bin";
            if ( matcherCRFPP.group(5) != null )
            	model_filename = matcherCRFPP.group(6);

            CRFPPChunker chunker = new CRFPPChunker(threads);
            chunker.setTemplateFilename(template_filename);
            chunker.setModelFilename(model_filename);

            if ((inputFormat.equals("iob")) || (inputFormat.equals("ccl"))) {
            	Main.log("--> Training on file=" + inputFile);            
            	StreamReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
            	((TrainableChunkerInterface)chunker).train(reader.readParagraphSet());
            }
            else {
            	Main.log("--> Training on corpus=" + inputFile);
            	((TrainableChunkerInterface)chunker).train(CorpusFactory.get().query(inputFile));
            }
                        
            return chunker;		
		}
		else		
			return null;
	}

}