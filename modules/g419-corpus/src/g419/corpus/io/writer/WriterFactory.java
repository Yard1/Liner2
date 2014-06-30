package g419.corpus.io.writer;

import g419.corpus.structure.CrfTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class WriterFactory {

	private static final WriterFactory factory = new WriterFactory();
	
	private WriterFactory(){}
	
	public static WriterFactory get(){
		return WriterFactory.factory; 
	}
	
	/**
	 * TODO
	 * @return
	 */
	public AbstractDocumentWriter getStreamWriter(String outputFile, String outputFormat) throws Exception {
        if (outputFormat.equals("tei")){
            return getTEIWriter(outputFile);
        }
        else if (outputFormat.startsWith("batch:")){
            String format = outputFormat.substring(6);
            return new CclBatchWriter(outputFile, format);
        }
        else{
            return getStreamWriter(getOutputStream(outputFile), outputFormat);
        }
	}

    public AbstractDocumentWriter getArffWriter(OutputStream out, CrfTemplate template){
        return new ArffStreamWriter(out, template);
    }

    public AbstractDocumentWriter getArffWriter(String outputFile, CrfTemplate template) throws Exception{
        return getArffWriter(getOutputStream(outputFile), template);
    }
	
	public AbstractDocumentWriter getStreamWriter(OutputStream out, String outputFormat) throws Exception {
        if (outputFormat.equals("ccl"))
			return new CclStreamWriter(out);
		else if (outputFormat.equals("iob"))
			return new IobStreamWriter(out);
		else if (outputFormat.equals("tuples"))
			return new TuplesStreamWriter(out);
		else if (outputFormat.equals("tokens"))
			return new TokensStreamWriter(out);
        else if (outputFormat.equals("arff"))
            throw new Exception("In order to write to arff format use getArffWriter instead of getStreamWriter");
		else		
			throw new Exception("Output format " + outputFormat + " not recognized.");
	}

    public AbstractDocumentWriter getTEIWriter(String outputFolder) throws Exception{
        OutputStream text = getOutputStream(new File(outputFolder,"text.xml").getPath());
        OutputStream annSegmentation = getOutputStream(new File(outputFolder,"ann_segmentation.xml").getPath());
        OutputStream annMorphosyntax = getOutputStream(new File(outputFolder,"ann_morphosyntax.xml").getPath());
        OutputStream annNamed = getOutputStream(new File(outputFolder,"ann_named.xml").getPath());
        return new TEIStreamWriter(text, annSegmentation, annMorphosyntax, annNamed, new File(outputFolder).getName());
    }
	
	private OutputStream getOutputStream(String outputFile) throws Exception {
		if ((outputFile == null) || (outputFile.isEmpty()))
			return System.out;
		else {
			try {
				return new FileOutputStream(outputFile);
			} catch (IOException ex) {
				throw new Exception("Unable to write output file: " + outputFile);
			}
		}
	}
}