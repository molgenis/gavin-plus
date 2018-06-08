package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.impl.TrioFilter;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.TrioData;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.*;

public class TrioFilterTest extends Setup
{

	protected File inputVcfFile;
	protected File cgdFile;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/TrioFilterTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "TrioFilterTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

	}

	@Test
	public void test() throws Exception
	{
		TrioData td = TrioFilter.getTrioData(inputVcfFile);
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, true);
		Iterator<GavinRecord> rv3 = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(), cgdFile, td.getParents()).go();
		ConvertToGeneStream gs = new ConvertToGeneStream(rv3);
		Iterator<GavinRecord> gsi = gs.go();

		TrioFilter tf = new TrioFilter(gsi, td);
		Iterator<GavinRecord> it = tf.go();

		// heterozygous child, 1 heterozygous parent
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// homozygous child, homref parents
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{p01=AFFECTED}");

		// homozygous child, 1 heterozygous parent
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{p01=AFFECTED}");

		// homozygous child, 2 heterozygous parents
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{p01=AFFECTED}");

		// homozygous child, 1 homozygous parent (mother)
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// homozygous child, 1 homozygous parent (father)
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// homozygous child, 2 homozygous parents
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// homref child, 1 heterozygous parent
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// heterozygous child, homref parents
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{p01=AFFECTED}");

		// heterozygous child, 1 heterozygous parent
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// heterozygous child, 2 heterozygous parents
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// homozygous child, 1 homozygous parent
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{}");

		// homozygous child, 2 heterozygous parents
		// note: unlikely for dominant disease, but based on inheritance we don't filter it out
		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getSampleStatus().toString(), "{p01=AFFECTED}");

	}

}
