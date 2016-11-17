package org.molgenis.data.annotation.splitrlv;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.DiscoverRelevantVariantsTest;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.*;

public class SplitMultiRlvTest
{

	protected File multiGeneInputVcfFile;
	protected File multiGeneExpectedOutputVcfFile;
	protected File multiGeneObservedOutputVcfFile;

	protected File multiAlleleInputVcfFile;
	protected File multiAlleleExpectedOutputVcfFile;
	protected File multiAlleleObservedOutputVcfFile;

	protected File multiAlleleMultiGeneInputVcfFile;
	protected File multiAlleleMultiGeneExpectedOutputVcfFile;
	protected File multiAlleleMultiGeneObservedOutputVcfFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {

		// multi gene
		InputStream multiGeneInputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiGeneInputTestFile.vcf");
		multiGeneInputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiGeneInputVcfTestFile.vcf");
		FileCopyUtils.copy(multiGeneInputVcf, new FileOutputStream(multiGeneInputVcfFile));

		InputStream multiGeneOutputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiGeneExpectedOutputTestFile.vcf");
		multiGeneExpectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiGeneExpectedOutputTestFile.vcf");
		FileCopyUtils.copy(multiGeneOutputVcf, new FileOutputStream(multiGeneExpectedOutputVcfFile));

		multiGeneObservedOutputVcfFile = new File(FileUtils.getTempDirectory(), "multiGeneOutputVcfFile.vcf");

		// multi allele
		InputStream multiAlleleInputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiAlleleInputTestFile.vcf");
		multiAlleleInputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiAlleleInputVcfTestFile.vcf");
		FileCopyUtils.copy(multiAlleleInputVcf, new FileOutputStream(multiAlleleInputVcfFile));

		InputStream multiAlleleOutputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiAlleleExpectedOutputTestFile.vcf");
		multiAlleleExpectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiAlleleExpectedOutputTestFile.vcf");
		FileCopyUtils.copy(multiAlleleOutputVcf, new FileOutputStream(multiAlleleExpectedOutputVcfFile));

		multiAlleleObservedOutputVcfFile = new File(FileUtils.getTempDirectory(), "multiAlleleOutputVcfFile.vcf");

		// multi allele & multi gene
		InputStream multiAlleleMultiGeneInputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiAlleleMultiGeneInputTestFile.vcf");
		multiAlleleMultiGeneInputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiAlleleMultiGeneInputVcfTestFile.vcf");
		FileCopyUtils.copy(multiAlleleMultiGeneInputVcf, new FileOutputStream(multiAlleleMultiGeneInputVcfFile));

		InputStream multiAlleleMultiGeneOutputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/splitrlv/SplitRlvMultiAlleleMultiGeneExpectedOutputTestFile.vcf");
		multiAlleleMultiGeneExpectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "SplitRlvMultiAlleleMultiGeneExpectedOutputTestFile.vcf");
		FileCopyUtils.copy(multiAlleleMultiGeneOutputVcf, new FileOutputStream(multiAlleleMultiGeneExpectedOutputVcfFile));

		multiAlleleMultiGeneObservedOutputVcfFile = new File(FileUtils.getTempDirectory(), "multiAlleleMultiGeneOutputVcfFile.vcf");

	}


	@Test
	public void multiGeneTest() throws Exception
	{
		// make sure there is no output file when we start
		if(multiGeneObservedOutputVcfFile.exists())
		{
			multiGeneObservedOutputVcfFile.delete();
		}
		assertTrue(!multiGeneObservedOutputVcfFile.exists());

		// run tool
		new SplitRlvTool().start(multiGeneInputVcfFile, multiGeneObservedOutputVcfFile);

		System.out.println("Going to compare files:\n" + multiGeneExpectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + multiGeneObservedOutputVcfFile.getAbsolutePath());

		assertEquals(FileUtils.readLines(multiGeneObservedOutputVcfFile), FileUtils.readLines(multiGeneExpectedOutputVcfFile));

	}


	@Test
	public void multiAlleleTest() throws Exception
	{
		// make sure there is no output file when we start
		if(multiAlleleObservedOutputVcfFile.exists())
		{
			multiAlleleObservedOutputVcfFile.delete();
		}
		assertTrue(!multiAlleleObservedOutputVcfFile.exists());

		// run tool
		new SplitRlvTool().start(multiAlleleInputVcfFile, multiAlleleObservedOutputVcfFile);

		System.out.println("Going to compare files:\n" + multiAlleleExpectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + multiAlleleObservedOutputVcfFile.getAbsolutePath());

		assertEquals(FileUtils.readLines(multiAlleleObservedOutputVcfFile), FileUtils.readLines(multiAlleleExpectedOutputVcfFile));

	}

	@Test
	public void multiAlleleMultiGeneTest() throws Exception
	{
		// make sure there is no output file when we start
		if(multiAlleleMultiGeneObservedOutputVcfFile.exists())
		{
			multiAlleleMultiGeneObservedOutputVcfFile.delete();
		}
		assertTrue(!multiAlleleMultiGeneObservedOutputVcfFile.exists());

		// run tool
		new SplitRlvTool().start(multiAlleleMultiGeneInputVcfFile, multiAlleleMultiGeneObservedOutputVcfFile);

		System.out.println("Going to compare files:\n" + multiAlleleMultiGeneExpectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + multiAlleleMultiGeneObservedOutputVcfFile.getAbsolutePath());

		assertEquals(FileUtils.readLines(multiAlleleMultiGeneObservedOutputVcfFile), FileUtils.readLines(multiAlleleMultiGeneExpectedOutputVcfFile));

	}




}
