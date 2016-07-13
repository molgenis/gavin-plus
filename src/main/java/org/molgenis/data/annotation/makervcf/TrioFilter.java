package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.vcf.datastructures.Trio;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance.status;

/**
 * Created by joeri on 6/29/16.
 *
 * find and mark denovo
 * remove variants that are no longer relevant, ie that have an equal parental genotype
 *
 *
 * TODO
 *
 */
public class TrioFilter {

    private Iterator<RelevantVariant> relevantVariants;
    private HashMap<String, Trio> trios;
    private boolean verbose;
    public TrioFilter(Iterator<RelevantVariant> relevantVariants, HashMap<String, Trio> trios, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.trios = trios;
        this.verbose = verbose;
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

//                        if (rv.getSampleStatus() != null) {
//
//                            for (String sample : rv.getSampleStatus().keySet()) {
//                                status status = rv.getSampleStatus().get(sample);
//
//                                if (status.isCompound(status)) {
//                                   // System.out.println("compound status for " + sample + ", " + rv.getVariant().getChr() +":"+rv.getVariant().getPos() + " -> " + status);
//                                }
//
//
//                                if (status == status.HETEROZYGOUS) {
//                                    if (trios.containsKey(sample)) {
//                                        Trio t = trios.get(sample);
//                                        //get parental genotype.... or just check if affected too ??
//                                    }
//                                }
//                            }
//                        }

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
