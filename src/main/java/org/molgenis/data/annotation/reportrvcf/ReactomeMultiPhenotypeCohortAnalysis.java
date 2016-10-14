package org.molgenis.data.annotation.reportrvcf;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by joeri on 10/12/16.
 */
public class ReactomeMultiPhenotypeCohortAnalysis {

    File rvcfInputFile;
    File outputZscoreFile;
    HashMap<String, Set<String>> pathwayToGenes;
    HashMap<String, Set<String>> geneToPathways;


    public static void main(String[] args) throws Exception {
        ReactomeMultiPhenotypeCohortAnalysis mdca = new ReactomeMultiPhenotypeCohortAnalysis(new File(args[0]), new File(args[1]), new File(args[2]));
        mdca.start();
    }

    public ReactomeMultiPhenotypeCohortAnalysis(File rvcfInputFile, File outputZscoreFile, File reactomeFile) throws Exception {
        this.rvcfInputFile = rvcfInputFile;
        this.outputZscoreFile = outputZscoreFile;

        /** Reactome mapping **/
//        this.pathwayToGenes = Reactome.load(reactomeFile);
//        this.geneToPathways = Reactome.makeGeneToPathwaysMap(pathwayToGenes);

        /** OmimHpo mapping **/
        this.pathwayToGenes = OmimHpo.load(reactomeFile);
        filter();
        this.geneToPathways = OmimHpo.makeGeneToHpo(pathwayToGenes);

        /** CGD mapping **/
//        Map<String, CGDEntry> cgd = LoadCGD.loadCGD(reactomeFile);
//        this.pathwayToGenes = LoadCGD.makeCgdToGenesMap(cgd);
//        this.geneToPathways = LoadCGD.makeGenesToDiseaseName(cgd);


    }

    public void filter()
    {
        String[] items = new String[]{"HP:..."};
        HashMap<String, Set<String>> pathwayToGenesNew = new HashMap<String, Set<String>>();
        for(String hpo : pathwayToGenes.keySet())
        {
            if(stringContainsItemFromList(hpo, items)){
                pathwayToGenesNew.put(hpo, pathwayToGenes.get(hpo));
            }
        }
        this.pathwayToGenes = pathwayToGenesNew;
    }

