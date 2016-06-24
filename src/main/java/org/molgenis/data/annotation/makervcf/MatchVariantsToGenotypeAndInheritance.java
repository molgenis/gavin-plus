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

            //extra checks that things are okay
            if(gavinGene != null && clinvarGene != null && !gavinGene.equals(clinvarGene))
            {
                throw new Exception("Conflicting genes passed to MatchVariantsToGenotypeAndInheritance: " + gavinGene + " vs " + clinvarGene);
            }
            if(gavinGene == null && clinvarGene == null)
            {
                throw new Exception("No genes passed to MatchVariantsToGenotypeAndInheritance!");
            }

            String gene = gavinGene != null ? gavinGene : clinvarGene;


            CGDEntry ce = cgd.get(gene);
            generalizedInheritance inh = ce != null ? ce.getGeneralizedInheritance() : generalizedInheritance.NOTINCGD;
            HashMap<String, Entity> affectedSamples = findMatchingSamples(rv.getVariant(), rv.getAllele(), inh, true);
            HashMap<String, Entity> carrierSamples = findMatchingSamples(rv.getVariant(), rv.getAllele(), inh, false);

            rv.setCgdInfo(ce);

            String actingTerminology = "HOMOZYGOUS";
            String nonActingTerminology = "HETEROZYGOUS";

            // regular inheritance types, recessive and/or dominant or some type, we use affected/carrier because we know how the inheritance acts
            // ie. DOMINANT, RECESSIVE, XL_DOMINANT, XL_RECESSIVE, DOMINANT_OR_RECESSIVE
            if (cgd.containsKey(gene) && (cgd.get(gene).getGeneralizedInheritance().toString().contains("DOMINANT") || cgd.get(gene).getGeneralizedInheritance().toString().contains("RECESSIVE")))
            {
                actingTerminology = "AFFECTED";
                nonActingTerminology = "CARRIER";
            }
            else if (cgd.containsKey(gene) && (cgd.get(gene).getGeneralizedInheritance() == generalizedInheritance.BLOODGROUP))
            {
                actingTerminology = "BLOODGROUP";
                nonActingTerminology = "BLOODGROUP";
            }

            Map<String, String> sampleStatus = new HashMap<>();

            for(String key : affectedSamples.keySet())
            {
                sampleStatus.put(key, actingTerminology);
            }
            for(String key : carrierSamples.keySet())
            {
                sampleStatus.put(key, nonActingTerminology);
            }
            rv.setSampleStatus(sampleStatus);

//                if(affectedSamples.size() > 0)
//                {
//                    System.out.println(gene + " " + ce.getInheritance() + " " + ce.getGeneralizedInheritance() + " has " + affectedSamples.size() + " affected and " + carrierSamples.size() + " carriers, variant: " + rv.getVariant().toString());
//
//                }

            // System.out.println(gene + " in CGD but non-regular inheritance " + ce.getInheritance() + " " + ce.getGeneralizedInheritance() + " has " + affectedSamples.size() + " \"affected\" samples");
            //  System.out.println(gene + " (not in CGD) has " + unknownInheritanceSamples.size() + " \"affected\" samples");



        }
    }

    /**
     * TODO: compound heterozygous
     * TODO: trio filter
     * @param record
     * @param alt
     * @param inheritance
     * @param lookingForAffected
     * @return
     * @throws Exception
     */
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
            //blood group markers
            //TODO: find out how this works, and don't look for "pathogenic" variants but for "informative" ones
            else if(inheritance.equals(generalizedInheritance.BLOODGROUP))
            {
                if ( genotype.contains(altIndex+"") && lookingForAffected )
                {
                    matchingSamples.put(sampleName, sample);
                }
            }

            //other types (digenic, maternal, YL, etc)
            //report when 1 alle found
            else if(inheritance.equals(generalizedInheritance.OTHER) || inheritance.equals(generalizedInheritance.NOTINCGD))
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
