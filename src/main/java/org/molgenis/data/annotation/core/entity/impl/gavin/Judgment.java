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

	private Classification classification;
	private Method confidence;
	private String reason;

	public Judgment(Classification classification, Method confidence, String reason)
	{
		this.classification = classification;
		this.confidence = confidence;
		this.reason = reason;
	}

	public Classification getClassification()
	{
		return classification;
	}

	public Method getConfidence()
	{
		return confidence;
	}

	public String getReason()
	{
		return reason;
	}
}