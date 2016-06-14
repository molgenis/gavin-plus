package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores.Mode;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.io.File;
import java.util.List;

/**
 * Created by joeri on 6/1/16.
 *
 * Does input/output checks, and runs MakeRVCFforClinicalVariants.
 * In addition, we require the input VCF to be annotated with SnpEff, CADD (as much as possible), ExAC
 * [optionally GoNL and 1000G ?]
 * Check the headers and throw errors if incomplete.
 *
 * Info @
 * https://docs.google.com/presentation/d/1tT15JVKsModpxohIAhRSLW60zmexCg8wJZB2s5pc8vo/edit#slide=id.g1347b3c92c_0_0
 *
 *
 */
public class Run {

    public Run(File vcfFile, File gavinFile, File clinvarFile, File cgdFile, File caddFile, Mode mode) throws Exception
    {
        List<RelevantVariant> relevantVariants = new DiscoverRelevantVariants(vcfFile, gavinFile, clinvarFile, caddFile, mode).findRelevantVariants();
        System.out.println("found " + relevantVariants.size() + " interesting variants");

        //enhance relevant variants with sample genotype disease inheritance mode matches
        new MatchVariantsToGenotypeAndInheritance(relevantVariants, cgdFile).go();


//        for(RelevantVariant rv : relevantVariants)
//        {
//            System.out.println(rv.toString());
//        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 6)
        {
            throw new Exception("please provide: input vcf, gavin calibration, clinvar vcf, CGD file, cadd supplement file, mode ["+Mode.ANALYSIS+" or "+Mode.CREATEFILEFORCADD +"]");
        }

        File vcfFile = new File(args[0]);
        File gavinFile = new File(args[1]);
        File clinvarFile = new File(args[2]);
        File cgdFile = new File(args[3]);
        File caddFile = new File(args[4]);
        Mode mode = Mode.valueOf(args[5]);

        if(!vcfFile.isFile())
        {
            throw new Exception("VCF input file "+vcfFile+" does not exist or is directory");
        }
        if(!gavinFile.isFile())
        {
            throw new Exception("GAVIN file "+gavinFile+" does not exist or is directory");
        }
        if(!clinvarFile.isFile())
        {
            throw new Exception("clinvar VCF file "+clinvarFile+" does not exist or is directory");
        }
        if(!cgdFile.isFile())
        {
            throw new Exception("CGD VCF file "+cgdFile+" does not exist or is directory");
        }

        if(!mode.equals(Mode.ANALYSIS) && !mode.equals(Mode.CREATEFILEFORCADD))
        {
            throw new Exception("Mode must be '"+Mode.ANALYSIS+"' or '"+Mode.CREATEFILEFORCADD+"'");
        }

        if(mode.equals(Mode.ANALYSIS))
        {
            if (!caddFile.isFile())
            {
                throw new Exception("CADD file does not exist or directory: " + caddFile.getAbsolutePath());
            }
        }
        else if(mode.equals(Mode.CREATEFILEFORCADD))
        {
            if (caddFile.isFile())
            {
                throw new Exception("tried to create new CADD intermediate but already exists at " + caddFile.getAbsolutePath());
            }
        }

        new Run(vcfFile, gavinFile, clinvarFile, cgdFile, caddFile, mode);

    }

}
