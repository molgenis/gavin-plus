package org.molgenis.gavin.classify;

import com.google.auto.value.AutoValue;
import org.molgenis.vcf.VcfRecord;

@AutoValue
public abstract class AnalyzedVariant
{
	public abstract VcfRecord getVcfRecord();

	public abstract VariantAnalysis getAnalysis();

	public static AnalyzedVariant create(VcfRecord vcfRecord, VariantAnalysis variantAnalysis)
	{
		return new AutoValue_AnalyzedVariant(vcfRecord, variantAnalysis);
	}
}
