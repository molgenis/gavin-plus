package org.molgenis.data.annotation.makervcf.control;

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
        HashMap<String, Integer> countPerGene = new HashMap<String, Integer>();
        while(vcfIterator.hasNext())
        {
            VcfEntity record = new VcfEntity(vcfIterator.next());

            String gene = null;

            //e.g. BRCA2:c.5603_5606delACAG
            if(record.getId() != null && record.getId().split(":", -1).length == 2)
            {
                gene = record.getId().split(":", -1)[0];
            }
            else {
                gene = record.getGenes().toArray()[0].toString();
                if (record.getGenes().size() > 1)
                {
                    System.out.println("WARNING: multiple genes: " + record.getGenes().toString() + ", using: " + gene);
                }
            }

            //count per gene
            if(countPerGene.containsKey(gene))
            {
                countPerGene.put(gene, countPerGene.get(gene) + 1);
            }
            else{
                countPerGene.put(gene, 1);
            }

        }
        System.out.println("gold standard counts per gene: " + countPerGene.toString());

        //now we iterate over the actual results and see how many we have missed per gene
        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;
            int relevantVariantsFound = 0;

            String previousGene = "n/a";
            String currentGene;

            @Override
            public boolean hasNext() {
                try {
                    while (relevantVariants.hasNext()) {
                        RelevantVariant rv = relevantVariants.next();

                        boolean clinVarPatho = rv.getClinvarJudgment().getClassification().equals(Judgment.Classification.Pathogenic);
                        boolean gavinPatho = rv.getGavinJudgment() != null ? rv.getGavinJudgment().getClassification().equals(Judgment.Classification.Pathogenic) : false;

                        if(!clinVarPatho && !gavinPatho)
                        {
                            continue;
                        }

                        currentGene = rv.getGene();
                        System.out.println("current gene : " + currentGene);

                        if(!currentGene.equals(previousGene))
                        {
                            if(countPerGene.containsKey(previousGene))
                            {
                                System.out.println(previousGene + "\t" + (((1.0 - (double)relevantVariantsFound / (double)countPerGene.get(previousGene)) * 100.0)));
                            }
                            else
                            {
                                System.out.println(previousGene + "\t" + "??");
                            }
                            relevantVariantsFound = 0;
                        }

                        relevantVariantsFound++;

                        previousGene = currentGene;

                        nextResult = rv;
                        return true;
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
