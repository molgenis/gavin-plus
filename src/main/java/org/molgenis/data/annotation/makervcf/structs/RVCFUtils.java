package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Strings;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.*;

public class RVCFUtils
{
	public static final String RVCF_SAMPLESEP = "/";
	public static final String EMPTY_VALUE = ".";
	private static final String RVCF_GENEALLELECOMBISEP = ",";
	private static final String RVCF_FIELDSEP = "|";
	private static final String RVCF_KEYVALSEP = ":";
	private static final String VCF_INFOFIELDSEP = ";";

	private RVCFUtils()
	{
	}

	public static String getMergedFieldVcfString(RVCF rvcf)
	{
		return escapeToSafeVCF(rvcf.getAllele()) + RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getAlleleFreq()) + RVCF_FIELDSEP
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
		String prefix = "["+rvcf.getAllele() + "|" +rvcf.getGene()+"]";
		Map<String, String> infoFields = new HashMap<>();
		RVCFUtils.addOrUpdateInfoField(RLV_PRESENT, "TRUE", prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_ALLELE,rvcf.getAllele(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_ALLELEFREQ,rvcf.getAlleleFreq(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_GENE, rvcf.getGene(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_FDR,rvcf.getFDR(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_TRANSCRIPT,rvcf.getTranscript(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPE,rvcf.getPhenotype(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEINHERITANCE,rvcf.getPhenotypeInheritance(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEONSET,rvcf.getPhenotypeOnset(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEDETAILS,rvcf.getPhenotypeDetails(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_PHENOTYPEGROUP,rvcf.getPhenotypeGroup(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLESTATUS, RVCFUtils.printSampleStatus(rvcf.getSampleStatus()), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLEPHENOTYPE, RVCFUtils.printSampleList(rvcf.getSamplePhenotype()), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLEGENOTYPE, RVCFUtils.printSampleList(rvcf.getSampleGenotype()), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_SAMPLEGROUP, RVCFUtils.printSampleList(rvcf.getSampleGroup()), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCE,rvcf.getVariantSignificance(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCESOURCE,rvcf.getVariantSignificanceSource(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCEJUSTIFICATION,rvcf.getVariantSignificanceJustification(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTCOMPOUNDHET,rvcf.getVariantMultiGenic(), prefix,currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTGROUP,rvcf.getVariantGroup(), prefix,currentValues, infoFields);
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

	private static String addOrUpdateInfoField(String key, String value, String prefix, Map<String, String> currentValues,
			Map<String, String> infoFields)
	{
		return infoFields.put(key,createInfoField(key, value, prefix, currentValues.get(key)));
	}

	private static String createInfoField(String key, String value, String prefix, String currentInfoValue)
	{
		String newInfoValue;
		if(isNullOrEmpty(currentInfoValue)){
			if(Strings.isNullOrEmpty(value)){
				newInfoValue = EMPTY_VALUE;
			}else
			{
				newInfoValue = prefix + escapeToSafeVCF(value);
			}
		}else{
			if(value == null)
			{
				value = "";
			}
			if(currentInfoValue.equals(EMPTY_VALUE)){
				newInfoValue = "," + prefix + escapeToSafeVCF(value);
			}else{
				newInfoValue = currentInfoValue + "," + prefix + escapeToSafeVCF(value);
			}
		}
		return key + "=" + newInfoValue;
	}

	private static String printSampleStatus(Map<String, MatchVariantsToGenotypeAndInheritance.Status> samples)
	{
		Map<String, String> samplesString = new HashMap<>();
		for (Map.Entry<String, MatchVariantsToGenotypeAndInheritance.Status> sample : samples.entrySet())
		{
			samplesString.put(sample.getKey(), sample.getValue().toString());
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
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> sample : samples.entrySet())
		{
			sb.append(escapeToSafeVCF(sample.getKey()))
			  .append(RVCF_KEYVALSEP)
			  .append(genotypes ? escapeGenotype(sample.getValue()) : escapeToSafeVCF(sample.getValue()))
			  .append(RVCF_SAMPLESEP);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
