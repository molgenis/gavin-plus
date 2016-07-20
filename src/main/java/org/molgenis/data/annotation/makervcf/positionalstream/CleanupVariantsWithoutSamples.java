package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 */
public class CleanupVariantsWithoutSamples {

    private Iterator<RelevantVariant> relevantVariants;
    private boolean verbose;

    public CleanupVariantsWithoutSamples(Iterator<RelevantVariant> relevantVariants, boolean verbose)
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

                        if(rv.getSampleStatus().size() != rv.getSampleGenotypes().size())
                        {
                            throw new Exception("[CleanupVariantsWithoutSamples] rv.getSampleStatus().size() != rv.getSampleGenotypes().size()");
                        }

                        //we want at least 1 interesting sample
                        if(rv.getSampleStatus().size() > 0)
                        {
                            nextResult = rv;
                            return true;
                        }
                        else if(verbose)
                        {
                            if(verbose) { System.out.println("[CleanupVariantsWithoutSamples] Removing variant at " +rv.getVariant().getChr() +":"+rv.getVariant().getPos() + " because it has 0 samples left"); }
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
