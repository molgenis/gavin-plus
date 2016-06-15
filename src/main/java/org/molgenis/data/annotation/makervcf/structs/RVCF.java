package org.molgenis.data.annotation.makervcf.structs;

import java.util.List;

/**
 * Created by joeri on 6/1/16.
 */
public class RVCF {



    String allele;
    String gene;
    String phenoType;
    List<String> affectedSamples;
    List<String> affectedSampleGroups;
    List<String> carrierSamples;
    List<String> carrierSampleGroups;
    String comoundHet;
    String predictionTool;
    String trustedSource;
    String reason;

    @Override
    public String toString() {
    //    return allele + "|" + gene + "|" + phenoType + "|" + carrierSamples.toString() + "|" + carrierSampleGroups.toString() + "|" + affectedSamples.toString() + "|" + affectedSampleGroups.toString() + "|" + comoundHet + "|" + predictionTool + "|" + trustedSource + "|" + reason;
        return allele + "|" + gene;
    }

    public String getAllele() {
        return allele;
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getPhenoType() {
        return phenoType;
    }

    public void setPhenoType(String phenoType) {
        this.phenoType = phenoType;
    }

    public List<String> getAffectedSamples() {
        return affectedSamples;
    }

    public void setAffectedSamples(List<String> affectedSamples) {
        this.affectedSamples = affectedSamples;
    }

    public List<String> getAffectedSampleGroups() {
        return affectedSampleGroups;
    }

    public void setAffectedSampleGroups(List<String> affectedSampleGroups) {
        this.affectedSampleGroups = affectedSampleGroups;
    }

    public List<String> getCarrierSamples() {
        return carrierSamples;
    }

    public void setCarrierSamples(List<String> carrierSamples) {
        this.carrierSamples = carrierSamples;
    }

    public List<String> getCarrierSampleGroups() {
        return carrierSampleGroups;
    }

    public void setCarrierSampleGroups(List<String> carrierSampleGroups) {
        this.carrierSampleGroups = carrierSampleGroups;
    }

    public String getComoundHet() {
        return comoundHet;
    }

    public void setComoundHet(String comoundHet) {
        this.comoundHet = comoundHet;
    }

    public String getPredictionTool() {
        return predictionTool;
    }

    public void setPredictionTool(String predictionTool) {
        this.predictionTool = predictionTool;
    }

    public String getTrustedSource() {
        return trustedSource;
    }

    public void setTrustedSource(String trustedSource) {
        this.trustedSource = trustedSource;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
