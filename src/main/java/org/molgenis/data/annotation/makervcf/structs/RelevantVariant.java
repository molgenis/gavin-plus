package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;

import java.util.HashMap;
import java.util.List;

/**
 * Created by joeri on 6/13/16.
 */
public class RelevantVariant
{
    VcfEntity variant;
    Judgment gavinJudgment;
    Judgment clinvarJudgment;
    String allele;
    String gene;
    HashMap<String, Entity> affectedSamples;
    HashMap<String, Entity> carrierSamples;
    HashMap<String, Entity> unknownInheritanceModeSamples;
    CGDEntry cgdInfo;

    public RelevantVariant(VcfEntity variant, String allele, String gene, Judgment gavinJudgment, Judgment clinvarJudgment)
    {
        this.variant = variant;
        this.allele = allele;
        this.gene = gene;
        this.gavinJudgment = gavinJudgment;
        this.clinvarJudgment = clinvarJudgment;
    }

    public String getAllele() {
        return allele;
    }

    public String getGene() {
        return gene;
    }

    public VcfEntity getVariant() {
        return variant;
    }

    public Judgment getGavinJudgment() {
        return gavinJudgment;
    }

    public Judgment getClinvarJudgment() {
        return clinvarJudgment;
    }

    @Override
    public String toString() {
        return "RelevantVariant{" +
                "variant=" + variant +
                ", gavinJudgment=" + gavinJudgment +
                ", clinvarPathoMatch=" + clinvarJudgment +
                '}';
    }

    public CGDEntry getCgdInfo() {
        return cgdInfo;
    }

    public void setCgdInfo(CGDEntry cgdInfo) {
        this.cgdInfo = cgdInfo;
    }

    public HashMap<String, Entity> getAffectedSamples() {
        return affectedSamples;
    }

    public void setAffectedSamples(HashMap<String, Entity> affectedSamples) {
        this.affectedSamples = affectedSamples;
    }

    public HashMap<String, Entity> getCarrierSamples() {
        return carrierSamples;
    }

    public void setCarrierSamples(HashMap<String, Entity> carrierSamples) {
        this.carrierSamples = carrierSamples;
    }

    public HashMap<String, Entity> getUnknownInheritanceModeSamples() {
        return unknownInheritanceModeSamples;
    }

    public void setUnknownInheritanceModeSamples(HashMap<String, Entity> unknownInheritanceModeSamples) {
        this.unknownInheritanceModeSamples = unknownInheritanceModeSamples;
    }
}
