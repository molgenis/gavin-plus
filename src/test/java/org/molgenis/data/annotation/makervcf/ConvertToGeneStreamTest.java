package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.*;

public class ConvertToGeneStreamTest extends Setup
{

	protected File inputVcfFile1;
	protected File inputVcfFile2;

	public static final String expected1 = "3_4_1_2_5_7_9_10_6_8_11_13_12_14_15_18_19_17_16_20_23_22_24_21_25_27_28_29_26_30_31_32_33_34_35_37_36_38_40_41_42_43_44_45_";
	public static final String expected2 = "1_2_3_4_5_6_7_8_9_10_11_12_13_14_15_16_17_18_19_20_21_22_23_24_25_";


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException
	{
		InputStream inputVcf1 = DiscoverRelevantVariantsTest.class.getResourceAsStream("/ConvertToGeneStreamTestFile.vcf");
		inputVcfFile1 = new File(FileUtils.getTempDirectory(), "ConvertToGeneStreamTestFile.vcf");
		FileCopyUtils.copy(inputVcf1, new FileOutputStream(inputVcfFile1));

		InputStream inputVcf2 = DiscoverRelevantVariantsTest.class.getResourceAsStream("/AssignCompoundHetTestFile.vcf");
		inputVcfFile2 = new File(FileUtils.getTempDirectory(), "AssignCompoundHetTestFile.vcf");
		FileCopyUtils.copy(inputVcf2, new FileOutputStream(inputVcfFile2));

	}

	@Test
	public void test() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile1, gavinFile, repPathoFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<GavinRecord> it = new ConvertToGeneStream(discover.findRelevantVariants()).go();
		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
			positions.append(it.next().getPosition() + "_");
		}

		// note: the order is arbitrary to some respects, for example when 2 genes end at exactly the same position.
		// in this test, gene F spans 12-13 and G is at 13, but G is but before F, (13_12) but in this case 12_13 would also be correct
		// note 2: sites without relevant variants are not present in the output, e.g. number 39 in this test
		// variants that are relevant to one gene but not the other, in combination with overlapping genes, makes for many complicated scenarios
		// note 3: we changed Hashmap to LinkedHashMap in ConvertToGeneStream to preserve variant read order when possible -
		// this means that before the order was 30_32_33_31_34 but now it is 30_31_32_33_34

		assertEquals(positions.toString(), expected1);

	}

	@Test
	public void test2() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile2, gavinFile, repPathoFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<GavinRecord> it = new ConvertToGeneStream(discover.findRelevantVariants()).go();
		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
			positions.append(it.next().getPosition() + "_");
		}

		// test bug where A -> A,B -> B was reversed incorrectly to 13_14_16_15_17_18

		assertEquals(positions.toString(), expected2);
	}

}
