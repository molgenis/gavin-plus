package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

public class GavinRecord
{
	private AnnotatedVcfRecord annotatedVcfRecord;
	private List<Relevance> relevances;
	/**
	 * Any associated genes
	 */
	private Set<String> genes;
	private Double[] caddPhredScores;

	// TODO remove
	private String rlvVcfValue; // TODO generate from relevance list (see MakeRVCFforClinicalVariants)

	public GavinRecord(VcfRecord record)
	{
		this(record, null);
	}

	public GavinRecord(VcfRecord record, List<Relevance> relevances)
	{
		this.annotatedVcfRecord = new AnnotatedVcfRecord(record);
		this.relevances = relevances;

		this.genes = annotatedVcfRecord.getGenesFromAnn();
		this.caddPhredScores = annotatedVcfRecord.getCaddPhredScores();
	}

	public AnnotatedVcfRecord getAnnotatedVcfRecord()
	{
		return annotatedVcfRecord;
	}

	public void setRelevances(List<Relevance> relevances)
	{
		this.relevances = relevances;
	}

	public Double getCaddPhredScores(int i)
	{
		return caddPhredScores[i];
	}

	public void setCaddPhredScore(int i, Double phredScore)
	{
		this.caddPhredScores[i] = phredScore;
	}

	public Set<String> getGenes()
	{
		return genes;
	}

	public void setGenes(String gene)
	{
		this.genes = singleton(gene);
	}

	public void setGenes(Set<String> genes)
	{
		this.genes = genes;
	}

	public List<Relevance> getRelevance()
	{
		return relevances;
	}

	public String toStringShort()
	{
		return annotatedVcfRecord.getChromosome() + " " + annotatedVcfRecord.getPosition() + " "
				+ VcfRecordUtils.getRef(annotatedVcfRecord) + " " + VcfRecordUtils.getAltString(annotatedVcfRecord);
	}

	// TODO remove
	public void setRlv(String rlv)
	{
		this.rlvVcfValue = rlv;
	}

	// TODO remove
	public String getRlv()
	{
		return rlvVcfValue;
	}

	/**
	 * @deprecated TODO refactor such that this method does not expose VcfReader data structures
	 */
	@Deprecated
	public String getSampleFieldValue(VcfSample vcfSample, String field)
	{
		return VcfRecordUtils.getSampleFieldValue(annotatedVcfRecord, vcfSample, field);
	}

	/**
	 * @deprecated TODO refactor such that this method does not expose VcfReader data structures
	 */
	@Deprecated
	public Iterable<VcfSample> getSamples()
	{
		return annotatedVcfRecord.getSamples();
	}

	/**
	 * @deprecated TODO refactor such that this method does not expose VcfReader data structures
	 */
	@Deprecated
	public VcfMeta getVcfMeta()
	{
		return annotatedVcfRecord.getVcfMeta();
	}

	public int getAltAlleleIndex(String alt)
	{
		return VcfRecordUtils.getAltAlleleIndex(annotatedVcfRecord, alt);
	}

	public String getChromosome()
	{
		return annotatedVcfRecord.getChromosome();
	}

	public int getPosition()
	{
		return annotatedVcfRecord.getPosition();
	}

	public String getRef()
	{
		return VcfRecordUtils.getRef(annotatedVcfRecord);
	}

	public String getAlt()
	{
		return VcfRecordUtils.getAlt(annotatedVcfRecord);
	}

	public String getAlt(int altIndex)
	{
		return VcfRecordUtils.getAlt(annotatedVcfRecord, altIndex);
	}

	public String getChrPosRefAlt()
	{
		return VcfRecordUtils.getChrPosRefAlt(annotatedVcfRecord);
	}

	public int getAltIndex(String allele) throws Exception
	{
		return VcfRecordUtils.getAltIndex(annotatedVcfRecord, allele);
	}

	public String[] getAlts()
	{
		return VcfRecordUtils.getAlts(annotatedVcfRecord);
	}

	public double getExAcAlleleFrequencies(int i)
	{
		return annotatedVcfRecord.getExAcAlleleFrequencies(i);
	}

	public double getGoNlAlleleFrequencies(int i)
	{
		return annotatedVcfRecord.getGoNlAlleleFrequencies(i);
	}

	public Impact getImpact(int i, String gene)
	{
		return annotatedVcfRecord.getImpact(i, gene);
	}

	public String getTranscript(int i, String gene)
	{
		return annotatedVcfRecord.getTranscript(i, gene);
	}

	public String getId()
	{
		return VcfRecordUtils.getId(annotatedVcfRecord);
	}
}
