package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Splitter;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.Status;
import org.molgenis.vcf.VcfInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by joeri on 6/1/16.
 */
public class RVCF
{
	public static final String RLV_PRESENT = "RLV_PRESENT";
	public static final String RLV_ALLELE = "RLV_ALLELE";
	public static final String RLV_ALLELEFREQ = "RLV_ALLELEFREQ";
	public static final String RLV_GENE = "RLV_GENE";
	public static final String RLV_FDR = "RLV_FDR";
	public static final String RLV_TRANSCRIPT = "RLV_TRANSCRIPT";
	public static final String RLV_PHENOTYPE = "RLV_PHENOTYPE";
	public static final String RLV_PHENOTYPEINHERITANCE = "RLV_PHENOTYPEINHERITANCE";
	public static final String RLV_PHENOTYPEONSET = "RLV_PHENOTYPEONSET";
	public static final String RLV_PHENOTYPEDETAILS = "RLV_PHENOTYPEDETAILS";
	public static final String RLV_PHENOTYPEGROUP = "RLV_PHENOTYPEGROUP";
	public static final String RLV_SAMPLESTATUS = "RLV_SAMPLESTATUS";
	public static final String RLV_SAMPLEPHENOTYPE = "RLV_SAMPLEPHENOTYPE";
	public static final String RLV_SAMPLEGENOTYPE = "RLV_SAMPLEGENOTYPE";
	public static final String RLV_SAMPLEGROUP = "RLV_SAMPLEGROUP";
	public static final String RLV_VARIANTSIGNIFICANCE = "RLV_VARIANTSIGNIFICANCE";
	public static final String RLV_VARIANTSIGNIFICANCESOURCE = "RLV_VARIANTSIGNIFICANCESOURCE";
	public static final String RLV_VARIANTSIGNIFICANCEJUSTIFICATION = "RLV_VARIANTSIGNIFICANCEJUSTIFICATION";
	public static final String RLV_VARIANTMULTIGENIC = "RLV_VARIANTMULTIGENIC";
	public static final String RLV_VARIANTGROUP = "RLV_VARIANTGROUP";
	public static final String FIELD_NAME = "RLV";

	public static final int NR_OF_FIELDS = 19;
	private static final String RVCF_FIELDSEP = "|";
	public static final String DESCRIPTION = "Allele " + RVCF_FIELDSEP + " AlleleFreq " + RVCF_FIELDSEP + " Gene " + RVCF_FIELDSEP + " FDR "
				+ RVCF_FIELDSEP + " Transcript " + RVCF_FIELDSEP + " Phenotype " + RVCF_FIELDSEP
				+ " PhenotypeInheritance " + RVCF_FIELDSEP + " PhenotypeOnset " + RVCF_FIELDSEP
				+ " PhenotypeDetails " + RVCF_FIELDSEP + " PhenotypeGroup " + RVCF_FIELDSEP + " SampleStatus "
				+ RVCF_FIELDSEP + " SamplePhenotype " + RVCF_FIELDSEP + " SampleGenotype " + RVCF_FIELDSEP
				+ " SampleGroup " + RVCF_FIELDSEP + " VariantSignificance " + RVCF_FIELDSEP
				+ " VariantSignificanceSource " + RVCF_FIELDSEP + " VariantSignificanceJustification "
				+ RVCF_FIELDSEP + " VariantMultiGenic " + RVCF_FIELDSEP + " VariantGroup";

	String allele;
	String alleleFreq;
	String gene;
	String FDR;
	String transcript;
	String phenotype;
	String phenotypeInheritance;
	String phenotypeOnset;
	String phenotypeDetails;
	String phenotypeGroup;
	Map<String, Status> sampleStatus;
	Map<String, String> samplePhenotype;
	Map<String, String> sampleGenotype;
	Map<String, String> sampleGroup;
	String variantSignificance;
	String variantSignificanceSource;
	String variantSignificanceJustification;
	String variantMultiGenic;
	String variantGroup;

	public static List<RVCF> fromVcfInfo(VcfInfo vcfInfo)
	{
		Object obj = vcfInfo.getVal();
		if (!(obj instanceof List))
		{
			throw new AnnotatedVcfParseException(
					String.format("Error parsing RLV info field value '%s'. Value is not a list of strings.",
							vcfInfo.getValRaw()));
		}
		@SuppressWarnings("unchecked")
		List<String> rlvValue = (List<String>) obj;
		return rlvValue.stream().map(RVCF::fromString).collect(toList());
	}

	public RVCF(){
		//empty constructor used in the RlvInfoMapper and this classes' "fromString()" method
	}

