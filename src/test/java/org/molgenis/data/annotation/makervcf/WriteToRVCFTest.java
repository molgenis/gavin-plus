package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;

/**
 * FIXME: Right now, we only test the content AND NOT THE HEADERS of the VCF files
 * <p>
 * The headers are not equal!
 * <p>
 * Expected would be this:
 * <p>
 * ##fileformat=VCFv4.1
 * ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
 * ##INFO=<ID=CADD_SCALED,Number=.,Type=Float,Description="Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use that value as a \"normalized\" and now externally comparable unit of analysis. In our case, we scored and ranked all ~8.6 billion SNVs of the GRCh37/hg19 reference and then \"PHRED-scaled\" those values by expressing the rank in order of magnitude terms rather than the precise rank itself. For example, reference genome single nucleotide variants at the 10th-% of CADD scores are assigned to CADD-10, top 1% to CADD-20, top 0.1% to CADD-30, etc. The results of this transformation are the \"scaled\" CADD scores.(source: http://cadd.gs.washington.edu/info)">
 * ##INFO=<ID=ANN,Number=.,Type=String,Description="Functional annotations: 'Allele | Annotation | Annotation_Impact | Gene_Name | Gene_ID | Feature_Type | Feature_ID | Transcript_BioType | Rank | HGVS.c | HGVS.p | cDNA.pos / cDNA.length | CDS.pos / CDS.length | AA.pos / AA.length | Distance | ERRORS / WARNINGS / INFO' ">
 * ##INFO=<ID=EXAC_AF,Number=.,Type=String,Description="The ExAC allele frequency">
 * ##INFO=<ID=GoNL_AF,Number=.,Type=String,Description="The allele frequency for variants seen in the population used for the GoNL project">
 * ##INFO=<ID=Thousand_Genomes_AF,Number=.,Type=String,Description="The allele frequency for variants seen in the population used for the thousand genomes project">
 * ##INFO=<ID=RLV,Number=.,Type=String,Description="Allele | AlleleFreq | Gene | FDR | Transcript | Phenotype | PhenotypeInheritance | PhenotypeOnset | PhenotypeDetails | PhenotypeGroup | SampleStatus | SamplePhenotype | SampleGenotype | SampleGroup | VariantSignificance | VariantSignificanceSource | VariantSignificanceJustification | VariantCompoundHet | VariantGroup">
 * <p>
 * instead, we find:
 * <p>
 * ##fileformat=VCFv4.1
 * ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
 * ##INFO=<ID=#CHROM,Number=.,Type=String,Description="The chromosome on which the variant is observed">
 * ##INFO=<ID=ALT,Number=.,Type=String,Description="The alternative allele observed">
 * ##INFO=<ID=POS,Number=.,Type=Float,Description="The position on the chromosome which the variant is observed">
 * ##INFO=<ID=REF,Number=.,Type=String,Description="The reference allele">
 * ##INFO=<ID=FILTER,Number=.,Type=String,Description="Description not provided">
 * ##INFO=<ID=QUAL,Number=.,Type=String,Description="Description not provided">
 * ##INFO=<ID=ID,Number=.,Type=String,Description="Description not provided">
 * ##INFO=<ID=INTERNAL_ID,Number=.,Type=String,Description="Description not provided">
 * ##INFO=<ID=CADD_SCALED,Number=.,Type=String,Description="Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use that value as a \">
 * ##INFO=<ID=ANN,Number=.,Type=String,Description="Functional annotations: 'Allele | Annotation | Annotation_Impact | Gene_Name | Gene_ID | Feature_Type | Feature_ID | Transcript_BioType | Rank | HGVS.c | HGVS.p | cDNA.pos / cDNA.length | CDS.pos / CDS.length | AA.pos / AA.length | Distance | ERRORS / WARNINGS / INFO' ">
 * ##INFO=<ID=EXAC_AF,Number=.,Type=String,Description="The ExAC allele frequency">
 * ##INFO=<ID=GoNL_AF,Number=.,Type=String,Description="The allele frequency for variants seen in the population used for the GoNL project">
 * ##INFO=<ID=Thousand_Genomes_AF,Number=.,Type=String,Description="The allele frequency for variants seen in the population used for the thousand genomes project">
 * ##INFO=<ID=RLV,Number=.,Type=String,Description="Allele | AlleleFreq | Gene | FDR | Transcript | Phenotype | PhenotypeInheritance | PhenotypeOnset | PhenotypeDetails | PhenotypeGroup | SampleStatus | SamplePhenotype | SampleGenotype | SampleGroup | VariantSignificance | VariantSignificanceSource | VariantSignificanceJustification | VariantCompoundHet | VariantGroup">
 * <p>
 * This probably wont cause problems but it still isn't very nice.
 */
public class WriteToRVCFTest extends Setup
{

	protected File inputVcfFile;
	protected File expectedOutputVcfFile;
	protected File observedOutputVcfFile;
	protected File cgdFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException
	{
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream(
				"/MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

		InputStream outputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream(
				"/WriteToRVCFTestExpectedOutput.vcf");
		expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "WriteToRVCFTestExpectedOutput.vcf");
		FileCopyUtils.copy(outputVcf, new FileOutputStream(expectedOutputVcfFile));

		observedOutputVcfFile = new File(FileUtils.getTempDirectory(), "outputVcfFile.vcf");
	}

	@Test
	public void test() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile,
				null, HandleMissingCaddScores.Mode.ANALYSIS);
		Iterator<GavinRecord> match = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(),
				cgdFile, new HashSet<String>()).go();

		new WriteToRVCF().writeRVCF(match, observedOutputVcfFile, inputVcfFile, "test","command",true);

		System.out.println("Going to compare files:\n" + expectedOutputVcfFile.getAbsolutePath() + "\nvs.\n"
				+ observedOutputVcfFile.getAbsolutePath());
		assertEquals(readVcfLinesWithoutHeader(observedOutputVcfFile),
				readVcfLinesWithoutHeader(expectedOutputVcfFile));

	}

	public ArrayList<String> readVcfLinesWithoutHeader(File vcf) throws FileNotFoundException
	{
		ArrayList<String> res = new ArrayList<>();
		Scanner s = new Scanner(vcf);
		while (s.hasNext())
		{
			String line = s.nextLine();
			if (line.startsWith("##"))
			{
				continue;
			}
			res.add(line);
		}
		return res;
	}
}
