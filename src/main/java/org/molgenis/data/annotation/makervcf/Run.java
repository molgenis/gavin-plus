package org.molgenis.data.annotation.makervcf;

import com.google.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.makervcf.genestream.impl.AssignCompoundHet;
import org.molgenis.data.annotation.makervcf.genestream.impl.CombineWithSVcalls;
import org.molgenis.data.annotation.makervcf.genestream.impl.PhasingCompoundCheck;
import org.molgenis.data.annotation.makervcf.genestream.impl.TrioFilter;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores.Mode;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertBackToPositionalStream;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MAFFilter;
import org.molgenis.data.annotation.makervcf.positionalstream.MakeRVCFforClinicalVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.data.vcf.utils.VcfWriterUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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

    public static void main(String[] args) throws Exception {

        // FOR DEVELOPMENT, delete output file and 'file for cadd'
        new File(args[6]).delete();
        if(Mode.valueOf(args[5]).equals(Mode.CREATEFILEFORCADD))  {new File(args[4]).delete();}

        if(args.length != 8)
        {
            throw new Exception("please provide: input VCF file, GAVIN calibration file, ClinVar VCF file, CGD file, CADD supplement file, mode ["+Mode.ANALYSIS+" or "+Mode.CREATEFILEFORCADD +"], output VCF file, verbose TRUE/FALSE");
        }

        File inputVcfFile = new File(args[0]);
        File gavinFile = new File(args[1]);
        File clinvarFile = new File(args[2]);
        File cgdFile = new File(args[3]);
        File caddFile = new File(args[4]);
        Mode mode = Mode.valueOf(args[5]);
        File outputVcfFile = new File(args[6]);
        boolean verbose = Boolean.parseBoolean(args[7]);

        if(!inputVcfFile.isFile())
        {
            throw new Exception("Input VCF file "+inputVcfFile+" does not exist or is directory");
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

        if (outputVcfFile.isFile())
        {
            throw new Exception("output VCF file "+outputVcfFile.getAbsolutePath()+" already exists, deleting !");
        }

        new Pipeline().run(inputVcfFile, gavinFile, clinvarFile, cgdFile, caddFile, mode, outputVcfFile, verbose);

    }

}
