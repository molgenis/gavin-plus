package org.molgenis.data.annotation.makervcf.control;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by joeri on 6/29/16.
 *
 * False Discovery Rate
 *
 * TODO: the last gene!!
 *
 */
public class FDR {

    private Iterator<RelevantVariant> relevantVariants;

    public FDR(Iterator<RelevantVariant> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
    }

    public Iterator<RelevantVariant> go()
    {
        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;
            Set<String> uniquelyAffectedSamplesForThisGene = new HashSet<String>();

            String previousGene = "n/a";
            String currentGene;

            @Override
            public boolean hasNext() {
                try {
                    while (relevantVariants.hasNext()) {
                        RelevantVariant rv = relevantVariants.next();

                      //  System.out.println(rv.getGene());

                        currentGene = rv.getGene();


                        if(!currentGene.equals(previousGene))
                        {
                            System.out.println(previousGene + "\t" + (uniquelyAffectedSamplesForThisGene.size() / 2504.0) * 100);
                            uniquelyAffectedSamplesForThisGene.clear();
                        }

                        //same as last time, so keep counting
                        for(String sample : rv.getSampleStatus().keySet())
                        {
                            //todo create ENUM for status stuff..
                            if(!rv.getSampleStatus().get(sample).contains("CARRIER"))
                            {
                                uniquelyAffectedSamplesForThisGene.add(sample);
                            }
                        }

                        previousGene = currentGene;



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
