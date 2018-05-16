package org.molgenis.vcf;

import org.molgenis.vcf.meta.VcfMeta;

import java.io.File;
import java.io.FileNotFoundException;

public class VcfWriterFactory
{
	public VcfWriter create(File file, VcfMeta vcfMeta) throws FileNotFoundException
	{
		if (isBlockCompressedFile(file))
		{
			return new BlockCompressedVcfWriter(file, vcfMeta);
		}
		else
		{
			return new UncompressedVcfWriter(file, vcfMeta);
		}
	}

	private boolean isBlockCompressedFile(File file)
	{
		return file.getName().toLowerCase().endsWith(".gz");
	}
}
