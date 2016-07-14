package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 *
 * TODO
 *
 */
public class ApplyFDRThreshold {

    private Iterator<RelevantVariant> relevantVariants;

    public ApplyFDRThreshold(Iterator<RelevantVariant> relevantVariants)
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
            public RelevantVariant next() {
                return nextResult;
            }
        };
    }
}
