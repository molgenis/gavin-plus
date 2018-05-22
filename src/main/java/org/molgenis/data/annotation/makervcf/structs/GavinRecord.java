package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.vcf.VcfRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GavinRecord extends VcfEntity
{
	public static final String CADD_SCALED = "CADD_SCALED";
	public static final String ANN = "ANN";

	List<Relevance> relevance;
	private String rlvMetadata;
	private String rlv;
	private Set<String> genes; //any associated genes, not in any given order
	private Double[] caddPhredScores;

	public GavinRecord(VcfRecord record)
	{
		this(record, null);
	}

	public GavinRecord(VcfRecord record, List<Relevance> relevance)
	{
		super(record);
		this.relevance = relevance;
		this.genes = GavinUtils.getGenesFromAnn(GavinUtils.getInfoStringValue(record, ANN));
		this.caddPhredScores = getAltAlleleOrderedDoubleField(CADD_SCALED);
	}

	public Double[] getCaddPhredScores()
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
		Set<String> genes = new HashSet<>();
		genes.add(gene);
		this.genes = genes;
	}

	public void setGenes(Set<String> genes)
	{
		//FIXME: shouldn't this update the genes field in the info
		this.genes = genes;
	}

	public void setCaddPhredScore(int i, Double setMe)
	{
		//FIXME: shouldn't this update the CADD field in the info
		this.caddPhredScores[i] = setMe;
	}

	/**
	 * Helper function to group all genes of relevance
	 *
	 * @return
	 */
	public Set<String> getRelevantGenes()
	{
		HashSet res = new HashSet<>();
		for (Relevance rlv : this.relevance)
		{
			res.add(rlv.getGene());
		}
		return res;
	}

	public Set<String> getRelevantAlts()
	{
		HashSet res = new HashSet<>();
		for (Relevance rlv : this.relevance)
		{
			res.add(rlv.getAllele());
		}
		return res;
	}

	public List<Relevance> getRelevance()
	{
		return relevance;
	}

	public Relevance getRelevanceForGene(String gene)
	{
		for (Relevance rlv : relevance)
		{
			if (rlv.getGene().equals(gene))
			{
				return rlv;
			}
		}
		return null;
	}

	public String toStringShort()
	{
		return getChromosome() + " " + getPosition() + " " + getRef() + " " + getAltString();
	}

	public void setRlvMetadata(String rlvMetadata)
	{
		this.rlvMetadata = rlvMetadata;
	}

	public void setRlv(String rlv)
	{
		this.rlv = rlv;
	}

	public String getRlv()
	{
		return rlv;
	}
}
