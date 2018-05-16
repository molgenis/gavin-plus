package org.molgenis.vcf;

import net.sf.samtools.util.BlockCompressedOutputStream;
import org.molgenis.vcf.meta.VcfMeta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

class BlockCompressedVcfWriter extends AbstractVcfWriter
{
	BlockCompressedVcfWriter(File file, VcfMeta vcfMeta)
	{
		super(new BufferedWriter(new OutputStreamWriter(new BlockCompressedOutputStream(file), UTF_8)), vcfMeta);
	}
}
