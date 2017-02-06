package g419.corpus.structure;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class Document{

	String name = null;
	String uri = null;
	TokenAttributeIndex attributeIndex = null;
	List<Paragraph> paragraphs = new ArrayList<Paragraph>();
	DocumentDescriptor documentDescriptor = new DocumentDescriptor();
	Set<Frame> frames = new HashSet<Frame>();

	private HashMap<String, Integer> bases = null;
	private HashMap<String, Integer> titleBases = null;
	private int tokenNumber = 0;

	/* Zbiór relacji */
	RelationSet relations = new RelationSet();
	
	public Document(String name, TokenAttributeIndex attributeIndex){
		this.name = name;
		this.attributeIndex = attributeIndex;
	}
	
	public Document(String name, List<Paragraph> paragraphs, TokenAttributeIndex attributeIndex){
		this.name = name;
		this.paragraphs = paragraphs;
		for(Paragraph paragraph: paragraphs) paragraph.setDocument(this);
		this.attributeIndex = attributeIndex;
	}
	
	public Document(String name, List<Paragraph> paragraphs, TokenAttributeIndex attributeIndex, RelationSet relations){
		this.name = name;
		this.paragraphs = paragraphs;
		this.attributeIndex = attributeIndex;
		this.relations = relations;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Get the name of document source. If the document was read from a file, 
	 * it is a relative path to the file. 
	 * @return source of the document
	 */
	public String getName(){
		return this.name;
	}
	
	public String getUri(){
		return this.uri;
	}
	
	public void setUri(String uri){
		this.uri = uri;
	}
	
	public RelationSet getRelations(){
		return this.relations;
	}
	
	public RelationSet getRelations(String set){
		return this.relations.filterBySet(set);
	}
	
	public void setRelations(RelationSet relations){
		this.relations = relations;
	}
	
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
		if (paragraph.getAttributeIndex() == null)
			paragraph.setAttributeIndex(this.attributeIndex);
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public List<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public Set<Frame> getFrames(){
		return this.frames;
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

	public void removeAnnotations(List<Annotation> annotations){
		for(Annotation annotation : annotations)
			annotation.getSentence().getChunks().remove(annotation);
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

    /**
     * Removes all anotation
     */
    public void removeAnnotations() {
        for (Paragraph paragraph : this.paragraphs)
            for (Sentence sentence : paragraph.getSentences())
                sentence.chunks = new LinkedHashSet<Annotation>();

    }

	public void removeAnnotations2(List<Pattern> types) {
		for (Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences()){
				LinkedHashSet<Annotation> newAnnotationSet = new LinkedHashSet<Annotation>();
				for (Annotation a : sentence.chunks){
					for (Pattern p : types){
						if(!p.matcher(a.getType()).find()){
							newAnnotationSet.add(a);
						}
					}
				}
				sentence.chunks = newAnnotationSet;
			}

	}

	/**
	 * Removes metadata from chunks with given name
	 */
	public void removeMetadata(String key) {
		for (Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				for (Annotation annotation : sentence.chunks)
					annotation.getMetadata().remove(key);

	}

    /**
     * Retreives Annotation given sentence id, channel and annotation index in channel
     */
    public Annotation getAnnotation(String sentenceId, String channelName, int annotationIdx){
    	for (Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				if(sentence.getId().equals(sentenceId))
					return sentence.getAnnotationInChannel(channelName, annotationIdx);
    	return null;
    }
    
    public ArrayList<Annotation> getAnnotations(List<Pattern> types){
    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    	for(Sentence sentence: getSentences()){
    		for(Annotation sentenceAnnotation: sentence.getAnnotations(types)){
    			annotations.add(sentenceAnnotation);
    		}
    	}
    	
    	return annotations;
    }
    
    public ArrayList<Annotation> getAnnotations(){
    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    	
    	for(Sentence sentence: getSentences()){
    		for(Annotation sentenceAnnotation: sentence.getChunks()){
    			annotations.add(sentenceAnnotation);
    		}
    	}
    	
    	return annotations;
    }

    public Document clone(){
        Document copy = new Document(name, attributeIndex.clone());
        for(Paragraph p: paragraphs){
            copy.addParagraph(p.clone());
        }
		copy.documentDescriptor = documentDescriptor.clone();
        return copy;

    }
    
    public void addRelation(Relation relation){
    	this.relations.addRelation(relation);
    }

	/**
	 * Removes given annotations from relational clusters and refreshes
	 * documents' relation set
	 * @param annotations
	 */
    public void filterAnnotationClusters(List<Annotation> annotations){
    	AnnotationClusterSet clusterSet = AnnotationClusterSet.fromRelationSet(relations);
    	clusterSet.removeAnnotations(annotations);
    	this.relations = clusterSet.getRelationSet(new AnnotationCluster.ReturnRelationsToHead());
    }

    /**
     * Przepięcie relacji z anotacji źródłowej do anotacji docelowej
     * @param source
     * @param dest
     */
	public void rewireSingleRelations(Annotation source, Annotation dest) {
			List<Relation> rewired = new ArrayList<Relation>();
		
			if(this.relations.incomingRelations.containsKey(source)){
				for(Relation incoming : this.relations.incomingRelations.get(source)){
					if(incoming.getAnnotationTo().equals(dest)) continue;
					Relation rwRel = new Relation(incoming.getAnnotationFrom(), dest, incoming.getType(), incoming.getSet(), this);
 					rewired.add(rwRel);
					this.relations.relations.remove(incoming);
				}
				this.relations.refresh();
//				this.relations.incomingRelations.remove(source);
			}
				
			if(this.relations.outgoingRelations.containsKey(source)){
				for(Relation outgoing : this.relations.outgoingRelations.get(source)){
					if(outgoing.getAnnotationFrom().equals(dest)) continue;
					Relation rwRel = new Relation(dest, outgoing.getAnnotationTo(), outgoing.getType(), outgoing.getSet(), this);
					rewired.add(rwRel);
					this.relations.relations.remove(outgoing);
				}
//				this.relations.outgoingRelations.remove(source);
			}
			
			for(Relation relation : rewired) this.relations.addRelation(relation);
			this.relations.refresh();
	}
	
	public DocumentDescriptor getDocumentDescriptor() {
		return documentDescriptor;
	}

	public int getBaseCount(String base){
		if (this.bases == null){
			this.bases = new HashMap<>();
			for (Paragraph p : this.paragraphs){
				for (Sentence s : p.getSentences()){
					for (Token t : s.getTokens()){
						this.tokenNumber += 1;
						String lemma = t.getAttributeValue("base");
						if (this.bases.containsKey(lemma))
							this.bases.put(lemma, this.bases.get(lemma) + 1);
						else this.bases.put(lemma, 1);
					}
				}
			}
		}
		if (this.bases.containsKey(base))
			return this.bases.get(base);
		return 0;
	}

	public int getTokenNumber(){
		return this.tokenNumber;
	}


	public int isInTitle(String base){
		if (this.titleBases == null){
			this.titleBases = new HashMap<>();
			for (Paragraph p : this.paragraphs){
				if (p.getChunkMetaData("type").equals("title")){
					for (Sentence s : p.getSentences()){
						for (Token t : s.getTokens()){
							String lemma = t.getAttributeValue("base");
							this.titleBases.put(lemma, 1);
						}
					}
				}
			}
		}
		if (this.titleBases.containsKey(base))
			return this.titleBases.get(base);
		return 0;
	}
}