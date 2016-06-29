package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 */
public class TrioAwareFilter {

    private Iterator<RelevantVariant> relevantVariants;
    public TrioAwareFilter(Iterator<RelevantVariant> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
    }

    public Iterator<RelevantVariant> go()
    {
        return new Iterator<RelevantVariant>(){

            @Override
            public boolean hasNext() {
                return relevantVariants.hasNext();
            }

            @Override
            public RelevantVariant next() {

                try {
                    RelevantVariant rv = relevantVariants.next();




                    return rv;
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }


            }
        };
    }
}
