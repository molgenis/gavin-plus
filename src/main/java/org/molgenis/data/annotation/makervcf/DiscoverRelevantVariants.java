package org.molgenis.data.annotation.makervcf;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.calibratecadd.support.JudgedVariant;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 6/1/16.
 *
 * Scan through a VCF, apply GAVIN to site-variant-allele-gene combinations, mark variants that are pathogenic or VOUS.
 * Also run past known pathogenic/likely pathogenic variants in ClinVar.
 * Result is a list of 'judged variants' with all meta data stored within.
 *
 */
public class DiscoverRelevantVariants {

    private List<JudgedVariant> relevantVariants;
    private VcfRepository vcf;
    private GavinUtils gavin;
    private HandleMissingCaddScores hmcs;

    public DiscoverRelevantVariants(File vcfFile, File gavinFile, String mode) throws Exception
    {
        this.vcf = new VcfRepository(vcfFile, "vcf");
        this.gavin = new GavinUtils(gavinFile);
        this.hmcs = new HandleMissingCaddScores(mode);
    }

    public List<JudgedVariant> findRelevantVariants() throws Exception
    {
        Iterator<Entity> it = vcf.iterator();

        while (it.hasNext())
        {
            VcfEntity record = new VcfEntity(it.next());

            /**
             * Iterate over alternatives, if applicable multi allelic example: 1:1148100-1148100
             */
            for (int i = 0; i < record.getAlts().length; i++)
            {

                hmcs.dealWithMissingCaddScore(record.getCadd(i));

                for(String gene : record.getGenes())
                {

                    Judgment judgment = gavin.classifyVariant(gene, exac_af, impact, cadd);

                }

                //TODO: clinvar check


            }
        }
    }
}
