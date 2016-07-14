package org.molgenis.data.annotation.makervcf.genestream;

import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.vcf.datastructures.Trio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.data.vcf.utils.VcfWriterUtils;

/**
 * Created by joeri on 6/29/16.
 *
 * find and mark denovo
 * remove variants that are no longer relevant, ie that have an equal parental genotype
 *
 *
 * TODO
 *
 */
public class TrioFilter extends GeneStream{


    private HashMap<String, Trio> trios;

    public TrioFilter(Iterator<RelevantVariant> relevantVariants, HashMap<String, Trio> trios, boolean verbose)
    {
        super(relevantVariants, verbose);
        this.trios = trios;
    }

    public static HashMap<String, Trio> getTrios(File inputVcfFile, boolean verbose) throws IOException {
        BufferedReader bufferedVCFReader = VcfWriterUtils.getBufferedVCFReader(inputVcfFile);
        return VcfUtils.getPedigree(bufferedVCFReader);
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {

    }
}
