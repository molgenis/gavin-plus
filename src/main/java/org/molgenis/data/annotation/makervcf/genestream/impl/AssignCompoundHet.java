package org.molgenis.data.annotation.makervcf.genestream.impl;

import org.apache.commons.collections.map.MultiKeyMap;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by joeri on 7/13/16.
 */
public class AssignCompoundHet extends GeneStream {

    public AssignCompoundHet(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        super(relevantVariants, verbose);
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception
    {
        MultiKeyMap geneAlleleToSeenSamples = new MultiKeyMap();
        MultiKeyMap geneAlleleToMarkedSamples = new MultiKeyMap();

        for(RelevantVariant rv: variantsPerGene)
        {
            for(Relevance rlv : rv.getRelevance())
            {
                for(String sample: rlv.getSampleStatus().keySet())
                {
                    if(rlv.getSampleStatus().get(sample) == status.HETEROZYGOUS || rlv.getSampleStatus().get(sample) == status.CARRIER)
                    {
                        if(verbose){System.out.println("[AssignCompoundHet] Gene " + rlv.getGene() + " , sample: " +sample + ", status: " + rlv.getSampleStatus().get(sample));}
                        if( geneAlleleToSeenSamples.get(rlv.getGene(), rlv.getAllele()) != null && ((Set<String>)geneAlleleToSeenSamples.get(rlv.getGene(), rlv.getAllele())).contains(sample) )
                        {
                            if(verbose){System.out.println("[AssignCompoundHet] Marking as potential compound heterozygous: " + sample);}

                            if( geneAlleleToMarkedSamples.containsKey(rlv.getGene(), rlv.getAllele()) )
                            {
                                ((Set<String>)geneAlleleToMarkedSamples.get(rlv.getGene(), rlv.getAllele())).add(sample);
                            }
                            else {
                                Set<String> markedSamples = new HashSet<>();
                                markedSamples.add(sample);
                                geneAlleleToMarkedSamples.put(rlv.getGene(), rlv.getAllele(), markedSamples);
                            }
                        }

                        if( geneAlleleToSeenSamples.containsKey(rlv.getGene(), rlv.getAllele()) )
                        {
                            ((Set<String>)geneAlleleToSeenSamples.get(rlv.getGene(), rlv.getAllele())).add(sample);
                        }
                        else {
                            Set<String> seenSamples = new HashSet<>();
                            seenSamples.add(sample);
                            geneAlleleToSeenSamples.put(rlv.getGene(), rlv.getAllele(), seenSamples);
                        }
                    }


                }
            }

        }

        //iterate again and update marked samples
        for(RelevantVariant rv: variantsPerGene)
        {
            for(Relevance rlv : rv.getRelevance()) {
                if(geneAlleleToMarkedSamples.get(rlv.getGene(), rlv.getAllele()) != null)
                {
                    for (String sample : ((Set<String>)geneAlleleToMarkedSamples.get(rlv.getGene(), rlv.getAllele()))) {
                        if (rlv.getSampleStatus().containsKey(sample)) {
                            if (rlv.getSampleStatus().get(sample) == MatchVariantsToGenotypeAndInheritance.status.HETEROZYGOUS) {
                                if (verbose) {
                                    System.out.println("[AssignCompoundHet] Reassigning " + sample + " from " + MatchVariantsToGenotypeAndInheritance.status.HETEROZYGOUS + " to " + MatchVariantsToGenotypeAndInheritance.status.HOMOZYGOUS_COMPOUNDHET);
                                }
                                rlv.getSampleStatus().put(sample, MatchVariantsToGenotypeAndInheritance.status.HOMOZYGOUS_COMPOUNDHET);
                            } else if (rlv.getSampleStatus().get(sample) == MatchVariantsToGenotypeAndInheritance.status.CARRIER) {
                                if (verbose) {
                                    System.out.println("[AssignCompoundHet] Reassigning " + sample + " from " + MatchVariantsToGenotypeAndInheritance.status.CARRIER + " to " + MatchVariantsToGenotypeAndInheritance.status.AFFECTED_COMPOUNDHET);
                                }

                                rlv.getSampleStatus().put(sample, MatchVariantsToGenotypeAndInheritance.status.AFFECTED_COMPOUNDHET);
                            }
                        }
                    }
                }

            }
        }
    }

}
