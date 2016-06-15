package org.molgenis.data.annotation.makervcf;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores.Mode;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRunner.Impact;
import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores;
import org.molgenis.data.annotation.makervcf.clinvar.ClinVar;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.util.*;

/**
 * Created by joeri on 6/1/16.
 *
 * Scan through a VCF, apply GAVIN to site-variant-allele-gene combinations, mark variants that are pathogenic or VOUS.
 * Also run past known pathogenic/likely pathogenic variants in ClinVar.
 * Result is a list of 'judged variants' with all meta data stored within.
 *
 */
public class DiscoverRelevantVariants {

    private VcfRepository vcf;
    private GavinUtils gavin;
    private HandleMissingCaddScores hmcs;
    private ClinVar clinvar;
    private EntityMetaData vcfMeta;

    public DiscoverRelevantVariants(File vcfFile, File gavinFile, File clinvarFile, File caddFile, Mode mode) throws Exception
    {
        this.vcf = new VcfRepository(vcfFile, "vcf");
        this.clinvar = new ClinVar(clinvarFile);
        this.gavin = new GavinUtils(gavinFile);
        this.hmcs = new HandleMissingCaddScores(mode, caddFile);
        this.vcfMeta = vcf.getEntityMetaData();
    }


    public EntityMetaData getVcfMeta() {
        return vcfMeta;
    }

    public List<RelevantVariant> findRelevantVariants() throws Exception
    {
        //GAVIN pathogenic detection
        List<RelevantVariant> relevantVariants = new ArrayList<>();
        Iterator<Entity> it = vcf.iterator();
        while (it.hasNext())
        {
            VcfEntity record = new VcfEntity(it.next());

            boolean gavinPathoFound = false;

            /**
             * Iterate over alternatives, if applicable multi allelic example: 1:1148100-1148100
             */
            for (int i = 0; i < record.getAlts().length; i++)
            {
                Double cadd = hmcs.dealWithCaddScores(record, i);
                for (String gene : record.getGenes())
                {
                    Impact impact = record.getImpact(i, gene);
                    Judgment gavinJudgment = gavin.classifyVariant(gene, record.getExac_AFs(i), impact, cadd);
                    Judgment clinvarJudgment = clinvar.classifyVariant(record, record.getAlts(i), gene);
                    if (gavinJudgment.getClassification().equals(Judgment.Classification.Pathogn) || clinvarJudgment.getClassification().equals(Judgment.Classification.Pathogn))
                    {
                        relevantVariants.add(new RelevantVariant(record, record.getAlts(i), gene, gavinJudgment, clinvarJudgment));
                    }
                }
            }
        }

        return relevantVariants;
    }
}
