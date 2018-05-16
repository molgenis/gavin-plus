package org.molgenis.vcf;

import java.io.IOException;

public interface VcfWriter extends AutoCloseable
{
	void write(VcfRecord vcfRecord) throws IOException;
}
