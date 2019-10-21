package org.molgenis.data.annotation.makervcf;

import com.google.auto.value.AutoValue;
import org.molgenis.data.annotation.makervcf.Main.RlvMode;

@AutoValue
public abstract class VcfRecordMapperSettings
{
	public abstract RlvMode rlvMode();

	public static VcfRecordMapperSettings create(RlvMode rlvMode)
	{
		return new AutoValue_VcfRecordMapperSettings(rlvMode);
	}
}
