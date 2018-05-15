package org.molgenis.data.annotation.core.entity.impl.gavin;

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

	public Classification classification;
	public Method confidence;
	public String gene;
	public String reason;
	public String source;
	public String type;

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


}