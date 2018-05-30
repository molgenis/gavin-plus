package org.molgenis.vcf;

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

	// TODO code cleanup
	public static Double[] getAltAlleleOrderedDoubleField(VcfRecord vcfRecord, String fieldName)
	{
		Double[] res = new Double[getAltsAsStringArray(vcfRecord).length];
		if (getInfoStringValue(vcfRecord, fieldName) == null)
		{
			//the entire field is not present
			return res;
		}
		String[] split = getInfoStringValue(vcfRecord, fieldName) == null ? null : getInfoStringValue(vcfRecord,
				fieldName).split(",", -1);
		if (split != null)
		{
			if (split.length != getAltsAsStringArray(vcfRecord).length)
			{
				//TODO JvdV what is happening? loading back RVCF file:
				//Exception in thread "main" java.lang.Exception: CADD_SCALED split length not equal to alt allele split length for record vcf=[#CHROM=1,ALT=TG,C,POS=1116188,REF=CG,FILTER=PASS,QUAL=100.0,ID=rs367560627,INTERNAL_ID=RNWUDmMnfJqUyWdP6mlXlA,INFO={#CHROM_vcf=null,ALT_vcf=null,POS_vcf=null,REF_vcf=null,FILTER_vcf=null,QUAL_vcf=null,ID_vcf=null,INTERNAL_ID_vcf=null,CIEND=null,CIPOS=null,CS=null,END=null,IMPRECISE=false,MC=null,MEINFO=null,MEND=null,MLEN=null,MSTART=null,SVLEN=null,SVTYPE=null,TSD=null,AC=3,13,AF=5.99042E-4,0.00259585,NS=2504,AN=5008,LEN=null,TYPE=null,OLD_VARIANT=null,VT=null,EAS_AF=0.0,0.0129,EUR_AF=0.0,0.0,AFR_AF=0.0023,0.0,AMR_AF=0.0,0.0,SAS_AF=0.0,0.0,DP=6911,AA=null,ANN=C|frameshift_variant|HIGH|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.706delG|p.Ala236fs|857/2259|706/2022|236/673||INFO_REALIGN_3_PRIME,TG|missense_variant|MODERATE|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.703C>T|p.Arg235Trp|854/2259|703/2022|235/673||,LOF=(TTLL10|TTLL10|1|1.00),NMD=null,EXAC_AF=3.148E-4,0.001425,EXAC_AC_HOM=0,1,EXAC_AC_HET=31,145,GoNL_GTC=null,GoNL_AF=null,CADD=3.339984,CADD_SCALED=22.9,RLV=TG|3.148E-4|TTLL10|NM_001130045.1||||||NA19346:HOMOZYGOUS_COMPOUNDHET/NA19454:HETEROZYGOUS/HG03130:HETEROZYGOUS||NA19346:0p1/NA19454:0p1/HG03130:1p0||Predicted pathogenic|GAVIN|Variant MAF of 3.148E-4 is rare enough to be potentially pathogenic and its CADD score of 22.9 is greater than a global threshold of 15.||},SAMPLES_ENTITIES=org.molgenis.data.vcf.format.VcfToEntity$1@7f416310]
				throw new RuntimeException(
						fieldName + " split length " + split.length + " of string '" + getInfoStringValue(vcfRecord,
								fieldName) + "' not equal to alt allele split length " + getAltsAsStringArray(
								vcfRecord).length + " for record " + vcfRecord.toString());
				//   System.out.println("WARNING: fieldName split length not equal to alt allele split length for record " + record.toString());
			}
			for (int i = 0; i < split.length; i++)
			{
				res[i] = (split[i] != null && !split[i].isEmpty() && !split[i].equals(".")) ? Double.parseDouble(
						split[i]) : null;
			}
		}
		else
		{
			throw new RuntimeException(fieldName + " split is null");
		}

		return res;
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
