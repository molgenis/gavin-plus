package org.molgenis.data.annotation.makervcf;

import com.google.auto.value.AutoValue;
import org.molgenis.data.annotation.makervcf.Main.RlvMode;

@AutoValue
public abstract class VcfRecordMapperSettings
{
	public abstract boolean includeSamples();

	public abstract RlvMode rlvMode();

	public abstract boolean addSplittedAnnFields();

	public abstract boolean prefixSplittedRlvFields();

	public static VcfRecordMapperSettings create(boolean includeSamples, RlvMode rlvMode,
			boolean addSplittedAnnFields, boolean prefixSplittedRlvFields)
	{
		return new AutoValue_VcfRecordMapperSettings(includeSamples, rlvMode, addSplittedAnnFields,
				prefixSplittedRlvFields);
	}
}
