package org.molgenis.gavin.classify;

import org.molgenis.genotype.Allele;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class VcfRecordContext
{
	private String chromosome;
	private Integer position;
	private Allele referenceAllele;
	private List<Allele> alternateAlleles;
	private Set<String> analyzedChromosomes;

	VcfRecordContext()
	{
		analyzedChromosomes = new HashSet<>();
	}

	public String getChromosome()
	{
		return chromosome;
	}

	public void setChromosome(String chromosome)
	{
		this.chromosome = chromosome;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}

	public Allele getReferenceAllele()
	{
		return referenceAllele;
	}

	public void setReferenceAllele(Allele referenceAllele)
	{
		this.referenceAllele = referenceAllele;
	}

	public List<Allele> getAlternateAlleles()
	{
		return alternateAlleles;
	}

	public void setAlternateAlleles(List<Allele> alternateAlleles)
	{
		this.alternateAlleles = alternateAlleles;
	}

	public boolean hasAnalyzedChromose(String chromosome)
	{
		return analyzedChromosomes.contains(chromosome);
	}

	public void addAnalyzedChromose(String chromosome)
	{
		analyzedChromosomes.add(chromosome);
	}
}
