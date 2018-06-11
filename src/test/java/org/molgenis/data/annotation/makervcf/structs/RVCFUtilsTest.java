package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.molgenis.data.annotation.makervcf.structs.RVCF.*;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.RLV_VARIANTGROUP;
import static org.testng.Assert.*;

public class RVCFUtilsTest
{
	private RVCF rvcf1;

	@BeforeMethod
	public void setUp(){
		Map<String,MatchVariantsToGenotypeAndInheritance.Status> sampleStatus = new HashMap<>();
		sampleStatus.put("1",MatchVariantsToGenotypeAndInheritance.Status.CARRIER);
		sampleStatus.put("2",MatchVariantsToGenotypeAndInheritance.Status.AFFECTED);
		Map<String,String> sampleGenotype = new HashMap<>();
		sampleGenotype.put("1","1|0");
		sampleGenotype.put("2","1|1");
		Map<String,String> samplePhenotype = new HashMap<>();
		samplePhenotype.put("1","pheno1");
		samplePhenotype.put("2","pheno2");
		Map<String,String> sampleGroup = new HashMap<>();
		sampleGroup.put("1","group1");
		sampleGroup.put("2","group2");

		rvcf1 = new RVCF("allele","alleleFreq","gene","FDR","transcript", "",
				"phenotypeInheritance", "phenotypeOnset", "phenotypeDetails", "phenotypeGroup",
				sampleStatus, samplePhenotype, sampleGenotype, sampleGroup, "variantSignificance", "variantSignificanceSource",
				"variantSignificanceJustification", "variantMultiGenic", "variantGroup"); }

	@Test
	public void testGetMergedFieldVcfString()
	{
		assertEquals(RVCFUtils.getMergedFieldVcfString(rvcf1),"allele|alleleFreq|gene|FDR|transcript||phenotypeInheritance|phenotypeOnset|phenotypeDetails|phenotypeGroup|1:CARRIER/2:AFFECTED|1:pheno1/2:pheno2|1:1p0/2:1p1|1:group1/2:group2|variantSignificance|variantSignificanceSource|variantSignificanceJustification|variantMultiGenic|variantGroup");
	}

