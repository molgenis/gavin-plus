package org.molgenis.data.annotation.makervcf.structs;

import com.google.auto.value.AutoValue;
import org.molgenis.data.annotation.entity.impl.gavin.GavinEntry;

import java.util.Map;

@AutoValue
public abstract class GavinCalibrations
{
	public abstract double getGenomewideCaddThreshold();

	public abstract double getGenomewideMafThreshold();

	public abstract Map<String, GavinEntry> getGavinEntries();

	public static GavinCalibrations create(double genomewideCaddThreshold, double genomewideMafThreshold,
			Map<String, GavinEntry> gavinEntries)
	{
		return new AutoValue_GavinCalibrations(genomewideCaddThreshold, genomewideMafThreshold, gavinEntries);
	}
}
