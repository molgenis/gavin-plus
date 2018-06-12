package org.molgenis.data.vcf.datastructures;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Sample
{
	private final String id;
	private final Optional<String> genotype;
	private final Optional<Double> depth;

	public Sample(String id, String genotype, Double depth)
	{
		this.id = requireNonNull(id);
		this.genotype = Optional.ofNullable(genotype);
		this.depth = Optional.ofNullable(depth);
	}

	public String getId()
	{
		return id;
	}

	public Optional<String> getGenotype()
	{
		return genotype;
	}

	public Optional<Double> getDepth()
	{
		return depth;
	}
}