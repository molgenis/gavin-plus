package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * {@link VcfRecord} annotated with SnpEff, CADD (as much as possible), ExAC, GoNL and 1000G.
 */
public class AnnotatedVcfRecord extends VcfRecord
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatedVcfRecord.class);

	private static final String EXAC_AF = "EXAC_AF";
	private static final String GO_NL_AF = "GoNL_AF";
	private static final String CLSF = "CLSF";
	private static final String ANN = "ANN";
	private static final String RLV = "RLV";
	private static final String CLINVAR = "CLINVAR";
	private static final String CADD_SCALED = "CADD_SCALED";

	public AnnotatedVcfRecord(VcfRecord record)
	{
		super(record.getVcfMeta(), record.getTokens());
	}

	double getExAcAlleleFrequencies(int i)
	{
		Double[] alleleFrequencies = VcfRecordUtils.getAltAlleleOrderedDoubleField(this, EXAC_AF);
		return alleleFrequencies[i] != null ? alleleFrequencies[i] : 0;
	}

	double getGoNlAlleleFrequencies(int i)
	{
		Double[] alleleFrequencies = VcfRecordUtils.getAltAlleleOrderedDoubleField(this, GO_NL_AF);
		return alleleFrequencies[i] != null ? alleleFrequencies[i] : 0;
	}

	// TODO return Optional.empty() instead of empty String
	public String getClsf()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(CLSF, this);
		return optionalVcfInfo.map(vcfInfo -> (String) vcfInfo.getVal()).orElse("");
	}

	Set<String> getGenesFromAnn()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(ANN, this);
		return optionalVcfInfo.map(vcfInfo ->
		{
			String ann = vcfInfo.getValRaw();
			Set<String> genes = new HashSet<>();
			String[] annSplit = ann.split(",", -1);
			for (String oneAnn : annSplit)
			{
				String[] fields = oneAnn.split("\\|", -1);
				String gene = fields[3];
				genes.add(gene);
			}
			return genes;

		}).orElse(emptySet());
	}

	// TODO return Optional<Impact> instead of Impact
	@Nullable
	Impact getImpact(int i, String gene)
	{
		String allele = VcfRecordUtils.getAltsAsStringArray(this)[i];
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(ANN, this);
		return optionalVcfInfo.map(vcfInfo -> getImpact(vcfInfo.getValRaw(), gene, allele)).orElse(null);
	}

	// TODO return Optional<String> instead of String
	@Nullable
	String getTranscript(int i, String gene)
	{
		String allele = VcfRecordUtils.getAltsAsStringArray(this)[i];
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(ANN, this);
		return optionalVcfInfo.map(vcfInfo -> getTranscript(vcfInfo.getValRaw(), gene, allele)).orElse(null);
	}

	// TODO return empty list instead of null
	@Nullable
	public List<RVCF> getRvcf()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(RLV, this);
		return optionalVcfInfo.map(RVCF::fromVcfInfo).orElse(null);
	}

	/**
	 * @return phred scores (can contain null values)
	 */
	Double[] getCaddPhredScores()
	{
		return VcfRecordUtils.getAltAlleleOrderedDoubleField(this, CADD_SCALED);
	}

	@Nullable
	public String getClinvar()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(CLINVAR, this);
		return optionalVcfInfo.map(vcfInfo -> (String) vcfInfo.getVal()).orElse(null);
	}

	private static Impact getImpact(String ann, String gene, String allele)
	{
		//get the right annotation entry that matches both gene and allele
		String findAnn = getAnn(ann, gene, allele);
		if (findAnn == null)
		{
			LOG.warn("failed to get impact for gene '{}', allele '{}' in {}", gene, allele, ann);
			return null;
		}
		else
		{
			//from the right one, get the impact
			String[] fields = findAnn.split("\\|", -1);
			String impact = fields[2];
			return Impact.valueOf(impact);
		}
	}

	private static String getTranscript(String ann, String gene, String allele)
	{
		//get the right annotation entry that matches both gene and allele
		String findAnn = getAnn(ann, gene, allele);
		if (findAnn == null)
		{
			LOG.warn("failed to get impact for gene '" + gene + "', allele '" + allele + "' in " + ann);
			return null;
		}
		else
		{
			//from the right one, get the impact
			String[] fields = findAnn.split("\\|", -1);
			return fields[6];//fields[6] == transcript
		}
	}

	private static String getAnn(String ann, String gene, String allele)
	{
		String[] annSplit = ann.split(",", -1);
		for (String oneAnn : annSplit)
		{
			String[] fields = oneAnn.split("\\|", -1);
			String geneFromAnn = fields[3];
			if (gene.equals(geneFromAnn))
			{
				String alleleFromAnn = fields[0];
				if (allele.equals(alleleFromAnn))
				{
					return oneAnn;
				}
			}
		}
		LOG.warn("annotation could not be found for " + gene + ", allele=" + allele + ", ann=" + ann);
		return null;
	}
}
