package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.*;

/**
 * Created by joeri on 6/29/16.
 */
public class AssignCompoundHeterozygous {

    private Iterator<RelevantVariant> relevantVariants;
    private ArrayList<Integer> positionalOrder;

    public AssignCompoundHeterozygous(Iterator<RelevantVariant> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
        this.positionalOrder = new ArrayList<>(); //TODO any way to reset this without nullpointering?
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
                    System.out.println("dumping result batch - returning next element!");
                    nextResult = resultBatch.next();
                    return true;
                }
                else if(resultBatch != null && genesToDelete != null)
                {
                    System.out.println("cleanup after dumping result batch - clearing data for " + genesToDelete.toString() + ", update genes seen from "+genesSeenForPreviousVariant.toString()+" to " + genesSeenForCurrentVariantUpdate);
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

                        // TODO: write out results in the correct order of position
                        System.out.println("getting variant on pos " + rv.getVariant().getChr() + ":" + pos);

                        // all genes to which the current variant may belong to
                        Set<String> genesSeenForCurrentVariant = rv.getVariant().getGenes();
                        System.out.println("genesSeenForCurrentVariant = " + genesSeenForCurrentVariant.toString());

                        // put gene and variant in a map, grouping all variants for certain gene
                        for (String gene : genesSeenForCurrentVariant)
                        {
                            List<RelevantVariant> newRvList = geneToVariantsToCheck.get(gene);
                            if(newRvList == null) {
                                newRvList = new ArrayList<>();
                                geneToVariantsToCheck.put(gene, newRvList);
                                System.out.println("added variant to " + gene);
                            }
                            newRvList.add(rv);
                        }



                        // the moment we 'stop seeing' a gene being annoted to a variant, we process all variants for this gene
                        // now this is tricky: ANGPTL7 is contained within MTOR. Variants are annoted as [MTOR] or [ANGPTL7, MTOR].
                        // however, variants relevent for only ANGPTL7 should not be evaluated for MTOR. However, we need to keep track of MTOR
                        // because it continues later on, so compound MTOR mutations could be found with ANGPTL7 inbetween.
                        List<RelevantVariant> returnThisShit = new ArrayList<>();
                        List<String> genesNotFound = new ArrayList<>();
                        for (String gene : genesSeenForPreviousVariant) {
                          System.out.println("checking if previously seen " + gene + " is still seen in current variant gene list" + genesSeenForCurrentVariant.toString());


                            if (!genesSeenForCurrentVariant.contains(gene)) {

                                System.out.println("no, so going to process " + geneToVariantsToCheck.get(gene).size() + " variants for gene " + gene);
                                List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(gene);
                                variantsToCheck = prefilterOnGene(variantsToCheck, gene);
                                returnThisShit.addAll(variantsToCheck);
                                genesNotFound.add(gene);

                                System.out.println("after prefilterOnGene: " + variantsToCheck.size() + " variants");
                            }
                            else
                            {
                                System.out.println("yes, so continuing");
                            }
                        }

                        if(genesNotFound.size() > 0)
                        {
                            //TODO call a function that checks comp het

                            //remove the gene+variants from the map

                            // geneToVariantsToCheck.remove(gene);
                            genesToDelete = genesNotFound;
                            System.out.println("marking to delete variants for gene: " + genesNotFound);


                            resultBatch = returnThisShit.iterator();


                            //clear the previously seen genes, and add the current ones

                            //genesSeenForPreviousVariant.clear();
                            // genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);

                            genesSeenForCurrentVariantUpdate = genesSeenForCurrentVariant;
                            System.out.println("marking genesSeenForCurrentVariantUpdate: " + genesSeenForCurrentVariantUpdate.toString());

                            System.out.println("we have a new result batch ready, with " + returnThisShit.size() + " elements");
                            // prepare the next result to be handed out by next()
                            if(resultBatch.hasNext()){
                                System.out.println("returning nextResult...");
                                nextResult = resultBatch.next();
                                return true;
                            }

                        }

                         System.out.println("not returned new batches, so clearning genesSeenForPreviousVariant for next variant and adding " + genesSeenForCurrentVariant.toString());
                        //clear the previously seen genes, and add the current ones
                        genesSeenForPreviousVariant.clear();
                        genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);
                        if(genesToDelete != null){
                            System.out.println("no results returned after filter, so also cleaning out genes to be deleted: " + genesToDelete.toString());
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


                System.out.println("OUTSIDE THE WHILE LOOP, so processing last remaining bits");
                if(remainingGenes == null)
                {
                    System.out.println("assigning last remainingGenes: " + geneToVariantsToCheck.keySet().toString());
                    remainingGenes = geneToVariantsToCheck.keySet().iterator();
                    genesToDelete = null;
                }

                if(remainingGenes.hasNext())
                {

                    String remainingGene = remainingGenes.next();
                    System.out.println("iterating on next remainingGene = " + remainingGene);
                    List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(remainingGene);
                    System.out.println("found = " + variantsToCheck.size() + " variantsToCheck");
                    variantsToCheck = prefilterOnGene(variantsToCheck, remainingGene);
                    System.out.println("after prefilterOnGene: " + variantsToCheck.size() + " remaining variants");

                    //TODO call a function that checks comp het


                    resultBatch = variantsToCheck.iterator();

                    System.out.println("we have " + variantsToCheck.size() + " remaining variants in this batch ready");
                    // prepare the next result to be handed out by next()
                    if(resultBatch.hasNext()){
                        System.out.println("returning nextResult of remaining data...");
                        nextResult = resultBatch.next();
                        return true;
                    }
                    else{
                        System.out.println("WARNING nothing in nextResult of remaining data ??");
                    }
                }


                System.out.println("all done!!");
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
        List<RelevantVariant> res = new ArrayList<RelevantVariant>();
        for(RelevantVariant rv : variantsToCheck)
        {
            if(rv.getGene().equals(gene))
            {
                res.add(rv);
            }
        }
        return res;
    }


}
