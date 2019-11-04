package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification;
import org.molgenis.vcf.VcfInfo;
import java.util.List;
import static java.util.stream.Collectors.toList;

public class RVCF
{
	public static final String RLV_ALLELE = "RLV_ALLELE";
	public static final String RLV_GENE = "RLV_GENE";
	public static final String RLV_VARIANTSIGNIFICANCE = "RLV_VARIANTSIGNIFICANCE";
	public static final String RLV_VARIANTSIGNIFICANCEJUSTIFICATION = "RLV_VARIANTSIGNIFICANCEJUSTIFICATION";
	public static final String FIELD_NAME = "RLV";

	public static final int NR_OF_FIELDS = 4;
	private static final String RVCF_FIELDSEP = "|";
	public static final String DESCRIPTION =
			"Allele "
					+ RVCF_FIELDSEP + " Gene "
					+ RVCF_FIELDSEP + " VariantSignificance "
					+ RVCF_FIELDSEP + " VariantSignificanceJustification ";

	String allele;
	String gene;
	Classification variantSignificance;
	String variantSignificanceJustification;

	public static List<RVCF> fromVcfInfo(VcfInfo vcfInfo)
	{
		Object obj = vcfInfo.getVal();
		if (!(obj instanceof List))
		{
			throw new RuntimeException(
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

	public RVCF(String allele, String gene, Classification variantSignificance,
			String variantSignificanceJustification)
	{
		this.allele = allele;
		this.gene = gene;
		this.variantSignificance = variantSignificance;
		this.variantSignificanceJustification = variantSignificanceJustification;
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
		rvcfInstance.setGene(split[2]);

		rvcfInstance.setVariantSignificance(Classification.valueOf(split[14]));
		rvcfInstance.setVariantSignificanceJustification(split[16]);

		return rvcfInstance;
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

	public Classification getVariantSignificance()
	{
		return variantSignificance;
	}

	public void setVariantSignificance(Classification variantSignificance)
	{
		this.variantSignificance = variantSignificance;
	}

	public String getVariantSignificanceJustification()
	{
		return variantSignificanceJustification != null ? variantSignificanceJustification : "";
	}

	public void setVariantSignificanceJustification(String variantSignificanceJustification)
	{
		this.variantSignificanceJustification = variantSignificanceJustification;
	}
}
