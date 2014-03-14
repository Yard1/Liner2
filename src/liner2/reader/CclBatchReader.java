package liner2.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import liner2.reader.parser.CclSaxParser;
import liner2.structure.Document;
import liner2.structure.TokenAttributeIndex;
import liner2.tools.DataFormatException;

/**
 *
 */
public class CclBatchReader extends AbstractDocumentReader {

    private TokenAttributeIndex attributeIndex;
    private int fileIndex=0;
    private List<String> files = new ArrayList<String>();
    private File root = null;

    /**
     * 
     * @param is — the stream contains relative or absolute paths to ccl files,
     * @param root — absolute path to a root for absolute paths from the stream,
     * @throws DataFormatException
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public CclBatchReader(InputStream is, String root) throws DataFormatException, IOException{
    	this.root = new File(root);
        this.attributeIndex = new TokenAttributeIndex();
        this.attributeIndex.addAttribute("orth");
        this.attributeIndex.addAttribute("base");
        this.attributeIndex.addAttribute("ctag");
        
        BufferedReader ir = new BufferedReader(new InputStreamReader(is));
        while (true) {
            String line;
            try {
                line = ir.readLine();
            } catch (IOException ex) {
                throw new DataFormatException("I/O error.");
            }
            if(line == null)
                break;
            String name = line.trim().split(";")[0];
            String cclFile = name;
            if (cclFile.length() == 0)
                break;
            
            if (!cclFile.startsWith("/"))
            	cclFile = new File(this.root, cclFile).getAbsolutePath();

            if (!new File(cclFile).exists())
            	System.err.println("File not found while reading ccl-batch: " + cclFile);
            else{
            	this.files.add(name);
            }
        }
        ir.close();
    }
    
    @Override
    public Document nextDocument() throws DataFormatException, IOException {
    	if ( this.fileIndex < this.files.size() ){
        	String name = this.files.get(this.fileIndex++);
            InputStream  fileAsStream = new FileInputStream(new File(this.root, name).getAbsolutePath());
            CclSaxParser parser_out = new CclSaxParser(name, fileAsStream, this.attributeIndex);
    		Document document = parser_out.getDocument();
    		fileAsStream.close();
    		return document;
    	}
    	else
    		return null;
    }
    
    @Override
    protected TokenAttributeIndex getAttributeIndex() {
        return this.attributeIndex;
    }

    @Override
    public void close() throws DataFormatException {
    }

}
