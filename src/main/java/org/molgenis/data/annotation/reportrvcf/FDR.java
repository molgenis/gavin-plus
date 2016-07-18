package org.molgenis.data.annotation.reportrvcf;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    private VcfRepository vcf;


    public FDR(File rvcfInput) throws Exception {
        this.vcf = new VcfRepository(rvcfInput, "vcf");
    }

    public void go() throws Exception {
        Iterator<Entity> vcfIterator = vcf.iterator();
        while(vcfIterator.hasNext()) {

            VcfEntity record = new VcfEntity(vcfIterator.next());
            RVCF rvcf = record.getRvcf();
            System.out.println(rvcf.toString());

            //count sample status per gene and write results


        }
    }

    public static void main(String[] args) throws Exception {
        FDR fdr = new FDR(new File("/Users/joeri/Desktop/1000G_diag_FDR/exomePlus/FDR.tsv"));
        fdr.go();
    }


//    private PrintWriter pw;
//
//    public FDR(Iterator<RelevantVariant> relevantVariants, File writeTo, boolean verbose) throws FileNotFoundException {
//        super(relevantVariants, verbose);
//        this.pw = new PrintWriter(writeTo);
//        this.pw.println("Chr" + "\t" + "Pos" + "\t" + "Gene" + "\t" + "Total" + "\t" + "Affected" + "\t" + "Carrier");
//    }
//
//    @Override
//    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {
//
//        Set<String> uniqueTotalSamplesForThisGene = new HashSet<String>();
//        Set<String> uniqueAffectedSamplesForThisGene = new HashSet<String>();
//        Set<String> uniqueCarrierSamplesForThisGene = new HashSet<String>();
//        String chrom = null;
//        String pos= null;
//
//        for(RelevantVariant rv : variantsPerGene)
//        {
//            if(chrom == null) {
//                chrom = rv.getVariant().getChr();
//                pos = rv.getVariant().getPos();
//            }
//            //extra check, may be removed FIXME
//            if(!rv.getGene().equals(gene))
//            {
//                throw new Exception("ERROR: Gene mismatch: " + rv.getGene() + " vs " + gene);
//            }
//            for(String sample : rv.getSampleStatus().keySet())
//            {
//                uniqueTotalSamplesForThisGene.add(sample);
//
//                if(status.isPresumedAffected(rv.getSampleStatus().get(sample)))
//                {
//                    uniqueAffectedSamplesForThisGene.add(sample);
//                }
//                else if(status.isPresumedCarrier(rv.getSampleStatus().get(sample)))
//                {
//                    uniqueCarrierSamplesForThisGene.add(sample);
//                }
//                else{
//                    throw new Exception("ERROR: Unknown sample status: " +rv.getSampleStatus().get(sample));
//                }
//            }
//        }
//
//        pw.println(chrom + "\t" + pos + "\t" + gene + "\t" + (uniqueTotalSamplesForThisGene.size()) + "\t" + (uniqueAffectedSamplesForThisGene.size()) + "\t" + (uniqueCarrierSamplesForThisGene.size()));
//        pw.flush();
//    }

}
