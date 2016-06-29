package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.*;

/**
 * Created by joeri on 6/29/16.
 */
public class AssignCompoundHeterozygous {

    private Iterator<RelevantVariant> relevantVariants;
    public AssignCompoundHeterozygous(Iterator<RelevantVariant> relevantVariants)
    {
        this.relevantVariants = relevantVariants;
    }

    public Iterator<RelevantVariant> go()
    {




        return new Iterator<RelevantVariant>(){

            ArrayList<Integer> positionalOrder = new ArrayList<>();

            HashMap<String, List<RelevantVariant>> geneToVariantsToCheck = new HashMap<String, List<RelevantVariant>>();
            Set<String> genesSeenForPreviousVariant = new HashSet<String>();

            RelevantVariant nextResult;
            Iterator<RelevantVariant> resultBatch;


            @Override
            public boolean hasNext() {

                if(resultBatch != null && resultBatch.hasNext())
                {
                    System.out.println("returning next element in the result batch");
                    nextResult = resultBatch.next();
                    return true;
                }

                while(relevantVariants.hasNext()) {
                    try {
                        RelevantVariant rv = relevantVariants.next();
                        positionalOrder.add(Integer.parseInt(rv.getVariant().getPos()));

                        // TODO: write out results in the correct order of position
                        System.out.println("getting variant");

                        // all genes to which the current variant may belong to
                        Set<String> genesSeenForCurrentVariant = rv.getVariant().getGenes();

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
                        for (String gene : genesSeenForPreviousVariant) {
                            System.out.println("checking if previously seen " + gene + " is still seen for variant " + genesSeenForCurrentVariant.toString());

                            //TODO what happens to the last variant??
                            if (!genesSeenForCurrentVariant.contains(gene)) {

                                System.out.println("going to comphet-check " + geneToVariantsToCheck.get(gene).size() + " variants for gene " + geneToVariantsToCheck.get(gene));
                                List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(gene);

                                //TODO call a function that checks comp het

                                //remove the variants from the map
                                geneToVariantsToCheck.remove(gene);

                                resultBatch = variantsToCheck.iterator();


                                System.out.println("clearing genesSeenForPreviousVariant before returning");
                                //clear the previously seen genes, and add the current ones
                                genesSeenForPreviousVariant.clear();
                                genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);

                                System.out.println("returning true, we have a new result batch ready, with " + variantsToCheck.size() + " elements");
                                // prepare the next result to be handed out by next()
                                nextResult = resultBatch.next();
                                return true;
                            }
                        }

                        System.out.println("clearing genesSeenForPreviousVariant");
                        //clear the previously seen genes, and add the current ones
                        genesSeenForPreviousVariant.clear();
                        genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
                return false;
            }

            @Override
            public RelevantVariant next() {
                System.out.println("returning nextResult");
                return nextResult;
            }
        };
    }
}
