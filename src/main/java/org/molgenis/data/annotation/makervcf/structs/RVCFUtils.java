package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.*;

public class RVCFUtils
{
	public static final String RVCF_SAMPLESEP = "/";
	public static final String EMPTY_VALUE = ".";//Note: this conflicts with the splitRlvTool missing values are "NA"
	private static final String RVCF_GENEALLELECOMBISEP = ",";
	private static final String RVCF_FIELDSEP = "|";
	private static final String RVCF_KEYVALSEP = ":";
	private static final String VCF_INFOFIELDSEP = ";";

	private RVCFUtils()
	{
	}

	public static String getMergedFieldVcfString(RVCF rvcf)
	{
		return escapeToSafeVCF(rvcf.getAllele())
				+ RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getGene())
				+ RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getVariantSignificance())
				+ RVCF_FIELDSEP + escapeToSafeVCF(rvcf.getVariantSignificanceJustification());
	}

	public static Map<String, String> createRvcfValues(RVCF rvcf, Map<String, String> currentValues)
	{
		Map<String, String> infoFields = new HashMap<>();
		RVCFUtils.addOrUpdateInfoField(RLV_ALLELE,rvcf.getAllele(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_GENE, rvcf.getGene(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCE,rvcf.getVariantSignificance(), currentValues, infoFields);
		RVCFUtils.addOrUpdateInfoField(RLV_VARIANTSIGNIFICANCEJUSTIFICATION,rvcf.getVariantSignificanceJustification(), currentValues, infoFields);
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

	private static void addOrUpdateInfoField(String key, String value, Map<String, String> currentValues,
			Map<String, String> infoFields)
	{
		infoFields.put(key,createInfoField(value, currentValues.get(key)));
	}

	private static String createInfoField(String value, String currentInfoValue)
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
			if(isNullOrEmpty(value))
			{
				value = EMPTY_VALUE;
			}
			newInfoValue = currentInfoValue + "," + escapeToSafeVCF(value);
		}
		return newInfoValue;
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
