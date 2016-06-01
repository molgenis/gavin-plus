package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.Entity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by joeri on 6/1/16.
 *
 * Scan through a VCF, apply GAVIN to site-variant-allele-gene combinations, mark variants that are pathogenic or VOUS.
 * Also run past known pathogenic/likely pathogenic variants in ClinVar.
 * Result is a list of 'judged variants' with all meta data stored within.
 *
 */
public class DiscoverRelevantVariants {

    public DiscoverRelevantVariants(File vcfFile) throws IOException {

        VcfRepository vcf = new VcfRepository(vcfFile, "vcf");
        Iterator<Entity> it = vcf.iterator();


        while (it.hasNext())
        {

        }

        
    }

}
