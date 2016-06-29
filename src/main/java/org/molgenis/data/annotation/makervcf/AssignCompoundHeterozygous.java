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


            @Override
            public boolean hasNext() {

                if(resultBatch != null && resultBatch.hasNext())
                {
    //                System.out.println("returning next element in the result batch");
                    nextResult = resultBatch.next();
                    return true;
                }

                //nothing buffered in the resultBatch, build new batch
                while(relevantVariants.hasNext()) {
                    try {
                        RelevantVariant rv = relevantVariants.next();
                        positionalOrder.add(Integer.parseInt(rv.getVariant().getPos()));

                        // TODO: write out results in the correct order of position
        //                System.out.println("getting variant");

                        // all genes to which the current variant may belong to
                        Set<String> genesSeenForCurrentVariant = rv.getVariant().getGenes();

                        // put gene and variant in a map, grouping all variants for certain gene
                        for (String gene : genesSeenForCurrentVariant)
                        {
                            List<RelevantVariant> newRvList = geneToVariantsToCheck.get(gene);
                            if(newRvList == null) {
                                newRvList = new ArrayList<>();
                                geneToVariantsToCheck.put(gene, newRvList);
  //                              System.out.println("added variant to " + gene);
                            }
                            newRvList.add(rv);
                        }



                        // the moment we 'stop seeing' a gene being annoted to a variant, we process all variants for this gene
                        // now this is tricky: ANGPTL7 is contained within MTOR. Variants are annoted as [MTOR] or [ANGPTL7, MTOR].
                        // however, variants relevent for only ANGPTL7 should not be evaluated for MTOR. However, we need to keep track of MTOR
                        // because it continues later on, so compound MTOR mutations could be found with ANGPTL7 inbetween.
                        for (String gene : genesSeenForPreviousVariant) {
   //                         System.out.println("checking if previously seen " + gene + " is still seen for variant " + genesSeenForCurrentVariant.toString());


                            if (!genesSeenForCurrentVariant.contains(gene)) {

    //                            System.out.println("going to comphet-check " + geneToVariantsToCheck.get(gene).size() + " variants for gene " + geneToVariantsToCheck.get(gene));
                                List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(gene);
                                variantsToCheck = prefilterOnGene(variantsToCheck, gene);

                                //TODO call a function that checks comp het

                                //remove the gene+variants from the map
                                geneToVariantsToCheck.remove(gene);



                                resultBatch = variantsToCheck.iterator();


        //                        System.out.println("clearing genesSeenForPreviousVariant before returning");
                                //clear the previously seen genes, and add the current ones
                                genesSeenForPreviousVariant.clear();
                                genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);

                       //         System.out.println("returning true, we have a new result batch ready, with " + variantsToCheck.size() + " elements");
                                // prepare the next result to be handed out by next()
                                nextResult = resultBatch.next();
                                return true;
                            }
                        }

       //                 System.out.println("clearing genesSeenForPreviousVariant");
                        //clear the previously seen genes, and add the current ones
                        genesSeenForPreviousVariant.clear();
                        genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }

                //exited the while loop, must handle the remaining data
                //could technically take multiple iteratations depending how many genes are still present in the map
                if(geneToVariantsToCheck.size() > 0){

  //                  System.out.println("remaining data: " + geneToVariantsToCheck.toString());
                    for(String gene : geneToVariantsToCheck.keySet())
                    {
      //                  System.out.println("going to LAST comphet-check " + geneToVariantsToCheck.get(gene).size() + " variants for gene " + gene);
                        List<RelevantVariant> variantsToCheck = geneToVariantsToCheck.get(gene);
                        variantsToCheck = prefilterOnGene(variantsToCheck, gene);

                        //TODO call a function that checks comp het

                        //remove the gene+variants from the map
                        geneToVariantsToCheck.remove(gene);


                        resultBatch = variantsToCheck.iterator();
  //                      System.out.println("returning true, we have a new LAST result batch ready, with " + variantsToCheck.size() + " elements");
                        // prepare the next result to be handed out by next()
                        nextResult = resultBatch.next();
                        return true;
                    }

                }

     //           System.out.println("done!!");
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
