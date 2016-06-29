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
        for(String s : trios.keySet())
        {
            System.out.println(s + " -> " + trios.get(s).toString());
        }

        //initial discovery of any suspected/likely pathogenic variant
        DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, mode);
        Iterator<RelevantVariant> relevantVariants = discover.findRelevantVariants();

        //match sample genotype with disease inheritance mode, trio, denovo, compound, phasing, todo
        Iterator<RelevantVariant> relevantVariantsGenoMatched = new MatchVariantsToGenotypeAndInheritance(relevantVariants, cgdFile).go();

        //convert heterozygous/carrier status variants to compound heterozygous if they fall within the same gene
        Iterator<RelevantVariant> relevantVariantsGenoMatchedCompHet = new AssignCompoundHeterozygous(relevantVariantsGenoMatched).go();

        //use any parental information to filter out variants/status
        Iterator<RelevantVariant> relevantVariantsGenoMatchedCompHetTriof = new TrioAwareFilter(relevantVariantsGenoMatchedCompHet).go();

        //write convert RVCF records to Entity
        Iterator<Entity> relevantVariantsGenoMatchedEntities = new MakeRVCFforClinicalVariants(relevantVariantsGenoMatchedCompHetTriof, rlv).addRVCFfield();

        //write Entities output VCF file
        writeRVCF(relevantVariantsGenoMatchedEntities, outputVcfFile, inputVcfFile, discover.getVcfMeta(), rlv);

    }

    public void writeRVCF(Iterator<Entity> relevantVariants, File writeTo, File inputVcfFile, EntityMetaData vcfMeta, AttributeMetaData rlv) throws IOException, MolgenisInvalidFormatException {

        List<AttributeMetaData> attributes = Lists.newArrayList(vcfMeta.getAttributes());
        attributes.add(rlv);
        FileWriter fw = new FileWriter(writeTo);
        BufferedWriter outputVCFWriter = new BufferedWriter(fw);
        VcfWriterUtils.writeVcfHeader(inputVcfFile, outputVCFWriter, attributes);

        while(relevantVariants.hasNext())
        {
            Entity e = relevantVariants.next();
           // System.out.println(rv.getVariant().getOrignalEntity().toString());
            VcfWriterUtils.writeToVcf(e, outputVCFWriter);
            outputVCFWriter.newLine();
        }
        outputVCFWriter.close();
    }

    public static void main(String[] args) throws Exception {

        // FOR DEVELOPMENT
        new File(args[6]).delete();

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
