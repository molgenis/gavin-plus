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
	public void test() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);

		Iterator<RelevantVariant> it = new ConvertToGeneStream(discover.findRelevantVariants(), true).go();


		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
			//System.out.println(it.next().getVariant().getGenes() + " -> " + it.next().getVariant() + " at " + it.next().getVariant().getPos());
			positions.append(it.next().getVariant().getPos() + "_");
		}

		// note: the order is arbitrary to some respects, for example when 2 genes end at exactly the same position.
		// in this test, gene F spans 12-13 and G is at 13, but G is but before F, (13_12) but in this case 12_13 would also be correct
		// note 2: sites without relevant variants are not present in the output, e.g. number 39 in this test
		// variants that are relevant to one gene but not the other, in combination with overlapping genes, makes for many complicated scenarios
		String expected = "3_4_1_2_5_7_9_10_6_8_11_13_12_14_15_18_19_17_16_20_23_22_24_21_25_27_28_29_26_30_32_33_31_34_35_37_36_38_40_41_42_";

		assertEquals(positions.toString(), expected);


	}

}
