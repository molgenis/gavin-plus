package org.molgenis.data.annotation.makervcf;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class VcfRecordMapperSettings
{
	public abstract boolean includeSamples();

	public abstract boolean includeNonRelevantRecords();

	public static VcfRecordMapperSettings create(boolean includeSamples, boolean includeNonRelevantRecords)
	{
		return new AutoValue_VcfRecordMapperSettings(includeSamples, includeNonRelevantRecords);
	}
}
