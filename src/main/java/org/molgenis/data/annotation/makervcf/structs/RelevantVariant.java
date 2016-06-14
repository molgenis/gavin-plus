package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;

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
    //TODO list of matching affected samples & carriers ?
    List<Entity> affectedSamples;
    List<Entity> carrierSamples;
    List<Entity> unknownEffectSamples;
    CGDEntry cgdInfo;

    public RelevantVariant(VcfEntity variant, String allele, Judgment gavinJudgment, Judgment clinvarJudgment)
    {
        this.variant = variant;
        this.allele = allele;
        this.gavinJudgment = gavinJudgment;
        this.clinvarJudgment = clinvarJudgment;
    }

    public String getAllele() {
        return allele;
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
}
