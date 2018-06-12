package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 */
public class MAFFilter {
    private static final Logger LOG = LoggerFactory.getLogger(MAFFilter.class);
    private Iterator<GavinRecord> relevantVariants;
    double threshold = 0.05;

    public MAFFilter(Iterator<GavinRecord> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
    }

    public Iterator<GavinRecord> go()
    {
        return new Iterator<GavinRecord>(){

            GavinRecord nextResult;

            @Override
            public boolean hasNext() {
                    while (relevantVariants.hasNext()) {
                        GavinRecord rv = relevantVariants.next();

                        for(Relevance rlv : rv.getRelevance())
                        {
                            //use GoNL/ExAC MAF to control for false positives (or non-relevant stuff) in ClinVar
                            if(rlv.getGonlAlleleFreq() < threshold && rlv.getAlleleFreq() < threshold)
                            {
                                nextResult = rv;
                                return true;
                            }
                            LOG.debug("[MAFFilter] Removing variant at " +rv.getChromosome() +":"+rv.getPosition() + " because it has AF >"+threshold+". ExAC: "+rlv.getAlleleFreq()+", GoNL: "+rlv.getGonlAlleleFreq()+"");
                        }

                    }
                    return false;
            }

            @Override
            public GavinRecord next() {
                return nextResult;
            }
        };
    }
}
