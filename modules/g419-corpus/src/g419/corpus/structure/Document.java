package g419.corpus.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class Document {

	String name = null;
	TokenAttributeIndex attributeIndex = null;
	ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public Document(String name, TokenAttributeIndex attributeIndex){
		this.name = name;
		this.attributeIndex = attributeIndex;
	}
	
	public Document(String name, ArrayList<Paragraph> paragraphs, TokenAttributeIndex attributeIndex){
		this.name = name;
		this.paragraphs = paragraphs;
		this.attributeIndex = attributeIndex;
	}
	
	/**
	 * Get the name of document source. If the document was read from a file, 
	 * it is a path to the file. 
	 * @return source of the document
	 */
	public String getName(){
		return this.name;
	}
		
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
		if (paragraph.getAttributeIndex() == null)
			paragraph.setAttributeIndex(this.attributeIndex);
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public ArrayList<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public void setAttributeIndex(TokenAttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Paragraph p : this.paragraphs)
			p.setAttributeIndex(this.attributeIndex);
	}

	/**
	 * Creates a copy of collections of annotations. A new collection is created. The annotation
	 * and sentence are not copied. 
	 * @return
	 */
	public HashMap<Sentence, AnnotationSet> getChunkings() {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences()){
				LinkedHashSet<Annotation> annotations = new LinkedHashSet<Annotation>();
				annotations.addAll(sentence.getChunks());
				chunkings.put(sentence, new AnnotationSet(sentence, annotations));
			}
		return chunkings;
	}

	/**
	 * Add annotations to sentences.
	 * @param chunkings
	 */
	public void addAnnotations(HashMap<Sentence, AnnotationSet> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.addAnnotations(chunkings.get(sentence));
	}

	/**
	 * Discard existing annotations and set given set.
	 * @param chunkings
	 */
	public void setAnnotations(HashMap<Sentence, AnnotationSet> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.setAnnotations(chunkings.get(sentence));
	}

	public ArrayList<Sentence> getSentences() {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		for ( Paragraph paragraph : this.paragraphs )
			sentences.addAll(paragraph.getSentences());
		return sentences;
	}

	/**
	 * Removes all anotation with given name.
	 * @param annotation
	 */
	public void removeAnnotations(String annotation) {
		for (Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.removeAnnotations(annotation);
		
	}
}