	public RVCF(String allele, String alleleFreq, String gene, String FDR, String transcript, String phenotype,
			String phenotypeInheritance, String phenotypeOnset, String phenotypeDetails, String phenotypeGroup,
			Map<String, Status> sampleStatus, Map<String, String> samplePhenotype, Map<String, String> sampleGenotype,
			Map<String, String> sampleGroup, String variantSignificance, String variantSignificanceSource,
			String variantSignificanceJustification, String variantMultiGenic, String variantGroup)
	{
		this.allele = allele;
		this.alleleFreq = alleleFreq;
		this.gene = gene;
		this.FDR = FDR;
		this.transcript = transcript;
		this.phenotype = phenotype;
		this.phenotypeInheritance = phenotypeInheritance;
		this.phenotypeOnset = phenotypeOnset;
		this.phenotypeDetails = phenotypeDetails;
		this.phenotypeGroup = phenotypeGroup;
		this.sampleStatus = sampleStatus;
		this.samplePhenotype = samplePhenotype;
		this.sampleGenotype = sampleGenotype;
		this.sampleGroup = sampleGroup;
		this.variantSignificance = variantSignificance;
		this.variantSignificanceSource = variantSignificanceSource;
		this.variantSignificanceJustification = variantSignificanceJustification;
		this.variantMultiGenic = variantMultiGenic;
		this.variantGroup = variantGroup;
	}

	public static RVCF fromString(String rvcfEntry)
	{
		String[] split = rvcfEntry.split("\\|", -1);
		RVCF rvcfInstance = new RVCF();
		if (split.length != NR_OF_FIELDS)
		{
			System.out.println("RVCF parsing failed for " + rvcfEntry);
			throw new RuntimeException("Splitting RVCF entry on '|' did not yield " + NR_OF_FIELDS
					+ " fields, invalid format? tried to split: " + rvcfEntry + " but had " + split.length + " fields");
		}
		rvcfInstance.setAllele(split[0]);
		rvcfInstance.setAlleleFreq(split[1]);
		rvcfInstance.setGene(split[2]);
		rvcfInstance.setFDR(split[3]);
		rvcfInstance.setTranscript(split[4]);

		rvcfInstance.setPhenotype(split[5]);
		rvcfInstance.setPhenotypeInheritance(split[6]);
		rvcfInstance.setPhenotypeOnset(split[7]);
		rvcfInstance.setPhenotypeDetails(split[8]);
		rvcfInstance.setPhenotypeGroup(split[9]);

		// 'no-sample' variants where at the same site, another variant does have samples, add empty lists
		// example: 1	45795040	rs147923905	C	A,G
		rvcfInstance.setSampleStatusString(split[10].isEmpty() ? new HashMap<>() : Splitter.on(RVCFUtils.RVCF_SAMPLESEP)
																						   .withKeyValueSeparator(":")
																						   .split(split[10]));
		rvcfInstance.setSampleGenotype(split[12].isEmpty() ? new HashMap<>() : Splitter.on(RVCFUtils.RVCF_SAMPLESEP)
																					   .withKeyValueSeparator(":")
																					   .split(split[12]));

		rvcfInstance.setVariantSignificance(split[14]);
		rvcfInstance.setVariantSignificanceSource(split[15]);
		rvcfInstance.setVariantSignificanceJustification(split[16]);
		rvcfInstance.setVariantMultiGenic(split[17]);
		rvcfInstance.setVariantGroup(split[18]);

		return rvcfInstance;
	}

	public String getFDR()
	{
		return FDR != null ? FDR : "";
	}

	public void setFDR(String FDR)
	{
		this.FDR = FDR;
	}

	public String getAlleleFreq()
	{
		return alleleFreq != null ? alleleFreq : "";
	}

	public void setAlleleFreq(String alleleFreq)
	{
		this.alleleFreq = alleleFreq;
	}

	public String getPhenotypeOnset()
	{
		return phenotypeOnset != null ? phenotypeOnset : "";
	}

	public void setPhenotypeOnset(String phenotypeOnset)
	{
		this.phenotypeOnset = phenotypeOnset;
	}

	public String getAllele()
	{
		return allele != null ? allele : "";
	}

	public void setAllele(String allele)
	{
		this.allele = allele;
	}

	public String getGene()
	{
		return gene != null ? gene : "";
	}

	public void setGene(String gene)
	{
		this.gene = gene;
	}

	public String getTranscript()
	{
		return transcript;
	}

	public void setTranscript(String transcript)
	{
		this.transcript = transcript;
	}

	public String getPhenotype()
	{
		return phenotype != null ? phenotype : "";
	}

	public void setPhenotype(String phenotype)
	{
		this.phenotype = phenotype;
	}

	public String getPhenotypeInheritance()
	{
		return phenotypeInheritance != null ? phenotypeInheritance : "";
	}

	public void setPhenotypeInheritance(String phenotypeInheritance)
	{
		this.phenotypeInheritance = phenotypeInheritance;
	}

