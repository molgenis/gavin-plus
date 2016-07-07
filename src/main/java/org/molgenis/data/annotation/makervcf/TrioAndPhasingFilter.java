package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.vcf.datastructures.Trio;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by joeri on 6/29/16.
 *
 * mark denovo?
 * remove variants that are no longer relevant?
 * remove compound het on same allele?
 * re-use phasing data?
 *
 *
 * For samples that are 'compound' affected, check if this is really so
 * Get all variants for a gene
 * Use either trio data or pre-phased data to check if both chromosomal copies are affected
 * Or if all 'relevant variants' reside on the same copy, in which case it is not compound.
 *
 * TODO
 *
 */
public class TrioAndPhasingFilter {

    private Iterator<RelevantVariant> relevantVariants;
    private HashMap<String, Trio> trios;
    public TrioAndPhasingFilter(Iterator<RelevantVariant> relevantVariants, HashMap<String, Trio> trios)
    {
        this.relevantVariants = relevantVariants;
        this.trios = trios;
    }

    public Iterator<RelevantVariant> go()
    {
        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;

            @Override
            public boolean hasNext() {
                try {
                    while (relevantVariants.hasNext()) {
                        RelevantVariant rv = relevantVariants.next();

                        if (rv.getSampleStatus() != null) {

                            for (String sample : rv.getSampleStatus().keySet()) {
                                String status = rv.getSampleStatus().get(sample);

                                if (status.contains("COMPOUND")) {
                   //                 System.out.println("compound status for " + sample + ", " + rv.getVariant().getChr() +":"+rv.getVariant().getPos() + " -> " + status);
                                }


                                if (status.equals("HETEROZYGOUS")) {
                                    if (trios.containsKey(sample)) {
                                        Trio t = trios.get(sample);
                                        //get parental genotype.... or just check if affected too ??
                                    }
                                }
                            }
                        }

                        nextResult = rv;
                        return true;
                    }

                    return false;
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public RelevantVariant next() {
                return nextResult;
            }
        };
    }
}
