package org.molgenis.data.annotation.makervcf.positionalstream;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.GenoMatchSamples;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.cgd.CGDEntry.generalizedInheritance;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by joeri on 6/1/16.
 *
 * Take the output of DiscoverRelevantVariants, re-iterate over the original VCF file, but this time check the genotypes.
 * We want to match genotypes to disease inheritance mode, ie. dominant/recessive acting.
 *
 */
public class MatchVariantsToGenotypeAndInheritance {

    Iterator<RelevantVariant> relevantVariants;
    Map<String, CGDEntry> cgd;
    int minDepth;
    boolean verbose;
    private Set<String> parents;

    public enum status{
        HETEROZYGOUS, HOMOZYGOUS, AFFECTED, CARRIER, BLOODGROUP, HOMOZYGOUS_COMPOUNDHET, AFFECTED_COMPOUNDHET, HETEROZYGOUS_MULTIHIT;

        public static boolean isCompound(status status){
            return (status == HOMOZYGOUS_COMPOUNDHET || status == AFFECTED_COMPOUNDHET) ? true : false;}

        public static boolean isPresumedCarrier(status status){
            return (status == HETEROZYGOUS || status == HETEROZYGOUS_MULTIHIT || status == CARRIER) ? true : false;}

        public static boolean isPresumedAffected(status status){
            return (status == HOMOZYGOUS || status == HOMOZYGOUS_COMPOUNDHET || status == AFFECTED || status == AFFECTED_COMPOUNDHET) ? true : false;}

        public static boolean isHomozygous(String genotype) throws Exception {
            if (genotype.length() == 1) { return true;}
            if (genotype.length() != 3) { throw new Exception("Genotype '" + genotype + "' not length 3");}
            if (genotype.charAt(0) == genotype.charAt(2)) { return true;}
            return false;
        }
    }

    public MatchVariantsToGenotypeAndInheritance(Iterator<RelevantVariant> relevantVariants, File cgdFile, Set<String> parents, boolean verbose) throws IOException
    {
        this.relevantVariants = relevantVariants;
        this.cgd = LoadCGD.loadCGD(cgdFile);
        this.minDepth = 1;
        this.verbose = verbose;
        this.parents = parents;
    }


    public Iterator<RelevantVariant > go() throws Exception {

        return new Iterator<RelevantVariant>(){


            @Override
            public boolean hasNext() {
                return relevantVariants.hasNext();
            }

            @Override
            public RelevantVariant next() {


                try {
                    RelevantVariant rv = relevantVariants.next();

                    //key: gene, alt allele
                    MultiKeyMap fullGenoMatch = findMatchingSamples(rv);


                    for(Relevance rlv : rv.getRelevance()) {

                        String gene = rlv.getGene();

                        CGDEntry ce = cgd.get(gene);
                        rlv.setCgdInfo(ce);

                        status actingTerminology = status.HOMOZYGOUS;
                        status nonActingTerminology = status.HETEROZYGOUS;

                        // regular inheritance types, recessive and/or dominant or some type, we use affected/carrier because we know how the inheritance acts
                        // females can be X-linked carriers, though since X is inactivated, they might be (partly) affected
                        if (cgd.containsKey(gene) && (generalizedInheritance.hasKnownInheritance(cgd.get(gene).getGeneralizedInheritance()))) {
                            actingTerminology = status.AFFECTED;
                            nonActingTerminology = status.CARRIER;
                        }
                        //TODO: handle blood group marker information? not pathogenic but still valuable?
//                    else if (cgd.containsKey(gene) && (cgd.get(gene).getGeneralizedInheritance() == generalizedInheritance.BLOODGROUP))
//                    {
//                        actingTerminology = status.BLOODGROUP;
//                        nonActingTerminology = status.BLOODGROUP;
//                    }

                        Map<String, status> sampleStatus = new HashMap<>();
                        Map<String, String> sampleGenotypes = new HashMap<>();
                        GenoMatchSamples genoMatch = (GenoMatchSamples)fullGenoMatch.get(rlv.getGene(), rlv.getAllele());

                        if(genoMatch != null){
                            for (String key : genoMatch.affected.keySet()) {
                                sampleStatus.put(key, actingTerminology);
                                sampleGenotypes.put(key, genoMatch.affected.get(key).get("GT").toString());
                            }
                            for (String key : genoMatch.carriers.keySet()) {
                                sampleStatus.put(key, nonActingTerminology);
                                sampleGenotypes.put(key, genoMatch.carriers.get(key).get("GT").toString());
                            }
                        }


                        if (!sampleStatus.isEmpty()) {
                            rlv.setSampleStatus(sampleStatus);
                            rlv.setSampleGenotypes(sampleGenotypes);
                            rlv.setParentsWithReferenceCalls(genoMatch.parentsWithReferenceCalls);
                            if (verbose) {
                                System.out.println("[MatchVariantsToGenotypeAndInheritance] Assigned sample status: " + sampleStatus.toString() + ", having genotypes: " + sampleGenotypes + ", plus trio parents with reference alleles: " + genoMatch.parentsWithReferenceCalls.toString());
                            }
                        }
                    }
                    return rv;
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }

            }
        };
    }

