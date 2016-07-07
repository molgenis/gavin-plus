package org.molgenis.data.annotation.makervcf;

import com.google.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.makervcf.cadd.HandleMissingCaddScores.Mode;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.data.vcf.utils.VcfWriterUtils;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.meta.VcfMeta;

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

    private AttributeMetaData rlv = new DefaultAttributeMetaData(RVCF.attributeName).setDescription(RVCF.attributeMetaData);

    public Run(File inputVcfFile, File gavinFile, File clinvarFile, File cgdFile, File caddFile, Mode mode, File outputVcfFile) throws Exception
    {
        HashMap<String, Trio> trios = MatchVariantsToGenotypeAndInheritance.getTrios(inputVcfFile);
//        for(String s : trios.keySet())
//        {
//            System.out.println(s + " -> " + trios.get(s).toString());
//        }

        //initial discovery of any suspected/likely pathogenic variant
        DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, mode);
        Iterator<RelevantVariant> rv1 = discover.findRelevantVariants();

        //MAF filter to control false positives / non relevant variants in ClinVar
        Iterator<RelevantVariant> rv2 = new MAFFilter(rv1).go();

        //match sample genotype with known disease inheritance mode
        Iterator<RelevantVariant> rv3 = new MatchVariantsToGenotypeAndInheritance(rv2, cgdFile).go();

        //convert heterozygous/carrier status variants to compound heterozygous if they fall within the same gene
        AssignCompoundHeterozygous compHet = new AssignCompoundHeterozygous(rv3);
        Iterator<RelevantVariant> rv4 = compHet.go();

        //use any parental information to filter out variants/status TODO - at least phasing info !!
        Iterator<RelevantVariant> rv5 = new TrioAwareFilter(rv4, trios).go();

        //report hits per gene, right before the stream is swapped from 'gene based' to 'position based'
        Iterator<RelevantVariant> rv6 = new ReportCandidatesPerGene(rv5).go(); //todo

        //fix order in which variants are written out (was re-ordered by compoundhet check to gene-based)
        Iterator<RelevantVariant> rv7 = new CorrectPositionalOrderIterator(rv6, compHet.getPositionalOrder()).go();

        //write convert RVCF records to Entity
        Iterator<Entity> rve = new MakeRVCFforClinicalVariants(rv7, rlv).addRVCFfield();

        //write Entities output VCF file
        writeRVCF(rve, outputVcfFile, inputVcfFile, discover.getVcfMeta(), rlv, false);

    }

    public void writeRVCF(Iterator<Entity> relevantVariants, File writeTo, File inputVcfFile, EntityMetaData vcfMeta, AttributeMetaData rlv, boolean writeToDisk) throws IOException, MolgenisInvalidFormatException {

        List<AttributeMetaData> attributes = Lists.newArrayList(vcfMeta.getAttributes());
        attributes.add(rlv);
        FileWriter fw = new FileWriter(writeTo);
        BufferedWriter outputVCFWriter = new BufferedWriter(fw);
        VcfWriterUtils.writeVcfHeader(inputVcfFile, outputVCFWriter, attributes);

        while(relevantVariants.hasNext())
        {
            Entity e = relevantVariants.next();

            if(writeToDisk)
            {
                VcfWriterUtils.writeToVcf(e, outputVCFWriter);
                outputVCFWriter.newLine();
            }
        }
        outputVCFWriter.flush();
        outputVCFWriter.close();
    }

    public static void main(String[] args) throws Exception {

        // FOR DEVELOPMENT, delete output file and 'file for cadd'
        new File(args[6]).delete();
        if(Mode.valueOf(args[5]).equals(Mode.CREATEFILEFORCADD))  {new File(args[4]).delete();}

        if(args.length != 7)
        {
            throw new Exception("please provide: input VCF file, GAVIN calibration file, ClinVar VCF file, CGD file, CADD supplement file, mode ["+Mode.ANALYSIS+" or "+Mode.CREATEFILEFORCADD +"], output VCF file");
        }

        File inputVcfFile = new File(args[0]);
        File gavinFile = new File(args[1]);
        File clinvarFile = new File(args[2]);
        File cgdFile = new File(args[3]);
        File caddFile = new File(args[4]);
        Mode mode = Mode.valueOf(args[5]);
        File outputVcfFile = new File(args[6]);

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

        new Run(inputVcfFile, gavinFile, clinvarFile, cgdFile, caddFile, mode, outputVcfFile);

    }

}
