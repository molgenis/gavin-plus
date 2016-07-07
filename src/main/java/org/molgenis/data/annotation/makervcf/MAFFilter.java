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

    public MAFFilter(Iterator<RelevantVariant> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
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
                        if(rv.getGonlAlleleFreq() < 0.05 && rv.getAlleleFreq() < 0.05)
                        {
                            nextResult = rv;
                            return true;
                        }
                        else
                        {
                            //System.out.println("removing variant at " +rv.getVariant().getChr() +":"+rv.getVariant().getPos() + " has AF >5% ; ExAC: "+rv.getAlleleFreq()+", GoNL: "+rv.getGonlAlleleFreq()+"");
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
