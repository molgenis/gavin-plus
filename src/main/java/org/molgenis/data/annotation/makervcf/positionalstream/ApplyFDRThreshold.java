package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;

import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 * TODO JvdV:
 *
 */
public class ApplyFDRThreshold {

    private Iterator<GavinRecord> relevantVariants;

    public ApplyFDRThreshold(Iterator<GavinRecord> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
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

                        //get FDR and possibly apply threshold
                        //at least 'annotate' with FDR, possibly FOR

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
            public GavinRecord next() {
                return nextResult;
            }
        };
    }
}
