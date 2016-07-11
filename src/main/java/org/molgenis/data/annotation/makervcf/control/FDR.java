package org.molgenis.data.annotation.makervcf.control;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
    private File writeTo;
    private PrintWriter pw;

    public FDR(Iterator<RelevantVariant> relevantVariants, File writeTo) throws FileNotFoundException {
        this.relevantVariants = relevantVariants;
        this.writeTo = writeTo;
        this.pw = new PrintWriter(writeTo);
    }

    public Iterator<RelevantVariant> go()
    {
        this.pw.println("Gene\tFDR");

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

                        currentGene = rv.getGene();


                        if(!currentGene.equals(previousGene))
                        {
                            pw.println(previousGene + "\t" + (uniquelyAffectedSamplesForThisGene.size()));
                            pw.flush();
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


                    //last gene
                    pw.println(previousGene + "\t" + (uniquelyAffectedSamplesForThisGene.size()));
                    pw.flush();
                    uniquelyAffectedSamplesForThisGene.clear();


                    pw.flush();
                    pw.close();
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
