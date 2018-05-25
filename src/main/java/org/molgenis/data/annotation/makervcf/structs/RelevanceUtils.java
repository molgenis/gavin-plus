package org.molgenis.data.annotation.makervcf.structs;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class RelevanceUtils
{
	private RelevanceUtils()
	{
	}

	/**
	 * Helper function to group all genes of relevance
	 *
	 * @param relevances
	 */
	public static Set<String> getRelevantGenes(List<Relevance> relevances)
	{
		return relevances.stream().map(Relevance::getGene).collect(toSet());
	}

	public static Set<String> getRelevantAlts(List<Relevance> relevances)
	{
		return relevances.stream().map(Relevance::getAllele).collect(toSet());
	}

	public static Relevance getRelevanceForGene(List<Relevance> relevances, String gene)
	{
		return relevances.stream().filter(relevance -> relevance.getGene().equals(gene)).findFirst().orElse(null);
	}
}
