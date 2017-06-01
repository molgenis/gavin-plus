package org.molgenis.data.annotation.mergeback;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.data.annotation.makervcf.Pipeline;

import java.io.File;

import static java.util.Arrays.asList;

/**
 * Created by joeri on 10/13/16.
 */
public class MergeBackMain
{
    public static void main(String[] args) throws Exception {
        OptionParser parser = createOptionParser();
        OptionSet options = parser.parse(args);
        new MergeBackMain().run(options, parser);
    }

    protected static OptionParser createOptionParser()
    {
        OptionParser parser = new OptionParser();

        parser.acceptsAll(asList("i", "input"), "Your original VCF file containing all variants and genotypes").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("v", "rvcf"), "RVCF file of which non-relevant variants and genotypes were stripped").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("o", "output"), "Target output VCF file that merges original VCF file with annotations from the RVCF file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("h", "help"), "Prints this help text");
        parser.acceptsAll(asList("r", "replace"), "Enables output VCF override, replacing a file with the same name as the argument for the -o option");


        return parser;
    }

    public void run(OptionSet options, OptionParser parser) throws Exception
    {
        if (options.has("input") && options.has("output") && options.has("rvcf"))
        {
            System.out.println("Arguments OK.");
        }
        else if(options.has("help"))
        {
            System.out.println("Help page requested.");
            parser.printHelpOn(System.out);
            return;
        }
        else
        {
            System.out.println("Bad arguments. Showing help:");
            parser.printHelpOn(System.out);
            return;
        }

        /**
         * Input check
         */
        File inputVcfFile = (File) options.valueOf("input");
        if (!inputVcfFile.exists())
        {
            System.out.println("Input VCF file not found at " + inputVcfFile);
            return;
        }
        else if (inputVcfFile.isDirectory())
        {
            System.out.println("Input VCF file is a directory, not a file!");
            return;
        }
        File rvcfFile = (File) options.valueOf("rvcf");
        if (!rvcfFile.exists())
        {
            System.out.println("RVCF file not found at " + rvcfFile);
            return;
        }
        else if (rvcfFile.isDirectory())
        {
            System.out.println("RVCF file is a directory, not a file!");
            return;
        }

        /**
         * Output and replace check
         */
        File outputVCFFile = (File) options.valueOf("output");
        if (outputVCFFile.exists())
        {
            if (options.has("replace"))
            {
                System.out.println("Override enabled, replacing existing output RVCF file with specified output: "
                        + outputVCFFile.getAbsolutePath());
            }
            else
            {
                System.out.println(
                        "Output RVCF file already exists, please either enter a different output name or use the '-r' option to overwrite the output file.");
                return;
            }
        }

        /**
         * Everything OK, start pipeline
         */
        System.out.println("Starting..");
        new MergeBackTool().start(inputVcfFile, rvcfFile, outputVCFFile);
        System.out.println("..done!");

    }
}
