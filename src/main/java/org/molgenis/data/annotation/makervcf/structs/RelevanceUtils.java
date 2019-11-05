package org.molgenis.data.annotation.makervcf.structs;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class RelevanceUtils
{
	private RelevanceUtils()
	{
	}

	public static Relevance getRelevanceForGene(List<Relevance> relevances, String gene)
	{
		return relevances.stream().filter(relevance -> relevance.getGene().equals(gene)).findFirst().orElse(null);
	}
}
