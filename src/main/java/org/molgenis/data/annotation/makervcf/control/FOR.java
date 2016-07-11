package org.molgenis.data.annotation.makervcf.control;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Trio;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by joeri on 6/29/16.
 *
 * False Omission Rate
 *
 * TODO: last gene!
 *
 */
public class FOR {

    private Iterator<RelevantVariant> relevantVariants;
    private VcfRepository vcf;

    public FOR(Iterator<RelevantVariant> relevantVariants, File vcfFile) throws IOException {
        this.relevantVariants = relevantVariants;
        this.vcf = new VcfRepository(vcfFile, "vcf");
    }

    public Iterator<RelevantVariant> go() throws Exception {

        //first, since we don't know the original, read the input VCF file that was processed before
        //and count how many pathogenic variants per gene should have been found
        //assumes that all variants in this list are (likely) pathogenic
        Iterator<Entity> vcfIterator = vcf.iterator();

        HashMap<String, String> variantToGene = new HashMap<String, String>(); //e.g. 10_126092389_G_A -> OAT, 10_126097170_C_T -> OAT
        while(vcfIterator.hasNext())
        {
            VcfEntity record = new VcfEntity(vcfIterator.next());

            String gene;
            if(record.getId() != null && record.getId().split(":", -1).length == 2)
            {
                gene = record.getId().split(":", -1)[0];
            }
            else {
                if(record.getGenes().size() > 1)
                {
                   throw  new Exception("more than 1 gene ("+record.getGenes().toString()+") for "+ record.toString());
                }
                gene = record.getGenes().toArray()[0].toString();
            }

            variantToGene.put(record.getChr()+"_"+record.getPos()+"_"+record.getRef()+"_"+record.getAlt(), gene);


        }


        HashMap<String, Integer> countPerGeneExpected = new HashMap<String, Integer>();
        for(String variant : variantToGene.keySet())
        {
            String gene = variantToGene.get(variant);
            if(countPerGeneExpected.containsKey(gene))
            {
                countPerGeneExpected.put(gene, countPerGeneExpected.get(gene) + 1);
            }
            else{
                countPerGeneExpected.put(gene, 1);
            }
        }

        System.out.println("gold standard patho variant counts per gene: " + countPerGeneExpected.toString());

        //now we iterate over the actual results and see how many we have missed per gene
        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;
            int relevantVariantsFound = 0;

            String previousGene = "n/a";
            String currentGene;

            CGDEntry previousEntry;
            CGDEntry currentEntry;

            @Override
            public boolean hasNext() {
                try {
                    while (relevantVariants.hasNext()) {
                        RelevantVariant rv = relevantVariants.next();

                        String key = rv.getVariant().getChr() + "_" + rv.getVariant().getPos() + "_" + rv.getVariant().getRef() + "_" + rv.getVariant().getAlt();

                        variantToGene.remove(key);
//
//                        currentGene = rv.getGene();
//                        currentEntry = rv.getCgdInfo();
//
//                        if(!currentGene.equals(previousGene))
//                        {
//                            if(previousGene.equals("n/a"))
//                            {
//                                System.out.println("Gene\tFOR\tDisorder");
//                            }else{
//                                if(countPerGene.containsKey(previousGene))
//                                {
//                                    System.out.println(previousGene + "\t" + (((1.0 - (double)relevantVariantsFound / (double)countPerGene.get(previousGene)) * 100.0)) + "\t" + (previousEntry != null ? previousEntry.getCondition() : "n/a"));
//                                }
//                                else
//                                {
//                                    System.out.println(previousGene + "\t" + "?????");
//                                }
//                                relevantVariantsFound = 0;
//                            }
//
//
//                        }
//
//                        relevantVariantsFound++;
//
//                        previousGene = currentGene;
//                        previousEntry = currentEntry;

                        nextResult = rv;
                        return true;
                    }


                    System.out.println("size after: " + variantToGene.size());
                    HashMap<String, Integer> countPerGeneLeftover = new HashMap<String, Integer>();

                    for(String variant : variantToGene.keySet())
                    {
                        String gene = variantToGene.get(variant);

                        if(countPerGeneLeftover.containsKey(gene))
                        {
                            countPerGeneLeftover.put(gene, countPerGeneLeftover.get(gene) + 1);
                        }
                        else{
                            countPerGeneLeftover.put(gene, 1);
                        }
                    }

                    System.out.println("left over counts per gene: " + countPerGeneLeftover.toString());


                    for(String gene : countPerGeneExpected.keySet())
                    {
                        //if no leftovers, subtract 0
                        int subtract = 0;
                        if(countPerGeneLeftover.containsKey(gene))
                        {
                          //  System.out.println("countPerGeneLeftover.containsKey(gene) " + gene);
                            subtract = countPerGeneLeftover.get(gene);
                        }
                        double exp = countPerGeneExpected.get(gene);
                        double obs = (countPerGeneExpected.get(gene)-subtract);
                        System.out.println(gene + "\t" + ((1.0-(obs/exp))*100.0));
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
