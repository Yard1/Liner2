package g419.liner2.api.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;


public class InTitleFeature extends TokenInSentenceFeature{

	public InTitleFeature(String name){
		super(name);
	}


	@Override
	public void generate(Sentence sentence){
		int thisFeatureIdx = sentence.getAttributeIndex().getIndex(this.getName());
		ArrayList<Token> tokens = sentence.getTokens();

		int tokenIdx = 0;
		while (tokenIdx < sentence.getTokenNumber()) {
			Token t = tokens.get(tokenIdx);
			String base = t.getAttributeValue("base");
			t.setAttributeValue(thisFeatureIdx, sentence.getDocument().isInTitle(base) + "");
			tokenIdx++;
		}
	}

}
