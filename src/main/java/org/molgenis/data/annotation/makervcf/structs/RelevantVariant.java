package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.MatchVariantsToGenotypeAndInheritance.status;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, status> sampleStatus;
    Map<String, String> sampleGenotypes;
    double alleleFreq;
    double gonlAlleleFreq;
    String transcript;

    CGDEntry cgdInfo;

    public RelevantVariant(VcfEntity variant, String allele, String transcript, double alleleFreq, double gonlAlleleFreq, String gene, Judgment gavinJudgment, Judgment clinvarJudgment)
    {
        this.variant = variant;
        this.allele = allele;
        this.transcript = transcript;
        this.alleleFreq = alleleFreq;
        this.gonlAlleleFreq = gonlAlleleFreq;
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

    public double getAlleleFreq() {
        return alleleFreq;
    }

    public double getGonlAlleleFreq() {
        return gonlAlleleFreq;
    }

    public String getTranscript() {
        return transcript != null ? transcript : "";
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

    public String toStringShort(){
        return "RelevantVariant{"+variant.getChr()+" " +variant.getPos()+ " " + variant.getRef()+ " " + variant.getAltString() + " in gene " + gene + ", gavin:" + gavinJudgment + ", clinvar: " + clinvarJudgment + '}';
    }

    @Override
    public String toString() {
        return "RelevantVariant{" +
                "variant=" + variant +
                ", gavinJudgment=" + gavinJudgment +
                ", clinvarJudgment=" + clinvarJudgment +
                ", allele='" + allele + '\'' +
                ", gene='" + gene + '\'' +
                ", sampleStatus=" + sampleStatus +
                ", alleleFreq=" + alleleFreq +
                ", gonlAlleleFreq=" + gonlAlleleFreq +
                ", transcript='" + transcript + '\'' +
                ", cgdInfo=" + cgdInfo +
                '}';
    }

    public CGDEntry getCgdInfo() {
        return cgdInfo;
    }

    public void setCgdInfo(CGDEntry cgdInfo) {
        this.cgdInfo = cgdInfo;
    }

    public Map<String, status> getSampleStatus() {
        return sampleStatus != null ? sampleStatus : new HashMap<String, status>();
    }

    public void setSampleStatus(Map<String, status> sampleStatus) {
        this.sampleStatus = sampleStatus;
    }
    public void setSampleGenotypes(Map<String, String> sampleGenotypes) {
        this.sampleGenotypes = sampleGenotypes;
    }
    public Map<String, String> getSampleGenotypes() {
        return sampleGenotypes != null ? sampleGenotypes : new HashMap<String, String>();
    }

}
