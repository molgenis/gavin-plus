package org.molgenis.data.annotation.makervcf.structs;

import java.util.ArrayList;
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
        return getAllele() + "|" + getGene() + "|" + getPhenoType() + "|" + printSampleList(getCarrierSamples()) + "|" + printSampleList(getCarrierSampleGroups()) + "|" + printSampleList(getAffectedSamples()) + "|" + printSampleList(getAffectedSampleGroups()) + "|" + getComoundHet() + "|" + getPredictionTool() + "|" + getTrustedSource() + "|" + getReason();
    }

    public String printSampleList(List<String> samples){
        if(samples.size() == 0)
        {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(String sample : samples)
        {
            sb.append(sample);
            sb.append("/");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String getAllele() {
        return allele != null  ? allele : "";
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }

    public String getGene() {
        return gene != null ? gene : "";
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getPhenoType() {
        return phenoType != null ? phenoType : "";
    }

    public void setPhenoType(String phenoType) {
        this.phenoType = phenoType;
    }

    public List<String> getAffectedSamples() {
        return affectedSamples != null ? affectedSamples : new ArrayList<String>();
    }

    public void setAffectedSamples(List<String> affectedSamples) {
        this.affectedSamples = affectedSamples;
    }

    public List<String> getAffectedSampleGroups() {
        return affectedSampleGroups != null ? affectedSampleGroups : new ArrayList<String>();
    }

    public void setAffectedSampleGroups(List<String> affectedSampleGroups) {
        this.affectedSampleGroups = affectedSampleGroups;
    }

    public List<String> getCarrierSamples() {
        return carrierSamples != null ? carrierSamples : new ArrayList<String>();
    }

    public void setCarrierSamples(List<String> carrierSamples) {
        this.carrierSamples = carrierSamples;
    }

    public List<String> getCarrierSampleGroups() {
        return carrierSampleGroups != null ? carrierSampleGroups : new ArrayList<String>();
    }

    public void setCarrierSampleGroups(List<String> carrierSampleGroups) {
        this.carrierSampleGroups = carrierSampleGroups;
    }

    public String getComoundHet() {
        return comoundHet != null ? comoundHet : "";
    }

    public void setComoundHet(String comoundHet) {
        this.comoundHet = comoundHet;
    }

    public String getPredictionTool() {
        return predictionTool != null ? predictionTool : "";
    }

    public void setPredictionTool(String predictionTool) {
        this.predictionTool = predictionTool;
    }

    public String getTrustedSource() {
        return trustedSource != null ? trustedSource : "";
    }

    public void setTrustedSource(String trustedSource) {
        this.trustedSource = trustedSource;
    }

    public String getReason() {
        return reason != null ? reason : "";
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
