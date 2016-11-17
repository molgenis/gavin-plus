package org.molgenis.data.annotation.splitrlv;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.DiscoverRelevantVariantsTest;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SplitRlvTest
{

	protected File inputVcfFile;
	protected File expectedOutputVcfFile;
	protected File observedOutputVcfFile;

	protected File inputVcfFileNoSamples;
	protected File expectedOutputVcfFileNoSamples;
	protected File observedOutputVcfFileNoSamples;

	protected File splitRlvNotAllowedInputTestFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvInputVcfTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvInputVcfTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream outputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvExpectedOutputTestFile.vcf");
		expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvExpectedOutputTestFile.vcf");
		FileCopyUtils.copy(outputVcf, new FileOutputStream(expectedOutputVcfFile));

		observedOutputVcfFile = new File(FileUtils.getTempDirectory(), "outputVcfFile.vcf");

		InputStream inputVcfNoSamples = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvInputVcfTestFileNoSamples.vcf");
		inputVcfFileNoSamples = new File(FileUtils.getTempDirectory(), "SplitRlvInputVcfTestFileNoSamples.vcf");
		FileCopyUtils.copy(inputVcfNoSamples, new FileOutputStream(inputVcfFileNoSamples));

		InputStream outputVcfNoSamples = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvExpectedOutputTestFileNoSamples.vcf");
		expectedOutputVcfFileNoSamples = new File(FileUtils.getTempDirectory(), "SplitRlvExpectedOutputTestFileNoSamples.vcf");
		FileCopyUtils.copy(outputVcfNoSamples, new FileOutputStream(expectedOutputVcfFileNoSamples));

		observedOutputVcfFileNoSamples = new File(FileUtils.getTempDirectory(), "outputVcfFileNoSamples.vcf");

		InputStream splitRlvNotAllowed = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiAlleleInputTestFile.vcf");
		splitRlvNotAllowedInputTestFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiAlleleInputTestFile.vcf");
		FileCopyUtils.copy(splitRlvNotAllowed, new FileOutputStream(splitRlvNotAllowedInputTestFile));

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

		assertEquals(FileUtils.readLines(observedOutputVcfFile), FileUtils.readLines(expectedOutputVcfFile));

	}

	@Test
	public void testNoSamples() throws Exception
	{
		// make sure there is no output file when we start
		if(observedOutputVcfFileNoSamples.exists())
		{
			observedOutputVcfFileNoSamples.delete();
		}
		assertTrue(!observedOutputVcfFileNoSamples.exists());

		// run tool
		new SplitRlvTool().start(inputVcfFileNoSamples, observedOutputVcfFileNoSamples);

		System.out.println("Going to compare files:\n" + expectedOutputVcfFileNoSamples.getAbsolutePath() + "\nvs.\n" + observedOutputVcfFileNoSamples.getAbsolutePath());

		assertEquals(FileUtils.readLines(observedOutputVcfFileNoSamples), FileUtils.readLines(expectedOutputVcfFileNoSamples));

	}


}
