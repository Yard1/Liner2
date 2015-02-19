package g419.crete.api.features.clustermention;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.features.enumvalues.Gender;
import g419.crete.api.features.enumvalues.Number;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingMentionNumber extends AbstractClusterMentionFeature<Number>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = Number.UNDEFINED;
		}
		else{
			closestPreceeding.assignHead();
			Token headToken = closestPreceeding.getSentence().getTokens().get(closestPreceeding.getHead());
			
			TokenAttributeIndex ai = closestPreceeding.getSentence().getAttributeIndex();
					
			this.value = Number.fromValue(ai.getAttributeValue(headToken, "number"));
		}
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_number";
	}

	@Override
	public Class<Number> getReturnTypeClass() {
		return Number.class;
	}
	
	@Override
	public int getSize() {
		return Number.values().length;
	}

	@Override
	public List<Number> getAllValues(){
		return Arrays.asList(Number.values());
	}

	
	
}