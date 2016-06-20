package org.molgenis.data.annotation.makervcf.structs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by joeri on 6/1/16.
 */
public class RVCF {


    // 11 fields
    String allele;
    String gene;
    String phenoType;
    List<String> affectedSamples;
    List<String> affectedSampleGroups;
    List<String> carrierSamples;
    List<String> carrierSampleGroups;
    String compoundHet;
    String predictionTool;
    String trustedSource;
    String reason;

    private String SAMPLELISTSEPARATOR = "/";

    @Override
    public String toString() {
        return getAllele() + "|" + getGene() + "|" + getPhenoType() + "|" + printSampleList(getCarrierSamples()) + "|" + printSampleList(getCarrierSampleGroups()) + "|" + printSampleList(getAffectedSamples()) + "|" + printSampleList(getAffectedSampleGroups()) + "|" + getCompoundHet() + "|" + getPredictionTool() + "|" + getTrustedSource() + "|" + getReason();
    }

    public RVCF fromString(String rvcfEntry) throws Exception {
        String[] split = rvcfEntry.split("\\|", -1);
        RVCF rvcfInstance = new RVCF();
        if(split.length != 11)
        {
            throw new Exception("Splitting RVCF entry did not yield 11 fields, invalid format?");
        }
        rvcfInstance.setAllele(split[0]);
        rvcfInstance.setGene(split[1]);
        rvcfInstance.setPhenoType(split[2]);
        rvcfInstance.setAffectedSamples(Arrays.asList(split[3].split(SAMPLELISTSEPARATOR)));
        rvcfInstance.setAffectedSampleGroups(Arrays.asList(split[4].split(SAMPLELISTSEPARATOR)));
        rvcfInstance.setCarrierSamples(Arrays.asList(split[5].split(SAMPLELISTSEPARATOR)));
        rvcfInstance.setCarrierSampleGroups(Arrays.asList(split[6].split(SAMPLELISTSEPARATOR)));
        rvcfInstance.setCompoundHet(split[7]);
        rvcfInstance.setPredictionTool(split[8]);
        rvcfInstance.setTrustedSource(split[9]);
        rvcfInstance.setReason(split[10]);

        return rvcfInstance;
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
            sb.append(SAMPLELISTSEPARATOR);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String escapeToSafeVCF (String in)
    {
        return in.replace(";", ",").replace("|", ",");
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
        return phenoType != null ? escapeToSafeVCF(phenoType) : "";
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

    public String getCompoundHet() {
        return compoundHet != null ? compoundHet : "";
    }

    public void setCompoundHet(String compoundHet) {
        this.compoundHet = compoundHet;
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
