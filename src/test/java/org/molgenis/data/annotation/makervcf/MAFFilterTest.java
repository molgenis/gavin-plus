package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MAFFilter;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MAFFilterTest extends Setup
{

	protected File inputVcfFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/MAFFilterTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "MAFFilterTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

	}

	@Test
	public void testPredictedPathogenic() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS);

		Iterator<GavinRecord> it = new MAFFilter(discover.findRelevantVariants()).go();
		assertTrue(it.hasNext());
		assertEquals(0.02, it.next().getRelevance().get(0).getGonlAlleleFreq());
		assertTrue(it.hasNext());
		assertEquals(0.03, it.next().getRelevance().get(0).getGonlAlleleFreq());
		assertTrue(it.hasNext());
		assertEquals(0.01, it.next().getRelevance().get(0).getGonlAlleleFreq());
		assertFalse(it.hasNext());

	}

}
