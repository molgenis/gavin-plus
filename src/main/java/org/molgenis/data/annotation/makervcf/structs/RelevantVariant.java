package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by joeri on 6/13/16.
 */
public class RelevantVariant
{
    VcfEntity variant;
    Judgment judgment;
    String allele;
    String gene;
    String FDR;
    Map<String, status> sampleStatus;
    Map<String, String> sampleGenotypes;
    Set<String> parentsWithReferenceCalls;
    double alleleFreq;
    double gonlAlleleFreq;
    String transcript;

    CGDEntry cgdInfo;

    public RelevantVariant(VcfEntity variant, String allele, String transcript, double alleleFreq, double gonlAlleleFreq, String gene, Judgment judgment)
    {
        this.variant = variant;
        this.allele = allele;
        this.transcript = transcript;
        this.alleleFreq = alleleFreq;
        this.gonlAlleleFreq = gonlAlleleFreq;
        this.gene = gene;
        this.judgment = judgment;
    }

    public String getFDR() {
        return FDR != null ? FDR : "";
    }

    public void setFDR(String FDR) {
        this.FDR = FDR;
    }

    public Set<String> getParentsWithReferenceCalls() {
        return parentsWithReferenceCalls;
    }

    public void setParentsWithReferenceCalls(Set<String> parentsWithReferenceCalls) {
        this.parentsWithReferenceCalls = parentsWithReferenceCalls;
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

    public Judgment getJudgment() {
        return judgment;
    }

    public String toStringShort(){
        return "RelevantVariant{"+variant.getChr()+" " +variant.getPos()+ " " + variant.getRef()+ " " + variant.getAltString() + " in gene " + gene + ", judgment:" + judgment + '}';
    }

    @Override
    public String toString() {
        return "RelevantVariant{" +
                "variant=" + variant +
                ", judgment=" + judgment +
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
