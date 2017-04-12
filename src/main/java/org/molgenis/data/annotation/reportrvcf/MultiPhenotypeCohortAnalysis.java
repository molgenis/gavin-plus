package org.molgenis.data.annotation.reportrvcf;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.broadinstitute.variant.vcf.VCFUtils;
import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by joeri on 10/12/16.
 */
public class MultiPhenotypeCohortAnalysis {

    File rvcfInputFile;
    File outputZscoreFile;
    File cgdFile;



    public static void main(String[] args) throws Exception {
        MultiPhenotypeCohortAnalysis mdca = new MultiPhenotypeCohortAnalysis(new File(args[0]), new File(args[1]), new File(args[2]));
        mdca.start();
    }

    public MultiPhenotypeCohortAnalysis(File rvcfInputFile, File outputZscoreFile, File cgdFile)
    {
        this.rvcfInputFile = rvcfInputFile;
        this.outputZscoreFile = outputZscoreFile;
        this.cgdFile = cgdFile;
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
        phenotypeToNrOfIndividualsFromInputVcf(individualsToPhenotype, phenotypeToNrOfIndividuals, this.rvcfInputFile);
        System.out.println("individualsToPhenotype: " + individualsToPhenotype.toString());
        System.out.println("phenotypeToNrOfIndividuals: " + phenotypeToNrOfIndividuals.toString());

        countAffectedPerGene(geneToChromPos, individualsToPhenotype, geneAndPhenotypeToAffectedCount, this.rvcfInputFile);

        ReactomeMultiPhenotypeCohortAnalysis.convertToZscore(geneAndPhenotypeToAffectedCount, geneAndPhenotypeToAffectedZscores, phenotypeToNrOfIndividuals.keySet());

        printToOutput(geneToChromPos, geneAndPhenotypeToAffectedZscores, this.outputZscoreFile, phenotypeToNrOfIndividuals.keySet(), this.cgdFile);
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

        while(vcfIterator.hasNext()) {

            VcfEntity record = new VcfEntity(vcfIterator.next());

            for (RVCF rvcf : record.getRvcf())
            {
                String gene = rvcf.getGene();

                for(String sample : rvcf.getSampleStatus().keySet()) {

                    if (MatchVariantsToGenotypeAndInheritance.status.isPresumedAffected(rvcf.getSampleStatus().get(sample)))
                    {

                        String phenotype = individualsToPhenotype.get(sample);
                        Integer count = geneAndPhenotypeToAffectedCount.containsKey(gene, phenotype) ? (Integer)geneAndPhenotypeToAffectedCount.get(gene, phenotype) : 0;
                        count++;
                        geneAndPhenotypeToAffectedCount.put(gene, phenotype, count);

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

    public void printToOutput(HashMap<String, String> geneToChromPos, MultiKeyMap geneAndPhenotypeToAffectedZscores, File outputZscoreFile, Set<String> phenotypes, File cgdFile) throws IOException
    {

        Map<String, CGDEntry> cgd = LoadCGD.loadCGD(cgdFile);

        PrintWriter pw = new PrintWriter(outputZscoreFile);

        String phenotypeHeader = "";
        for(String p : phenotypes)
        {
            phenotypeHeader += "\t" + p;
        }

        pw.println("Gene" + "\t" + "Condition" + "\t" + "Chr" + "\t" + "Pos" + phenotypeHeader);

        for(String gene : geneToChromPos.keySet())
        {

            String condition = cgd.containsKey(gene) ? cgd.get(gene).getCondition() : "";

            StringBuffer zScores = new StringBuffer();
            for(String p : phenotypes)
            {
                if(geneAndPhenotypeToAffectedZscores.containsKey(gene, p))
                {
                    zScores.append("\t"+(double)geneAndPhenotypeToAffectedZscores.get(gene, p));

                }
                else
                {
                    zScores.append("\t"+"0");
                }
            }

            pw.println(gene + "\t" + condition + "\t" + geneToChromPos.get(gene) + zScores.toString());
        }

        pw.flush();
        pw.close();

    }


}
