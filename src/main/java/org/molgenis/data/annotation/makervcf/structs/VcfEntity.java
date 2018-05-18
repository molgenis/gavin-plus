package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.genotype.Allele;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

public class VcfEntity extends VcfRecord
{
	public static final String EXAC_AF = "EXAC_AF";
	public static final String GO_NL_AF = "GoNL_AF";
	public static final String CLSF = "CLSF";
	public static final String ANN = "ANN";
	public static final String RLV = "RLV";
	public static final String CLINVAR = "CLINVAR";

	public VcfEntity(VcfRecord record)
	{
		super(record.getVcfMeta(), record.getTokens());
	}

	private Double[] getExac_AFs()
	{
		return getAltAlleleOrderedDoubleField(EXAC_AF);
	}

	public Double[] getGoNL_AFs()
	{
		return getAltAlleleOrderedDoubleField(GO_NL_AF);
	}

	public double getExac_AFs(int i)
	{
		return getExac_AFs()[i] != null ? getExac_AFs()[i] : 0;
	}

	public double getGoNL_AFs(int i)
	{
		return getGoNL_AFs()[i] != null ? getGoNL_AFs()[i] : 0;
	}

	public String getChrPosRefAlt()
	{
		return getChromosome() + "_" + getPosition() + "_" + this.getRef() + "_" + StringUtils.join(this.getAlts(),
				',');
	}

	public List<RVCF> getRvcfFromVcfInfoField()
	{
		String infoField = GavinUtils.getInfoStringValue(this, RVCF.attributeName);
		if (infoField == null)
		{
			return null;
		}
		String[] split = infoField.split(",");
		ArrayList<RVCF> res = new ArrayList<>();
		for (String s : split)
		{
			try
			{
				res.add(RVCF.fromString(s));
			}
			catch (Exception e)
			{
				System.out.println("RVCF parsing failed for " + s);
				throw new RuntimeException(e);
			}
		}
		return res;
	}

	public String getClsf()
	{
		String clsf = GavinUtils.getInfoStringValue(this, CLSF);
		return clsf != null ? clsf : "";
	}

	public String getId()
	{
		return String.join(",", getIdentifiers());
	}

	private String[] getAltsAsStringArray()
	{
		return getAlternateAlleles().stream()
									.map(Allele::getAlleleAsString)
									.collect(Collectors.toList())
									.toArray(new String[getAlternateAlleles().size()]);
	}

