package org.molgenis.data.annotation.makervcf;

import net.didion.jwnl.data.Exc;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertBackToPositionalStream;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class DuplicateAndReversedPositionTest extends Setup
{

	protected File inputDupVcfFile;
	protected File inputDupBadVcfFile;
	protected File inputRevVcfFile;
	protected File inputChromBadVcfFile;


	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/DuplicatePositionTestFile.vcf");
		inputDupVcfFile = new File(FileUtils.getTempDirectory(), "DuplicatePositionTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputDupVcfFile));

		InputStream inputVcf1 = DiscoverRelevantVariantsTest.class.getResourceAsStream("/DuplicatePositionBadTestFile.vcf");
		inputDupBadVcfFile = new File(FileUtils.getTempDirectory(), "DuplicatePositionBadTestFile.vcf");
		FileCopyUtils.copy(inputVcf1, new FileOutputStream(inputDupBadVcfFile));

		InputStream inputVcf2 = DiscoverRelevantVariantsTest.class.getResourceAsStream("/ReversePositionTestFile.vcf");
		inputRevVcfFile = new File(FileUtils.getTempDirectory(), "ReversePositionTestFile.vcf");
		FileCopyUtils.copy(inputVcf2, new FileOutputStream(inputRevVcfFile));

		InputStream inputVcf3 = DiscoverRelevantVariantsTest.class.getResourceAsStream("/ChromBadTestFile.vcf");
		inputChromBadVcfFile = new File(FileUtils.getTempDirectory(), "ChromBadTestFile.vcf");
		FileCopyUtils.copy(inputVcf3, new FileOutputStream(inputChromBadVcfFile));
	}

	@Test
	public void duplicatePositionTest() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputDupVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		ConvertToGeneStream gs = new ConvertToGeneStream(discover.findRelevantVariants(), false);
		Iterator<RelevantVariant> it = new ConvertBackToPositionalStream(gs.go(), gs.getPositionalOrder(), false).go();
		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
			positions.append(it.next().getVariant().getPos() + "_");
		}

		String expected = "1_2_2_3_4_4_5_6_7_8_8_8_9_10_";
		assertEquals(positions.toString(), expected);
	}

	@Test
	public void duplicatePositionBadTest() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputDupBadVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		ConvertToGeneStream gs = new ConvertToGeneStream(discover.findRelevantVariants(), false);
		Iterator<RelevantVariant> it = new ConvertBackToPositionalStream(gs.go(), gs.getPositionalOrder(), false).go();

		assertTrue(it.hasNext());

		try {
			it.hasNext();
			fail("No exception caught!");
		}
		catch (RuntimeException ex) {
			//assertEquals(ex.getCause().getClass(), RuntimeException.class);
			assertEquals(ex.getMessage(), "java.lang.Exception: Chrom-pos-ref-alt combination seen twice: 1_2_G_T. This is not allowed. Please check your VCF file.");
		}
	}

	@Test
	public void reversePositionTest() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputRevVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		ConvertToGeneStream gs = new ConvertToGeneStream(discover.findRelevantVariants(), false);
		Iterator<RelevantVariant> it = new ConvertBackToPositionalStream(gs.go(), gs.getPositionalOrder(), false).go();

		try {
			while(it.hasNext())
			{
				it.next();
			}
			fail("No exception caught!");
		}
		catch (RuntimeException ex) {
			//assertEquals(ex.getCause().getClass(), RuntimeException.class);
			assertEquals(ex.getMessage(), "java.lang.Exception: Site position 1 before 6 on the same chromosome (3) not allowed. Please sort your VCF file.");
		}
	}

	@Test
	public void chromBadTest() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputChromBadVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		ConvertToGeneStream gs = new ConvertToGeneStream(discover.findRelevantVariants(), false);
		Iterator<RelevantVariant> it = new ConvertBackToPositionalStream(gs.go(), gs.getPositionalOrder(), false).go();

		try {
			while(it.hasNext())
			{
				it.next();
			}
			fail("No exception caught!");
		}
		catch (RuntimeException ex) {
			//assertEquals(ex.getCause().getClass(), RuntimeException.class);
			assertEquals(ex.getMessage(), "java.lang.Exception: Chromosome 1 was interrupted by other chromosomes. Please sort your VCF file.");
		}
	}

}
