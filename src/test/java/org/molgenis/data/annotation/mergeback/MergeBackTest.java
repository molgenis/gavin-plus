package org.molgenis.data.annotation.mergeback;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.DiscoverRelevantVariantsTest;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MergeBackTest
{

	protected File inputVcfFile;
	protected File rvcfFile;
	protected File expectedOutputVcfFile;
	protected File observedOutputVcfFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/mergeback/MergeBackInputVcfTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "MergeBackInputVcfTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream rvcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/mergeback/MergeBackRvcfTestFile.vcf");
		rvcfFile = new File(FileUtils.getTempDirectory(), "MergeBackRvcfTestFile.vcf");
		FileCopyUtils.copy(rvcf, new FileOutputStream(rvcfFile));

		InputStream outputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/mergeback/MergeBackExpectedOutputTestFile.vcf");
		expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "MergeBackExpectedOutputTestFile.vcf");
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
		new MergeBackTool().start(inputVcfFile, rvcfFile, observedOutputVcfFile);

		System.out.println("Going to compare files:\n" + expectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + observedOutputVcfFile.getAbsolutePath());

		assertEquals(FileUtils.readLines(expectedOutputVcfFile), FileUtils.readLines(observedOutputVcfFile));

	}

}
