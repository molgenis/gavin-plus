package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MatchVariantsToGenotypeAndInheritanceTest extends Setup
{

	protected File inputVcfFile;
	protected File cgdFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = MatchVariantsToGenotypeAndInheritanceTest.class.getResourceAsStream("/MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

	}

	@Test
	public void test() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<RelevantVariant> disc = discover.findRelevantVariants();

		MatchVariantsToGenotypeAndInheritance m = new MatchVariantsToGenotypeAndInheritance(disc, cgdFile, new HashSet<>(), false);
		Iterator<RelevantVariant> it = m.go();

//		while(it.hasNext())
//		{
//			System.out.println(it.next());
//		}

		// dominant gene
		assertTrue(it.hasNext());
		String sampleStatus = it.next().getRelevance().get(0).getSampleStatus().toString();
		assertTrue(!sampleStatus.contains("p01"));
		assertTrue(sampleStatus.contains("p02=AFFECTED"));
		assertTrue(sampleStatus.contains("p03=AFFECTED"));

		// recessive gene
		assertTrue(it.hasNext());
		sampleStatus = it.next().getRelevance().get(0).getSampleStatus().toString();
		assertTrue(!sampleStatus.contains("p01"));
		assertTrue(sampleStatus.contains("p02=CARRIER"));
		assertTrue(sampleStatus.contains("p03=AFFECTED"));

		// X-linked
		assertTrue(it.hasNext());
		sampleStatus = it.next().getRelevance().get(0).getSampleStatus().toString();
		assertTrue(!sampleStatus.contains("p01"));
		assertTrue(sampleStatus.contains("p02=CARRIER"));
		assertTrue(sampleStatus.contains("p03=AFFECTED"));

		// dominant or recessive
		assertTrue(it.hasNext());
		sampleStatus = it.next().getRelevance().get(0).getSampleStatus().toString();
		assertTrue(!sampleStatus.contains("p01"));
		assertTrue(sampleStatus.contains("p02=AFFECTED"));
		assertTrue(sampleStatus.contains("p03=AFFECTED"));

		// Y-linked
		assertTrue(it.hasNext());
		sampleStatus = it.next().getRelevance().get(0).getSampleStatus().toString();
		assertTrue(!sampleStatus.contains("p01"));
		assertTrue(!sampleStatus.contains("p02"));
		assertTrue(sampleStatus.contains("p03=HOMOZYGOUS"));

		// no more variants
		assertTrue(!it.hasNext());

	}

}
