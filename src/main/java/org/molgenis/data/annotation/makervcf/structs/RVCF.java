package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Splitter;

import java.util.*;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;

/**
 * Created by joeri on 6/1/16.
 */
public class RVCF {

    private static int nrOfFields = 18;
    private static String RVCF_SAMPLESEP = "/";
    private static String RVCF_FIELDSEP = "|";
    private static String RVCF_KEYVALSEP = ":";
    private static String VCF_INFOFIELDSEP= ";";

    public static String attributeName = "RLV";
    public static String attributeMetaData = "Allele "+RVCF_FIELDSEP+" AlleleFreq "+RVCF_FIELDSEP+" Gene "+RVCF_FIELDSEP+" Transcript "+RVCF_FIELDSEP +
            " Phenotype "+RVCF_FIELDSEP+" PhenotypeInheritance "+RVCF_FIELDSEP+" PhenotypeOnset " +RVCF_FIELDSEP+" PhenotypeDetails "+RVCF_FIELDSEP+" PhenotypeGroup "+RVCF_FIELDSEP +
            " SampleStatus "+RVCF_FIELDSEP+" SamplePhenotype "+RVCF_FIELDSEP+" SampleGenotype "+RVCF_FIELDSEP+" SampleGroup "+RVCF_FIELDSEP +
            " VariantSignificance "+RVCF_FIELDSEP+" VariantSignificanceSource "+RVCF_FIELDSEP+" VariantSignificanceJustification "+RVCF_FIELDSEP+" VariantCompoundHet "+RVCF_FIELDSEP+" VariantGroup";

    String allele;
    String alleleFreq;
    String gene;
    String transcript;
    String phenotype;
    String phenotypeInheritance;
    String phenotypeOnset;
    String phenotypeDetails;
    String phenotypeGroup;
    Map<String, status> sampleStatus;
    Map<String, String> samplePhenotype;
    Map<String, String> sampleGenotype;
    Map<String, String> sampleGroup;
    String variantSignificance;
    String variantSignificanceSource;
    String variantSignificanceJustification;
    String variantCompoundHet;
    String variantGroup;


    public static RVCF fromString(String rvcfEntry) throws Exception {
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
        rvcfInstance.setPhenotypeOnset(split[6]);
        rvcfInstance.setPhenotypeDetails(split[7]);
        rvcfInstance.setPhenotypeGroup(split[8]);

        rvcfInstance.setSampleStatusString(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[9]));
        rvcfInstance.setSamplePhenotype(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[10]));
        rvcfInstance.setSampleGenotype(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[11]));
        rvcfInstance.setSampleGroup(Splitter.on(RVCF_SAMPLESEP).withKeyValueSeparator(":").split(split[12]));

        rvcfInstance.setVariantSignificance(split[13]);
        rvcfInstance.setVariantSignificanceSource(split[14]);
        rvcfInstance.setVariantSignificanceJustification(split[15]);
        rvcfInstance.setVariantCompoundHet(split[16]); //todo: is this necessary?
        rvcfInstance.setVariantGroup(split[17]);

        return rvcfInstance;
    }

    public String escapeToSafeVCF (String in)
    {
        return in.replace(VCF_INFOFIELDSEP, " ").replace(RVCF_FIELDSEP, " ").replace(RVCF.RVCF_SAMPLESEP, " ");
    }

    @Override
    public String toString() {
        return escapeToSafeVCF(getAllele()) + RVCF_FIELDSEP + escapeToSafeVCF(getAlleleFreq()) + RVCF_FIELDSEP + escapeToSafeVCF(getGene()) + RVCF_FIELDSEP + escapeToSafeVCF(getTranscript()) + RVCF_FIELDSEP +
                escapeToSafeVCF(getPhenotype()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeInheritance()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeOnset()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeDetails()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeGroup()) + RVCF_FIELDSEP +
                printSampleStatus(getSampleStatus()) + RVCF_FIELDSEP + printSampleList(getSamplePhenotype()) + RVCF_FIELDSEP + printSampleList(getSampleGenotype(), true) + RVCF_FIELDSEP + printSampleList(getSampleGroup()) + RVCF_FIELDSEP +
                escapeToSafeVCF(getVariantSignificance()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantSignificanceSource()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantSignificanceJustification()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantCompoundHet()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantGroup());
    }

    /** sigh **/
    public String printSampleStatus(Map<String, status> samples){
        Map<String, String> samplesString = new HashMap<>();
        for(String sample : samples.keySet()){
            samplesString.put(sample, samples.get(sample).toString());
        }
        return printSampleList(samplesString);
    }

    private String escapeGenotype(String s) {
        return s.replace("/", "s").replace("|", "p");
    }
    private String unEscapeGenotype(String s) {
        return s.replace("s", "/").replace("p", "|");
    }

    public String printSampleList(Map<String, String> samples){
        return printSampleList(samples, false);

    }
    public String printSampleList(Map<String, String> samples, boolean genotypes){
        if(samples.size() == 0)
        {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(String sample : samples.keySet())
        {
            sb.append(escapeToSafeVCF(sample) + RVCF_KEYVALSEP + (genotypes ? escapeGenotype(samples.get(sample)) : escapeToSafeVCF(samples.get(sample))) + RVCF_SAMPLESEP);
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

    public String getPhenotypeOnset() {
        return phenotypeOnset != null ? phenotypeOnset : "";
    }

    public void setPhenotypeOnset(String phenotypeOnset) {
        this.phenotypeOnset = phenotypeOnset;
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

    public Map<String, String> getSampleGenotype() {
        return sampleGenotype != null ? sampleGenotype : new HashMap<String, String>();
    }

    public void setSampleGenotype(Map<String, String> sampleGenotype) {
        for(String key : sampleGenotype.keySet())
        {
            sampleGenotype.put(key, unEscapeGenotype(sampleGenotype.get(key)));
        }
        this.sampleGenotype = sampleGenotype;
    }

    public Map<String, status> getSampleStatus() {
        return sampleStatus != null ? sampleStatus : new HashMap<String, status>();
    }

    public void setSampleStatusString(Map<String, String> sampleStatus) {
        Map<String, status> res = new HashMap<>();
        for(String sample : sampleStatus.keySet())
        {
            res.put(sample, status.valueOf(sampleStatus.get(sample)));
        }
        this.sampleStatus = res;
    }

    public void setSampleStatus(Map<String, status> sampleStatus) {
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
