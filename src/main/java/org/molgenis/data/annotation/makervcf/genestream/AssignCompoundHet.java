package org.molgenis.data.annotation.makervcf.genestream;

import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
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
        Set<String> samplesSeen = new HashSet<String>();
        Set<String> markedSamples  = new HashSet<String>();
        for(RelevantVariant rv: variantsPerGene)
        {
            for(String sample: rv.getSampleStatus().keySet())
            {
                if(rv.getSampleStatus().get(sample) == status.HETEROZYGOUS || rv.getSampleStatus().get(sample) == status.CARRIER)
                {
                    if(verbose){System.out.println("gene : " + rv.getGene() + " , sample: " +sample + ", status: " + rv.getSampleStatus().get(sample));}
                    if(samplesSeen.contains(sample))
                    {
                        if(verbose){System.out.println("comphet");}
                        markedSamples.add(sample);
                    }
                    samplesSeen.add(sample);
                }


            }
        }

        //iterate again and update marked samples
        for(RelevantVariant rv: variantsPerGene) {

            for(String sample : markedSamples)
            {
                if(verbose){System.out.println("marked sample: " + sample);}
                if(rv.getSampleStatus().containsKey(sample))
                {
                    if(rv.getSampleStatus().get(sample).equals(MatchVariantsToGenotypeAndInheritance.status.HETEROZYGOUS))
                    {
                        if(verbose){System.out.println("reassigning " + sample + " from " + MatchVariantsToGenotypeAndInheritance.status.HETEROZYGOUS + " to " + MatchVariantsToGenotypeAndInheritance.status.HOMOZYGOUS_COMPOUNDHET);}
                        rv.getSampleStatus().put(sample, MatchVariantsToGenotypeAndInheritance.status.HOMOZYGOUS_COMPOUNDHET);
                    }
                    if(rv.getSampleStatus().get(sample).equals(MatchVariantsToGenotypeAndInheritance.status.CARRIER))
                    {
                        if(verbose){System.out.println("reassigning " + sample + " from " + MatchVariantsToGenotypeAndInheritance.status.CARRIER + " to " + MatchVariantsToGenotypeAndInheritance.status.AFFECTED_COMPOUNDHET);}

                        rv.getSampleStatus().put(sample, MatchVariantsToGenotypeAndInheritance.status.AFFECTED_COMPOUNDHET);
                    }

                    if(verbose){System.out.println("updating " +  rv.getGene() + " for sample + " + sample + " to COMPOUNDHET");}
                }
            }
        }
    }

}
