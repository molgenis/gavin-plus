package org.molgenis.data.annotation.makervcf.genestream.impl;

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

    public TrioFilter(Iterator<RelevantVariant> relevantVariants, File inputVcfFile, boolean verbose) throws IOException {
        super(relevantVariants, verbose);
        this.trios = getTrios(inputVcfFile, verbose);
        System.out.println("[TrioFilter] Trios: " + trios);
    }

    public HashMap<String, Trio> getTrios(File inputVcfFile, boolean verbose) throws IOException {
        BufferedReader bufferedVCFReader = VcfWriterUtils.getBufferedVCFReader(inputVcfFile);
        return VcfUtils.getPedigree(bufferedVCFReader);
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {

        int variantIndex = 0;
        for(RelevantVariant rv : variantsPerGene) {
            int nrOfInterestingSamples = rv.getSampleStatus().size();

            char affectedIndex = Character.forDigit(rv.getVariant().getAltIndex(rv.getAllele()), 10);
            for (String sample : rv.getSampleStatus().keySet()) {
                if(trios.containsKey(sample))
                {
                    String childGeno = rv.getSampleGenotypes().get(sample);
                    String momId = trios.get(sample).getMother() != null ? trios.get(sample).getMother().getId() : null;
                    String motherGeno = rv.getSampleGenotypes().get(momId);
                    String dadId = trios.get(sample).getFather()  != null ? trios.get(sample).getFather().getId() : null;
                    String fatherGeno = rv.getSampleGenotypes().get(dadId);

                    System.out.println("[TrioFilter] Child "+sample+" has genotype " + childGeno + " mom: " + motherGeno + ", dad: " + fatherGeno);


                    if(motherGeno != null && childGeno.equals(motherGeno) || fatherGeno != null && childGeno.equals(fatherGeno))
                    {
                        System.out.println("[TrioFilter] child geno == father/mother geno, dropping");
                        //todo: drop ? update?
                        //todo: swapped genotypes (phases)
                        //todo: also remove parent(s) !!
                        nrOfInterestingSamples--;
                    }

                }
            }

            System.out.println("[TrioFilter] Interesting samples left after trio filter:" + nrOfInterestingSamples);
            variantIndex++;
        }
    }
}
