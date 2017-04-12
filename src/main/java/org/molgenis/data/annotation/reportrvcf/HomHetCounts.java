package org.molgenis.data.annotation.reportrvcf;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 * Hom and Het counts
 *
 */
public class HomHetCounts {

    private VcfRepository vcf;
    private PrintWriter pw;
    int nrOfSamples;


    public static String HEADER = "Gene" + "\t" + "HetAbs" + "\t" + "HomAbs" + "\t" + "HomFrac" + "\t" + "HetFrac";

    public static void main(String[] args) throws Exception {
        HomHetCounts hhc = new HomHetCounts(new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/ALL.chr1to22plusXYMT_RVCF_r1.0.vcf"),
                new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/HomHetCounts_r1.0.tsv"),
                2504);
        hhc.go();
    }


    public HomHetCounts(File rvcfInput, File outputHHC, int nrOfSamples) throws Exception {
        this.vcf = new VcfRepository(rvcfInput, "vcf");
        this.pw = new PrintWriter(outputHHC);
        this.nrOfSamples = nrOfSamples;
    }

    public void go() throws Exception {

        Map<String, Integer> geneToHom = new HashMap<>();
        Map<String, Integer> geneToHet = new HashMap<>();

        //make sure we only count every sample once per gene
        Set<String> sampleGeneCombo = new HashSet<>();

        Iterator<Entity> vcfIterator = vcf.iterator();
        while(vcfIterator.hasNext())
        {

            VcfEntity record = new VcfEntity(vcfIterator.next());

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
                    if(status.isHomozygous(rvcf.getSampleGenotype().get(sample)))
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
