package org.molgenis.data.annotation.makervcf;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

public class BadCaddWebserviceFile extends Setup
{

	protected File inputVcfFile;
	protected File cgdFile;
	protected File fdrFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {

	}

	@Test
	public void testGzInsteadOfTsv() throws Exception
	{
		//assertTrue(false);

//		Main.main(new String[]{"-i " + File.createTempFile("tmp", "vcf")});

//		new Pipeline();
//
//		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
//		Iterator<GavinRecord> match = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(), cgdFile, new HashSet<String>(), false).go();


	}

	@Test
	public void testBadTsv() throws Exception
	{
	//	assertTrue(false);



	}

}