    /**
     *
     */
    public MultiKeyMap findMatchingSamples(RelevantVariant rv) throws Exception {

        VcfEntity record = rv.getVariant();
        Set<String> alts = rv.getRelevantAlts();
        Set<String> genes = rv.getRelevantGenes();

        MultiKeyMap res = new MultiKeyMap();

        Set<String> parentsWithReferenceCalls = new HashSet<String>();

        Iterator<Entity> samples = record.getSamples();

        while(samples.hasNext())
        {
            Entity sample = samples.next();
            if(sample.get("GT") == null)
            {
                continue;
            }

            String genotype = sample.get("GT").toString();
            String sampleName = sample.get("ORIGINAL_NAME").toString();

            // quality filter: we want depth X or more, if available
            if(sample.get("DP") != null)
            {
                int depthOfCoverage = Integer.parseInt(sample.get("DP").toString());
                if (depthOfCoverage < minDepth)
                {
                    continue;
                }
            }

            // skip empty genotypes
            if ( genotype.equals("./.") || genotype.equals(".|.") || genotype.equals(".") )
            {
                continue;
            }

            // skip reference genotypes unless parents of a child for de novo detection
            if ( genotype.equals("0/0") || genotype.equals("0|0")  || genotype.equals("0") )
            {
                if(parents.contains(sampleName))
                {
                    parentsWithReferenceCalls.add(sampleName);
                }
                continue;
            }

            //now that everything is okay, we can match to inheritance mode for each alt
            for(String alt : alts)
            {
                int altIndex = VcfEntity.getAltAlleleIndex(record, alt);

                //and each gene
                for (String gene : genes)
                {
                    HashMap<String, Entity> carriers = new HashMap<String, Entity>();
                    HashMap<String, Entity> affected = new HashMap<String, Entity>();

                    CGDEntry ce = cgd.get(gene);
                    generalizedInheritance inheritance = ce != null ? ce.getGeneralizedInheritance() : generalizedInheritance.NOTINCGD;

                    //all dominant types, so no carriers, and only requirement is that genotype contains 1 alt allele somewhere
                    if (inheritance.equals(generalizedInheritance.DOMINANT_OR_RECESSIVE) || inheritance.equals(generalizedInheritance.DOMINANT)) {
                        // 1 or more, so works for hemizygous too
                        if (genotype.contains(altIndex + "")) {
                            affected.put(sampleName, sample);
                        }
                    }

                    //all other types, unknown, complex or recessive
                    //for recessive we know if its acting or not, but this is handled in the terminology of a homozygous hit being labeled as 'AFFECTED'
                    //for other (digenic, maternal, YL, etc) and not-in-CGD we don't know, but we still report homozygous as 'acting' and heterozygous as 'carrier' to make that distinction
                    else if (inheritance.equals(generalizedInheritance.RECESSIVE) || inheritance.equals(generalizedInheritance.XL_LINKED)
                            || inheritance.equals(generalizedInheritance.OTHER) || inheritance.equals(generalizedInheritance.NOTINCGD)
                            || inheritance.equals(generalizedInheritance.BLOODGROUP)) {
                        boolean homozygous = genotype.equals(altIndex + "/" + altIndex) || genotype.equals(altIndex + "|" + altIndex);
                        boolean hemizygous = genotype.equals(altIndex + "");
                        boolean heterozygous = genotype.length() == 3 && StringUtils.countMatches(genotype, altIndex + "") == 1;

                        // regular homozygous
                        if (homozygous) {
                            affected.put(sampleName, sample);
                        }
                        //for hemizygous, 1 allele is enough of course
                        else if (hemizygous) {
                            affected.put(sampleName, sample);
                        }
                        // heterozygous, ie. carriers when disease is recessive
                        else if (heterozygous) {
                            carriers.put(sampleName, sample);
                        }

                    } else {
                        throw new Exception("inheritance unknown: " + inheritance);
                    }

                    //FIXME: set directly above with put instead of via putAll afterwards?
                    if(res.containsKey(gene, alt))
                    {
                        GenoMatchSamples match = (GenoMatchSamples)res.get(gene, alt);
                        match.carriers.putAll(carriers);
                        match.affected.putAll(affected);
                    }
                    else
                    {
                        GenoMatchSamples match = new GenoMatchSamples(carriers, affected);
                        res.put(gene, alt, match);
                    }
                }
            }
        }

        //for relevant combinations, set parents with reference calls (--> this is not related to alternative alleles or gene combinations)
        // FIXME: also this can be set directly, earlier?
        for(String alt : alts) {
            for (String gene : genes) {
                if(res.get(gene, alt) != null)
                {
                    ((GenoMatchSamples)res.get(gene, alt)).setParentsWithReferenceCalls(parentsWithReferenceCalls);
                }
            }
        }

        return res;
    }

}
