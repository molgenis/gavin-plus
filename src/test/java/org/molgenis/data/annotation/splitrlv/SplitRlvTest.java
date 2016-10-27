package org.molgenis.data.annotation.splitrlv;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.DiscoverRelevantVariantsTest;
import org.molgenis.data.annotation.mergeback.MergeBackTool;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SplitRlvTest
{

	protected File inputVcfFile;
	protected File expectedOutputVcfFile;
	protected File observedOutputVcfFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvInputVcfTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvInputVcfTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream outputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvExpectedOutputTestFile.vcf");
		expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvExpectedOutputTestFile.vcf");
		FileCopyUtils.copy(outputVcf, new FileOutputStream(expectedOutputVcfFile));

		observedOutputVcfFile = new File(FileUtils.getTempDirectory(), "outputVcfFile.vcf");

	}

	@Test
	public void test() throws Exception
	{
		// make sure there is no output file when we start
		if(observedOutputVcfFile.exists())
		{
			observedOutputVcfFile.delete();
		}
		assertTrue(!observedOutputVcfFile.exists());

		// run tool
		new SplitRlvTool().start(inputVcfFile, observedOutputVcfFile);

		System.out.println("Going to compare files:\n" + expectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + observedOutputVcfFile.getAbsolutePath());

		assertEquals(FileUtils.readLines(expectedOutputVcfFile), FileUtils.readLines(observedOutputVcfFile));

	}

}
