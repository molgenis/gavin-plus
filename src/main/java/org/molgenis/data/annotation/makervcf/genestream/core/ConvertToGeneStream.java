package org.molgenis.data.annotation.makervcf.genestream.core;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;

import java.util.*;

/**
 * Created by joeri on 6/29/16.
 *
 * We re-order the stream of variants so that genes are always grouped together
 *
 */
public class ConvertToGeneStream {

    private Iterator<GavinRecord> relevantVariants;
    private ArrayList<Integer> positionalOrder;
    private boolean verbose;

    public ConvertToGeneStream(Iterator<GavinRecord> relevantVariants, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.positionalOrder = new ArrayList<>();
        this.verbose = verbose;
    }

    public ArrayList<Integer> getPositionalOrder() {
        return positionalOrder;
    }

    public Iterator<GavinRecord> go()
    {

        return new Iterator<GavinRecord>(){

            // the next result as prepared by hasNext() and outputted by next()
            GavinRecord nextResult;

            // result iterators that become active once one or more genes end and output nextResult
            LinkedHashMap<String, Iterator<GavinRecord>> resultBatches;

            // variantBuffer with genes and variants that lags behind the input and gets turned into result batches
            HashMap<String, List<GavinRecord>> variantBuffer = new HashMap<>();

            // set of genes seen for variant in previous iteration
            Set<String> underlyingGenesForPreviousVariant = new HashSet<>();

            // within returning a batch, there may be duplicates (e.g. variants relevant for multiple genes)
            // we keep track of the positions and make sure they are only outputted once
            List<String> positionCheck = new ArrayList<>();


            @Override
            public boolean hasNext() {

                GavinRecord nextFromResultBatches = getNextFromResultBatches(resultBatches, positionCheck);
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
                                ArrayList<String> removeVariantsByPosition = new ArrayList<>(variantBuffer.get(gene).size());
                                for(GavinRecord rv : variantBuffer.get(gene))
                                {
                                    removeVariantsByPosition.add(rv.getChrPosRefAlt());
                                }
                                for(String geneInBuffer : variantBuffer.keySet())
                                {

                                    Iterator<GavinRecord> it = variantBuffer.get(geneInBuffer).iterator();
                                    while (it.hasNext()) {
                                        GavinRecord rlvToCheck = it.next();
                                        if(removeVariantsByPosition.contains(rlvToCheck.getChrPosRefAlt()))
                                        {
                                            it.remove();
                                        }
                                    }
                                }
                                variantBuffer.remove(gene);
                            }
                            positionCheck.clear();
                            resultBatches = null;
                        }

                        // get variant, store position, and get underlying genes
                        GavinRecord rv = relevantVariants.next();
                        int pos = rv.getPosition();
                        positionalOrder.add(pos);
                        Set<String> underlyingGenesForCurrentVariant = rv.getGenes();

                        if(verbose){System.out.println("[ConvertToGeneStream] Assessing next variant: " +rv.toStringShort() );}

                        // put genes and variants in a map, grouping all variants per gene
                        for (String gene : underlyingGenesForCurrentVariant)
                        {
                            //variants are only outputted for a certain gene if they are also thought to be relevant for that gene
                            for(Relevance rlv : rv.getRelevance())
                            {
                                if(rlv.getGene().equals(gene))
                                {
                                    List<GavinRecord> variants = variantBuffer.get(gene);
                                    if(variants == null) {
                                        variants = new ArrayList<>();
                                    }
                                    variantBuffer.put(gene, variants);
                                    variants.add(rv);
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
                            // added check: still variants left for this gene to be outputted
                            if (!underlyingGenesForCurrentVariant.contains(gene) && variantBuffer.get(gene) != null && variantBuffer.get(gene).size() > 0 )
                            {
                                if(verbose){System.out.println("[ConvertToGeneStream] Gene " + gene + " ended, creating result batch. Putting " + variantBuffer.get(gene).size() + " variants in output batch");}
                                List<GavinRecord> variants = variantBuffer.get(gene);
                                resultBatches.put(gene, variants.iterator());
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
                        if(variantBuffer.get(gene).size() > 0){
                            List<GavinRecord> variants = variantBuffer.get(gene);
                            resultBatches.put(gene, variants.iterator());
                        }
                    }
                    if(resultBatches.size() > 0) {
                        nextResult = getNextFromResultBatches(resultBatches, positionCheck);
                        if (nextResult != null) {
                            if (verbose) {
                                System.out.println("[ConvertToGeneStream] Flushing first of remaining variants: " + nextResult.toStringShort());
                            }
                            return true;
                        }
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


    /**
     * Get next result item from a collection of potentially multiple iterators
     * We return every unique variant position only once, and reset these positions after all result batches are done
     * @param resultBatches
     * @return
     */
    private GavinRecord getNextFromResultBatches(LinkedHashMap<String, Iterator<GavinRecord>> resultBatches, List<String> positionAltsAlreadyReturned) {
        if(resultBatches == null)
        {
            return null;
        }

        for(String gene : resultBatches.keySet())
        {
            while(resultBatches.get(gene).hasNext())
            {
                GavinRecord next = resultBatches.get(gene).next();
                if(!positionAltsAlreadyReturned.contains(next.getChrPosRefAlt()))
                {
                    if(verbose){System.out.println("[ConvertToGeneStream] Positions seen " + positionAltsAlreadyReturned + " does not contain " + next.getChrPosRefAlt() + ", so we output it");}
                    positionAltsAlreadyReturned.add(next.getChrPosRefAlt());
                    return next;
                }
            }
        }
        return null;
    }

}
