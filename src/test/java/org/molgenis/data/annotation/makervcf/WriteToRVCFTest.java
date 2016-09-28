package org.molgenis.data.annotation.makervcf;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MakeRVCFforClinicalVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class WriteToRVCFTest extends Setup
{

	protected File inputVcfFile;
	protected File expectedOutputVcfFile;
	protected File observedOutputVcfFile;
	protected File cgdFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

		InputStream outputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/WriteToRVCFTestExpectedOutput.vcf");
		expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "WriteToRVCFTestExpectedOutput.vcf");
		FileCopyUtils.copy(outputVcf, new FileOutputStream(expectedOutputVcfFile));

		observedOutputVcfFile = new File(FileUtils.getTempDirectory(), "outputVcfFile.vcf");
	}

	@Test
	public void test() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<RelevantVariant> match = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(), cgdFile, new HashSet<String>(), false).go();
		Iterator<Entity> it = new MakeRVCFforClinicalVariants(match, Pipeline.RLV, false).addRVCFfield();

		List<AttributeMetaData> attributes = Lists.newArrayList(discover.getVcfMeta().getAttributes());
		attributes.add(Pipeline.RLV);
		new WriteToRVCF().writeRVCF(it, observedOutputVcfFile, inputVcfFile, attributes, true, false);

		System.out.println("Going to compare files:\n" + expectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + observedOutputVcfFile.getAbsolutePath());
		assertEquals(FileUtils.readLines(observedOutputVcfFile), FileUtils.readLines(expectedOutputVcfFile));
		System.out.println("\n--> they are equal.");

	}
}
