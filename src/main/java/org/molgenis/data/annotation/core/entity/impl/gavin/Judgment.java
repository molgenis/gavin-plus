package org.molgenis.data.annotation.core.entity.impl.gavin;

import java.util.Objects;

/**
 * Judgment result of the gavin method
 */
public class Judgment
{
	public enum Classification
	{
		Benign, Pathogenic, VOUS
	}

	public enum Method
	{
		calibrated, genomewide
	}

	private Classification classification;
	private Method confidence;
	private String gene;
	private String reason;
	private String source;
	private String type;

	public Judgment(Classification classification, Method confidence, String gene, String reason, String source,
			String type)
	{
		this.classification = classification;
		this.confidence = confidence;
		this.gene = gene;
		this.reason = reason;
		this.source = source;
		this.type = type;
	}

	public Classification getClassification()
	{
		return classification;
	}

	public Judgment setClassification(Classification classification)
	{
		this.classification = classification;
		return this;
	}

	public Method getConfidence()
	{
		return confidence;
	}

	public Judgment setConfidence(Method confidence)
	{
		this.confidence = confidence;
		return this;}

	public String getGene()
	{
		return gene;
	}

	public Judgment setGene(String gene)
	{
		this.gene = gene;
		return this;
	}

	public String getReason()
	{
		return reason;
	}

	public Judgment setReason(String reason)
	{
		this.reason = reason;
		return this;
	}

	public String getSource()
	{
		return source;
	}

	public Judgment setSource(String source)
	{
		this.source = source;
		return this;
	}

	public String getType()
	{
		return type;

	}

	public Judgment setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	public String toString()
	{
		return "Judgment{" + "classification=" + classification + ", confidence=" + confidence + ", gene='" + gene
				+ '\'' + ", reason='" + reason + '\'' + ", source='" + source + '\'' + ", type='" + type + '\'' + '}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Judgment judgment = (Judgment) o;
		return classification == judgment.classification && confidence == judgment.confidence && Objects.equals(gene,
				judgment.gene) && Objects.equals(reason, judgment.reason) && Objects.equals(source, judgment.source)
				&& Objects.equals(type, judgment.type);
	}

	@Override
	public int hashCode()
	{

		return Objects.hash(classification, confidence, gene, reason, source, type);
	}
}