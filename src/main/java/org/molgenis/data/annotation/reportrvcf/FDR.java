package org.molgenis.data.annotation.reportrvcf;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by joeri on 6/29/16.
 *
 * False Discovery Rate
 *
 *            //FDR: report false hits per gene, right before the stream is swapped from 'gene based' to 'position based'
             //FOR: report missed hits per gene, same as above with pathogenic gold standard set
             //Iterator<RelevantVariant> rv8 = new FDR(rv7, new File("/Users/joeri/Desktop/1000G_diag_FDR/exomePlus/FDR.tsv"), verbose).go();
             //Iterator<RelevantVariant> rv8 = new FOR(rv7, inputVcfFile).go();

 */
public class FDR {

    private VcfReader vcf;
    private PrintWriter pw;
    int nrOfSamples;


    public static String HEADER = "Gene" + "\t" + "AffectedAbs" + "\t" + "CarrierAbs" + "\t" + "AffectedFrac" + "\t" + "CarrierFrac";

    public static void main(String[] args) throws Exception {
        FDR fdr = new FDR(new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/ALL.chr1to22plusXYMT_RVCF_r1.0.vcf"),
                new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/FDR_r1.0.tsv"),
                2504);
        fdr.go();
    }


    public FDR(File rvcfInput, File outputFDR, int nrOfSamples) throws Exception {
        this.vcf = GavinUtils.getVcfReader(rvcfInput);
        this.pw = new PrintWriter(outputFDR);
        this.nrOfSamples = nrOfSamples;
    }

    public void go() throws Exception {

        Map<String, Integer> geneToAffected = new HashMap<>();
        Map<String, Integer> geneToCarrier = new HashMap<>();

        //make sure we only count every sample once per gene
        Set<String> sampleGeneCombo = new HashSet<>();

        Iterator<VcfRecord> vcfIterator = vcf.iterator();
        while(vcfIterator.hasNext())
        {

            VcfEntity record = new VcfEntity(vcfIterator.next());

            //TODO: check implications of this being a loop now instead of 1 rvcf
            for(RVCF rvcf : record.getRvcfFromVcfInfoField()){

            String gene = rvcf.getGene();

            if(!geneToAffected.containsKey(gene))
            {
                geneToAffected.put(gene, 0);
                geneToCarrier.put(gene, 0);
            }

                for(String sample : rvcf.getSampleStatus().keySet())
                {
                    if(status.isPresumedAffected(rvcf.getSampleStatus().get(sample)))
                    {
                        if(!sampleGeneCombo.contains(gene + "_" + sample))
                        {
                            int count = geneToAffected.get(gene);
                            geneToAffected.put(gene, count + 1);
                            sampleGeneCombo.add(gene + "_" + sample);
                        }

                    }
                    else if(status.isPresumedCarrier(rvcf.getSampleStatus().get(sample)))
                    {
                        if(!sampleGeneCombo.contains(gene + "_" + sample))
                        {
                            int count = geneToCarrier.get(gene);
                            geneToCarrier.put(gene, count + 1);
                            sampleGeneCombo.add(gene + "_" + sample);
                        }
                    }
                    else{
                        throw new Exception("ERROR: Unknown sample status: " +rvcf.getSampleStatus().get(sample));
                    }
                }
            }


        }

        pw.println(HEADER);
        for(String gene : geneToAffected.keySet())
        {
            pw.println(gene + "\t" + geneToAffected.get(gene) + "\t" + geneToCarrier.get(gene)+ "\t" + (((double)geneToAffected.get(gene))/((double)nrOfSamples)) + "\t" + (((double)geneToCarrier.get(gene))/((double)nrOfSamples)));
        }

        pw.flush();
        pw.close();

    }

}
