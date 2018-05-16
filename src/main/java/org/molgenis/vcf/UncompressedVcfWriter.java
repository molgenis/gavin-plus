package org.molgenis.vcf;

import org.molgenis.vcf.meta.VcfMeta;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

class UncompressedVcfWriter extends AbstractVcfWriter
{
	UncompressedVcfWriter(File file, VcfMeta vcfMeta) throws FileNotFoundException
	{
		super(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8)), vcfMeta);
	}
}
