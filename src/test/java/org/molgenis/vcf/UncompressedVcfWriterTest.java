package org.molgenis.vcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.vcf.meta.VcfMeta;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UncompressedVcfWriterTest
{
	@Test
	public void testWrite() throws Exception
	{
		File file = new File(
				"C:\\Users\\Dennis\\Dev\\gavin-plus\\src\\test\\resources\\mergeback\\MergeBackRvcfTestFile.vcf");
		try (VcfReader vcfReader = new VcfReader(new InputStreamReader(new FileInputStream(file), UTF_8)))
		{
			VcfMeta vcfMeta = vcfReader.getVcfMeta();
			File tmpFile = File.createTempFile("UncompressedVcfWriterTest", ".vcf");
			try (UncompressedVcfWriter uncompressedVcfWriter = new UncompressedVcfWriter(tmpFile, vcfMeta))
			{

			}
			System.out.println(FileUtils.readFileToString(tmpFile, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void testWriteSamples() throws Exception
	{
		File file = new File(
				"C:\\Users\\Dennis\\Dev\\gavin-plus\\src\\test\\resources\\TrioFilterTestFile.vcf");
		try (VcfReader vcfReader = new VcfReader(new InputStreamReader(new FileInputStream(file), UTF_8)))
		{
			VcfMeta vcfMeta = vcfReader.getVcfMeta();
			File tmpFile = File.createTempFile("UncompressedVcfWriterTest", ".vcf");
			try (UncompressedVcfWriter uncompressedVcfWriter = new UncompressedVcfWriter(tmpFile, vcfMeta))
			{
				vcfReader.iterator().forEachRemaining(uncompressedVcfWriter::write);
			}
			System.out.println(FileUtils.readFileToString(tmpFile, StandardCharsets.UTF_8));
		}
	}
}