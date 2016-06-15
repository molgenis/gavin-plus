package org.molgenis.data.annotation.makervcf;

import org.molgenis.calibratecadd.support.JudgedVariant;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joeri on 6/1/16.
 *
 * Take the results from MatchVariantsToGenotypeAndInheritance, and report the findins in RVCF format.
 * We grab the header from the original VCF, the original VCF records, and add onto this our results.
 *
 */
public class MakeRVCFforClinicalVariants {

    List<RelevantVariant> relevantVariants;
    AttributeMetaData rlv;

    public MakeRVCFforClinicalVariants(List<RelevantVariant> relevantVariants,  AttributeMetaData rlv)
    {
        this.relevantVariants = relevantVariants;
        this.rlv = rlv;
    }

    public void addRVCFfield()
    {
        for(RelevantVariant rv : relevantVariants)
        {

            RVCF rvcf = new RVCF();

            rvcf.setAllele(rv.getAllele());
            if(rv.getAffectedSamples() != null) {
                rvcf.setAffectedSamples(new ArrayList<>(rv.getAffectedSamples().keySet()));
            }
            if(rv.getCarrierSamples() != null) {
                rvcf.setCarrierSamples(new ArrayList<>(rv.getCarrierSamples().keySet()));
            }

            rvcf.setGene(rv.getGene());

            if(rv.getCgdInfo() != null) {
                rvcf.setPhenoType(rv.getCgdInfo().getCondition());
            }
            rvcf.setPredictionTool(rv.getGavinJudgment().getClassification().equals(Judgment.Classification.Pathogn) ? "GAVIN" : "");
            rvcf.setPredictionTool(rv.getClinvarJudgment().getClassification().equals(Judgment.Classification.Pathogn) ? "ClinVar" : "");
            rvcf.setReason((rv.getGavinJudgment().getClassification().equals(Judgment.Classification.Pathogn) ? rv.getGavinJudgment().getReason() : "") + (rv.getClinvarJudgment().getClassification().equals(Judgment.Classification.Pathogn) ? rv.getClinvarJudgment().getReason() : ""));

            Entity e = rv.getVariant().getOrignalEntity();
            DefaultEntityMetaData emd = (DefaultEntityMetaData) e.getEntityMetaData();
            DefaultAttributeMetaData infoAttribute = (DefaultAttributeMetaData) emd.getAttribute(VcfRepository.INFO);
            infoAttribute.addAttributePart(rlv);

            e.set("RLV",rvcf.toString());

        }
    }
}
