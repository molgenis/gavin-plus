package org.molgenis.data.annotation.makervcf.structs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by joeri on 6/13/16.
 */
//FIXME: refactor to extend VcfEntity
public class RelevantVariant
{
	VcfEntity variant;
	List<Relevance> relevance;
	private String rlvMetadata;
	private String rlv;

	public RelevantVariant(VcfEntity variant, List<Relevance> relevance)
	{
		this.variant = variant;
		this.relevance = relevance;
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

	public VcfEntity getVariant()
	{
		return variant;
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
		return variant.getChr() + " " + variant.getPos() + " " + variant.getRef() + " " + variant.getAltString();
	}

	@Override
	public String toString()
	{
		return "RelevantVariant{" + "variant=" + variant + ", relevance=" + relevance + '}';
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
