package org.molgenis.data.annotation.makervcf.genestream.impl;

import org.apache.commons.collections.map.MultiKeyMap;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.Status;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by joeri on 7/13/16.
 */
public class AssignCompoundHet extends GeneStream
{

	private static final Logger LOG = LoggerFactory.getLogger(AssignCompoundHet.class);

	public AssignCompoundHet(Iterator<GavinRecord> relevantVariants)
	{
		super(relevantVariants);
	}

	@Override
	public void perGene(String gene, List<GavinRecord> variantsPerGene)
	{
		MultiKeyMap geneAlleleToSeenSamples = new MultiKeyMap();
		MultiKeyMap geneAlleleToMarkedSamples = new MultiKeyMap();

		for (GavinRecord gavinRecord : variantsPerGene)
		{
			if (gavinRecord.isRelevant())
			{
				for (Relevance rlv : gavinRecord.getRelevance())
				{
					if (!rlv.getGene().equals(gene))
					{
						continue;
					}
					for (String sample : rlv.getSampleStatus().keySet())
					{
						if (rlv.getSampleStatus().get(sample)
								== MatchVariantsToGenotypeAndInheritance.Status.HETEROZYGOUS
								|| rlv.getSampleStatus().get(sample)
								== MatchVariantsToGenotypeAndInheritance.Status.CARRIER)
						{
							LOG.debug("[AssignCompoundHet] Gene {} , sample: {}, Status: {}", rlv.getGene(), sample,
									rlv.getSampleStatus().get(sample));
							if (geneAlleleToSeenSamples.get(rlv.getGene(), rlv.getAllele()) != null
									&& ((Set<String>) geneAlleleToSeenSamples.get(rlv.getGene(),
									rlv.getAllele())).contains(sample))
							{
								LOG.debug("[AssignCompoundHet] Marking as potential compound heterozygous: {}", sample);

								if (geneAlleleToMarkedSamples.containsKey(rlv.getGene(), rlv.getAllele()))
								{
									((Set<String>) geneAlleleToMarkedSamples.get(rlv.getGene(), rlv.getAllele())).add(
											sample);
								}
								else
								{
									Set<String> markedSamples = new HashSet<>();
									markedSamples.add(sample);
									geneAlleleToMarkedSamples.put(rlv.getGene(), rlv.getAllele(), markedSamples);
								}
							}

							if (geneAlleleToSeenSamples.containsKey(rlv.getGene(), rlv.getAllele()))
							{
								((Set<String>) geneAlleleToSeenSamples.get(rlv.getGene(), rlv.getAllele())).add(sample);
							}
							else
							{
								Set<String> seenSamples = new HashSet<>();
								seenSamples.add(sample);
								geneAlleleToSeenSamples.put(rlv.getGene(), rlv.getAllele(), seenSamples);
							}
						}

					}
				}
			}
		}

		//iterate again and update marked samples
		for (GavinRecord rv : variantsPerGene)
		{
			for (Relevance rlv : rv.getRelevance())
			{
				if (!rlv.getGene().equals(gene))
				{
					continue;
				}
				if (geneAlleleToMarkedSamples.get(rlv.getGene(), rlv.getAllele()) != null)
				{
					for (String sample : ((Set<String>) geneAlleleToMarkedSamples.get(rlv.getGene(), rlv.getAllele())))
					{
						if (rlv.getSampleStatus().containsKey(sample))
						{
							if (rlv.getSampleStatus().get(sample) == Status.HETEROZYGOUS)
							{
								LOG.debug("[AssignCompoundHet] Reassigning {} from {} to {}", sample,
										MatchVariantsToGenotypeAndInheritance.Status.HETEROZYGOUS,
										Status.HOMOZYGOUS_COMPOUNDHET);
								rlv.getSampleStatus().put(sample, Status.HOMOZYGOUS_COMPOUNDHET);
							}
							else if (rlv.getSampleStatus().get(sample)
									== MatchVariantsToGenotypeAndInheritance.Status.CARRIER)
							{
								LOG.debug("[AssignCompoundHet] Reassigning {} from {} to {}", sample,
										MatchVariantsToGenotypeAndInheritance.Status.CARRIER,
										Status.AFFECTED_COMPOUNDHET);
								rlv.getSampleStatus()
								   .put(sample, MatchVariantsToGenotypeAndInheritance.Status.AFFECTED_COMPOUNDHET);
							}
						}
					}
				}

			}
		}
	}

}
