package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;

import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 */
public class CleanupVariantsWithoutSamples {

    private Iterator<GavinRecord> relevantVariants;
    private boolean verbose;

    public CleanupVariantsWithoutSamples(Iterator<GavinRecord> relevantVariants, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.verbose = verbose;
    }

    public Iterator<GavinRecord> go()
    {
        return new Iterator<GavinRecord>(){

            GavinRecord nextResult;

            @Override
            public boolean hasNext() {
                try {
                    while (relevantVariants.hasNext()) {
                        GavinRecord rv = relevantVariants.next();

                        if(verbose) {
                            System.out.println("[CleanupVariantsWithoutSamples] Looking at: " + rv.toString());
                        }

                        for(Relevance rlv : rv.getRelevance())
                        {
                            if(rlv.getSampleStatus().size() != rlv.getSampleGenotypes().size())
                            {
                                throw new Exception("[CleanupVariantsWithoutSamples] rv.getSampleStatus().size() != rv.getSampleGenotypes().size()");
                            }

                            //we want at least 1 interesting sample
                            if(rlv.getSampleStatus().size() > 0)
                            {
                                nextResult = rv;
                                return true;
                            }
                            else if(verbose)
                            {
                                if(verbose) { System.out.println("[CleanupVariantsWithoutSamples] Removing variant at " +rv.getChromosome() +":"+rv.getPosition() + " because it has 0 samples left"); }
                            }
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
            public GavinRecord next() {
                return nextResult;
            }
        };
    }
}
