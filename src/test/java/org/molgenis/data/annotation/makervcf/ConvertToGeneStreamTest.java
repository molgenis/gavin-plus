package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MAFFilter;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.*;

public class ConvertToGeneStreamTest extends Setup
{

	protected File inputVcfFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/ConvertToGeneStreamTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "ConvertToGeneStreamTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

	}

	@Test
	public void testPredictedPathogenic() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);

		Iterator<RelevantVariant> it = new ConvertToGeneStream(discover.findRelevantVariants(), false).go();


		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
			System.out.println(it.next().getVariant().getGenes() + " -> " + it.next().getGene() + " at " + it.next().getVariant().getPos());
			positions.append(it.next().getVariant().getPos() + "_");
		}

		String expected = "3_4_1_2_5_7_9_10_6_8_11_12_13_14_15_18_19_17_16_20_23_22_24_21_25_27_28_29_26_30_31_32_33_34_35_36_";

		assertEquals(positions.toString(), expected);


	}

}
