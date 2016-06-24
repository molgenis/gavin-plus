package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Splitter;

import java.util.*;

/**
 * Created by joeri on 6/1/16.
 */
public class RVCF {

    private static int nrOfFields = 16;
    private static String RVCF_SAMPLESEP = "/";
    private static String RVCF_FIELDSEP = "|";
    private static String RVCF_KEYVALSEP = ":";
    private static String VCF_INFOFIELDSEP= ";";

    public static String attributeName = "RLV";
    public static String attributeMetaData = "Allele "+RVCF_FIELDSEP+" AlleleFreq "+RVCF_FIELDSEP+" Gene "+RVCF_FIELDSEP+" Transcript "+RVCF_FIELDSEP +
            " Phenotype "+RVCF_FIELDSEP+" PhenotypeInheritance "+RVCF_FIELDSEP+" PhenotypeDetails "+RVCF_FIELDSEP+" PhenotypeGroup "+RVCF_FIELDSEP +
            " SampleStatus "+RVCF_FIELDSEP+" SamplePhenotype "+RVCF_FIELDSEP+" SampleGroup "+RVCF_FIELDSEP +
            " VariantSignificance "+RVCF_FIELDSEP+" VariantSignificanceSource "+RVCF_FIELDSEP+" VariantSignificanceJustification "+RVCF_FIELDSEP+" VariantCompoundHet "+RVCF_FIELDSEP+" VariantGroup";

    String allele;
    String alleleFreq;
    String gene;
    String transcript;
    String phenotype;
    String phenotypeInheritance;
    String phenotypeDetails;
    String phenotypeGroup;
    Map<String, String> sampleStatus;
    Map<String, String> samplePhenotype;
    Map<String, String> sampleGroup;
    String variantSignificance;
    String variantSignificanceSource;
    String variantSignificanceJustification;
    String variantCompoundHet;
    String variantGroup;


