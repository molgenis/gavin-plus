package org.molgenis.vcf;

import org.apache.commons.lang.StringUtils;
import org.molgenis.genotype.Allele;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

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
		return vcfRecord.getAlternateAlleles().stream()
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
}