	@Test
	public void testCreateRvcfInfoFields()
	{
		Map<String, String> expected = new HashMap<>();
		expected.put(RLV_PRESENT, "[allele|gene]TRUE");
		expected.put(RLV_ALLELE, "[allele|gene]allele");
		expected.put(RLV_ALLELEFREQ, "[allele|gene]alleleFreq");
		expected.put(RLV_GENE, "[allele|gene]gene");
		expected.put(RLV_FDR, "[allele|gene]FDR");
		expected.put(RLV_TRANSCRIPT, "[allele|gene]transcript");
		expected.put(RLV_PHENOTYPE, ".");
		expected.put(RLV_PHENOTYPEINHERITANCE, "[allele|gene]phenotypeInheritance");
		expected.put(RLV_PHENOTYPEONSET, "[allele|gene]phenotypeOnset");
		expected.put(RLV_PHENOTYPEDETAILS, "[allele|gene]phenotypeDetails");
		expected.put(RLV_PHENOTYPEGROUP, "[allele|gene]phenotypeGroup");
		expected.put(RLV_SAMPLESTATUS, "[allele|gene]1:CARRIER_2:AFFECTED");
		expected.put(RLV_SAMPLEPHENOTYPE, "[allele|gene]1:pheno1_2:pheno2");
		expected.put(RLV_SAMPLEGENOTYPE, "[allele|gene]1:1_0_2:1_1");
		expected.put(RLV_SAMPLEGROUP, "[allele|gene]1:group1_2:group2");
		expected.put(RLV_VARIANTSIGNIFICANCE, "[allele|gene]variantSignificance");
		expected.put(RLV_VARIANTSIGNIFICANCESOURCE, "[allele|gene]variantSignificanceSource");
		expected.put(RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "[allele|gene]variantSignificanceJustification");
		expected.put(RLV_VARIANTCOMPOUNDHET, "[allele|gene]variantMultiGenic");
		expected.put(RLV_VARIANTGROUP, "[allele|gene]variantGroup");

		assertEquals(RVCFUtils.createRvcfValues(rvcf1, Collections.emptyMap()),expected);
	}
	@Test
	public void testCreateRvcfInfoFieldsExistingValues()
	{
		Map<String, String> expected = new HashMap<>();
		expected.put(RLV_PRESENT, "PRESENT,[allele|gene]TRUE");
		expected.put(RLV_ALLELE, "ALLELE,[allele|gene]allele");
		expected.put(RLV_ALLELEFREQ, "ALLELEFREQ,[allele|gene]alleleFreq");
		expected.put(RLV_GENE, "GENE,[allele|gene]gene");
		expected.put(RLV_FDR, ",[allele|gene]FDR");
		expected.put(RLV_TRANSCRIPT, "TRANSCRIPT,[allele|gene]transcript");
		expected.put(RLV_PHENOTYPE, "PHENOTYPE,");
		expected.put(RLV_PHENOTYPEINHERITANCE, "PHENOTYPEINHERITANCE,[allele|gene]phenotypeInheritance");
		expected.put(RLV_PHENOTYPEONSET, "PHENOTYPEONSET,[allele|gene]phenotypeOnset");
		expected.put(RLV_PHENOTYPEDETAILS, "PHENOTYPEDETAILS,[allele|gene]phenotypeDetails");
		expected.put(RLV_PHENOTYPEGROUP, "PHENOTYPEGROUP,[allele|gene]phenotypeGroup");
		expected.put(RLV_SAMPLESTATUS, "SAMPLESTATUS,[allele|gene]1:CARRIER_2:AFFECTED");
		expected.put(RLV_SAMPLEPHENOTYPE, "SAMPLEPHENOTYPE,[allele|gene]1:pheno1_2:pheno2");
		expected.put(RLV_SAMPLEGENOTYPE, "SAMPLEGENOTYPE,[allele|gene]1:1_0_2:1_1");
		expected.put(RLV_SAMPLEGROUP, "SAMPLEGROUP,[allele|gene]1:group1_2:group2");
		expected.put(RLV_VARIANTSIGNIFICANCE, "VARIANTSIGNIFICANCE,[allele|gene]variantSignificance");
		expected.put(RLV_VARIANTSIGNIFICANCESOURCE, "VARIANTSIGNIFICANCESOURCE,[allele|gene]variantSignificanceSource");
		expected.put(RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "VARIANTSIGNIFICANCEJUSTIFICATION,[allele|gene]variantSignificanceJustification");
		expected.put(RLV_VARIANTCOMPOUNDHET, "VARIANTCOMPOUNDHET,[allele|gene]variantMultiGenic");
		expected.put(RLV_VARIANTGROUP, "VARIANTGROUP,[allele|gene]variantGroup");

		Map<String, String> existing = new HashMap<>();
		existing.put(RLV_PRESENT, "PRESENT");
		existing.put(RLV_ALLELE, "ALLELE");
		existing.put(RLV_ALLELEFREQ, "ALLELEFREQ");
		existing.put(RLV_GENE, "GENE");
		existing.put(RLV_FDR, ".");
		existing.put(RLV_TRANSCRIPT, "TRANSCRIPT");
		existing.put(RLV_PHENOTYPE, "PHENOTYPE");
		existing.put(RLV_PHENOTYPEINHERITANCE, "PHENOTYPEINHERITANCE");
		existing.put(RLV_PHENOTYPEONSET, "PHENOTYPEONSET");
		existing.put(RLV_PHENOTYPEDETAILS, "PHENOTYPEDETAILS");
		existing.put(RLV_PHENOTYPEGROUP, "PHENOTYPEGROUP");
		existing.put(RLV_SAMPLESTATUS, "SAMPLESTATUS");
		existing.put(RLV_SAMPLEPHENOTYPE, "SAMPLEPHENOTYPE");
		existing.put(RLV_SAMPLEGENOTYPE, "SAMPLEGENOTYPE");
		existing.put(RLV_SAMPLEGROUP, "SAMPLEGROUP");
		existing.put(RLV_VARIANTSIGNIFICANCE, "VARIANTSIGNIFICANCE");
		existing.put(RLV_VARIANTSIGNIFICANCESOURCE, "VARIANTSIGNIFICANCESOURCE");
		existing.put(RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "VARIANTSIGNIFICANCEJUSTIFICATION");
		existing.put(RLV_VARIANTCOMPOUNDHET, "VARIANTCOMPOUNDHET");
		existing.put(RLV_VARIANTGROUP, "VARIANTGROUP");

		assertEquals(RVCFUtils.createRvcfValues(rvcf1, existing),expected);
	}

}