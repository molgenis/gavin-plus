package org.molgenis.gavin.classify;

import org.molgenis.vcf.VcfRecord;

public interface VariantAnalyzer
{
	AnalyzedVariant analyze(VcfRecord vcfRecord);
}