	Double[] getAltAlleleOrderedDoubleField(String fieldName)
	{
		Double[] res = new Double[getAltsAsStringArray().length];
		if (GavinUtils.getInfoStringValue(this, fieldName) == null)
		{
			//the entire field is not present
			return res;
		}
		String[] split =
				GavinUtils.getInfoStringValue(this, fieldName) == null ? null : GavinUtils.getInfoStringValue(this,
						fieldName).split(",", -1);
		if (split != null)
		{
			if (split.length != getAltsAsStringArray().length)
			{
				//todo what is happening? loading back RVCF file:
				//Exception in thread "main" java.lang.Exception: CADD_SCALED split length not equal to alt allele split length for record vcf=[#CHROM=1,ALT=TG,C,POS=1116188,REF=CG,FILTER=PASS,QUAL=100.0,ID=rs367560627,INTERNAL_ID=RNWUDmMnfJqUyWdP6mlXlA,INFO={#CHROM_vcf=null,ALT_vcf=null,POS_vcf=null,REF_vcf=null,FILTER_vcf=null,QUAL_vcf=null,ID_vcf=null,INTERNAL_ID_vcf=null,CIEND=null,CIPOS=null,CS=null,END=null,IMPRECISE=false,MC=null,MEINFO=null,MEND=null,MLEN=null,MSTART=null,SVLEN=null,SVTYPE=null,TSD=null,AC=3,13,AF=5.99042E-4,0.00259585,NS=2504,AN=5008,LEN=null,TYPE=null,OLD_VARIANT=null,VT=null,EAS_AF=0.0,0.0129,EUR_AF=0.0,0.0,AFR_AF=0.0023,0.0,AMR_AF=0.0,0.0,SAS_AF=0.0,0.0,DP=6911,AA=null,ANN=C|frameshift_variant|HIGH|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.706delG|p.Ala236fs|857/2259|706/2022|236/673||INFO_REALIGN_3_PRIME,TG|missense_variant|MODERATE|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.703C>T|p.Arg235Trp|854/2259|703/2022|235/673||,LOF=(TTLL10|TTLL10|1|1.00),NMD=null,EXAC_AF=3.148E-4,0.001425,EXAC_AC_HOM=0,1,EXAC_AC_HET=31,145,GoNL_GTC=null,GoNL_AF=null,CADD=3.339984,CADD_SCALED=22.9,RLV=TG|3.148E-4|TTLL10|NM_001130045.1||||||NA19346:HOMOZYGOUS_COMPOUNDHET/NA19454:HETEROZYGOUS/HG03130:HETEROZYGOUS||NA19346:0p1/NA19454:0p1/HG03130:1p0||Predicted pathogenic|GAVIN|Variant MAF of 3.148E-4 is rare enough to be potentially pathogenic and its CADD score of 22.9 is greater than a global threshold of 15.||},SAMPLES_ENTITIES=org.molgenis.data.vcf.format.VcfToEntity$1@7f416310]
				throw new RuntimeException(
						fieldName + " split length " + split.length + " of string '" + GavinUtils.getInfoStringValue(
								this, fieldName) + "' not equal to alt allele split length "
								+ this.getAltsAsStringArray().length + " for record " + this.toString());
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

	public int getAltIndex(String alt) throws Exception
	{
		for (int i = 0; i < getAltsAsStringArray().length; i++)
		{
			if (alt.equals(getAltsAsStringArray()[i]))
			{
				return i + 1;
			}
		}
		throw new Exception("alt not found");
	}

	public Impact getImpact(int i, String gene) throws Exception
	{
		return GavinUtils.getImpact(GavinUtils.getInfoStringValue(this, ANN), gene, this.getAltsAsStringArray()[i]);
	}

	public String getTranscript(int i, String gene) throws Exception
	{
		return GavinUtils.getTranscript(GavinUtils.getInfoStringValue(this, ANN), gene,
				this.getAltsAsStringArray()[i]);
	}

	/**
	 * Returns information without RVCF information
	 */
	public Iterable<VcfInfo> getVcfEntityInformation()
	{
		List<RVCF> rvcf;
		rvcf = getRvcfFromVcfInfoField();

		Iterable<VcfInfo> rvcfInformation;
		if (rvcf == null)
		{
			rvcfInformation = super.getInformation();
		}
		else
		{
			String key = RLV;
			String value = rvcf.stream().map(RVCF::toString).collect(Collectors.joining(","));
			VcfInfo rlvVcfInfo = new VcfInfo(getVcfMeta(), key, value);
			rvcfInformation = Iterables.concat(super.getInformation(), singleton(rlvVcfInfo));
		}
		return rvcfInformation;
	}

	public String getRef()
	{
		return getReferenceAllele().getAlleleAsString();
	}

	public String getAltString()
	{
		StringBuilder sb = new StringBuilder();
		for (String alt : this.getAltsAsStringArray())
		{
			sb.append(alt).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String getAlt(int i)
	{
		return this.getAltsAsStringArray()[i];
	}

	public String[] getAlts()
	{
		return this.getAltsAsStringArray();
	}

	public String getAlt() throws RuntimeException
	{
		if (getAltsAsStringArray().length > 1)
		{
			throw new RuntimeException("more than 1 alt ! " + this.toString());
		}
		return getAltsAsStringArray()[0];
	}

	public int getAltAlleleIndex(String alt)
	{
		return Arrays.asList(this.getAlts()).indexOf(alt) + 1;
	}

	public String getClinvar()
	{
		return GavinUtils.getInfoStringValue(this, CLINVAR);
	}
}
