package org.molgenis.data.annotation.makervcf;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joeri on 6/1/16.
 *
 * Take the output of DiscoverRelevantVariants, re-iterate over the original VCF file, but this time check the genotypes.
 * We want to match genotypes to disease inheritance mode, ie. dominant/recessive acting.
 *
 */
public class MatchVariantsToGenotypeAndInheritance {

    List<RelevantVariant> relevantVariants;
    Map<String, CGDEntry> cgd;

    public MatchVariantsToGenotypeAndInheritance(List<RelevantVariant> relevantVariants, File cgdFile) throws IOException
    {
        this.relevantVariants = relevantVariants;
        this.cgd = LoadCGD.loadCGD(cgdFile);



    }


    public void go()
    {
        for (RelevantVariant rv : relevantVariants) {
            boolean hit = false;
            for (String gene : rv.getVariant().getGenes()) {
                if (cgd.containsKey(gene)) {
                    hit = true;
                    CGDEntry ce = cgd.get(gene);
                    System.out.println(gene + " " + ce.getInheritance() + " " + ce.getGeneralizedInheritance());
                }
            }
            if (!hit) {
                System.out.println("no hit.. genes: " + rv.getVariant().getGenes());
            }
        }
    }

    public HashMap<String, Entity> findInterestingSamples(VcfEntity record, String gene, int altIndex, int exacHets, int exacHoms)
    {

        for (Entity sample : record.getSamples()) {
            String genotype = sample.get("GT").toString();
            String sampleName = sample.get("ORIGINAL_NAME").toString();


            if (genotype.equals("./."))
            {
                continue;
            }


        }
        return null;
    }
}
