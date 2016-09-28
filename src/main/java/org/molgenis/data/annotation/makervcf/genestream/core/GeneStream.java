package org.molgenis.data.annotation.makervcf.genestream.core;

import com.sun.tools.javac.jvm.Gen;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 6/29/16.
 *
 */
public abstract class GeneStream {

    private Iterator<RelevantVariant> relevantVariants;
    protected boolean verbose;

    public GeneStream(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.verbose = verbose;
    }

    public Iterator<RelevantVariant> go()
    {
        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;
            String previousGene;
            String currentGene;
            List<RelevantVariant> variantsForGene = new ArrayList<>();
            Iterator<RelevantVariant> resultBatch;
            boolean cleanup = false;
            RelevantVariant variantSkippedOver;

            @Override
            public boolean hasNext() {
                try {
                    if(resultBatch != null && resultBatch.hasNext())
                    {
                        if(verbose){System.out.println("[GeneStream] Returning subsequent result of gene stream batch");}
                        nextResult = resultBatch.next();
                        return true;
                    }
                    else if(cleanup)
                    {
                        if(verbose){System.out.println("[GeneStream] Cleanup of variants and genes");}
                        //this last element is the first element of the next gene
                        variantsForGene = new ArrayList<>();
                        variantsForGene.add(variantSkippedOver);
                        if(verbose){System.out.println("[GeneStream] Liftover of variant from previous iteration: " + variantsForGene.toString());}
                        cleanup = false;
                    }
                    while (relevantVariants.hasNext()) {

                        RelevantVariant rv = relevantVariants.next();

                        for(Relevance rlv : rv.getRelevance())
                        {

                            currentGene = rlv.getGene();
                            if(verbose){System.out.println("[GeneStream] Entering while, looking at a variant in gene " + currentGene);}


                            if(!currentGene.equals(previousGene) && previousGene != null)
                            {
                                if(verbose){System.out.println("[GeneStream] Executing the abstract perGene() function on " + previousGene);}

                                perGene(previousGene, variantsForGene);

                                resultBatch = variantsForGene.iterator();

                                previousGene = currentGene;
                                cleanup = true;

                                variantSkippedOver = rv;

                                if(resultBatch.hasNext())
                                {
                                    if(verbose){System.out.println("[GeneStream] Returning first result of gene stream batch");}
                                    nextResult = resultBatch.next();
                                    return true;
                                }
                                else
                                {
                                    //nothing to return for this gene after perGene(previousGene, variantsForGene)
                                    //so we go straight to cleanup in the next iteration
                                }

                            }
                            //TODO how to handle hits in multiple genes
                            variantsForGene.add(rv);
                            previousGene = currentGene;
                        }



                    }

                    //process the last remaining data before ending
                    perGene(previousGene, variantsForGene);

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

    public abstract void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception;

}
