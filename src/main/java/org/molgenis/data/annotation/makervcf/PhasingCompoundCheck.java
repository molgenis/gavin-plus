package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.vcf.datastructures.Trio;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 * for phased data, check if for 1 sample on 1 gene, different parental alleles are affected
 * if not, update status to 'FALSE_COMPOUND' or something, in case we want to
 * TODO
 *
 */
public class PhasingCompoundCheck {

    private Iterator<RelevantVariant> relevantVariants;
    private boolean verbose;
    public PhasingCompoundCheck(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
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

                        if (rv.getSampleStatus() != null) {

                            for (String sample : rv.getSampleStatus().keySet()) {
                                status status = rv.getSampleStatus().get(sample);

                                if (status.isCompound(status)) {
                                   // System.out.println("compound status for " + sample + ", " + rv.getVariant().getChr() +":"+rv.getVariant().getPos() + " -> " + status);
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