    public RVCF fromString(String rvcfEntry) throws Exception {
        String[] split = rvcfEntry.split("\\|", -1);
        RVCF rvcfInstance = new RVCF();
        if(split.length != nrOfFields)
        {
            throw new Exception("Splitting RVCF entry on '|' did not yield "+nrOfFields+" fields, invalid format?");
        }
        rvcfInstance.setAllele(split[0]);
        rvcfInstance.setAllele(split[1]);
        rvcfInstance.setGene(split[2]);
        rvcfInstance.setTranscript(split[3]);

        rvcfInstance.setPhenotype(split[4]);
        rvcfInstance.setPhenotypeInheritance(split[5]);
        rvcfInstance.setPhenotypeDetails(split[6]);
        rvcfInstance.setPhenotypeGroup(split[7]);

        rvcfInstance.setSampleStatus(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[8]));
        rvcfInstance.setSamplePhenotype(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[9]));
        rvcfInstance.setSampleGroup(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[10]));

        rvcfInstance.setVariantSignificance(split[11]);
        rvcfInstance.setVariantSignificanceSource(split[12]);
        rvcfInstance.setVariantSignificanceJustification(split[13]);
        rvcfInstance.setVariantCompoundHet(split[14]);
        rvcfInstance.setVariantGroup(split[15]);

        return rvcfInstance;
    }

    public String escapeToSafeVCF (String in)
    {
        return in.replace(VCF_INFOFIELDSEP, " ").replace(RVCF_FIELDSEP, " ").replace(RVCF.RVCF_SAMPLESEP, " ");
    }

    @Override
    public String toString() {
        return escapeToSafeVCF(getAllele()) + RVCF_FIELDSEP + escapeToSafeVCF(getAlleleFreq()) + RVCF_FIELDSEP + escapeToSafeVCF(getGene()) + RVCF_FIELDSEP + escapeToSafeVCF(getTranscript()) + RVCF_FIELDSEP +
                escapeToSafeVCF(getPhenotype()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeInheritance()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeDetails()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeGroup()) + RVCF_FIELDSEP +
                printSampleList(getSampleStatus()) + RVCF_FIELDSEP + printSampleList(getSamplePhenotype()) + RVCF_FIELDSEP + printSampleList(getSampleGroup()) + RVCF_FIELDSEP +
                escapeToSafeVCF(getVariantSignificance()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantSignificanceSource()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantSignificanceJustification()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantCompoundHet()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantGroup());
    }

    public String printSampleList(Map<String, String> samples){
        if(samples.size() == 0)
        {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(String sample : samples.keySet())
        {
            sb.append(escapeToSafeVCF(sample) + RVCF_KEYVALSEP + escapeToSafeVCF(samples.get(sample)) + RVCF_SAMPLESEP);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String getAlleleFreq() {
        return alleleFreq != null ? alleleFreq : "";
    }

    public void setAlleleFreq(String alleleFreq) {
        this.alleleFreq = alleleFreq;
    }

    public String getAllele() {
        return allele != null ? allele : "";
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

    public String getTranscript() {
        return transcript != null ? transcript : "";
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getPhenotype() {
        return phenotype != null ? phenotype : "";
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    public String getPhenotypeInheritance() {
        return phenotypeInheritance != null ? phenotypeInheritance : "";
    }

    public void setPhenotypeInheritance(String phenotypeInheritance) {
        this.phenotypeInheritance = phenotypeInheritance;
    }

    public String getPhenotypeDetails() {
        return phenotypeDetails != null ? phenotypeDetails : "";
    }

    public void setPhenotypeDetails(String phenotypeDetails) {
        this.phenotypeDetails = phenotypeDetails;
    }

    public String getPhenotypeGroup() {
        return phenotypeGroup != null ? phenotypeGroup : "";
    }

    public void setPhenotypeGroup(String phenotypeGroup) {
        this.phenotypeGroup = phenotypeGroup;
    }

    public Map<String, String> getSampleStatus() {
        return sampleStatus != null ? sampleStatus : new HashMap<String, String>();
    }

    public void setSampleStatus(Map<String, String> sampleStatus) {
        this.sampleStatus = sampleStatus;
    }

    public Map<String, String> getSamplePhenotype() {
        return samplePhenotype != null ? samplePhenotype : new HashMap<String, String>();
    }

    public void setSamplePhenotype(Map<String, String> samplePhenotype) {
        this.samplePhenotype = samplePhenotype;
    }

    public Map<String, String> getSampleGroup() {
        return sampleGroup != null ? sampleGroup : new HashMap<String, String>();
    }

    public void setSampleGroup(Map<String, String> sampleGroup) {
        this.sampleGroup = sampleGroup;
    }

    public String getVariantSignificance() {
        return variantSignificance != null ? variantSignificance : "";
    }

    public void setVariantSignificance(String variantSignificance) {
        this.variantSignificance = variantSignificance;
    }

    public String getVariantSignificanceSource() {
        return variantSignificanceSource != null ? variantSignificanceSource : "";
    }

    public void setVariantSignificanceSource(String variantSignificanceSource) {
        this.variantSignificanceSource = variantSignificanceSource;
    }

    public String getVariantSignificanceJustification() {
        return variantSignificanceJustification != null ? variantSignificanceJustification : "";
    }

    public void setVariantSignificanceJustification(String variantSignificanceJustification) {
        this.variantSignificanceJustification = variantSignificanceJustification;
    }

    public String getVariantCompoundHet() {
        return variantCompoundHet != null ? variantCompoundHet : "";
    }

    public void setVariantCompoundHet(String variantCompoundHet) {
        this.variantCompoundHet = variantCompoundHet;
    }

    public String getVariantGroup() {
        return variantGroup != null ? variantGroup : "";
    }

    public void setVariantGroup(String variantGroup) {
        this.variantGroup = variantGroup;
    }
}
