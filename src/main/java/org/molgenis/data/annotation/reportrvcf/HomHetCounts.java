package org.molgenis.data.annotation.reportrvcf;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.Status;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 * Hom and Het counts
 *
 */
public class HomHetCounts {

    private VcfReader vcf;
    private PrintWriter pw;
    int nrOfSamples;


    public static String HEADER = "Gene" + "\t" + "HetAbs" + "\t" + "HomAbs" + "\t" + "HomFrac" + "\t" + "HetFrac";

    // args[0] = the 1000G FDR result file (RVCF), e.g. "ALL.chr1to22plusXYMT_RVCF_r1.0.vcf"
    // args[1] = the output homhetcounts, e.g. HomHetCounts_r1.0.tsv
    public static void main(String[] args) throws Exception {
        HomHetCounts hhc = new HomHetCounts(new File(args[0]), new File(args[1]), 2504);
        hhc.go();
    }


    public HomHetCounts(File rvcfInput, File outputHHC, int nrOfSamples) throws Exception {
        this.vcf = GavinUtils.getVcfReader(rvcfInput);
        this.pw = new PrintWriter(outputHHC);
        this.nrOfSamples = nrOfSamples;
    }

    public void go() throws Exception {

        Map<String, Integer> geneToHom = new HashMap<>();
        Map<String, Integer> geneToHet = new HashMap<>();

        //make sure we only count every sample once per gene
        Set<String> sampleGeneCombo = new HashSet<>();

        Iterator<VcfRecord> vcfIterator = vcf.iterator();
        while(vcfIterator.hasNext())
        {

            AnnotatedVcfRecord record = new AnnotatedVcfRecord(vcfIterator.next());

            for(RVCF rvcf : record.getRvcf())
            {

            String gene = rvcf.getGene();

            if(!geneToHom.containsKey(gene))
            {
                geneToHom.put(gene, 0);
                geneToHet.put(gene, 0);
            }

                for(String sample : rvcf.getSampleGenotype().keySet())
                {
                    if(Status.isHomozygous(rvcf.getSampleGenotype().get(sample)))
                    {
                        if(!sampleGeneCombo.contains(gene + "_" + sample))
                        {
                            int count = geneToHom.get(gene);
                            geneToHom.put(gene, count + 1);
                            sampleGeneCombo.add(gene + "_" + sample);
                        }

                    }

                    if(!sampleGeneCombo.contains(gene + "_" + sample))
                    {
                        int count = geneToHet.get(gene);
                        geneToHet.put(gene, count + 1);
                        sampleGeneCombo.add(gene + "_" + sample);
                    }
                }
            }
        }

        pw.println(HEADER);
        for(String gene : geneToHom.keySet())
        {
            pw.println(gene + "\t" + geneToHom.get(gene) + "\t" + geneToHet.get(gene)+ "\t" + (((double)geneToHom.get(gene))/((double)nrOfSamples)) + "\t" + (((double)geneToHet.get(gene))/((double)nrOfSamples)));
        }

        pw.flush();
        pw.close();

    }

}
