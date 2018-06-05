package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Strings;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.*;

public class RVCFUtils
{
	private static String RVCF_GENEALLELECOMBISEP = ",";
	public static String RVCF_SAMPLESEP = "/";
	private static String RVCF_FIELDSEP = "|";
	private static String RVCF_KEYVALSEP = ":";
	private static String VCF_INFOFIELDSEP = ";";
	public static final String EMPTY_VALUE = ".";

	public static String getMergedFieldVcfString(RVCF rvcf)
	{
		return FIELD_NAME + "=" + escapeToSafeVCF(rvcf.getAllele()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getAlleleFreq()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(rvcf.getGene()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getFDR()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(rvcf.getTranscript()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getPhenotype()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(rvcf.getPhenotypeInheritance()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getPhenotypeOnset())
				+ RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getPhenotypeDetails()) + RVCF_FIELDSEP + escapeToSafeVCF(
				rvcf.getPhenotypeGroup()) + RVCF_FIELDSEP + printSampleStatus(rvcf.getSampleStatus()) + RVCF_FIELDSEP
				+ printSampleList(rvcf.getSamplePhenotype()) + RVCF_FIELDSEP + printSampleList(rvcf.getSampleGenotype(), true)
				+ RVCF_FIELDSEP + printSampleList(rvcf.getSampleGroup()) + RVCF_FIELDSEP + escapeToSafeVCF(
				rvcf.getVariantSignificance()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getVariantSignificanceSource())
				+ RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getVariantSignificanceJustification()) + RVCF_FIELDSEP
				+ escapeToSafeVCF(rvcf.getVariantMultiGenic()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getVariantGroup());
	}

	public static Map<String, String> createRvcfInfoFields(RVCF rvcf, Map<String, String> currentValues)
	{
		Map<String, String> infoFields = new HashMap<>();
		RVCFUtils.addOrUpdateInfoField(RLV_PRESENT, "TRUE", currentValues, infoFields);//TODO: why is this not a "FLAG"
		RVCFUtils.addOrUpdateInfoField(RLV_ALLELE,rvcf.getAllele(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_ALLELEFREQ,rvcf.getAlleleFreq(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_GENE, rvcf.getGene(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_FDR,rvcf.getFDR(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_TRANSCRIPT,rvcf.getTranscript(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPE,rvcf.getPhenotype(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEINHERITANCE,rvcf.getPhenotypeInheritance(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEONSET,rvcf.getPhenotypeOnset(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEDETAILS,rvcf.getPhenotypeDetails(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEGROUP,rvcf.getPhenotypeGroup(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLESTATUS, RVCFUtils.printSampleStatus(rvcf.getSampleStatus()), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLEPHENOTYPE, RVCFUtils.printSampleList(rvcf.getSamplePhenotype()), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLEGENOTYPE, RVCFUtils.printSampleList(rvcf.getSampleGenotype()), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLEGROUP, RVCFUtils.printSampleList(rvcf.getSampleGroup()), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCE,rvcf.getVariantSignificance(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCESOURCE,rvcf.getVariantSignificanceSource(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCEJUSTIFICATION,rvcf.getVariantSignificanceJustification(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTCOMPOUNDHET,rvcf.getVariantMultiGenic(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTGROUP,rvcf.getVariantGroup(), currentValues, infoFields);
		return infoFields;
	}

	public static String escapeToSafeVCF(String in)
	{
		return in.replace(VCF_INFOFIELDSEP, "_")
				 .replace(RVCF_FIELDSEP, "_")
				 .replace(RVCF_SAMPLESEP, "_")
				 .replace(RVCF_GENEALLELECOMBISEP, "_")
				 .replaceAll("\\s", "_");
	}

	private static String addOrUpdateInfoField(String key, String value, Map<String, String> currentValues,
			Map<String, String> infoFields)
	{
		return infoFields.put(key,createInfoField(key, value, currentValues.get(key)));
	}

	private static String createInfoField(String key, String value, String currentInfoValue)
	{
		String newInfoValue = "";
		if(isNullOrEmpty(currentInfoValue)){
			if(Strings.isNullOrEmpty(value)){
				newInfoValue = EMPTY_VALUE;
			}else
			{
				newInfoValue = escapeToSafeVCF(value);
			}
		}else{
			if(value == null)
			{
				value = "";
			}
			if(currentInfoValue.equals(EMPTY_VALUE)){
				newInfoValue = "," + escapeToSafeVCF(value);
			}else{
				newInfoValue = currentInfoValue + "," + escapeToSafeVCF(value);
			}
		}
		return key + "=" + newInfoValue;
	}

	private static String printSampleStatus(Map<String, MatchVariantsToGenotypeAndInheritance.Status> samples)
	{
		Map<String, String> samplesString = new HashMap<>();
		for (String sample : samples.keySet())
		{
			samplesString.put(sample, samples.get(sample).toString());
		}
		return printSampleList(samplesString);
	}

	private static String escapeGenotype(String s)
	{
		return s.replace("/", "s").replace("|", "p");
	}

	public static String unEscapeGenotype(String s)
	{
		return s.replace("s", "/").replace("p", "|");
	}

	private static String printSampleList(Map<String, String> samples)
	{
		return printSampleList(samples, false);

	}

	private static String printSampleList(Map<String, String> samples, boolean genotypes)
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
}
