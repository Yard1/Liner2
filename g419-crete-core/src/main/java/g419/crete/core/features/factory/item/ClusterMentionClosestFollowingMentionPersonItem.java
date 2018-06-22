package g419.crete.core.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.clustermention.following.ClusterMentionClosestFollowingMentionPerson;
import g419.crete.core.features.enumvalues.Person;
import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestFollowingMentionPersonItem implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Person> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Person> createFeature() {
		return new ClusterMentionClosestFollowingMentionPerson();
	}

}
