package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertBackToPositionalStream;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.impl.*;
import org.molgenis.data.annotation.makervcf.positionalstream.*;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.datastructures.Trio;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by joeri on 7/18/16.
 */
public class Pipeline {

    private AttributeMetaData rlv = new DefaultAttributeMetaData(RVCF.attributeName).setDescription(RVCF.attributeMetaData);

    public void run(File inputVcfFile, File gavinFile, File clinvarFile, File cgdFile, File caddFile, File FDRfile, HandleMissingCaddScores.Mode mode, File outputVcfFile, boolean verbose) throws Exception
    {
        //get trios and parents if applicable
        HashMap<String, Trio> trios = TrioFilter.getTrios(inputVcfFile);
        Set<String> parents = TrioFilter.getParents(trios);

        //initial discovery of any suspected/likely pathogenic variant
        DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, mode, verbose);
        Iterator<RelevantVariant> rv1 = discover.findRelevantVariants();

        //MAF filter to control false positives / non relevant variants in ClinVar
        Iterator<RelevantVariant> rv2 = new MAFFilter(rv1, verbose).go();

        //match sample genotype with known disease inheritance mode
        Iterator<RelevantVariant> rv3 = new MatchVariantsToGenotypeAndInheritance(rv2, cgdFile, parents, verbose).go();

        //swap over stream from strict position-based to gene-based so we can do a number of things
        ConvertToGeneStream gs = new ConvertToGeneStream(rv3, verbose);
        Iterator<RelevantVariant> gsi = gs.go();

        //convert heterozygous/carrier status variants to compound heterozygous if they fall within the same gene
        Iterator<RelevantVariant> rv4 = new AssignCompoundHet(gsi, verbose).go();

        // TODO
        //if available: use any parental information to filter out variants/status
        TrioFilter tf = new TrioFilter(rv4, inputVcfFile, trios, parents, verbose);
        Iterator<RelevantVariant> rv5 = tf.go();

        //if available: use any phasing information to filter out compounds
        Iterator<RelevantVariant> rv6 = new PhasingCompoundCheck(rv5, verbose).go();

        // TODO
        //if available: use any SV data to give weight to carrier/heterozygous variants that may be complemented by a deleterious structural event
        Iterator<RelevantVariant> rv7 = new CombineWithSVcalls(rv6, verbose).go();

        //add gene-specific FDR based on 1000G and this pipeline
        Iterator<RelevantVariant> rv8 = new AddGeneFDR(rv7, FDRfile, verbose).go();

        //fix order in which variants are written out (was re-ordered by compoundhet check to gene-based)
        Iterator<RelevantVariant> rv9 = new ConvertBackToPositionalStream(rv8, gs.getPositionalOrder(), verbose).go();

        //cleanup stream by ditching variants without samples due to filtering
        Iterator<RelevantVariant> rv10 = new CleanupVariantsWithoutSamples(rv9, verbose).go();

        //write convert RVCF records to Entity
        Iterator<Entity> rve = new MakeRVCFforClinicalVariants(rv10, rlv).addRVCFfield();

        //write Entities output VCF file
        new WriteToRVCF().writeRVCF(rve, outputVcfFile, inputVcfFile, discover.getVcfMeta(), rlv, true);

    }
}
