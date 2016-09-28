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
        this.positionalOrder = new ArrayList<>(); //TODO any way to reset this without nullpointering?
        this.verbose = verbose;
    }

    public ArrayList<Integer> getPositionalOrder() {
        return positionalOrder;
    }

    public Iterator<RelevantVariant> go()
    {

        return new Iterator<RelevantVariant>(){

            HashMap<String, List<RelevantVariant>> geneToVariantsToCheck = new HashMap<String, List<RelevantVariant>>();
            Set<String> genesSeenForPreviousVariant = new HashSet<String>();

            RelevantVariant nextResult;
            Iterator<RelevantVariant> resultBatch;

            Iterator<String> remainingGenes;
            List<String> genesToDelete;
            Set<String> genesSeenForCurrentVariantUpdate;


            @Override
            public boolean hasNext() {

                if(resultBatch != null && resultBatch.hasNext())
                {
                    if(verbose) { System.out.println("[ConvertToGeneStream] dumping result batch - returning next element!"); }
                    nextResult = resultBatch.next();
                    return true;
                }
                else if(resultBatch != null && genesToDelete != null)
                {
                    if(verbose){System.out.println("[ConvertToGeneStream] Cleanup after streaming result batch, clearing data for " + genesToDelete.toString() + ", update genes seen from "+genesSeenForPreviousVariant.toString()+" to " + genesSeenForCurrentVariantUpdate);}
                    //result batch emptied, clear data and continue while
                    for(String geneToDelete : genesToDelete)
                    {
                        geneToVariantsToCheck.remove(geneToDelete);
                    }
                    genesToDelete = null;
                    genesSeenForPreviousVariant.clear();
                    genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariantUpdate);
                    genesSeenForCurrentVariantUpdate = null;
                }

                //nothing buffered in the resultBatch, build new batch
                while(relevantVariants.hasNext()) {
                    try {
                        RelevantVariant rv = relevantVariants.next();
                        int pos = Integer.parseInt(rv.getVariant().getPos());
                        positionalOrder.add(Integer.parseInt(rv.getVariant().getPos()));

                        if(verbose) { System.out.println("[ConvertToGeneStream] getting variant on pos " + rv.getVariant().getChr() + ":" + pos); }

                        // all genes to which the current variant may belong to
                        Set<String> genesSeenForCurrentVariant = rv.getVariant().getGenes();
                            if(verbose) { System.out.println("[ConvertToGeneStream] genesSeenForCurrentVariant = " + genesSeenForCurrentVariant.toString()); }

                        // put gene and variant in a map, grouping all variants for certain gene
                        for (String gene : genesSeenForCurrentVariant)
                        {
                            List<RelevantVariant> newRvList = geneToVariantsToCheck.get(gene);
                            if(newRvList == null) {
                                newRvList = new ArrayList<>();
                                geneToVariantsToCheck.put(gene, newRvList);
                                if(verbose) { System.out.println("[ConvertToGeneStream] added variant to " + gene); }
                            }
                            newRvList.add(rv);
                        }



                        // the moment we 'stop seeing' a gene being annoted to a variant, we process all variants for this gene
                        // now this is tricky: ANGPTL7 is contained within MTOR. Variants are annoted as [MTOR] or [ANGPTL7, MTOR].
                        // however, variants relevent for only ANGPTL7 should not be evaluated for MTOR. However, we need to keep track of MTOR
                        // because it continues later on, so compound MTOR mutations could be found with ANGPTL7 inbetween.
                        List<RelevantVariant> returnTheseVariants = new ArrayList<>();
                        List<String> genesNotFound = new ArrayList<>();
                        for (String gene : genesSeenForPreviousVariant) {
                            if(verbose) { System.out.println("[ConvertToGeneStream] checking if previously seen " + gene + " is still seen in current variant gene list" + genesSeenForCurrentVariant.toString()); }


                            if (!genesSeenForCurrentVariant.contains(gene)) {

                                if(verbose) { System.out.println("[ConvertToGeneStream] Found " + geneToVariantsToCheck.get(gene).size() + " variants for gene " + gene + ", streaming them to next iterator"); }
                                List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(gene);
                                variantsToCheck = prefilterOnGene(variantsToCheck, gene);

                            //    compoundHetCheck(variantsToCheck); not anymore here!

                                returnTheseVariants.addAll(variantsToCheck);
                                genesNotFound.add(gene);

                                if(verbose) { System.out.println("[ConvertToGeneStream] after prefilterOnGene: " + variantsToCheck.size() + " variants"); }
                            }
                            else
                            {
                                if(verbose) { System.out.println("[ConvertToGeneStream] yes, so continuing"); }
                            }
                        }

                        if(genesNotFound.size() > 0)
                        {


                            //remove the gene+variants from the map

                            // geneToVariantsToCheck.remove(gene);
                            genesToDelete = genesNotFound;
                            if(verbose) { System.out.println("[ConvertToGeneStream] marking to delete variants for gene: " + genesNotFound); }


                            resultBatch = returnTheseVariants.iterator();


                            //clear the previously seen genes, and add the current ones

                            //genesSeenForPreviousVariant.clear();
                            // genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);

                            genesSeenForCurrentVariantUpdate = genesSeenForCurrentVariant;
                            if(verbose) { System.out.println("[ConvertToGeneStream] marking genesSeenForCurrentVariantUpdate: " + genesSeenForCurrentVariantUpdate.toString()); }

                            if(verbose) { System.out.println("[ConvertToGeneStream] we have a new result batch ready, with " + returnTheseVariants.size() + " elements"); }
                            // prepare the next result to be handed out by next()
                            if(resultBatch.hasNext()){
                                if(verbose) { System.out.println("[ConvertToGeneStream] returning nextResult..."); }
                                nextResult = resultBatch.next();
                                return true;
                            }

                        }

                        if(verbose) { System.out.println("[ConvertToGeneStream] not returned new batches, so clearning genesSeenForPreviousVariant for next variant and adding " + genesSeenForCurrentVariant.toString()); }
                        //clear the previously seen genes, and add the current ones
                        genesSeenForPreviousVariant.clear();
                        genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);
                        if(genesToDelete != null){
                            if(verbose) { System.out.println("[ConvertToGeneStream] no results returned after filter, so also cleaning out genes to be deleted: " + genesToDelete.toString()); }
                            for(String geneToDelete : genesToDelete)
                            {
                                geneToVariantsToCheck.remove(geneToDelete);
                            }
                            genesToDelete = null;
                        }

                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }


                if(verbose) { System.out.println("[ConvertToGeneStream] OUTSIDE THE WHILE LOOP, so processing last remaining bits");}
                if(remainingGenes == null)
                {
                    if(verbose) { System.out.println("[ConvertToGeneStream] assigning last remainingGenes: " + geneToVariantsToCheck.keySet().toString());}
                    remainingGenes = geneToVariantsToCheck.keySet().iterator();
                    genesToDelete = null;
                }

                if(remainingGenes.hasNext())
                {

                    String remainingGene = remainingGenes.next();
                    if(verbose) { System.out.println("[ConvertToGeneStream] iterating on next remainingGene = " + remainingGene);}
                    List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(remainingGene);
                    if(verbose) { System.out.println("[ConvertToGeneStream] found = " + variantsToCheck.size() + " variantsToCheck");}
                    variantsToCheck = prefilterOnGene(variantsToCheck, remainingGene);
                    if(verbose) { System.out.println("[ConvertToGeneStream] after prefilterOnGene: " + variantsToCheck.size() + " remaining variants");}

             //       compoundHetCheck(variantsToCheck); not anymore here!
                    if(verbose) { System.out.println("[ConvertToGeneStream] Found " + variantsToCheck.size() + " variants for trailing gene " + remainingGene + ", streaming them to next iterator"); }

                    resultBatch = variantsToCheck.iterator();

                    if(verbose) { System.out.println("[ConvertToGeneStream] we have " + variantsToCheck.size() + " remaining variants in this batch ready");}
                    // prepare the next result to be handed out by next()
                    if(resultBatch.hasNext()){
                        if(verbose) { System.out.println("[ConvertToGeneStream] returning nextResult of remaining data...");}
                        nextResult = resultBatch.next();
                        return true;
                    }
                    else{
                        if(verbose) { System.out.println("[ConvertToGeneStream] WARNING nothing in nextResult of remaining data ??");}
                    }
                }


                if(verbose) { System.out.println("[ConvertToGeneStream] all done!!");}
                return false;



            }

            @Override
            public RelevantVariant next() {
                //            System.out.println("returning nextResult");
                return nextResult;
            }
        };
    }

    private List<RelevantVariant> prefilterOnGene(List<RelevantVariant> variantsToCheck, String gene) {
        if(verbose) { System.out.println("[ConvertToGeneStream] inside prefilterOnGene, filtering on: " + gene); }

        List<RelevantVariant> res = new ArrayList<RelevantVariant>();
        for(RelevantVariant rv : variantsToCheck)
        {
            for(Relevance rlv : rv.getRelevance())
            {
                if(rlv.getGene().equals(gene))
                {
                    if(verbose) { System.out.println("[ConvertToGeneStream] inside prefilterOnGene, adding: " + rv.toStringShort()); }
                    res.add(rv);
                }
                else{
                    if(verbose) { System.out.println("[ConvertToGeneStream] inside prefilterOnGene, not adding: " + rv.toStringShort()); }
                }
            }

        }
        return res;
    }



}
