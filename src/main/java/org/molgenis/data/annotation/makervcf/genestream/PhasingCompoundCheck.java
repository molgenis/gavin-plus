package org.molgenis.data.annotation.makervcf.genestream;

import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;

/**
 * Created by joeri on 6/29/16.
 *
 * detects fake compounds when affected alleles are all from 1 parent
 * if not, update status to 'FALSE_COMPOUND' or something, in case we want to TODO
 *
 * assumptions:
 * 2+ heterozygous/carrier variants per sample
 *
 * examples:
 *
 [PhasingCompoundCheck] gene: ATP11C
 Sample HG00178 has a HOMOZYGOUS_COMPOUNDHET genotype 1|0
 Sample HG00178 has a HOMOZYGOUS_COMPOUNDHET genotype 1|0
 leftHaploSamples: [HG00178]
 rightHaploSamples: []
 fake compounds: [HG00178]

 [PhasingCompoundCheck] gene: CDR1
 Sample HG03410 has a HOMOZYGOUS_COMPOUNDHET genotype 2|0
 Sample HG03410 has a HOMOZYGOUS_COMPOUNDHET genotype 1|0
 leftHaploSamples: [HG03410]
 rightHaploSamples: []
 fake compounds: [HG03410]

 [PhasingCompoundCheck] gene: MAGEC3
 Sample NA19391 has a HOMOZYGOUS_COMPOUNDHET genotype 1|0
 Sample NA19391 has a HOMOZYGOUS_COMPOUNDHET genotype 0|1
 leftHaploSamples: [NA19391]
 rightHaploSamples: [NA19391]
 fake compounds: []
 *
 */
public class PhasingCompoundCheck extends GeneStream{

    public PhasingCompoundCheck(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        super(relevantVariants, verbose);
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {


        if(verbose){System.out.println("[PhasingCompoundCheck] gene: " + gene);}

        // e.g. the 0 in 0|1
        Set<String> leftHaploSamples = new HashSet<String>();

        // e.g. the 1 in 0|1
        Set<String> rightHaploSamples = new HashSet<String>();

        // samples with 1+ variants unphased, cannot call fake comphet on them
        Set<String> samplesWithUnphasedVariants = new HashSet<String>();

        for(RelevantVariant rv : variantsPerGene)
        {
            char affectedIndex = Character.forDigit(rv.getVariant().getAltIndex(rv.getAllele()), 10);
            for(String sample : rv.getSampleStatus().keySet())
            {
                if(samplesWithUnphasedVariants.contains(sample))
                {
                    continue;
                }
                if(MatchVariantsToGenotypeAndInheritance.status.isCompound(rv.getSampleStatus().get(sample)))
                {
                    String geno = rv.getSampleGenotypes().get(sample);
                    if(verbose){System.out.println("[PhasingCompoundCheck] Sample "+sample+" has a "+rv.getSampleStatus().get(sample)+" genotype " + geno);}
                    if(geno.length() != 3)
                    {
                        throw new Exception("genotype length != 3");
                    }
                    // if there is a non-phased genotype, e.g. 0/1 or perhaps 1/2 where affected = 1, we have to stop
                    // since there are 2 (or more) variants to form a compound, having 1 (or more) unphased variants means that it can always be a real compound
                    if(geno.charAt(1) == '/')
                    {
                        samplesWithUnphasedVariants.add(sample);
                        if(verbose){System.out.println("[PhasingCompoundCheck] Sample unphased, excluded");}
                    }
                    else if(geno.charAt(0) == affectedIndex && geno.charAt(1) == '|' && geno.charAt(2) != affectedIndex)
                    {
                        leftHaploSamples.add(sample);
                    }
                    else if(geno.charAt(0) != affectedIndex && geno.charAt(1) == '|' && geno.charAt(2) == affectedIndex)
                    {
                        rightHaploSamples.add(sample);
                    }
                    else
                    {
                        throw new Exception("No match to either unphased or phased genotype, whats going on? sample "+sample+" has a "+rv.getSampleStatus().get(sample)+" genotype " + geno);}
                    }

                }
            }



        if(verbose){System.out.println("[PhasingCompoundCheck] leftHaploSamples: " + leftHaploSamples.toString());
        System.out.println("[PhasingCompoundCheck] rightHaploSamples: " + rightHaploSamples.toString());}
       // leftHaploSamples.retainAll(rightHaploSamples);
      //  System.out.println("true compounds: " + leftHaploSamples.toString());


        Set<String> union = new HashSet(leftHaploSamples);
        union.addAll(rightHaploSamples);

        // remove intersection
        for (String inA : leftHaploSamples)
        {
            if(rightHaploSamples.contains(inA)){
                union.remove(inA);
            }
        }
        if(verbose){System.out.println("[PhasingCompoundCheck] false compounds: " + union.toString());}


        for(RelevantVariant rv : variantsPerGene)
        {
            for(String sample : rv.getSampleStatus().keySet())
            {
                if(union.contains(sample) && status.isCompound(rv.getSampleStatus().get(sample)))
                {
                    if(verbose){System.out.println("[PhasingCompoundCheck] Going to update sample "+sample+" from "+rv.getSampleStatus().get(sample)+" to " + status.HETEROZYGOUS_MULTIHIT);}
                    rv.getSampleStatus().put(sample, status.HETEROZYGOUS_MULTIHIT);
                }
            }
        }


    }
}
