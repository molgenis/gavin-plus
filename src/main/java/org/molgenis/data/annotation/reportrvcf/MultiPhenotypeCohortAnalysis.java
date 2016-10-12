package org.molgenis.data.annotation.reportrvcf;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.broadinstitute.variant.vcf.VCFUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by joeri on 10/12/16.
 */
public class MultidiseaseCohortAnalysis {

    File rvcfInputFile;
    File outputZscoreFile;
//    private VcfRepository vcf;
//    private PrintWriter pw;



    public static void main(String[] args) throws Exception {
        MultiPhenotypeCohortAnalysis mdca = new MultiPhenotypeCohortAnalysis(new File(args[0]), new File(args[1]));
        mdca.start();
    }

    public MultidiseaseCohortAnalysis(File rvcfInputFile, File outputZscoreFile)
    {
        this.rvcfInputFile = rvcfInputFile;
        this.outputZscoreFile = outputZscoreFile;
    }

    public void start() throws Exception {


        // meta data: number of individuals seen with this phenotype as denoted by PHENOTYPE in VCF: ##SAMPLE=<ID=P583292,SEX=UNKNOWN,PHENOTYPE=IRONACC>
        HashMap<String, String> individualsToPhenotype = new HashMap<>();

        // meta data: number of individuals seen with this phenotype as denoted by PHENOTYPE in VCF: ##SAMPLE=<ID=P583292,SEX=UNKNOWN,PHENOTYPE=IRONACC>
        HashMap<String, Integer> phenotypeToNrOfIndividuals = new HashMap<>();

        // meta data: first encounter of this gene, chromosome and bp position
        HashMap<String, String> geneToChromPos = new HashMap<>();

        // while iterating over rvcf, count affected individuals per gene for each phenotype
        MultiKeyMap geneAndPhenotypeToAffectedCount = new MultiKeyMap();

        // when done, we can get the fractions based on total counts and phenotypeToNrOfIndividuals
        MultiKeyMap geneAndPhenotypeToAffectedFraction = new MultiKeyMap();

        // transform fractions into Z-scores by comparing 1 phenotype group to the others
        MultiKeyMap geneAndPhenotypeToAffectedZscores = new MultiKeyMap();

        // execution
        phenotypeToNrOfIndividualsFromInputVcf(individualsToPhenotype, diseaseToNrOfIndividuals, this.rvcfInputFile);
        System.out.println("individualsToPhenotype: " + individualsToPhenotype.toString());
        System.out.println("diseaseToNrOfIndividuals: " + diseaseToNrOfIndividuals.toString());

        countAffectedPerGene(geneToChromPos, individualsToPhenotype, geneAndPhenotypeToAffectedCount, this.rvcfInputFile);
        //System.out.println("countAffectedPerGene: " + geneAndPhenotypeToAffectedCount.toString());

        convertToFraction(diseaseToNrOfIndividuals, geneAndPhenotypeToAffectedCount, geneAndPhenotypeToAffectedFraction);

        convertToZscore(geneAndPhenotypeToAffectedFraction, geneDiseaseToAffectedZscores, diseaseToNrOfIndividuals.keySet());

        printToOutput(geneToChromPos, geneDiseaseToAffectedZscores, this.outputZscoreFile);
    }



    public void phenotypeToNrOfIndividualsFromInputVcf( HashMap<String, String> individualsToPhenotype, HashMap<String, Integer> phenotypeToNrOfIndividuals, File rvcfInputFile) throws Exception
    {
        individualsToPhenotype.putAll(VcfUtils.getSampleToPhenotype(rvcfInputFile));
        HashMap<String, List<String>> phenotypeToSampleIDs = VcfUtils.getPhenotypeToSampleIDs(individualsToPhenotype);
        for(String phenotype : phenotypeToSampleIDs.keySet())
        {
            phenotypeToNrOfIndividuals.put(phenotype, phenotypeToSampleIDs.get(phenotype).size());
        }
    }


    public void countAffectedPerGene(HashMap<String, String> geneToChromPos, HashMap<String, String> individualsToPhenotype, MultiKeyMap geneAndPhenotypeToAffectedCount, File rvcfInputFile) throws Exception {
        VcfRepository vcf = new VcfRepository(rvcfInputFile, "vcf");
        Iterator<Entity> vcfIterator = vcf.iterator();

        // "SampleID_GeneName"
        Set<String> sampleAddedForGene = new HashSet<>();

        while(vcfIterator.hasNext()) {

            VcfEntity record = new VcfEntity(vcfIterator.next());

            for (RVCF rvcf : record.getRvcf())
            {
                String gene = rvcf.getGene();

                for(String sample : rvcf.getSampleStatus().keySet()) {

                    if(sampleAddedForGene.contains(sample+"_"+gene))
                    {
                        continue;
                    }

                    if (MatchVariantsToGenotypeAndInheritance.status.isPresumedAffected(rvcf.getSampleStatus().get(sample)))
                    {

                        String phenotype = individualsToPhenotype.get(sample);
                        Integer count = geneAndPhenotypeToAffectedCount.containsKey(gene, phenotype) ? (Integer)geneAndPhenotypeToAffectedCount.get(gene, phenotype) : 0;
                        count++;
                        geneAndPhenotypeToAffectedCount.put(gene, phenotype, count);

                        // make sure we count an individual only once per gene
                        sampleAddedForGene.add(sample+"_"+gene);

                        // add gene to meta data
                        if(!geneToChromPos.containsKey(gene))
                        {
                            geneToChromPos.put(gene, record.getChr() + "\t" + record.getPos());
                        }
                    }
                }

            }
        }

    }

    public void convertToFraction(HashMap<String, Integer> phenotypeToNrOfIndividuals, MultiKeyMap geneAndPhenotypeToAffectedCount, MultiKeyMap geneAndPhenotypeToAffectedFraction) throws Exception {
        for(Object o : geneAndPhenotypeToAffectedCount.keySet())
        {
            MultiKey key = (MultiKey)o;
            Integer nrAffected = (Integer)geneAndPhenotypeToAffectedCount.get(key);
            Integer nrOfIndividuals = phenotypeToNrOfIndividuals.get(key.getKey(1));
            double fraction = ((double)nrAffected/(double)nrOfIndividuals) * 100.0;
            if(fraction > 100.0)
            {
                throw new Exception("Fraction exceeds 100: " + fraction);
            }
            geneAndPhenotypeToAffectedFraction.put(key, fraction);
            //System.out.println("put: " + key + " " + nrAffected + ", nrOfIndividuals "+ nrOfIndividuals + " fraction = " + fraction);
        }
    }

    public void convertToZscore(MultiKeyMap geneAndPhenotypeToAffectedFraction, MultiKeyMap geneAndPhenotypeToAffectedZscores, Set<String> phenotypes)
    {
        for(Object o : geneAndPhenotypeToAffectedFraction.keySet())
        {
            MultiKey key = (MultiKey)o;
            double fractionAffected = (double)geneAndPhenotypeToAffectedFraction.get(key);
            String gene = (String)key.getKey(0);
            String phenotype = (String)key.getKey(1);

            System.out.println("gene: " + gene + ", phenotype: " + phenotype + ", fractionAffected: " + fractionAffected);

            for(String phenotype : )


        }
    }

    public void printToOutput(HashMap<String, String> geneToChromPos, MultiKeyMap geneDiseaseToAffectedZscores, File outputZscoreFile)
    {

    }


}
