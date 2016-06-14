package org.molgenis.data.annotation.makervcf;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.cgd.CGDEntry.generalizedInheritance;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    int minDepth;

    public MatchVariantsToGenotypeAndInheritance(List<RelevantVariant> relevantVariants, File cgdFile) throws IOException
    {
        this.relevantVariants = relevantVariants;
        this.cgd = LoadCGD.loadCGD(cgdFile);
        this.minDepth = 10;
    }


    public void go() throws Exception {
        for (RelevantVariant rv : relevantVariants)
        {
            boolean hit = false;

            String gavinGene = rv.getGavinJudgment().getGene() != null ? rv.getGavinJudgment().getGene() : null;
            String clinvarGene = rv.getClinvarJudgment().getGene() != null ? rv.getClinvarJudgment().getGene() : null;

            if(gavinGene != null && clinvarGene != null && !gavinGene.equals(clinvarGene))
            {
                System.out.println("WARNING conflicting genes: " + gavinGene + " vs " + clinvarGene);
            }
            if(gavinGene == null && clinvarGene == null)
            {
                throw new Exception("no genes!!");
            }

            String gene = gavinGene != null ? gavinGene : clinvarGene;

            if (cgd.containsKey(gene)) {
                hit = true;
                CGDEntry ce = cgd.get(gene);
                HashMap<String, Entity> affectedSamples = findMatchingSamples(rv.getVariant(), rv.getAllele(), ce.getGeneralizedInheritance(), true);
                HashMap<String, Entity> carrierSamples = findMatchingSamples(rv.getVariant(), rv.getAllele(), ce.getGeneralizedInheritance(), false);

                System.out.println(gene + " " + ce.getInheritance() + " " + ce.getGeneralizedInheritance() + " has " + affectedSamples.size() + " affected and " + carrierSamples.size() + " carriers");


            }

            if (!hit) {
                System.out.println("no hit.. genes: " + rv.getVariant().getGenes());
            }
        }
    }

    public HashMap<String, Entity> findMatchingSamples(VcfEntity record, String alt, generalizedInheritance inheritance, boolean lookingForAffected) throws Exception {
        int altIndex = VcfEntity.getAltAlleleIndex(record, alt);
        HashMap<String, Entity> matchingSamples = new HashMap<>();

        for (Entity sample : record.getSamples())
        {
            String genotype = sample.get("GT").toString();
            String sampleName = sample.get("ORIGINAL_NAME").toString();


            if (genotype.equals("./."))
            {
                continue;
            }

            if(sample.get("DP") == null)
            {
                continue;
            }

            // quality filter: we want depth X or more
            int depthOfCoverage = Integer.parseInt(sample.get("DP").toString());
            if (depthOfCoverage < minDepth)
            {
                continue;
            }

            //now that everything is okay, we can match to inheritance mode

            //all dominant types, so no carriers, and only requirement is that genotype contains 1 alt allele somewhere
            if(inheritance.equals(generalizedInheritance.DOMINANT_OR_RECESSIVE) || inheritance.equals(generalizedInheritance.DOMINANT) || inheritance.equals(generalizedInheritance.XL_DOMINANT))
            {
                if ( genotype.contains(altIndex+"") && lookingForAffected )
                {
                    matchingSamples.put(sampleName, sample);
                }
            }

            //all recessive types
            else if(inheritance.equals(generalizedInheritance.RECESSIVE) || inheritance.equals(generalizedInheritance.XL_RECESSIVE))
            {
                //first homozygous alternative
                if ( (genotype.equals(altIndex + "/" + altIndex) || genotype.equals(altIndex + "|" + altIndex)) && lookingForAffected )
                {
                    matchingSamples.put(sampleName, sample);
                }
                // not-affected, ie carriers
                else if ( genotype.contains(altIndex+"") && !lookingForAffected )
                {
                    matchingSamples.put(sampleName, sample);
                }
            }

            //other types (digenic, maternal, YL, bloodgroup etc)
            //report when 1 alle found
            else if(inheritance.equals(generalizedInheritance.OTHER))
            {
                if ( genotype.contains(altIndex+"") && lookingForAffected )
                {
                    matchingSamples.put(sampleName, sample);
                }
            }

            else
            {
                throw new Exception("inheritance unknown: " + inheritance);
            }





        }
        return matchingSamples;
    }
}
