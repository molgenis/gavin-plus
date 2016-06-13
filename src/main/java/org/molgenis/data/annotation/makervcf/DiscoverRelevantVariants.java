package org.molgenis.data.annotation.makervcf;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores.Mode;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRunner.Impact;
import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores;
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
    private VcfRepository clinvar;

    public DiscoverRelevantVariants(File vcfFile, File gavinFile, File clinvarFile, File caddFile, Mode mode) throws Exception
    {
        this.vcf = new VcfRepository(vcfFile, "vcf");
        this.clinvar = new VcfRepository(clinvarFile, "clinvar");
        this.gavin = new GavinUtils(gavinFile);
        this.hmcs = new HandleMissingCaddScores(mode, caddFile);
    }

    public List<RelevantVariant> findRelevantVariants() throws Exception
    {
        //ClinVar match
        Iterator<Entity> cvIt = clinvar.iterator();
        Map<String, VcfEntity> posToClinVar = new HashMap<>();
        while (cvIt.hasNext())
        {
            VcfEntity record = new VcfEntity(cvIt.next());
            posToClinVar.put(record.getChr()+"_"+record.getPos() + "_", record); //todo map on chr_pos_ref_alt ? use trimming of alleles like for CADD ? do this a bit better..
        }

        //GAVIN pathogenic detection
        List<RelevantVariant> relevantVariants = new ArrayList<>();
        Iterator<Entity> it = vcf.iterator();
        while (it.hasNext())
        {
            VcfEntity record = new VcfEntity(it.next());

            boolean gavinPathoFound = false;
            String clinvarKey = record.getChr() + "_"+record.getPos() + "_";
            VcfEntity clinVarHit = posToClinVar.containsKey(clinvarKey) ? posToClinVar.get(clinvarKey) : null;

            /**
             * Iterate over alternatives, if applicable multi allelic example: 1:1148100-1148100
             */
            for (int i = 0; i < record.getAlts().length; i++) {

                Double cadd = hmcs.dealWithCaddScores(record, i);
                for (String gene : record.getGenes()) {
                    Impact impact = record.getImpact(i, gene);
                    Judgment judgment = gavin.classifyVariant(gene, record.getExac_AFs(i), impact, cadd);
                    if (judgment.getClassification().equals(Judgment.Classification.Pathogn)) {
                        relevantVariants.add(new RelevantVariant(record, judgment, i, clinVarHit));
                        gavinPathoFound = true;
                    }
                }
            }

            //TODO: per allele..
            if(!gavinPathoFound && clinVarHit != null)
            {
                relevantVariants.add(new RelevantVariant(record, null, null, clinVarHit));
            }

        }

        return relevantVariants;
    }
}
