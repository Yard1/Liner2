package g419.corpus.io.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import g419.corpus.io.writer.tei.Interps;
import g419.corpus.io.writer.tei.Pair;
import g419.corpus.io.writer.tei.TEILex;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationSet;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/28/13
 * Time: 2:29 PM
 */
public class TEIStreamWriter extends AbstractDocumentWriter{

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //ToDo: podzielić readParagraph na podmetody!!!!!

    private final String TAG_CORPUS      = "teiCorpus";
    private final String TAG_TEI         = "TEI";
    private final String TAG_TEXT        = "text";
    private final String TAG_FRONT       = "front";
    private final String TAG_TITLE       = "docTitle";
    private final String TAG_TITLEPART   = "titlePart";
    private final String TAG_BODY        = "body";
    private final String TAG_DIV         = "div";
    private final String TAG_PARAGRAPH   = "p";
    private final String TAG_SENTENCE    = "s";
    private final String TAG_SEGMENT     = "seg";
    private final String TAG_FEATURESET	  = "fs";
    private final String TAG_FEATURE	  = "f";
    private final String TAG_STRING	      = "string";
    private final String TAG_SYMBOL	      = "symbol";
    private final String TAG_VALT	      = "vAlt";
    private final String TAG_POINTER	  = "ptr";
    private final String TAG_BINARY		  = "binary";
    private final String TAG_METADATA     = "metadata";
    
    private final String ATTR_CORESP	= "coresp";
    private final String ATTR_NAME	= "name";

    private XMLStreamWriter textWriter;
    private XMLStreamWriter metadataWriter;
    private XMLStreamWriter annSegmentationWriter;
    private XMLStreamWriter annMorphosyntaxWriter;
    private XMLStreamWriter annPropsWriter;
    private XMLStreamWriter annNamedWriter;
    private XMLStreamWriter annMentionsWriter;
    private XMLStreamWriter annChunksWriter;
    private XMLStreamWriter annCoreferenceWriter;
    private XMLStreamWriter annRelationsWriter;
    private OutputStream text;
    private OutputStream metadata;
    private OutputStream annSegmentation;
    private OutputStream annMorphosyntax;
    private OutputStream annProps;
    private OutputStream annNamed;
    private OutputStream annMentions;
    private OutputStream annChunks;
    private OutputStream annCoreference;
    private OutputStream annRelations;
    private boolean open = false;
    private boolean indent = true;
    private String documentName;
    private int currentParagraphIdx;
    TokenAttributeIndex attributeIndex;
    
    private List<Pattern> namedPatterns;
    private List<Pattern> mentionPatterns;
    private List<Pattern> chunksPatterns;

    // Identyfikatory anotacji z nazwą pliku, w którym zostały zapisane, np. "ann_mentions.xml#an1" 
    private HashMap<Annotation, String> mentionRefs = new HashMap<Annotation, String>();
    // Identyfikatory anotacji bez nazwy pliku, np. "an1"
    private HashMap<Annotation, String> mentionIds = new HashMap<Annotation, String>();
    private int mentionNr = 0;
    private int chunkNr = 0;
    
