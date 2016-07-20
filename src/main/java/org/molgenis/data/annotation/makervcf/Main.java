package org.molgenis.data.annotation.makervcf;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.EnumUtils;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores.Mode;
import java.io.File;
import static java.util.Arrays.asList;

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
public class Main {


    public static void main(String[] args) throws Exception {
        OptionParser parser = createOptionParser();
        OptionSet options = parser.parse(args);
        new Main().run(options, parser);
    }

    protected static OptionParser createOptionParser()
    {
        OptionParser parser = new OptionParser();

        parser.acceptsAll(asList("i", "input"), "Input VCF file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("o", "output"), "Output RVCF file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("g", "gavin"), "GAVIN calibration file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("c", "clinvar"), "ClinVar pathogenic VCF file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("d", "cgd"), "CGD file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("f", "fdr"), "Gene-specific FDR file").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("a", "cadd"), "Input/output CADD missing annotations").withRequiredArg().ofType(File.class);
        parser.acceptsAll(asList("m", "mode"), "Create or use CADD file for missing annotations, either " + Mode.ANALYSIS.toString() + " or " + Mode.CREATEFILEFORCADD.toString()).withRequiredArg().ofType(String.class).defaultsTo(Mode.CREATEFILEFORCADD.toString());
        parser.acceptsAll(asList("v", "verbose"), "Verbally express what is happening underneath the programmatic hood."); //.withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE)
        parser.acceptsAll(asList("r", "replace"), "Enables output RVCF and CADD intermediate file override, replacing a file with the same name as the argument for the -o option");
        parser.acceptsAll(asList("h", "help"), "Prints this help text");

        return parser;
    }

    public void run(OptionSet options, OptionParser parser) throws Exception
    {
        if (!options.has("input") || options.has("help"))
        {
            System.out.println("\n" + "********************************************************************************\n"
                    + "* MOLGENIS RVCF Automated Diagnostics pipeline (MOLRAD), release 0.0.1-testing *\n"
                    + "********************************************************************************\n"
                    + "\n"
                    + "Finds potentially relevant clinical variants and matching samples within your input VCF.\n"
                    + "\n"
                    + "Global usage: java -jar MOLRAD-0.0.1-testing.jar [input] [output] [helperfiles] [mode/flags]\n"
                    + "\n"
                    + "Example: java -Xmx4g -jar MOLRAD-0.0.1-testing.jar -i patient76.snpeff.exac.caddsnv.vcf -o patient76_RVCF.vcf -g GAVIN_calibrations_r0.1.tsv -c clinvar.patho.fix.5.5.16.vcf.gz -g CGD_1jun2016.txt.gz -f exomePlus/FDR_allGenes.tsv -d fromCadd.tsv -m ANALYSIS\n"
                    + "\n"
                    + "PLEASE BE ADVISED:\n"
                    + "Typically, you want to first generate a file with any missing CADD annotations using '-d toCadd.tsv -m CREATEFILEFORCADD'\n"
                    + "After which, you want to score the variants in toCadd.tsv with the web service at http://cadd.gs.washington.edu/score\n"
                    + "The resulting file should be unpacked and then used for analysis with '-d fromCadd.tsv -m ANALYSIS'\n"
                    + "\n"
                    + "ALSO:\n"
                    + "The required helper files can be downloaded from: http://molgenis.org/downloads/gavin/bundle_r0.1/"
                    + "\n" + "----------------------------------------------------\n\n" + "Available options:\n");

            parser.printHelpOn(System.out);

            System.out.println("\n" + "----------------------------------------------------\n");

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
         * Check all kinds of files you need
         */
        File gavinFile = (File) options.valueOf("gavin");
        if (!gavinFile.exists())
        {
            System.out.println("GAVIN calibration file not found at " + inputVcfFile);
            return;
        }
        else if (gavinFile.isDirectory())
        {
            System.out.println("GAVIN calibration file is a directory, not a file!");
            return;
        }

        File clinvarFile = (File) options.valueOf("clinvar");
        if (!clinvarFile.exists())
        {
            System.out.println("ClinVar pathogenic VCF file not found at " + inputVcfFile);
            return;
        }
        else if (clinvarFile.isDirectory())
        {
            System.out.println("ClinVar pathogenic VCF file is a directory, not a file!");
            return;
        }

        File cgdFile = (File) options.valueOf("cgd");
        if (!cgdFile.exists())
        {
            System.out.println("CGD file not found at " + inputVcfFile);
            return;
        }
        else if (cgdFile.isDirectory())
        {
            System.out.println("CGD file is a directory, not a file!");
            return;
        }

        File FDRfile = (File) options.valueOf("fdr");
        if (!FDRfile.exists())
        {
            System.out.println("FDR file not found at " + inputVcfFile);
            return;
        }
        else if (FDRfile.isDirectory())
        {
            System.out.println("FDR file is a directory, not a file!");
            return;
        }

        /**
         * Check mode in combination with CADD file and replace
         */
        String modeString = (String) options.valueOf("mode");
        if(!EnumUtils.isValidEnum(Mode.class, modeString))
        {
            System.out.println("Mode must be one of the following: " + Mode.values().toString());
            return;
        }
        Mode mode = Mode.valueOf(modeString);

        File caddFile = (File) options.valueOf("cadd");
        if(mode == Mode.ANALYSIS)
        {
            if (!caddFile.exists())
            {
                System.out.println("CADD file not found at" + caddFile.getAbsolutePath());
                return;
            }
            else if (FDRfile.isDirectory())
            {
                System.out.println("CADD file is a directory, not a file!");
                return;
            }
        }
        else if(mode == Mode.CREATEFILEFORCADD)
        {
            if (caddFile.exists())
            {
                if (options.has("replace"))
                {
                    System.out.println("Override enabled, replacing existing CADD file with specified output: "
                            + caddFile.getAbsolutePath());
                }
                else
                {
                    System.out.println(
                            "CADD file already exists, please either enter a different output name or use the '-r' option to overwrite the output file.");
                    return;
                }
            }
        }

        /**
         * Verbose
         */
        boolean verbose = false;
        if(options.has("verbose"))
        {
            verbose = true;
        }

        /**
         * Everything OK, start pipeline
         */
        new Pipeline().start(inputVcfFile, gavinFile, clinvarFile, cgdFile, caddFile, FDRfile, mode, outputVCFFile, verbose);

    }

}
