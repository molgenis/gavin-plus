package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.impl.AssignCompoundHet;
import org.molgenis.data.annotation.makervcf.genestream.impl.PhasingCompoundCheck;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.RelevanceUtils;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

import static org.testng.Assert.assertTrue;

public class PhasingCompoundCheckTest extends Setup
{

	protected File inputVcfFile;
	protected File cgdFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/AssignCompoundHetTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "AssignCompoundHetTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));
		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

	}

	@Test
	public void test() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<GavinRecord> rv3 = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(), cgdFile, new HashSet<String>(), false).go();
		ConvertToGeneStream gs = new ConvertToGeneStream(rv3, false);
		Iterator<GavinRecord> gsi = gs.go();
		Iterator<GavinRecord> assignCompHet = new AssignCompoundHet(gsi, false).go();

		Iterator<GavinRecord> it = new PhasingCompoundCheck(assignCompHet, true).go();

		// AIMP1
		assertTrue(it.hasNext());
		assertTrue(!it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01"));
		assertTrue(!it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02"));
		assertTrue(it.hasNext());
		assertTrue(!it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01"));
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02=CARRIER"));

		// ADCY6
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HETEROZYGOUS_MULTIHIT, p02=AFFECTED_COMPOUNDHET"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HETEROZYGOUS_MULTIHIT, p02=AFFECTED_COMPOUNDHET"));

		// AK1
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED_COMPOUNDHET, p02=HETEROZYGOUS_MULTIHIT"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED_COMPOUNDHET, p02=HETEROZYGOUS_MULTIHIT"));

		// AK2
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HETEROZYGOUS_MULTIHIT, p02=HETEROZYGOUS_MULTIHIT"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HETEROZYGOUS_MULTIHIT, p02=HETEROZYGOUS_MULTIHIT"));

		// ALAD
		//part phased, part uncertain, here we are greedy
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED_COMPOUNDHET, p02=AFFECTED_COMPOUNDHET"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED_COMPOUNDHET, p02=AFFECTED_COMPOUNDHET"));

		// ALG1
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=CARRIER"));
		assertTrue(!it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02"));
		assertTrue(it.hasNext());
		assertTrue(!it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01"));
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02=CARRIER"));

		// ALG6
		//no phasing, greedy match
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED_COMPOUNDHET, p02=AFFECTED_COMPOUNDHET"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED_COMPOUNDHET, p02=AFFECTED_COMPOUNDHET"));

		// OverlapA and OverlapB check to find out if overlapping genes correctly have compounds detected
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HOMOZYGOUS_COMPOUNDHET, p02=HETEROZYGOUS_MULTIHIT"));
		assertTrue(it.hasNext());
		assertTrue(RelevanceUtils.getRelevanceForGene(it.next().getRelevance(), "OverlapA").getSampleStatus().toString().contains("p01=HOMOZYGOUS_COMPOUNDHET, p02=HETEROZYGOUS_MULTIHIT"));
		assertTrue(RelevanceUtils.getRelevanceForGene(it.next().getRelevance(), "OverlapB").getSampleStatus().toString().contains("p01=HETEROZYGOUS_MULTIHIT, p02=HOMOZYGOUS_COMPOUNDHET"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HETEROZYGOUS_MULTIHIT, p02=HOMOZYGOUS_COMPOUNDHET"));

		// ALG8
		//double homozygous in AR gene so affected
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED, p02=AFFECTED"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED, p02=AFFECTED"));

		// ALG9
		//single homozygous in AR gene so affected
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=CARRIER"));
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02=AFFECTED"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=AFFECTED"));
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02=CARRIER"));

		// unknown1
		//unknown gene, p01 homozygous by compound, p02 heterozygous
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HOMOZYGOUS_COMPOUNDHET, p02=HETEROZYGOUS"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HOMOZYGOUS_COMPOUNDHET"));
		assertTrue(!it.next().getRelevance().get(0).getSampleStatus().toString().contains("p02"));

		// unknown2
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HOMOZYGOUS, p02=HOMOZYGOUS"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getSampleStatus().toString().contains("p01=HETEROZYGOUS, p02=HOMOZYGOUS"));

		assertTrue(!it.hasNext());

	}

}