    public TEIStreamWriter(OutputStream text, OutputStream metadata, OutputStream annSegmentation, OutputStream annMorphosyntax, 
    		OutputStream annProps,
    		OutputStream annNamed, OutputStream annMentions, OutputStream annChunks, 
    		OutputStream annCoreference, OutputStream annRelations, String documentName) {
        this.documentName = documentName;
        this.text = text;
        this.metadata = metadata;
        this.annSegmentation = annSegmentation;
        this.annMorphosyntax = annMorphosyntax;
        this.annProps = annProps;
        this.annNamed = annNamed;
        this.annMentions = annMentions;
        this.annChunks = annChunks;
        this.annCoreference = annCoreference;
        this.annRelations = annRelations;
        
        this.namedPatterns = new ArrayList<Pattern>();
        this.namedPatterns.add(Pattern.compile(".*nam"));
        
        this.mentionPatterns = new ArrayList<Pattern>();
        this.mentionPatterns.add(Pattern.compile(".*nam"));
        this.mentionPatterns.add(Pattern.compile("anafora_wyznacznik"));
        // TODO: tymczasowo dodane elementy wyrażeń przestrzennych
        this.mentionPatterns.add(Pattern.compile("^(landmark|spatial_indicator|trajector|spatial_object|region|path)", Pattern.CASE_INSENSITIVE));
        
        this.chunksPatterns = new ArrayList<Pattern>();
        this.chunksPatterns.add(Pattern.compile("^chunk_.*"));
        
        XMLOutputFactory xmlof = XMLOutputFactory.newFactory();
        try {
            this.textWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(text)));
            this.annSegmentationWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annSegmentation)));
            this.annMorphosyntaxWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annMorphosyntax)));
            if ( this.metadata != null ){
            	this.metadataWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(metadata)));
            }
            if ( this.annProps != null ) {
            	this.annPropsWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annProps)));
            }
            if(this.annNamed != null){ 
            	this.annNamedWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annNamed)));
            }
            if(this.annMentions != null){ 
            	this.annMentionsWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annMentions)));
            }
            if(this.annChunks != null){ 
            	this.annChunksWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annChunks)));
            }
            if(this.annCoreference != null){ 
            	this.annCoreferenceWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annCoreference)));
            }
            if(this.annRelations != null){ 
            	this.annRelationsWriter = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(annRelations)));
            }
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }

    }

	@Override
	public void flush() {
	}
	
    public void open() {
        if (open)
            return;
        try {
            writeCommonOpening(textWriter);
            textWriter.writeCharacters("\n");
            this.indent(3, textWriter);
            textWriter.writeStartElement(TAG_FRONT);
            textWriter.writeCharacters("\n");
            this.indent(4, textWriter);
            textWriter.writeStartElement(TAG_TITLE);
            textWriter.writeCharacters("\n");
            this.indent(5, textWriter);
            textWriter.writeStartElement(TAG_TITLEPART);
            textWriter.writeAttribute("type", "title");
            textWriter.writeAttribute("xml:id", "titlePart-1");
            textWriter.writeCharacters(documentName);
            textWriter.writeEndElement();
            textWriter.writeCharacters("\n");
            writeEndElementLine(4, textWriter);
            writeEndElementLine(3, textWriter);
            this.indent(3, textWriter);
            textWriter.writeStartElement(TAG_BODY);
            textWriter.writeCharacters("\n");

            writeCommonOpening(annSegmentationWriter);
            annSegmentationWriter.writeAttribute("xml:lang", "pl");
            annSegmentationWriter.writeAttribute("xml:id", "segm_text");
            annSegmentationWriter.writeCharacters("\n");
            this.indent(3, annSegmentationWriter);
            annSegmentationWriter.writeStartElement(TAG_BODY);
            annSegmentationWriter.writeAttribute("xml:id", "segm_body");
            annSegmentationWriter.writeCharacters("\n");

            writeCommonOpening(annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeCharacters("\n");
            this.indent(3, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_BODY);
            annMorphosyntaxWriter.writeCharacters("\n");

            if(metadataWriter != null){
            	writeCommonOpening(metadataWriter);
            	metadataWriter.writeAttribute("xml:lang", "pl");
            	metadataWriter.writeCharacters("\n");
	            this.indent(3, metadataWriter);
	            metadataWriter.writeStartElement(TAG_BODY);
	            metadataWriter.writeCharacters("\n");
            }

            if(annNamedWriter != null){
            	writeCommonOpening(annNamedWriter);
	            annNamedWriter.writeAttribute("xml:lang", "pl");
	            annNamedWriter.writeCharacters("\n");
	            this.indent(3, annNamedWriter);
	            annNamedWriter.writeStartElement(TAG_BODY);
	            annNamedWriter.writeCharacters("\n");
            }

            if(annPropsWriter != null){
            	writeCommonOpening(annPropsWriter);
            	annPropsWriter.writeAttribute("xml:lang", "pl");
            	annPropsWriter.writeCharacters("\n");
	            this.indent(3, annPropsWriter);
	            annPropsWriter.writeStartElement(TAG_BODY);
	            annPropsWriter.writeCharacters("\n");
            }

            if(annMentionsWriter != null){
            	writeCommonOpening(annMentionsWriter);
	            annMentionsWriter.writeAttribute("xml:lang", "pl");
	            annMentionsWriter.writeCharacters("\n");
	            this.indent(3, annMentionsWriter);
	            annMentionsWriter.writeStartElement(TAG_BODY);
	            annMentionsWriter.writeCharacters("\n");
            }

            if(annChunksWriter != null){
            	writeCommonOpening(annChunksWriter);
            	annChunksWriter.writeAttribute("xml:lang", "pl");
            	annChunksWriter.writeCharacters("\n");
	            this.indent(3, annChunksWriter);
	            annChunksWriter.writeStartElement(TAG_BODY);
	            annChunksWriter.writeCharacters("\n");
            }

            if(annCoreferenceWriter != null){
            	writeCommonOpening(annCoreferenceWriter);
	            annCoreferenceWriter.writeAttribute("xml:lang", "pl");
	            annCoreferenceWriter.writeCharacters("\n");
	            this.indent(3, annCoreferenceWriter);
	            annCoreferenceWriter.writeStartElement(TAG_BODY);
	            annCoreferenceWriter.writeCharacters("\n");
            }

            if(annRelationsWriter != null){
            	writeCommonOpening(this.annRelationsWriter);
            	this.annRelationsWriter.writeAttribute("xml:lang", "pl");
            	this.annRelationsWriter.writeCharacters("\n");
	            this.indent(3, this.annRelationsWriter);
	            this.annRelationsWriter.writeStartElement(TAG_BODY);
	            this.annRelationsWriter.writeCharacters("\n");
            }

        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        open = true;
    }

    public void writeCommonOpening(XMLStreamWriter xmlw) throws XMLStreamException{
        xmlw.writeStartDocument("UTF-8", "1.0");
        xmlw.writeCharacters("\n");
        xmlw.writeStartElement(TAG_CORPUS);
        xmlw.writeAttribute("xmlns","http://www.tei-c.org/ns/1.0");
        xmlw.writeAttribute("xmlns:xi","http://www.w3.org/2001/XInclude");
        xmlw.writeAttribute("xmlns:nkjp","http://www.nkjp.pl/ns/1.0");
        xmlw.writeCharacters("\n");
        this.indent(1, xmlw);
        xmlw.writeStartElement(TAG_TEI);
        xmlw.writeCharacters("\n");
        this.indent(2, xmlw);
        xmlw.writeStartElement(TAG_TEXT);
    }

    private void writeMetadata(Map<String, String> metadata) throws XMLStreamException{
    	if ( this.metadataWriter != null ){
	    	for ( String key : metadata.keySet() ){
	    		this.metadataWriter.writeStartElement(TAG_METADATA);
	    		this.metadataWriter.writeAttribute("name", key);
	    		this.metadataWriter.writeAttribute("value", metadata.get(key));
	    		this.metadataWriter.writeEndElement();
	    		this.metadataWriter.writeCharacters("\n");
	    	}
    	}
    }
    
    private void writeParagraphStart(String paragraphId) throws XMLStreamException {
        this.indent(5, textWriter);
        textWriter.writeStartElement(TAG_PARAGRAPH);
        textWriter.writeAttribute("xml:id", paragraphId);

        this.indent(4, annSegmentationWriter);
        annSegmentationWriter.writeStartElement(TAG_PARAGRAPH);
        annSegmentationWriter.writeAttribute("corresp", "text.xml#" + paragraphId);
        annSegmentationWriter.writeAttribute("xml:id", "segm_" + paragraphId);
        annSegmentationWriter.writeCharacters("\n");

        this.indent(4, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_PARAGRAPH);
        annMorphosyntaxWriter.writeAttribute("xml:id", paragraphId);
        annMorphosyntaxWriter.writeCharacters("\n");

        if(annNamedWriter != null){
	        this.indent(4, annNamedWriter);
	        annNamedWriter.writeStartElement(TAG_PARAGRAPH);
	        annNamedWriter.writeAttribute("xml:id", paragraphId);
	        annNamedWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + paragraphId);
	        annNamedWriter.writeCharacters("\n");
        }
        
        if(annMentionsWriter != null){
	        this.indent(4, annMentionsWriter);
	        annMentionsWriter.writeStartElement(TAG_PARAGRAPH);
	        annMentionsWriter.writeAttribute("xml:id", paragraphId);
	        annMentionsWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + paragraphId);
	        annMentionsWriter.writeCharacters("\n");
        }
        
        if(annChunksWriter != null){
	        this.indent(4, annChunksWriter);
	        annChunksWriter.writeStartElement(TAG_PARAGRAPH);
	        annChunksWriter.writeAttribute("xml:id", paragraphId);
	        annChunksWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + paragraphId);
	        annChunksWriter.writeCharacters("\n");
        }        
    }

    @Override
    public void writeDocument(Document document) {
    	for ( Paragraph paragraph : document.getParagraphs() ){
    		this.writeParagraph(paragraph);
    	}    	
    	try {
        	this.writeMetadata(document.getDocumentDescriptor().getMetadata());
			this.writeCoreferenceRelations(document.getRelations(Relation.COREFERENCE));
			this.writeRelations(document.getRelations().getRelations());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Zapisuje wszystkie relacje do strumienia annRelationsWriter
     * @param relations
     * @throws XMLStreamException 
     */
    private void writeRelations(Set<Relation> relations) throws XMLStreamException{
    	int relId = 1;
    	for ( Relation relation : relations ){

    		String sourceRef = this.mentionRefs.get(relation.getAnnotationFrom());
    		String targetRef = this.mentionRefs.get(relation.getAnnotationTo());
    		
    		if ( sourceRef == null ){
    			sourceRef = "?";
    		}
    		else{
    			// TODO: logowanie brakującej anotacji
    			// Logger.getLogger(this.getClass()).warn("Annotation ref id not found for " + relation.getAnnotationFrom());
    		}

    		if ( targetRef == null ){
    			targetRef = "?";
    		}
    		else{
    			// TODO: logowanie brakującej anotacji
    			// Logger.getLogger(this.getClass()).warn("Annotation ref id not found for " + relation.getAnnotationTo());
    		}

    		this.indent(4, annRelationsWriter);
    		this.annRelationsWriter.writeStartElement(TAG_SEGMENT);
    		this.annRelationsWriter.writeAttribute("xml:id", "relation_" + relId++);
    		this.annRelationsWriter.writeAttribute("type", relation.getType());
    		this.annRelationsWriter.writeCharacters("\n");
    		
    		this.indent(5, this.annRelationsWriter);
    		this.annRelationsWriter.writeEmptyElement(TAG_POINTER);
    		this.annRelationsWriter.writeAttribute("type", "source");
    		this.annRelationsWriter.writeAttribute("target", sourceRef);
    		this.annRelationsWriter.writeCharacters("\n");

    		this.indent(5, this.annRelationsWriter);
    		this.annRelationsWriter.writeEmptyElement(TAG_POINTER);
    		this.annRelationsWriter.writeAttribute("type", "target");
    		this.annRelationsWriter.writeAttribute("target", targetRef);
    		this.annRelationsWriter.writeCharacters("\n");

    		this.indent(4, this.annRelationsWriter);
    		this.annRelationsWriter.writeEndElement();
    		this.annRelationsWriter.writeCharacters("\n");
    	}
    }
    
    private void writeParagraph(Paragraph paragraph) {
        attributeIndex = paragraph.getAttributeIndex();
        try{
            HashMap<String, String> currentIds = new HashMap<String, String>();
            if (!open){
                open();
            }
            currentParagraphIdx++;
            currentIds.put("paragraphId", "p-"+currentParagraphIdx);
            writeParagraphStart(currentIds.get("paragraphId"));

            int sentenceNr = 1;
            int tokenNr = 1;
            StringBuilder wholeParagraph = new StringBuilder();
            for (Sentence sent: paragraph.getSentences()){
                currentIds.put("sentenceId", currentIds.get("paragraphId")+ "." + (sentenceNr++) + "-s");
                writeSentence(sent, currentIds, tokenNr, wholeParagraph);
                tokenNr += sent.getTokenNumber();
            }
            writeEndElementLine(4, annSegmentationWriter);
            writeEndElementLine(4, annMorphosyntaxWriter);
            if(annNamedWriter != null) {
            	writeEndElementLine(4, annNamedWriter);
            }
            if(annMentionsWriter != null) {
            	writeEndElementLine(4, annMentionsWriter);
            }
            if (annChunksWriter != null ){
            	writeEndElementLine(4, annChunksWriter);            	
            }

            textWriter.writeCharacters(wholeParagraph.toString().trim());
            writeEndElementLine(0, textWriter);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void writeSentence(Sentence sent, HashMap<String, String> currentIds, int currentTokenNr, StringBuilder wholeParagraph) throws XMLStreamException {
        this.indent(5, annSegmentationWriter);
        annSegmentationWriter.writeStartElement(TAG_SENTENCE);
        annSegmentationWriter.writeAttribute("xml:id", "segm_" + currentIds.get("sentenceId"));
        annSegmentationWriter.writeCharacters("\n");

        this.indent(5, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_SENTENCE);
        annMorphosyntaxWriter.writeAttribute("corresp", "ann_segmentation.xml#segm_" + currentIds.get("sentenceId"));
        annMorphosyntaxWriter.writeAttribute("xml:id", currentIds.get("sentenceId"));
        annMorphosyntaxWriter.writeCharacters("\n");

        if(annNamedWriter != null){
	        this.indent(5, annNamedWriter);
	        annNamedWriter.writeStartElement(TAG_SENTENCE);
	        annNamedWriter.writeAttribute("xml:id", currentIds.get("sentenceId"));
	        annNamedWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId"));
	        annNamedWriter.writeCharacters("\n");
        }
        
        if(annMentionsWriter != null){
	        this.indent(5, annMentionsWriter);
	        annMentionsWriter.writeStartElement(TAG_SENTENCE);
	        annMentionsWriter.writeAttribute("xml:id", currentIds.get("sentenceId"));
	        annMentionsWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId"));
	        annMentionsWriter.writeCharacters("\n");
        }

        if(annChunksWriter != null){
	        this.indent(5, annChunksWriter);
	        annChunksWriter.writeStartElement(TAG_SENTENCE);
	        annChunksWriter.writeAttribute("xml:id", currentIds.get("sentenceId"));
	        annChunksWriter.writeAttribute("corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId"));
	        annChunksWriter.writeCharacters("\n");
        }

        List<Token> sentenceTokens = sent.getTokens();
        HashMap<Integer, String> tokenTEIIds = new HashMap<Integer, String>();
        boolean noPreviousSpace = false;
        for(int i=0; i < sent.getTokenNumber(); i++){
            currentIds.put("tokenId", currentIds.get("paragraphId") + "." + (currentTokenNr++) + "-seg");
            Token currentToken = sentenceTokens.get(i);
            tokenTEIIds.put(i, "morph_" + currentIds.get("tokenId"));
            writeToken(currentToken, currentIds, wholeParagraph, noPreviousSpace);
            noPreviousSpace = currentToken.getNoSpaceAfter();
            if (!noPreviousSpace){
                wholeParagraph.append(" ");
            }
        }

        // Write named entities
        if(annNamedWriter != null){
        	int annotationNr = 1;
        	for(Annotation ann: sent.getAnnotations(namedPatterns)){
        		writeAnnotation(ann, currentIds.get("sentenceId") + "_n" + (annotationNr++), tokenTEIIds);
        	}
        }

        // Write mentions
        if(annMentionsWriter != null){
	        for(Annotation ann: sent.getAnnotations(mentionPatterns)){
	        	writeMention(ann, "mention_" + (++this.mentionNr), tokenTEIIds);
	        }
        }

        // Write chunks
        if(annChunksWriter != null){
	        for(Annotation ann: sent.getAnnotations(this.chunksPatterns)){
	        	writeChunk(ann, "chunk_" + (++this.chunkNr), tokenTEIIds);
	        }
        }

        writeEndElementLine(6, annSegmentationWriter);
        writeEndElementLine(5, annMorphosyntaxWriter);
        if(annNamedWriter != null){
        	writeEndElementLine(5, annNamedWriter);
        }
        if(annMentionsWriter != null){
        	writeEndElementLine(5, annMentionsWriter);
        }
        if(annChunksWriter != null){
        	writeEndElementLine(5, annChunksWriter);
        }
    }

    
    private void writeRelationCluster(AnnotationCluster cluster, int clusterId) throws XMLStreamException{
    	int annCoreferenceIndent = 6;
    	this.indent(annCoreferenceIndent, annCoreferenceWriter);
    	String mentions = "";
    	for(Annotation ann : cluster.getAnnotations()){
    		mentions += ann.getText() + ", ";
    	}
    	annCoreferenceWriter.writeComment(mentions);
    	annCoreferenceWriter.writeCharacters("\n");
    	
    	this.indent(annCoreferenceIndent, annCoreferenceWriter);
    	annCoreferenceWriter.writeStartElement(TAG_SEGMENT);
    	annCoreferenceWriter.writeAttribute("xml:id", "coreference_" + clusterId);
    	annCoreferenceWriter.writeCharacters("\n");
    	
    	// Start feature set
    	this.indent(++annCoreferenceIndent, annCoreferenceWriter);
    	annCoreferenceWriter.writeStartElement(TAG_FEATURESET);
    	annCoreferenceWriter.writeAttribute("type", "coreference");
    	annCoreferenceWriter.writeCharacters("\n");
    	
    	// feature - relation type (dominant was omitted)
    	this.indent(++annCoreferenceIndent, annCoreferenceWriter);
    	annCoreferenceWriter.writeEmptyElement(TAG_FEATURE);
    	annCoreferenceWriter.writeAttribute("name", "type");
    	annCoreferenceWriter.writeAttribute("fVal", "ident");
    	annCoreferenceWriter.writeCharacters("\n");
    	
    	// End feature set
    	writeEndElementLine(--annCoreferenceIndent, annCoreferenceWriter);
    	
    	// Write mentions
    	for(Annotation ann : cluster.getAnnotations()){
    		this.indent(annCoreferenceIndent, annCoreferenceWriter);
	    	annCoreferenceWriter.writeEmptyElement(TAG_POINTER);
	    	annCoreferenceWriter.writeAttribute("target", "ann_mentions.xml#" + mentionIds.get(ann));
	    	annCoreferenceWriter.writeCharacters("\n");
    	}
    	
    	// End segment
    	writeEndElementLine(--annCoreferenceIndent, annCoreferenceWriter);
//    	<seg xml:id="coreference_4">
//        <fs type="coreference">
//          <f name="type" fVal="ident"/>
//          <f name="dominant" fVal="tytuł Europejskiego Króla Kurkowego"/>
//        </fs>
//        <ptr target="ann_mentions.xml#mention_7"/>
//        <ptr target="ann_mentions.xml#mention_25"/>
//        <ptr target="ann_mentions.xml#mention_31"/>
//        <ptr target="ann_mentions.xml#mention_33"/>
//        <ptr target="ann_mentions.xml#mention_37"/>
//      </seg>
    }
    
    private void writeCoreferenceRelations(RelationSet relations) throws XMLStreamException{
    	this.indent(4, annCoreferenceWriter);
    	annCoreferenceWriter.writeStartElement(TAG_PARAGRAPH);
    	annCoreferenceWriter.writeCharacters("\n");
    	
    	AnnotationClusterSet documentRelations = AnnotationClusterSet.fromRelationSet(relations);
    	int clusterId = 0;
    	for(AnnotationCluster cluster: documentRelations.getClusters()){
    		this.writeRelationCluster(cluster, ++clusterId);
    	}
    	
    	writeEndElementLine(4, annCoreferenceWriter);
    }
    
    private void writeChunk(Annotation ann, String annotationId, HashMap<Integer, String> tokenTEIIds) throws XMLStreamException {
    	this.mentionIds.put(ann, annotationId);
    	this.mentionRefs.put(ann, "ann_mentions.xml#" + annotationId);
    	
    	int annMentionsIndent = 6;
    	this.indent(annMentionsIndent, annChunksWriter);
    	//annChunksWriter.writeComment(" " + ann.getText() + " ");
    	annChunksWriter.writeCharacters("\n");
    	this.indent(annMentionsIndent, annChunksWriter);
    	annChunksWriter.writeStartElement(TAG_SEGMENT);
    	annChunksWriter.writeAttribute("xml:id", annotationId);
    	annChunksWriter.writeCharacters("\n");
        
        // Start feature set
        this.indent(++annMentionsIndent, annChunksWriter);
        annChunksWriter.writeStartElement(TAG_FEATURESET);
        // TODO: typ ustawiony na sztywno został zmienione na annotation.getType()
        annChunksWriter.writeAttribute("type", ann.getType());
        annChunksWriter.writeCharacters("\n");
    
        if(!ann.hasHead()) ann.assignHead(); 
        this.indent(++annMentionsIndent, annChunksWriter);
        annChunksWriter.writeEmptyElement(TAG_FEATURE);
        annChunksWriter.writeAttribute("name", "semh");
        annChunksWriter.writeAttribute("fVal", "ann_morphosyntax.xml#"+ tokenTEIIds.get(ann.getHead()));
        annChunksWriter.writeCharacters("\n");
        
        // End feature set        
        writeEndElementLine(--annMentionsIndent, annChunksWriter);
        
        for(int tokenIdx: ann.getTokens()){
            this.indent(annMentionsIndent, annChunksWriter);
            annChunksWriter.writeEmptyElement(TAG_POINTER);
            annChunksWriter.writeAttribute("target", "ann_morphosyntax.xml#" + tokenTEIIds.get(tokenIdx));
            annChunksWriter.writeCharacters("\n");
        }
        writeEndElementLine(--annMentionsIndent, annChunksWriter);      
    }    
    
    /**
     * 	<!-- płace kontrolerów  -->
     *   <seg xml:id="mention_2">
     *     <fs type="mention">
     *       <f name="semh" fVal="ann_morphosyntax.xml#morph_1.1.5-seg"/>
     *     </fs>
     *     <ptr target="ann_morphosyntax.xml#morph_1.1.5-seg"/>
     *     <ptr target="ann_morphosyntax.xml#morph_1.1.6-seg"/>
     *   </seg>
     * @param ann
     * @param annotationId
     * @param tokenTEIIds
     * @throws XMLStreamException
     */
    private void writeMention(Annotation ann, String annotationId, HashMap<Integer, String> tokenTEIIds) throws XMLStreamException {
    	this.mentionIds.put(ann, annotationId);
    	this.mentionRefs.put(ann, "ann_mentions.xml#" + annotationId);
    	
    	int annMentionsIndent = 6;
    	this.indent(annMentionsIndent, annMentionsWriter);
    	//annMentionsWriter.writeComment(ann.getText());
    	annMentionsWriter.writeCharacters("\n");
    	this.indent(annMentionsIndent, annMentionsWriter);
        annMentionsWriter.writeStartElement(TAG_SEGMENT);
        annMentionsWriter.writeAttribute("xml:id", annotationId);
        annMentionsWriter.writeCharacters("\n");
        
        // Start feature set
        this.indent(++annMentionsIndent, annMentionsWriter);
        annMentionsWriter.writeStartElement(TAG_FEATURESET);
        // TODO: typ ustawiony na sztywno został zmienione na annotation.getType()
        annMentionsWriter.writeAttribute("type", ann.getType());
        annMentionsWriter.writeCharacters("\n");
    
        if(!ann.hasHead()) ann.assignHead(); 
        this.indent(++annMentionsIndent, annMentionsWriter);
        annMentionsWriter.writeEmptyElement(TAG_FEATURE);
        annMentionsWriter.writeAttribute("name", "semh");
        annMentionsWriter.writeAttribute("fVal", "ann_morphosyntax.xml#"+ tokenTEIIds.get(ann.getHead()));
        annMentionsWriter.writeCharacters("\n");
        
        // End feature set        
        writeEndElementLine(--annMentionsIndent, annMentionsWriter);
        
        for(int tokenIdx: ann.getTokens()){
            this.indent(annMentionsIndent, annMentionsWriter);
            annMentionsWriter.writeEmptyElement(TAG_POINTER);
            annMentionsWriter.writeAttribute("target", "ann_morphosyntax.xml#" + tokenTEIIds.get(tokenIdx));
            annMentionsWriter.writeCharacters("\n");
        }
        writeEndElementLine(--annMentionsIndent, annMentionsWriter);               
    }
    
    public void writeAnnotation(Annotation ann, String annotationId, HashMap<Integer, String> tokenTEIIds) throws XMLStreamException {
        int annNamedIndent = 6;
        this.indent(annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_SEGMENT);
        annNamedWriter.writeAttribute("xml:id", annotationId);
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_FEATURESET);
        annNamedWriter.writeAttribute("type", "named");
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_FEATURE);
        annNamedWriter.writeAttribute("name", "type");
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeEmptyElement(TAG_SYMBOL);
        annNamedWriter.writeAttribute("value", ann.getType());
        annNamedWriter.writeCharacters("\n");
        writeEndElementLine(--annNamedIndent, annNamedWriter);
        this.indent(annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_FEATURE);
        annNamedWriter.writeAttribute("name", "orth");
        annNamedWriter.writeCharacters("\n");
        this.indent(++annNamedIndent, annNamedWriter);
        annNamedWriter.writeStartElement(TAG_STRING);
        annNamedWriter.writeCharacters(ann.getText());
        annNamedWriter.writeEndElement();
        annNamedWriter.writeCharacters("\n");
        writeEndElementLine(--annNamedIndent, annNamedWriter);
        writeEndElementLine(--annNamedIndent, annNamedWriter);
        for(int tokenIdx: ann.getTokens()){
            this.indent(annNamedIndent, annNamedWriter);
            annNamedWriter.writeEmptyElement(TAG_POINTER);
            annNamedWriter.writeAttribute("target", "ann_morphosyntax.xml#" + tokenTEIIds.get(tokenIdx));
            annNamedWriter.writeCharacters("\n");
        }
        writeEndElementLine(--annNamedIndent, annNamedWriter);
    }

    public void writeToken(Token tok, HashMap<String, String> currentIds, StringBuilder wholeParagraph, boolean noPreviousSpace) throws XMLStreamException {
        int tokenStart = wholeParagraph.length();
        String orth = tok.getOrth();

        this.indent(6, annSegmentationWriter);
        annSegmentationWriter.writeComment(" "+orth+" ");
        annSegmentationWriter.writeCharacters("\n");
        this.indent(6, annSegmentationWriter);
        annSegmentationWriter.writeEmptyElement(TAG_SEGMENT);
        annSegmentationWriter.writeAttribute("corresp", String.format("text.xml#string-range(%s,%d,%d)", currentIds.get("paragraphId"), tokenStart, orth.length()));
        if (noPreviousSpace){
            annSegmentationWriter.writeAttribute("nkjp:nps","true");
        }
        annSegmentationWriter.writeAttribute("xml:id","segm_" + currentIds.get("tokenId"));
        annSegmentationWriter.writeCharacters("\n");

        String morphId = "morph_" + currentIds.get("tokenId");
        currentIds.put("morphId", morphId );
        this.indent(6, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_SEGMENT);
        annMorphosyntaxWriter.writeAttribute("corresp", "ann_segmentation.xml#segm_" + currentIds.get("tokenId"));
        annMorphosyntaxWriter.writeAttribute("xml:id", morphId);
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(7, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
        annMorphosyntaxWriter.writeAttribute("type", "morph");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "orth");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(9, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
        annMorphosyntaxWriter.writeCharacters(orth);
        annMorphosyntaxWriter.writeEndElement();
        annMorphosyntaxWriter.writeCharacters("\n");
        writeEndElementLine(8, annMorphosyntaxWriter);
        if (noPreviousSpace){
        	this.indent(8, annMorphosyntaxWriter);
        	annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        	annMorphosyntaxWriter.writeAttribute("name", "nps");
            annMorphosyntaxWriter.writeCharacters("\n");
            this.indent(9, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeEmptyElement(TAG_BINARY);
            annMorphosyntaxWriter.writeAttribute("value", "true");
            annMorphosyntaxWriter.writeCharacters("\n");
            writeEndElementLine(8, annMorphosyntaxWriter);
        }
        
        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeComment(String.format("%s [%s,%s]",orth,tokenStart,orth.length()));

        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name","interps");
        annMorphosyntaxWriter.writeCharacters("\n");

        Interps interps = new Interps(tok.getTags());
        int annMorphoSyntaxIndent = 9;

        if (interps.getLexemes().size() > 1){
            this.indent(annMorphoSyntaxIndent++, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_VALT);
            annMorphosyntaxWriter.writeCharacters("\n");
        }
        int lexId = 0;
        for(TEILex lex: interps.getLexemes()){
            currentIds.put("lexId", morphId + "_" + (lexId++) + "-lex");
            writeLexeme(lex, currentIds, annMorphoSyntaxIndent);
        }

        if (interps.getLexemes().size() > 1){
            writeEndElementLine(--annMorphoSyntaxIndent, annMorphosyntaxWriter);
        }

        writeEndElementLine(8, annMorphosyntaxWriter);

        this.indent(8, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "disamb");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(9, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
        String featsVal =  attributeIndex.getAttributeValue(tok, "tagTool");
        annMorphosyntaxWriter.writeAttribute("feats", featsVal != null ? featsVal : "#unknown");
        annMorphosyntaxWriter.writeAttribute("type", "tool_report");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(10, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeEmptyElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("fVal", "#" + morphId + "_" + interps.getDisambIdx() + "-msd");
        annMorphosyntaxWriter.writeAttribute("name", "choice");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(10, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "interpretation");
        annMorphosyntaxWriter.writeCharacters("\n");
        this.indent(11, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
        annMorphosyntaxWriter.writeCharacters(interps.getDisamb());
        annMorphosyntaxWriter.writeEndElement();
        annMorphosyntaxWriter.writeCharacters("\n");
        for(int i=10; i > 5; i--){
            writeEndElementLine(i, annMorphosyntaxWriter);
        }
        wholeParagraph.append(orth);
        
        /* Zapisz propsy */
        if ( tok.getProps().size() > 0 ){
        	this.indent(1, this.annPropsWriter);
        	this.annPropsWriter.writeStartElement(TAG_SEGMENT);
        	this.annPropsWriter.writeAttribute(ATTR_CORESP, "ann_morphosyntax.xml#" + morphId);
        	this.annPropsWriter.writeCharacters("\n");
        	for ( String name : tok.getProps().keySet() ){
        		String value = tok.getProps().get(name);
        		this.indent(2, this.annPropsWriter);
        		this.annPropsWriter.writeStartElement(TAG_FEATURE);
        		this.annPropsWriter.writeAttribute(ATTR_NAME, name);
        		this.annPropsWriter.writeCharacters(value);
        		this.annPropsWriter.writeEndElement();
        		this.annPropsWriter.writeCharacters("\n");
        		//System.out.println("  " + name + "=" + value);
        	}
        	this.indent(1, this.annPropsWriter);
        	this.annPropsWriter.writeEndElement();
        	this.annPropsWriter.writeCharacters("\n");
        }
    }

    public void writeLexeme(TEILex lex, HashMap<String, String> currentIds, int currentIndent) throws XMLStreamException {
        this.indent(currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURESET);
        annMorphosyntaxWriter.writeAttribute("type", "lex");
        annMorphosyntaxWriter.writeAttribute("xml:id", currentIds.get("lexId"));
        annMorphosyntaxWriter.writeCharacters("\n");
        
        this.indent(++currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "base");
        annMorphosyntaxWriter.writeCharacters("\n");
        
        this.indent(++currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_STRING);
        annMorphosyntaxWriter.writeCharacters(lex.getBase());
        annMorphosyntaxWriter.writeEndElement();
        annMorphosyntaxWriter.writeCharacters("\n");
        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        
        this.indent(currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "ctag");
        annMorphosyntaxWriter.writeCharacters("\n");
        
        this.indent(++currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeEmptyElement(TAG_SYMBOL);
        annMorphosyntaxWriter.writeAttribute("value", lex.getCtag());
        annMorphosyntaxWriter.writeCharacters("\n");
        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        
        this.indent(currentIndent, annMorphosyntaxWriter);
        annMorphosyntaxWriter.writeStartElement(TAG_FEATURE);
        annMorphosyntaxWriter.writeAttribute("name", "msd");
        annMorphosyntaxWriter.writeCharacters("\n");
        if (lex.msdSize() > 1){
            this.indent(++currentIndent, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeStartElement(TAG_VALT);
            annMorphosyntaxWriter.writeCharacters("\n");
        }
        currentIndent++;
        for(Pair<String, Integer> entry: lex.getMsds()){
            String msd = entry.getFirst();
            int msdIdx = entry.getSecond();
            this.indent(currentIndent, annMorphosyntaxWriter);
            annMorphosyntaxWriter.writeEmptyElement(TAG_SYMBOL);
            annMorphosyntaxWriter.writeAttribute("value", msd);
            annMorphosyntaxWriter.writeAttribute("xml:id", currentIds.get("morphId") + "_" + msdIdx + "-msd");
            annMorphosyntaxWriter.writeCharacters("\n");
        }
        if (lex.msdSize() > 1){
            writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        }

        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
        writeEndElementLine(--currentIndent, annMorphosyntaxWriter);
    }

    private void writeEndElementLine(int indent, XMLStreamWriter xmlw) throws XMLStreamException {
        this.indent(indent, xmlw);
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");
    }


    @Override
    public void close() {
        try {
            writeClosing(textWriter, 4);
            writeClosing(annSegmentationWriter, 3);
            writeClosing(annMorphosyntaxWriter, 3);
            if(metadataWriter != null){
            	writeClosing(metadataWriter, 1);
            }
            if(annPropsWriter != null){
            	writeClosing(annPropsWriter, 1);
            }
            if(annNamedWriter != null){
            	writeClosing(annNamedWriter, 3);
            }
            if(annMentionsWriter != null){
            	writeClosing(annMentionsWriter, 3);
            }
            if(annChunksWriter != null){
            	writeClosing(annChunksWriter, 3);
            }
            if(annCoreferenceWriter != null){
            	writeClosing(annCoreferenceWriter, 3);
            }
            if(annRelationsWriter != null){
            	writeClosing(annRelationsWriter, 3);
            }
            
            this.closeStream(this.text, textWriter);
            this.closeStream(this.metadata, this.metadataWriter);
            this.closeStream(this.annSegmentation, this.annSegmentationWriter);
            this.closeStream(this.annMorphosyntax, this.annMorphosyntaxWriter);
            this.closeStream(this.annProps, this.annPropsWriter);
            this.closeStream(this.annNamed, this.annNamedWriter);
            this.closeStream(this.annMentions, this.annMentionsWriter);
            this.closeStream(this.annChunks, this.annChunksWriter);
            this.closeStream(this.annCoreference, this.annCoreferenceWriter);
            this.closeStream(this.annRelations, this.annRelationsWriter);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeClosing(XMLStreamWriter xmlw, int indent){
        try {
            while(indent > 0){
                this.indent(indent--, xmlw);
                xmlw.writeEndElement();
                xmlw.writeCharacters("\n");
            }
            xmlw.writeEndDocument();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void indent(int repeat, XMLStreamWriter xmlw) throws XMLStreamException{
        if (this.indent)
            for (int i=0; i<repeat; i++)
                xmlw.writeCharacters(" ");
    }

    private void closeStream(OutputStream stream, XMLStreamWriter writer) throws XMLStreamException, IOException{
    	if ( writer != null ){
    		writer.close();
    	}
    	if ( stream != null && stream instanceof GZIPOutputStream ){
    		((GZIPOutputStream)stream).finish();
    	}
    	if ( stream != null ){
    		stream.close();
    	}
    }
    
}