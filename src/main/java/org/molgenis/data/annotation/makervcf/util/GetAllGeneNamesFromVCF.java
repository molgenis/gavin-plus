package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by joeri on 7/14/16.
 */
public class GetAllGeneNamesFromVCF
{
    public static void main(String[] args) throws Exception
    {
        File vcfFile = new File("/Users/joeri/Desktop/1000G_diag_FDR/exomePlus/ALL.chr1to22plusXYMT.phase3_20130502.variantsOnly.snpEffNoIntergenic.exac.gonl.cadd.vcf.gz");
        PrintWriter pw = new PrintWriter(new File("/Users/joeri/Desktop/1000G_diag_FDR/exomePlus/allGenes.txt"));
        VcfRepository vcf = new VcfRepository(vcfFile, "vcf");
        Iterator<Entity> vcfIterator = vcf.iterator();

        Set<String> genes = new HashSet<String>();
        int i = 0;
        while(vcfIterator.hasNext()) {
            VcfEntity record = new VcfEntity(vcfIterator.next());
            i++;
            Set<String> genesForVariant = record.getGenes();
            if(genesForVariant != null)
            {
                genes.addAll(record.getGenes());
            }

            if(i % 10000 == 0)
            {
                System.out.println("Seen "+i+" variants, found " + genes.size() + " unique genes so far..");
            }
        }
        System.out.println("Writing results..");
        for(String gene : genes)
        {
            pw.println(gene);
        }
        pw.flush();
        pw.close();
        System.out.println("Done");

    }
}
