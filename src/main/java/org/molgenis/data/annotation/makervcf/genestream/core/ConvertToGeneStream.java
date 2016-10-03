package org.molgenis.data.annotation.makervcf.genestream.core;

import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.*;

/**
 * Created by joeri on 6/29/16.
 *
 * We re-order the stream of variants so that genes are always grouped together
 *
 */
public class ConvertToGeneStream {

    private Iterator<RelevantVariant> relevantVariants;
    private ArrayList<Integer> positionalOrder;
    private boolean verbose;

    public ConvertToGeneStream(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.positionalOrder = new ArrayList<>();
        this.verbose = verbose;
    }

    public ArrayList<Integer> getPositionalOrder() {
        return positionalOrder;
    }

    public Iterator<RelevantVariant> go()
    {

        return new Iterator<RelevantVariant>(){

            // the next result as prepared by hasNext() and outputted by next()
            RelevantVariant nextResult;

            // result iterators that become active once one or more genes end and output nextResult
            LinkedHashMap<String, Iterator<RelevantVariant>> resultBatches;

            // variantBuffer with genes and variants that lags behind the input and gets turned into result batches
            HashMap<String, LinkedHashMap<Integer, RelevantVariant>> variantBuffer = new HashMap<>();

            // set of genes seen for variant in previous iteration
            Set<String> underlyingGenesForPreviousVariant = new HashSet<>();

            // within returning a batch, there may be duplicates (e.g. variants relevant for multiple genes)
            // we keep track of the positions and make sure they are only outputted once
            List<Integer> positionCheck = new ArrayList<>();


            @Override
            public boolean hasNext() {

                RelevantVariant nextFromResultBatches = getNextFromResultBatches(resultBatches, positionCheck);
                if(nextFromResultBatches != null)
                {
                    if(verbose){System.out.println("[ConvertToGeneStream] Flushing next variant: " + nextFromResultBatches.toStringShort()); }
                    nextResult = nextFromResultBatches;
                    return true;
                }
                else
                {
                    while(relevantVariants.hasNext())
                    {

                        if(resultBatches != null)
                        {
                            if(verbose){System.out.println("[ConvertToGeneStream] Flush complete, cleanup of genes: " + resultBatches.keySet()); }

                            // we remove variants from the variantBuffer (by position) that were already written out for another gene before
                            // of course we also delete the variants for the genes that were written out
                            for(String gene : resultBatches.keySet())
                            {
                                ArrayList<Integer> removeVariantsByPosition = new ArrayList<Integer>(variantBuffer.get(gene).size());
                                for(Integer pos : variantBuffer.get(gene).keySet())
                                {
                                    removeVariantsByPosition.add(pos);
                                }
                                for(String geneInBuffer : variantBuffer.keySet())
                                {
                                    variantBuffer.get(geneInBuffer).keySet().removeAll(removeVariantsByPosition);
                                }
                                variantBuffer.remove(gene);
                            }
                            positionCheck.clear();
                            resultBatches = null;
                        }

                        // get variant, store position, and get underlying genes
                        RelevantVariant rv = relevantVariants.next();
                        int pos = rv.getVariant().getPos();
                        positionalOrder.add(pos);
                        Set<String> underlyingGenesForCurrentVariant = rv.getVariant().getGenes();

                        if(verbose){System.out.println("[ConvertToGeneStream] Assessing next variant: " +rv.toStringShort() );}

                        // put genes and variants in a map, grouping all variants per gene
                        for (String gene : underlyingGenesForCurrentVariant)
                        {
                            //variants are only outputted for a certain gene if they are also thought to be relevant for that gene
                            for(Relevance rlv : rv.getRelevance())
                            {
                                if(rlv.getGene().equals(gene))
                                {
                                    LinkedHashMap<Integer, RelevantVariant> variants = variantBuffer.get(gene);
                                    if(variants == null) {
                                        variants = new LinkedHashMap<>();
                                    }
                                    variantBuffer.put(gene, variants);
                                    variants.put(pos, rv);
                                    if(verbose){System.out.println("[ConvertToGeneStream] Adding variant for matching relevant gene " + gene);}
                                    break;
                                }
                            }

                        }

                        // when we stop seeing an underlying gene, we process all variants for that gene
                        //when multiple genes end at once, we have to start multiple batches
                        resultBatches = new LinkedHashMap<>();
                        for (String gene : underlyingGenesForPreviousVariant)
                        {
                            // include null check, for variants that are annotated to a gene but were not ever relevant for that gene
                            if (!underlyingGenesForCurrentVariant.contains(gene) && variantBuffer.get(gene) != null)
                            {
                                if(verbose){System.out.println("[ConvertToGeneStream] Gene " + gene + " ended, creating result batch");}
                                HashMap<Integer, RelevantVariant> variants = variantBuffer.get(gene);
                                resultBatches.put(gene, variants.values().iterator());
                            }
                        }

                        // cycle genes seen
                        underlyingGenesForPreviousVariant.clear();
                        underlyingGenesForPreviousVariant.addAll(underlyingGenesForCurrentVariant);

                        // if batch succesfully prepared, start streaming it out
                        if(!resultBatches.isEmpty()) {
                            nextResult = getNextFromResultBatches(resultBatches, positionCheck);
                            if(verbose){System.out.println("[ConvertToGeneStream] Flushing first variant of result batch: " + nextResult.toStringShort());}
                            return true;
                        }
                        else
                        {
                            // if not, reset to null and continue the while loop
                            resultBatches = null;
                        }
                    }

                    // remaining variants that are leftover, i.e. not terminated yet by a gene ending
                    resultBatches = new LinkedHashMap<>();
                    for(String gene : variantBuffer.keySet())
                    {
                        HashMap<Integer, RelevantVariant> variants = variantBuffer.get(gene);
                        resultBatches.put(gene, variants.values().iterator());
                    }
                    nextResult = getNextFromResultBatches(resultBatches, positionCheck);
                    if(nextResult != null)
                    {
                        if(verbose){System.out.println("[ConvertToGeneStream] Flushing first of remaining variants: " + nextResult.toStringShort());}
                        return true;
                    }

                }
                return false;
            }

            @Override
            public RelevantVariant next() {
                return nextResult;
            }
        };
    }


    /**
     * Get next result item from a collection of potentially multiple iterators
     * We return every unique variant position only once, and reset these positions after all result batches are done
     * @param resultBatches
     * @return
     */
    private RelevantVariant getNextFromResultBatches(LinkedHashMap<String, Iterator<RelevantVariant>> resultBatches, List<Integer> positionsAlreadyReturned)
    {
        if(resultBatches == null)
        {
            return null;
        }

        for(String gene : resultBatches.keySet())
        {
            while(resultBatches.get(gene).hasNext())
            {
                RelevantVariant next = resultBatches.get(gene).next();
                if(!positionsAlreadyReturned.contains(next.getVariant().getPos()))
                {
                    if(verbose){System.out.println("[ConvertToGeneStream] Positions seen " + positionsAlreadyReturned + " does not contain " + next.getVariant().getPos() + ", so we output it");}
                    positionsAlreadyReturned.add(next.getVariant().getPos());
                    return next;
                }
            }
        }
        return null;
    }

}
