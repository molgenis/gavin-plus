package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertBackToPositionalStream;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;

public class ConvertBackToPositionalStreamTest extends Setup
{

	protected File inputVcfFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/ConvertToGeneStreamTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "ConvertToGeneStreamTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

	}

	@Test
	public void test() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);

		ConvertToGeneStream gs = new ConvertToGeneStream(discover.findRelevantVariants(), false);

		Iterator<RelevantVariant> it = new ConvertBackToPositionalStream(gs.go(), gs.getPositionalOrder(), true).go();

		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
		//	System.out.println(it.next().getVariant().getGenes() + " -> " + it.next().getRelevance() + " at " + it.next().getVariant().getPos());
			positions.append(it.next().getVariant().getPosition() + "_");
		}

		//39 is missing, does not contain relevant variants (2x MODIFIER)
		String expected = "1_2_3_4_5_6_7_8_9_10_11_12_13_14_15_16_17_18_19_20_21_22_23_24_25_26_27_28_29_30_31_32_33_34_35_36_37_38_40_41_42_43_44_45_";

		assertEquals(positions.toString(), expected);


	}

}
