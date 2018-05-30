package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Splitter;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;
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

	public static int nrOfFields = 19;
	private static String RVCF_GENEALLELECOMBISEP = ",";
	private static String RVCF_SAMPLESEP = "/";
	private static String RVCF_FIELDSEP = "|";
	private static String RVCF_KEYVALSEP = ":";
	private static String VCF_INFOFIELDSEP = ";";

	public static String attributeName = "RLV";
	public static String attributeMetaData =
			"Allele " + RVCF_FIELDSEP + " AlleleFreq " + RVCF_FIELDSEP + " Gene " + RVCF_FIELDSEP + " FDR "
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
	Map<String, status> sampleStatus;
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

	public static RVCF fromString(String rvcfEntry)
	{
		String[] split = rvcfEntry.split("\\|", -1);
		RVCF rvcfInstance = new RVCF();
		if (split.length != nrOfFields)
		{
			System.out.println("RVCF parsing failed for " + rvcfEntry);
			throw new RuntimeException("Splitting RVCF entry on '|' did not yield " + nrOfFields
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
		rvcfInstance.setSampleStatusString(split[10].isEmpty() ? new HashMap<>() : Splitter.on(RVCF_SAMPLESEP)
																						   .withKeyValueSeparator(":")
																						   .split(split[10]));
		rvcfInstance.setSampleGenotype(split[12].isEmpty() ? new HashMap<>() : Splitter.on(RVCF_SAMPLESEP)
																					   .withKeyValueSeparator(":")
																					   .split(split[12]));

		rvcfInstance.setVariantSignificance(split[14]);
		rvcfInstance.setVariantSignificanceSource(split[15]);
		rvcfInstance.setVariantSignificanceJustification(split[16]);
		rvcfInstance.setVariantMultiGenic(split[17]);
		rvcfInstance.setVariantGroup(split[18]);

		return rvcfInstance;
	}

	public String escapeToSafeVCF(String in)
	{
		return in.replace(VCF_INFOFIELDSEP, "_")
				 .replace(RVCF_FIELDSEP, "_")
				 .replace(RVCF.RVCF_SAMPLESEP, "_")
				 .replace(RVCF_GENEALLELECOMBISEP, "_")
				 .replaceAll("\\s", "_");
	}

	// TODO do not use toString for this but a separate method
	@Override
	public String toString()
	{
		return escapeToSafeVCF(getAllele()) + RVCF_FIELDSEP + escapeToSafeVCF(getAlleleFreq()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(getGene()) + RVCF_FIELDSEP + escapeToSafeVCF(getFDR()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(getTranscript()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotype()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(getPhenotypeInheritance()) + RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeOnset())
				+ RVCF_FIELDSEP + escapeToSafeVCF(getPhenotypeDetails()) + RVCF_FIELDSEP + escapeToSafeVCF(
				getPhenotypeGroup()) + RVCF_FIELDSEP + printSampleStatus(getSampleStatus()) + RVCF_FIELDSEP
				+ printSampleList(getSamplePhenotype()) + RVCF_FIELDSEP + printSampleList(getSampleGenotype(), true)
				+ RVCF_FIELDSEP + printSampleList(getSampleGroup()) + RVCF_FIELDSEP + escapeToSafeVCF(
				getVariantSignificance()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantSignificanceSource())
				+ RVCF_FIELDSEP + escapeToSafeVCF(getVariantSignificanceJustification()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(getVariantMultiGenic()) + RVCF_FIELDSEP + escapeToSafeVCF(getVariantGroup());
	}

	/**
	 * sigh
	 **/
	public String printSampleStatus(Map<String, status> samples)
	{
		Map<String, String> samplesString = new HashMap<>();
		for (String sample : samples.keySet())
		{
			samplesString.put(sample, samples.get(sample).toString());
		}
		return printSampleList(samplesString);
	}

	private String escapeGenotype(String s)
	{
		return s.replace("/", "s").replace("|", "p");
	}

	private String unEscapeGenotype(String s)
	{
		return s.replace("s", "/").replace("p", "|");
	}

	public String printSampleList(Map<String, String> samples)
	{
		return printSampleList(samples, false);

	}

	public String printSampleList(Map<String, String> samples, boolean genotypes)
	{
		if (samples.size() == 0)
		{
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String sample : samples.keySet())
		{
			sb.append(escapeToSafeVCF(sample) + RVCF_KEYVALSEP + (genotypes ? escapeGenotype(
					samples.get(sample)) : escapeToSafeVCF(samples.get(sample))) + RVCF_SAMPLESEP);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
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
		return transcript != null ? transcript : "";
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
		return sampleGenotype != null ? sampleGenotype : new HashMap<String, String>();
	}

	public void setSampleGenotype(Map<String, String> sampleGenotype)
	{
		Map<String, String> sampleGenotypeUnEsc = new HashMap<>();
		for (String key : sampleGenotype.keySet())
		{
			sampleGenotypeUnEsc.put(key, unEscapeGenotype(sampleGenotype.get(key)));
		}
		this.sampleGenotype = sampleGenotypeUnEsc;
	}

	public Map<String, status> getSampleStatus()
	{
		return sampleStatus != null ? sampleStatus : new HashMap<String, status>();
	}

	public void setSampleStatusString(Map<String, String> sampleStatus)
	{
		Map<String, status> res = new HashMap<>();
		for (String sample : sampleStatus.keySet())
		{
			res.put(sample, status.valueOf(sampleStatus.get(sample)));
		}
		this.sampleStatus = res;
	}

	public void setSampleStatus(Map<String, status> sampleStatus)
	{
		this.sampleStatus = sampleStatus;
	}

	public Map<String, String> getSamplePhenotype()
	{
		return samplePhenotype != null ? samplePhenotype : new HashMap<String, String>();
	}

	public void setSamplePhenotype(Map<String, String> samplePhenotype)
	{
		this.samplePhenotype = samplePhenotype;
	}

	public Map<String, String> getSampleGroup()
	{
		return sampleGroup != null ? sampleGroup : new HashMap<String, String>();
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
}
