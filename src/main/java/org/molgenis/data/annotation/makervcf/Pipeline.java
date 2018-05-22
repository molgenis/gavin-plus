package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.genestream.core.ConvertBackToPositionalStream;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.impl.*;
import org.molgenis.data.annotation.makervcf.positionalstream.*;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.TrioData;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;

import java.io.File;
import java.util.Iterator;

/**
 * Created by joeri on 7/18/16.
 */
public class Pipeline
{

	public void start(File inputVcfFile, File gavinFile, File clinvarFile, File cgdFile, File caddFile, File FDRfile,
			HandleMissingCaddScores.Mode mode, File outputVcfFile, File labVariants, boolean verbose) throws Exception
	{
		//get trios and parents if applicable
		TrioData td = TrioFilter.getTrioData(inputVcfFile);

		//initial discovery of any suspected/likely pathogenic variant
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile,
				labVariants, mode, verbose);
		Iterator<GavinRecord> rv1 = discover.findRelevantVariants();

		//MAF filter to control false positives / non relevant variants in ClinVar
		Iterator<GavinRecord> rv2 = new MAFFilter(rv1, verbose).go();

		//match sample genotype with known disease inheritance mode
		Iterator<GavinRecord> rv3 = new MatchVariantsToGenotypeAndInheritance(rv2, cgdFile, td.getParents(),
				verbose).go();

		//swap over stream from strict position-based to gene-based so we can do a number of things
		ConvertToGeneStream gs = new ConvertToGeneStream(rv3, verbose);
		Iterator<GavinRecord> gsi = gs.go();

		//convert heterozygous/carrier status variants to compound heterozygous if they fall within the same gene
		Iterator<GavinRecord> rv4 = new AssignCompoundHet(gsi, verbose).go();

		//if available: use any parental information to filter out variants/status
		TrioFilter tf = new TrioFilter(rv4, td, verbose);
		Iterator<GavinRecord> rv5 = tf.go();

		//if available: use any phasing information to filter out compounds
		Iterator<GavinRecord> rv6 = new PhasingCompoundCheck(rv5, verbose).go();

		// TODO
		//if available: use any SV data to give weight to carrier/heterozygous variants that may be complemented by a deleterious structural event
		Iterator<GavinRecord> rv7 = new CombineWithSVcalls(rv6, verbose).go();

		//add gene-specific FDR based on 1000G and this pipeline
		Iterator<GavinRecord> rv8 = new AddGeneFDR(rv7, FDRfile, verbose).go();

		//fix order in which variants are written out (was re-ordered by compoundhet check to gene-based)
		Iterator<GavinRecord> rv9 = new ConvertBackToPositionalStream(rv8, gs.getPositionalOrder(), verbose).go();

		//cleanup stream by ditching variants without samples due to filtering
		Iterator<GavinRecord> rv10 = new CleanupVariantsWithoutSamples(rv9, verbose).go();

		//write convert RVCF records to Entity
		Iterator<GavinRecord> rve = new MakeRVCFforClinicalVariants(rv10, verbose).addRVCFfield();

		//write Entities output VCF file
		new WriteToRVCF().writeRVCF(rve, outputVcfFile, inputVcfFile,true, verbose);

	}
}
