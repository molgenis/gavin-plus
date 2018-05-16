package org.molgenis.vcf;

import java.io.IOException;

/**
 * @see org.molgenis.vcf.v4_2.Vcf42Writer
 */
public interface VcfWriter extends AutoCloseable
{
	void write(VcfRecord vcfRecord) throws IOException;
}
