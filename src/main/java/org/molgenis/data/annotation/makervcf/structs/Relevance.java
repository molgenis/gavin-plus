package org.molgenis.data.annotation.makervcf.structs;

import java.util.Objects;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;

import javax.annotation.Nullable;


public class Relevance
{
	private Judgment judgment;
	private String allele;
	private String gene;

	public Relevance(String allele, String gene, Judgment judgment)
	{
		this.allele = allele;
		this.gene = gene;
		this.judgment = judgment;
	}

	public String getAllele()
	{
		return allele;
	}

	public String getGene()
	{
		return gene;
	}

	public Judgment getJudgment()
	{
		return judgment;
	}

	@Override
	public String toString() {
		return "Relevance{" +
				"judgment=" + judgment +
				", allele='" + allele + '\'' +
				", gene='" + gene + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Relevance relevance = (Relevance) o;
		return getJudgment().equals(relevance.getJudgment()) &&
				Objects.equals(getAllele(), relevance.getAllele()) &&
				Objects.equals(getGene(), relevance.getGene());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getJudgment(), getAllele(), getGene());
	}
}
