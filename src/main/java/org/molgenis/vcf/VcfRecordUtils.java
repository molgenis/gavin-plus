package org.molgenis.vcf;

import joptsimple.internal.Strings;
import org.apache.commons.lang.StringUtils;
import org.molgenis.genotype.Allele;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class VcfRecordUtils
{
	private VcfRecordUtils()
	{
	}

	public static Optional<VcfInfo> getInformation(String key, VcfRecord vcfRecord)
	{
		for (VcfInfo vcfInfo : vcfRecord.getInformation())
		{
			if (vcfInfo.getKey().equals(key))
			{
				return Optional.of(vcfInfo);
			}
		}
		return Optional.empty();
	}

	public static String getChrPosRefAlt(VcfRecord vcfRecord)
	{
		return vcfRecord.getChromosome() + "_" + vcfRecord.getPosition() + "_" + getRef(vcfRecord) + "_"
				+ StringUtils.join(VcfRecordUtils.getAlts(vcfRecord), ',');
	}

	public static String getRef(VcfRecord vcfRecord)
	{
		return vcfRecord.getReferenceAllele().getAlleleAsString();
	}

	public static String[] getAltsAsStringArray(VcfRecord vcfRecord)
	{
		return vcfRecord.getAlternateAlleles()
						.stream()
						.map(Allele::getAlleleAsString)
						.collect(Collectors.toList())
						.toArray(new String[vcfRecord.getAlternateAlleles().size()]);
	}

	public static String getAltString(VcfRecord vcfRecord)
	{
		StringBuilder sb = new StringBuilder();
		for (String alt : getAltsAsStringArray(vcfRecord))
		{
			sb.append(alt).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static String getAlt(VcfRecord vcfRecord, int i)
	{
		return getAltsAsStringArray(vcfRecord)[i];
	}

	public static String[] getAlts(VcfRecord vcfRecord)
	{
		return getAltsAsStringArray(vcfRecord);
	}

	public static String getAlt(VcfRecord vcfRecord) throws RuntimeException
	{
		if (getAltsAsStringArray(vcfRecord).length > 1)
		{
			throw new RuntimeException("more than 1 alt ! " + vcfRecord.toString());
		}
		return getAltsAsStringArray(vcfRecord)[0];
	}

	public static int getAltAlleleIndex(VcfRecord vcfRecord, String alt)
	{
		return Arrays.asList(getAlts(vcfRecord)).indexOf(alt) + 1;
	}

	public static int getAltIndex(VcfRecord vcfRecord, String alt) throws Exception
	{
		for (int i = 0; i < getAltsAsStringArray(vcfRecord).length; i++)
		{
			if (alt.equals(getAltsAsStringArray(vcfRecord)[i]))
			{
				return i + 1;
			}
		}
		throw new Exception("alt not found");
	}

	public static String getId(VcfRecord vcfRecord)
	{
		return String.join(",", vcfRecord.getIdentifiers());
	}

	@Nullable
	public static String getSampleFieldValue(VcfRecord vcfEntity, VcfSample sample, String field)
	{
		String[] format = vcfEntity.getFormat();
		for (int i = 0; i < format.length; i++)
		{
			if (format[i].equals(field))
			{
				return sample.getData(i);
			}
		}
		return null;
	}

	/**
	 * @param vcfRecord
	 * @param fieldName
	 * @return an array of double values besed in the comma separated String of the info field
	 */
	public static Double[] getAltAlleleOrderedDoubleField(VcfRecord vcfRecord, String fieldName)
	{
		int nrOfAlts = getAltsAsStringArray(vcfRecord).length;
		String fieldValue = getInfoStringValue(vcfRecord, fieldName);

		Double[] result = new Double[nrOfAlts];

		if (fieldValue != null)
		{
			String[] split = fieldValue.split(",", -1);
			if (split != null)
			{
				if (split.length != nrOfAlts)
				{
					throw new RuntimeException(String.format("Split length %s of string '%s' not equal to alt allele split length %s for record '%s'",fieldName,split.length,fieldValue,nrOfAlts, vcfRecord.toString()));
				}
				for (int i = 0; i < split.length; i++)
				{
					result[i] = isValuePresent(split, i) ? Double.parseDouble(split[i]) : null;
				}
			}
			else
			{
				throw new RuntimeException(fieldName + " split is null");
			}
		}
		return result;
	}

	private static boolean isValuePresent(String[] split, int index)
	{
		String value = split[index];
		return !Strings.isNullOrEmpty(value) && !value.equals(".");
	}

	private static String getInfoStringValue(VcfRecord record, String infoField)
	{
		String result = null;
		Iterable<VcfInfo> infoFields = record.getInformation();
		for (VcfInfo info : infoFields)
		{
			if (info.getKey().equals(infoField))
			{
				Object vcfInfoVal = info.getVal();
				if (vcfInfoVal instanceof List<?>)
				{
					List<?> vcfInfoValTokens = (List<?>) vcfInfoVal;
					result = vcfInfoValTokens.stream()
											 .map(vcfInfoValToken ->
													 vcfInfoValToken != null ? vcfInfoValToken.toString() : ".")
											 .collect(joining(","));
				}
				else
				{
					result = vcfInfoVal.toString();
				}
			}
		}
		return result;
	}
}
