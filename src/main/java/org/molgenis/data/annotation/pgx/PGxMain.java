package org.molgenis.data.annotation.pgx;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;

import static java.util.Arrays.asList;

/**
 * Created by joeri
 *
 */
public class PGxMain
{
    public static void main(String[] args) throws Exception {
        OptionParser parser = createOptionParser();
        OptionSet options = parser.parse(args);
        new PGxMain().run(options, parser);
    }

    protected static OptionParser createOptionParser()
    {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("p", "pgx"), "VCF file with pharmacogenomic associations. Use 'DRUG' info field to group variants").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("i", "input"), "VCF file to be analyzed, with GT, GTC or AC fields").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("o", "output"), "Output file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("h", "help"), "Prints this help text");
        parser.acceptsAll(asList("r", "replace"), "Enables output VCF override, replacing a file with the same name as the argument for the -o option");

        return parser;
    }

    public void run(OptionSet options, OptionParser parser) throws Exception
    {
        if (options.has("input") && options.has("pgx") && options.has("output"))
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

        /**
         * Pgx vcf check
         */
        File pgxVcfFile = (File) options.valueOf("pgx");
        if (!pgxVcfFile.exists())
        {
            System.out.println("PGx VCF file not found at " + pgxVcfFile);
            return;
        }
        else if (pgxVcfFile.isDirectory())
        {
            System.out.println("PGx VCF file is a directory, not a file!");
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
         * Everything OK, start tool
         */
        System.out.println("Starting..");
        new PGxTool().start(inputVcfFile, pgxVcfFile, outputVCFFile);
        System.out.println("..done!");

    }
}
