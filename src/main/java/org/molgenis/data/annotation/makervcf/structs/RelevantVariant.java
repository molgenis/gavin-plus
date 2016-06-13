package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.annotation.entity.impl.gavin.Judgment;

/**
 * Created by joeri on 6/13/16.
 */
public class RelevantVariant
{
    VcfEntity variant;
    Judgment gavinJudgment;
    VcfEntity clinvarPathoMatch;

    public RelevantVariant(VcfEntity variant, Judgment gavinJudgment, VcfEntity clinvarPathoMatch)
    {
        this.variant = variant;
        this.gavinJudgment = gavinJudgment;
        this.clinvarPathoMatch = clinvarPathoMatch;
    }

    public VcfEntity getVariant() {
        return variant;
    }

    public Judgment getGavinJudgment() {
        return gavinJudgment;
    }

    public VcfEntity getClinvarPathoMatch() {
        return clinvarPathoMatch;
    }

    @Override
    public String toString() {
        return "RelevantVariant{" +
                "variant=" + variant +
                ", gavinJudgment=" + gavinJudgment +
                ", clinvarPathoMatch=" + clinvarPathoMatch +
                '}';
    }
}