	public String getPhenotypeDetails()
	{
		return phenotypeDetails != null ? phenotypeDetails : "";
	}

	public void setPhenotypeDetails(String phenotypeDetails)
	{
		this.phenotypeDetails = phenotypeDetails;
	}

	public String getPhenotypeGroup()
	{
		return phenotypeGroup != null ? phenotypeGroup : "";
	}

	public void setPhenotypeGroup(String phenotypeGroup)
	{
		this.phenotypeGroup = phenotypeGroup;
	}

	public Map<String, String> getSampleGenotype()
	{
		return sampleGenotype != null ? sampleGenotype : new HashMap<>();
	}

	public void setSampleGenotype(Map<String, String> sampleGenotype)
	{
		Map<String, String> sampleGenotypeUnEsc = new HashMap<>();
		for (Map.Entry<String, String> entry : sampleGenotype.entrySet())
		{
			sampleGenotypeUnEsc.put(entry.getKey(), RVCFUtils.unEscapeGenotype(entry.getValue()));
		}
		this.sampleGenotype = sampleGenotypeUnEsc;
	}

	public Map<String, Status> getSampleStatus()
	{
		return sampleStatus != null ? sampleStatus : new HashMap<>();
	}

	public void setSampleStatusString(Map<String, String> sampleStatus)
	{
		Map<String, MatchVariantsToGenotypeAndInheritance.Status> res = new HashMap<>();
		for (Map.Entry<String, String> sampleStatusEntry : sampleStatus.entrySet())
		{
			res.put(sampleStatusEntry.getKey(), MatchVariantsToGenotypeAndInheritance.Status.valueOf(sampleStatusEntry.getValue()));
		}
		this.sampleStatus = res;
	}

	public void setSampleStatus(Map<String, Status> sampleStatus)
	{
		this.sampleStatus = sampleStatus;
	}

	public Map<String, String> getSamplePhenotype()
	{
		return samplePhenotype != null ? samplePhenotype : new HashMap<>();
	}

	public void setSamplePhenotype(Map<String, String> samplePhenotype)
	{
		this.samplePhenotype = samplePhenotype;
	}

	public Map<String, String> getSampleGroup()
	{
		return sampleGroup != null ? sampleGroup : new HashMap<>();
	}

	public void setSampleGroup(Map<String, String> sampleGroup)
	{
		this.sampleGroup = sampleGroup;
	}

	public String getVariantSignificance()
	{
		return variantSignificance != null ? variantSignificance : "";
	}

	public void setVariantSignificance(String variantSignificance)
	{
		this.variantSignificance = variantSignificance;
	}

	public String getVariantSignificanceSource()
	{
		return variantSignificanceSource != null ? variantSignificanceSource : "";
	}

	public void setVariantSignificanceSource(String variantSignificanceSource)
	{
		this.variantSignificanceSource = variantSignificanceSource;
	}

	public String getVariantSignificanceJustification()
	{
		return variantSignificanceJustification != null ? variantSignificanceJustification : "";
	}

	public void setVariantSignificanceJustification(String variantSignificanceJustification)
	{
		this.variantSignificanceJustification = variantSignificanceJustification;
	}

	public String getVariantMultiGenic()
	{
		return variantMultiGenic != null ? variantMultiGenic : "";
	}

	public void setVariantMultiGenic(String variantMultiGenic)
	{
		this.variantMultiGenic = variantMultiGenic;
	}

	public String getVariantGroup()
	{
		return variantGroup != null ? variantGroup : "";
	}

	public void setVariantGroup(String variantGroup)
	{
		this.variantGroup = variantGroup;
	}

	@Override
	public String toString()
	{
		return "RVCF{" + "allele='" + allele + '\'' + ", alleleFreq='" + alleleFreq + '\'' + ", gene='" + gene + '\''
				+ ", FDR='" + FDR + '\'' + ", transcript='" + transcript + '\'' + ", phenotype='" + phenotype + '\''
				+ ", phenotypeInheritance='" + phenotypeInheritance + '\'' + ", phenotypeOnset='" + phenotypeOnset
				+ '\'' + ", phenotypeDetails='" + phenotypeDetails + '\'' + ", phenotypeGroup='" + phenotypeGroup + '\''
				+ ", sampleStatus=" + sampleStatus + ", samplePhenotype=" + samplePhenotype + ", sampleGenotype="
				+ sampleGenotype + ", sampleGroup=" + sampleGroup + ", variantSignificance='" + variantSignificance
				+ '\'' + ", variantSignificanceSource='" + variantSignificanceSource + '\''
				+ ", variantSignificanceJustification='" + variantSignificanceJustification + '\''
				+ ", variantMultiGenic='" + variantMultiGenic + '\'' + ", variantGroup='" + variantGroup + '\'' + '}';
	}
}
