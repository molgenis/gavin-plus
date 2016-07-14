package org.molgenis.data.annotation.makervcf.genestream;

import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by joeri on 6/29/16.
 *
 * for phased data, check if for 1 sample on 1 gene, different parental alleles are affected
 * if not, update status to 'FALSE_COMPOUND' or something, in case we want to
 * TODO
 *
 */
public class PhasingCompoundCheck extends GeneStream{

    public PhasingCompoundCheck(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        super(relevantVariants, verbose);
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {

        System.out.println("[PhasingCompoundCheck] gene: " + gene);

        // e.g. the 0 in 0|1
        Set<String> leftHaploSamples = new HashSet<String>();

        // e.g. the 1 in 0|1
        Set<String> rightHaploSamples = new HashSet<String>();

        for(RelevantVariant rv : variantsPerGene)
        {
            char affectedIndex = Character.forDigit(rv.getVariant().getAltIndex(rv.getAllele()), 10);
            for(String sample : rv.getSampleStatus().keySet())
            {
                if(MatchVariantsToGenotypeAndInheritance.status.isCompound(rv.getSampleStatus().get(sample)))
                {



                    String geno = rv.getSampleGenotypes().get(sample);
                    System.out.println("Sample "+sample+" has a "+rv.getSampleStatus().get(sample)+" genotype " + geno);
                    if(geno.length() != 3)
                    {
                        throw new Exception("genotype length != 3");
                    }
                    if(geno.charAt(0) == affectedIndex && geno.charAt(1) == '|' && geno.charAt(2) != affectedIndex)
                    {
                        leftHaploSamples.add(sample);
                    }
                    else if(geno.charAt(0) != affectedIndex && geno.charAt(1) == '|' && geno.charAt(2) == affectedIndex)
                    {
                        rightHaploSamples.add(sample);
                    }
                    //for non-phased, add to both? or none?
                }
            }
        }


        System.out.println("leftHaploSamples: " + leftHaploSamples.toString());
        System.out.println("rightHaploSamples: " + rightHaploSamples.toString());
        leftHaploSamples.retainAll(rightHaploSamples);
        System.out.println("true compounds: " + leftHaploSamples.toString());



    }
}
