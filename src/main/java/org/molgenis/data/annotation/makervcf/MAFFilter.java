package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.vcf.datastructures.Trio;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 */
public class MAFFilter {

    private Iterator<RelevantVariant> relevantVariants;
    double threshold = 0.05;
    boolean verbose;

    public MAFFilter(Iterator<RelevantVariant> relevantVariants, boolean verbose)
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

                        //use GoNL/ExAC MAF to control for false positives (or non-relevant stuff) in ClinVar
                        if(rv.getGonlAlleleFreq() < threshold && rv.getAlleleFreq() < threshold)
                        {
                            nextResult = rv;
                            return true;
                        }
                        else if(verbose)
                        {
                            System.out.println("removing variant at " +rv.getVariant().getChr() +":"+rv.getVariant().getPos() + " because it has AF >"+threshold+". ExAC: "+rv.getAlleleFreq()+", GoNL: "+rv.getGonlAlleleFreq()+"");
                        }
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
