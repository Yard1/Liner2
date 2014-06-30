package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;



public class IobStreamReader extends AbstractDocumentReader {
	
	private BufferedReader ir;
	private TokenAttributeIndex attributeIndex = null;
	private String nextParagraphId = null;
	private boolean nextParagraph = false;
	private boolean init = false;
	
	public IobStreamReader(InputStream is) {
		this.ir = new BufferedReader(new InputStreamReader(is));
	}

	/**
	 * Read -DOCSTART CONFIG FEATURES line.
	 */
	protected void init() throws DataFormatException {
		if (this.init)
			return;
		while (true) {
			String line = null;
			try {
				line = ir.readLine();
			} catch (IOException ex) {
				throw new DataFormatException("I/O error.");
			}
			
			if (line == null)
				throw new DataFormatException("Invalid file format.");
			if (!line.startsWith("-DOCSTART CONFIG FEATURES")){
				continue;
			}
			this.attributeIndex = new TokenAttributeIndex();
			String[] content = line.trim().split(" ");
			
			/* Pierwsze trzy elementy to -DOCSTART, CONFIG, FEATURES,
			 * więc je pomiń. */
			for (int i = 3; i < content.length; i++){
				this.attributeIndex.addAttribute(content[i]);
			}
			this.init = true;
			return;
		}
	}
	
	@Override
	public void close() throws DataFormatException {
		try {
			ir.close();
		} catch (IOException ex) {
			throw new DataFormatException("Failed to close input stream.");
		}
	}

	public boolean paragraphReady() throws DataFormatException {
		if (!this.init)
			init();
		if (this.nextParagraph)
			return true;
		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					this.nextParagraphId = null;
					this.nextParagraph = false;
					return false;
				}
				line = ir.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			String[] words = line.trim().split(" ");
			if (words.length < 2)
				continue;
			if ((words[0].equals("-DOCSTART")) &&
				(words[1].equals("FILE"))) {
				if (words.length >= 3)
					this.nextParagraphId = words[2];
				this.nextParagraph = true;
				return true;
			}
		}
	}
	
	@Override
	protected TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}

	@Override
	public Document nextDocument() throws DataFormatException {
		if (!paragraphReady())
			return null;
			
		TokenAttributeIndex index = this.attributeIndex.clone();
		
		Paragraph paragraph = new Paragraph(nextParagraphId);
		paragraph.setAttributeIndex(index);
		this.nextParagraphId = null;
		this.nextParagraph = false;
		Sentence currentSentence = new Sentence();
		Annotation currentChunk = null;

		while (true) {
			String line = null;
			try {
				if (!ir.ready()) {
					if (currentSentence.getTokenNumber() > 0)
						paragraph.addSentence(currentSentence);
					Document document = new Document("IOB todo", index);
					document.addParagraph(paragraph);
					return document;
				}		
				line = ir.readLine();
			} catch (IOException ex) {
				throw new DataFormatException("I/O error while reading paragraph.");
			}
			
			if (line.trim().isEmpty()) {
				if (currentSentence.getTokenNumber() > 0) {
					paragraph.addSentence(currentSentence);
					currentSentence = new Sentence();
				}
			}
			else {
				String[] words = line.trim().split(" ");
				if (words[0].equals("-DOCSTART")) {
					if (words[1].equals("FILE")) {
						if (words.length >= 3)
							this.nextParagraphId = words[2];
						this.nextParagraph = true;
						Document document = new Document("IOB todo", index);
						document.addParagraph(paragraph);
						return document;
					}
					else
						continue;
				}
				else {
					// add token
					try {
						currentSentence.addToken(createToken(words, index));
					} catch (Exception ex) {
						throw new DataFormatException("Error while reading token: " + ex.getMessage());
					}
					
					// add or update chunk if I/B tag present
					int last = words.length - 1;
					if (words[last].startsWith("B")) {
						int idx = currentSentence.getTokenNumber() - 1;
						String type = words[last].length() >= 3 ? words[last].substring(2) : "";
						currentChunk = new Annotation(idx, type, currentSentence);
						currentSentence.addChunk(currentChunk);
					}
					else if (words[last].startsWith("I")) {
						if (currentChunk != null) {
							int idx = currentSentence.getTokenNumber() - 1;
							
							currentChunk.addToken(idx);
						}
					}
				}
			}
		}
	}
	
	private Token createToken(String[] words, TokenAttributeIndex index) throws Exception {
		Token token = new Token(index);
		if (words.length != index.getLength() + 1){
			throw new Exception("Invalid number of attributes: " + StringUtils.join(words) 
					+ ". Expecting " + index.getLength());
		}
		for (int i = 0; i < words.length - 1; i++)
			token.setAttributeValue(i, words[i]);
		if (this.attributeIndex != null) {
			String base = index.getAttributeValue(token, "base");
			String ctag = index.getAttributeValue(token, "ctag");
			token.addTag(new Tag(base, ctag, false));
		}
		return token;
	}
}