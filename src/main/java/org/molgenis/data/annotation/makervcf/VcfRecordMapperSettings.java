package org.molgenis.data.annotation.makervcf;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class VcfRecordMapperSettings
{
	public abstract boolean includeSamples();

	public abstract boolean splitRlvField();

	public abstract boolean addSplittedAnnFields();

	public abstract boolean prefixSplittedRlvFields();

	public static VcfRecordMapperSettings create(boolean includeSamples, boolean splitRlvField,
			boolean addSplittedAnnFields, boolean prefixSplittedRlvFields)
	{
		return new AutoValue_VcfRecordMapperSettings(includeSamples, splitRlvField, addSplittedAnnFields,
				prefixSplittedRlvFields);
	}
}
