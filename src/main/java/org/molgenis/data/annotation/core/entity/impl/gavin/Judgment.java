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
	private String reason;
	private String type;

	public Judgment(Classification classification, Method confidence, String gene, String reason, String source,
			String type)
	{
		this.classification = classification;
		this.confidence = confidence;
		this.reason = reason;
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

	public String getReason()
	{
		return reason;
	}

	public Judgment setReason(String reason)
	{
		this.reason = reason;
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