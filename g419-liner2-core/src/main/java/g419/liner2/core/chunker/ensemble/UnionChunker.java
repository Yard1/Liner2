package g419.liner2.core.chunker.ensemble;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.core.chunker.Chunker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * TODO
 * <p>
 * Chunker stanowi sumę podanych chunkerów. Wynikiem jest zbiór chunków
 * rozpoznanych przez kolejne chunki z usunięciem duplikatów.
 *
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 */
public class UnionChunker extends Chunker {

  private ArrayList<Chunker> chunkers;

  public UnionChunker(ArrayList<Chunker> chunkers) {
    this.chunkers = chunkers;
  }

  public Map<Sentence, AnnotationSet> chunk(Document ps) {
    Map<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();

    for (Paragraph p : ps.getParagraphs()) {
      for (Sentence sentence : p.getSentences()) {
        chunkings.put(sentence, new AnnotationSet(sentence));
      }
    }

    for (Chunker chunker : this.chunkers) {
      Map<Sentence, AnnotationSet> chunkingThis = chunker.chunk(ps);
      for (Sentence sentence : chunkingThis.keySet()) {
        chunkings.get(sentence).union(chunkingThis.get(sentence));
      }
    }

    return chunkings;
  }


}
