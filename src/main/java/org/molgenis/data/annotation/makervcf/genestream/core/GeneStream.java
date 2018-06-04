package org.molgenis.data.annotation.makervcf.genestream.core;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by joeri on 6/29/16.
 *
 */
public abstract class GeneStream {

    private static final Logger LOG = LoggerFactory.getLogger(GeneStream.class);
    private Iterator<GavinRecord> relevantVariants;

    public GeneStream(Iterator<GavinRecord> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
    }

    public Iterator<GavinRecord> go()
    {
        return new Iterator<GavinRecord>(){

            GavinRecord nextResult;
            Set<String> previousGenes;
            Set<String> currentGenes;

            HashMap<String, List<GavinRecord>> variantBufferPerGene = new HashMap<>();
            List<GavinRecord> variantBuffer = new ArrayList<>();
            Iterator<GavinRecord> resultBatch;

            @Override
            public boolean hasNext() {
                    if(resultBatch != null && resultBatch.hasNext())
                    {
                       LOG.debug("[GeneStream] Returning subsequent result of gene stream batch");
                        nextResult = resultBatch.next();
                        return true;
                    }
                    else {

                        while (relevantVariants.hasNext())
                        {
                            // cleanup of result batch after previously flushed results
                            if (resultBatch != null) {
                                LOG.debug("[GeneStream] Cleanup by setting result batch to null");
                                resultBatch = null;
                            }

                            GavinRecord rv = relevantVariants.next();
                            currentGenes = RelevanceUtils.getRelevantGenes(rv.getRelevance());
                        LOG.debug("[GeneStream] Entering while, looking at a variant in gene " + currentGenes);


                            // if the previously seen genes are fully disjoint from the current genes, start processing per gene and flush buffer
                            if (previousGenes != null && Collections.disjoint(previousGenes, currentGenes)) {
                                LOG.debug("[GeneStream] Executing the abstract perGene() function on " + previousGenes);

                                // process per gene in abstract function
                                for (String gene : variantBufferPerGene.keySet()) {
                                    LOG.debug("[GeneStream] Processing gene "+gene+" having " + variantBufferPerGene.get(gene).size() + " variants");
                                    try
                                    {
                                        perGene(gene, variantBufferPerGene.get(gene));
                                    }
                                    catch (Exception e)
                                    {
                                        throw new RuntimeException(e);
                                    }
                                }
                                // create shallow copy, so that we can add another variant to buffer after we instantiate the iterator
                                resultBatch = new ArrayList<>(variantBuffer).iterator();

                                //reset buffers
                                variantBuffer = new ArrayList<>();
                                variantBufferPerGene = new HashMap<>();

                            }

                            // add current variant to gene-specific buffer
                            for (Relevance rlv : rv.getRelevance()) {
                                String gene = rlv.getGene();
                                if (variantBufferPerGene.containsKey(gene)) {
                                    variantBufferPerGene.get(gene).add(rv);
                                } else {
                                    List<GavinRecord> variants = new ArrayList<>();
                                    variants.add(rv);
                                    variantBufferPerGene.put(gene, variants);
                                }
                            }
                            // add variant to global buffer
                            variantBuffer.add(rv);

                            // cycle previous and current genes
                            previousGenes = currentGenes;

                            // if result batch ready, start streaming it out
                            if (resultBatch != null && resultBatch.hasNext()) {
                                LOG.debug("[GeneStream] Returning first result of gene stream batch");
                                nextResult = resultBatch.next();
                                return true;
                            } else {
                                //nothing to return for this gene after perGene(previousGene, variantsForGene)
                                //so we go straight to cleanup in the next iteration
                            }
                        }
                    }


                    //process the last remaining data before ending
                    if(variantBuffer.size() > 0)
                    {
                        LOG.debug("[GeneStream] Buffer has " + variantBuffer.size() + " variants left in " + variantBufferPerGene.keySet().toString());
                        for(String gene : variantBufferPerGene.keySet())
                        {
                            try
                            {
                                perGene(gene, variantBufferPerGene.get(gene));
                            }
                            catch (Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                        resultBatch = new ArrayList<>(variantBuffer).iterator();
                        variantBuffer = new ArrayList<>();
                        variantBufferPerGene = new HashMap<>();
                        if(resultBatch.hasNext())
                        {
                            LOG.debug("[GeneStream] Returning first of remaining variants");
                            nextResult = resultBatch.next();
                            return true;
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

    public abstract void perGene(String gene, List<GavinRecord> variantsPerGene) throws Exception;

}
