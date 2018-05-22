package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.vcf.VcfRecord;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class GavinRecord extends VcfEntity
{
	public static final String CADD_SCALED = "CADD_SCALED";
	public static final String ANN = "ANN";

	private List<Relevance> relevances;
	/**
	 * Any associated genes
	 */
	private Set<String> genes;
	private String rlvVcfValue; // TODO generate from relevance list (see MakeRVCFforClinicalVariants)
	private Double[] caddPhredScores;

	public GavinRecord(VcfRecord record)
	{
		this(record, null);
	}

	public GavinRecord(VcfRecord record, List<Relevance> relevances)
	{
		super(record);
		this.relevances = relevances;
		this.genes = GavinUtils.getGenesFromAnn(GavinUtils.getInfoStringValue(record, ANN));
		this.caddPhredScores = getAltAlleleOrderedDoubleField(CADD_SCALED);
	}

	private Double[] getCaddPhredScores()
	{
		return caddPhredScores;
	}

	public Double getCaddPhredScores(int i)
	{
		return getCaddPhredScores()[i];
	}

	public Set<String> getGenes()
	{
		return genes;
	}

	public void setGenes(String gene)
	{
		//FIXME: shouldn't this update the genes field in the info
		this.genes = Collections.singleton(gene);
	}

	public void setGenes(Set<String> genes)
	{
		//FIXME: shouldn't this update the genes field in the info
		this.genes = genes;
	}

	public void setCaddPhredScore(int i, Double phredScore)
	{
		//FIXME: shouldn't this update the CADD field in the info
		this.caddPhredScores[i] = phredScore;
	}

	/**
	 * Helper function to group all genes of relevance
	 */
	public Set<String> getRelevantGenes()
	{
		return relevances.stream().map(Relevance::getGene).collect(toSet());
	}

	public Set<String> getRelevantAlts()
	{
		return relevances.stream().map(Relevance::getAllele).collect(toSet());
	}

	public List<Relevance> getRelevance()
	{
		return relevances;
	}

	public Relevance getRelevanceForGene(String gene)
	{
		return relevances.stream().filter(relevance -> relevance.getGene().equals(gene)).findFirst().orElse(null);
	}

	public String toStringShort()
	{
		return getChromosome() + " " + getPosition() + " " + getRef() + " " + getAltString();
	}

	public void setRlv(String rlv)
	{
		this.rlvVcfValue = rlv;
	}

	public String getRlv()
	{
		return rlvVcfValue;
	}
}