    public void start() throws Exception {


        // meta data: number of individuals seen with this phenotype as denoted by PHENOTYPE in VCF: ##SAMPLE=<ID=P583292,SEX=UNKNOWN,PHENOTYPE=IRONACC>
        HashMap<String, String> individualsToPhenotype = new HashMap<>();

        // meta data: number of individuals seen with this phenotype as denoted by PHENOTYPE in VCF: ##SAMPLE=<ID=P583292,SEX=UNKNOWN,PHENOTYPE=IRONACC>
        HashMap<String, Integer> phenotypeToNrOfIndividuals = new HashMap<>();

        // while iterating over rvcf, count affected individuals per gene for each phenotype
        MultiKeyMap pathwayAndPhenotypeToAffectedCount = new MultiKeyMap();

        // transform fractions into Z-scores by comparing 1 phenotype group to the others
        MultiKeyMap pathwayAndPhenotypeToAffectedZscores = new MultiKeyMap();

        // execution
        phenotypeToNrOfIndividualsFromInputVcf(individualsToPhenotype, phenotypeToNrOfIndividuals, this.rvcfInputFile);
        System.out.println("individualsToPhenotype: " + individualsToPhenotype.toString());
        System.out.println("phenotypeToNrOfIndividuals: " + phenotypeToNrOfIndividuals.toString());

        countAffectedPerPathway(individualsToPhenotype, pathwayAndPhenotypeToAffectedCount, this.rvcfInputFile, this.geneToPathways);
  //      System.out.println("countAffectedPerGene: " + pathwayAndPhenotypeToAffectedCount.toString());

//
        convertToZscore(pathwayAndPhenotypeToAffectedCount, pathwayAndPhenotypeToAffectedZscores, phenotypeToNrOfIndividuals.keySet());
//
        printToOutput(pathwayToGenes.keySet(), pathwayAndPhenotypeToAffectedZscores, this.outputZscoreFile, phenotypeToNrOfIndividuals.keySet());
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


    public void countAffectedPerPathway(HashMap<String, String> individualsToPhenotype, MultiKeyMap pathwayAndPhenotypeToAffectedCount, File rvcfInputFile, HashMap<String, Set<String>> geneToPathways) throws Exception {
        VcfRepository vcf = new VcfRepository(rvcfInputFile, "vcf");
        Iterator<Entity> vcfIterator = vcf.iterator();

        // "SampleID_GeneName"
 //       Set<String> sampleAddedForGene = new HashSet<>();

        while(vcfIterator.hasNext()) {

            VcfEntity record = new VcfEntity(vcfIterator.next());

            for (RVCF rvcf : record.getRvcf())
            {
                String gene = rvcf.getGene();

                if(!geneToPathways.containsKey(gene))
                {
                    continue;
                }
                Set<String> pathways = geneToPathways.get(gene);

            //    System.out.println("looking at gene: " + gene + " having pathways " + pathways.toString());

                for(String sample : rvcf.getSampleStatus().keySet()) {
//
                    // overrepresentation vs amount presumed affected?
//                    if(sampleAddedForGene.contains(sample+"_"+gene))
//                    {
//                        continue;
//                    }

                    if (MatchVariantsToGenotypeAndInheritance.status.isPresumedAffected(rvcf.getSampleStatus().get(sample)))
                    //if (rvcf.getSampleStatus().get(sample).toString().contains("AFFECTED"))
                    {
                        String phenotype = individualsToPhenotype.get(sample);

                        for(String pathway : pathways)
                        {
                            Integer count = pathwayAndPhenotypeToAffectedCount.containsKey(pathway, phenotype) ? (Integer)pathwayAndPhenotypeToAffectedCount.get(pathway, phenotype) : 0;
                            count++;
                            pathwayAndPhenotypeToAffectedCount.put(pathway, phenotype, count);
                        }

                        // make sure we count an individual only once per gene
    //                    sampleAddedForGene.add(sample+"_"+gene);

                    }
                }

            }
        }

    }



    public static void convertToZscore(MultiKeyMap pathwayAndPhenotypeToAffectedCount, MultiKeyMap pathwayAndPhenotypeToAffectedZscores, Set<String> phenotypes)
    {
        for(Object o : pathwayAndPhenotypeToAffectedCount.keySet())
        {
            MultiKey key = (MultiKey)o;
            double nrAffected = Double.parseDouble(pathwayAndPhenotypeToAffectedCount.get(key).toString());
            String pathway = (String)key.getKey(0);
            String phenotype = (String)key.getKey(1);

            System.out.println("pathway: " + pathway + ", phenotype: " + phenotype + ", nrAffected: " + nrAffected);

            int i = 0;
            double[] testAgainst = new double[phenotypes.size()-1];
            for(String phenotypeToTestAgainst : phenotypes)
            {
                if(phenotypeToTestAgainst.equals(phenotype))
                {
                    System.out.println("skipping self phenotype: " + phenotype+ " for pathway " + pathway);
                    continue;
                }

                Object nrAffectedToTestAgainstObj = pathwayAndPhenotypeToAffectedCount.get(pathway, phenotypeToTestAgainst);
                double nrAffectedToTestAgainst = nrAffectedToTestAgainstObj == null ? 0.0 : Double.parseDouble(nrAffectedToTestAgainstObj.toString());

                testAgainst[i++] = nrAffectedToTestAgainst;

                System.out.println("test against: " + phenotypeToTestAgainst + " for pathway " + pathway + ", nrAffectedToTestAgainst: " + nrAffectedToTestAgainst);

            }

            Mean meanEval = new Mean();
            double mean = meanEval.evaluate(testAgainst);

            StandardDeviation sdEval = new StandardDeviation();
            double sd = sdEval.evaluate(testAgainst);

            double zScore = (nrAffected - mean) / sd;

            zScore = zScore == Double.POSITIVE_INFINITY ? 99 : zScore;

            System.out.println("mean: " + mean + ", sd: " + sd + ", Z-score: " + zScore);

            pathwayAndPhenotypeToAffectedZscores.put(pathway, phenotype, zScore);


        }
    }

    public void printToOutput(Set<String> pathways, MultiKeyMap pathwayAndPhenotypeToAffectedZscores, File outputZscoreFile, Set<String> phenotypes) throws IOException
    {

        PrintWriter pw = new PrintWriter(outputZscoreFile);

        String phenotypeHeader = "";
        for(String p : phenotypes)
        {
            phenotypeHeader += "\t" + p;
        }

        pw.println("Pathway" + phenotypeHeader);

        for(String pathway : pathways)
        {

            StringBuffer zScores = new StringBuffer();
            for(String p : phenotypes)
            {
                if(pathwayAndPhenotypeToAffectedZscores.containsKey(pathway, p))
                {
                    zScores.append("\t"+(double)pathwayAndPhenotypeToAffectedZscores.get(pathway, p));

                }
                else
                {
                    zScores.append("\t"+"0");

                }
            }

            pw.println(pathway + zScores.toString());
        }

        pw.flush();
        pw.close();

    }


    public static boolean stringContainsItemFromList(String inputString, String[] items)
    {
        for(int i = 0; i < items.length; i++)
        {
            if(inputString.contains(items[i]))
            {
                System.out.println("true items[i] " + items[i]);
                return true;
            }
        }
        return false;
    }

}
