package org.molgenis.vcf;

import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.v4_2.VcfMetaWriter;
import org.testng.annotations.Test;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VcfMetaWriterTest
{
	@Test
	public void testWrite() throws IOException
	{
		File file = new File(
				"C:\\Users\\Dennis\\Dev\\gavin-plus\\src\\test\\resources\\mergeback\\MergeBackRvcfTestFile.vcf");
		try (VcfReader vcfReader = new VcfReader(new InputStreamReader(new FileInputStream(file), UTF_8)))
		{
			VcfMeta vcfMeta = vcfReader.getVcfMeta();
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
			try
			{
				new VcfMetaWriter(bufferedWriter).write(vcfMeta);
			}
			finally
			{
				bufferedWriter.close();
			}
			System.out.println(stringWriter.toString());
		}
	}
}