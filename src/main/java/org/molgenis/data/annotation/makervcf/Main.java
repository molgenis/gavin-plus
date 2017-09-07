package org.molgenis.data.annotation.makervcf;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores.Mode;
import org.molgenis.data.annotation.makervcf.util.RvcfGenoRestore;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;

/**
 * Created by joeri on 6/1/16.
 *
 * High-performance automated variant interpretation
 *
 * Apply GAVIN, knowledge from ClinVar, CGD, variants from your own lab, structural variation,
 * rules of inheritance, and so on to find interesting variants and samples within a VCF file.
 *
 * Output is written to a VCF with all relevant variants where information is contained in the RLV field.
 * We require the input VCF to be annotated with SnpEff, CADD (as much as possible), ExAC, GoNL and 1000G.
 *
 * Tests TODO:
 * - Different ways to discover variants (clinvar, lab list, gavin)
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
        parser.acceptsAll(asList("l", "lab"), "VCF file with lab specific variant classifications").withOptionalArg().ofType(File.class);
        parser.acceptsAll(asList("s", "sv"), "[not available] Structural variation VCF file outputted by Delly, Manta or compatible").withOptionalArg().ofType(File.class);
        parser.acceptsAll(asList("m", "mode"), "Create or use CADD file for missing annotations, either " + Mode.ANALYSIS.toString() + " or " + Mode.CREATEFILEFORCADD.toString()).withRequiredArg().ofType(String.class);
        parser.acceptsAll(asList("v", "verbose"), "Verbally express what is happening underneath the programmatic hood.");
        parser.acceptsAll(asList("r", "replace"), "Enables output RVCF and CADD intermediate file override, replacing a file with the same name as the argument for the -o option");
        parser.acceptsAll(asList("h", "help"), "Prints this help text");
        parser.acceptsAll(asList("e", "restore"), "[not available] Supporting tool. Combine RVCF results with original VCF.").withOptionalArg().ofType(File.class);

        return parser;
    }

    public void printHelp(String version, String title, OptionParser parser) throws IOException {
        System.out.println(""
                + "Detect likely relevant clinical variants and matching samples in a VCF file.\n"
                + "Your input VCF must be fully annotated with SnpEff, ExAC frequencies and CADD scores, and optionally frequencies from GoNL and 1000G.\n"
                + "This can be done with MOLGENIS CmdlineAnnotator, available at https://github.com/molgenis/molgenis/releases/download/v1.21.1/CmdLineAnnotator-1.21.1.jar\n"
                + "\n"
                + "-- PLEASE BE ADVISED --\n"
                + "This is the first production version. Crashed and bugs may still happen. Please report them at https://github.com/molgenis/rvcf\n"
                + "\n"
                + "Typical usage: java -jar GAVIN-APP-"+version+".jar [inputfile] [outputfile] [helperfiles] [mode/flags]\n"
                + "Example usage:\n"
                + "java -Xmx4g -jar GAVIN-APP-"+version+".jar \\\n"
                + "-i patient76.snpeff.exac.gonl.caddsnv.vcf \\\n"
                + "-o patient76_RVCF.vcf \\\n"
                + "-g GAVIN_calibrations_r0.3.tsv \\\n"
                + "-c clinvar.patho.fix.11oct2016.vcf.gz \\\n"
                + "-d CGD_11oct2016.txt.gz \\\n"
                + "-f FDR_allGenes_r1.0.tsv \\\n"
                + "-a fromCadd.tsv \\\n"
                + "-m ANALYSIS \n"
                + "\n"
                + "Dealing with CADD intermediate files:\n"
                + "You first want to generate a intermediate file with any missing CADD annotations using '-d toCadd.tsv -m CREATEFILEFORCADD'\n"
                + "After which, you want to score the variants in toCadd.tsv with the web service at http://cadd.gs.washington.edu/score\n"
                + "The resulting scored file should be unpacked and then used for analysis with '-d fromCadd.tsv -m ANALYSIS'\n"
                + "\n"
                + "Details on the various helper files:\n"
                + "The required helper files for -g, -c, -d and -f can be downloaded from: http://molgenis.org/downloads/gavin at 'data_bundle'.\n"
                + "The -a file is either produced by the analysis (using -m CREATEFILEFORCADD) or used as an existing file (using -m ANALYSIS).\n"
                + "The -l is a user-supplied VCF of interpreted variants. Use 'CLSF=LP' or 'CLSF=P' as info field to denote (likely) pathogenic variants.\n"
                + "\n"
                + "Using pedigree data for filtering:\n"
                + "Please use the standard PEDIGREE notation in your VCF header, e.g. '##PEDIGREE=<Child=p01,Mother=p02,Father=p03>'. Trios and duos are allowed.\n"
                + "Parents are assumed unaffected, children affected. Using complex family trees, grandparents and siblings is not yet supported.\n"
                + "\n"
                + "Some other notes:\n"
                + "Phased genotypes are used to remove obvious false compound heterozygous hits. These are demoted to heterozygous multihit.\n"
                + "If GoNL annotations are provided, variants above 5% MAF are removed as presumed false positives (in addition to ExAC >5%).\n"
                + "The gene FDR values are based on 2,504 individuals from The 1000 Genomes project and may be used as a general indication of significance -\n"
                + "however - high FDR values may be caused by either faulty detection OR false positives from the low-coverage sequencing data.\n"
                + StringUtils.repeat('-', title.length()) + "\n"
                + "\n"
                + "Available options:\n");

        parser.printHelpOn(System.out);

        System.out.println("\n" + StringUtils.repeat('-', title.length()) + "\n");
    }

    public void run(OptionSet options, OptionParser parser) throws Exception
    {
        String version = "1.0";
        String title = "* MOLGENIS GAVIN+ for genome diagnostics, release "+version+"";
        String titl2 = "* Gene-Aware Variant INterpretation Plus";

        int len = Math.max(title.length(), titl2.length());
        String appTitle = "\n" + StringUtils.repeat('*', len) + "\n"
                + title + "\n"
                + titl2 + "\n"
                + StringUtils.repeat('*', len) + "\n";

        System.out.println(appTitle);

        if (
             (options.has("restore") && options.has("input") && options.has("output")) ||
             (options.has("input") && options.has("output") && options.has("gavin") && options.has("clinvar") && options.has("cgd") && options.has("fdr") && options.has("cadd") && options.has("mode"))
           )
        {
            System.out.println("Arguments OK.");
        }
        else if(options.has("help"))
        {
            System.out.println("Help page requested.");
            printHelp(version, title, parser);
            return;
        }
        else
        {
            System.out.println("Bad arguments. Showing help:");
            printHelp(version, title, parser);
            return;
        }

        /**************
         * "Restore mode" where we add back genotypes in an RVCF
         *************/
        if(options.has("restore"))
        {
            System.out.println("Restore mode not yet supported!");
            return;
//
//            File inputRVCFFile = (File) options.valueOf("restore");
//            if (!inputRVCFFile.exists())
//            {
//                System.out.println("Input RVCF file not found at " + inputRVCFFile);
//                return;
//            }
//
//            File outputRVCFFile = (File) options.valueOf("output");
//            if (outputRVCFFile.exists())
//            {
//                if (options.has("replace"))
//                {
//                    System.out.println("Override enabled, replacing existing output RVCF file with specified output: "
//                            + outputRVCFFile.getAbsolutePath());
//                }
//                else
//                {
//                    System.out.println(
//                            "Output RVCF file already exists, please either enter a different output name or use the '-r' option to overwrite the output file.");
//                    return;
//                }
//            }
//
//            boolean verbose = false;
//            if(options.has("verbose"))
//            {
//                verbose = true;
//            }
//
//            System.out.println("Starting RVCF genotype restore..");
//            new RvcfGenoRestore(inputRVCFFile, outputRVCFFile, verbose);
//            System.out.println("..done!");
//            return;
        }


        /**************
         * Regular mode
         *************/

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
            System.out.println("GAVIN calibration file not found at " + gavinFile);
            return;
        }
        else if (gavinFile.isDirectory())
        {
            System.out.println("GAVIN calibration file location is a directory, not a file!");
            return;
        }

        File clinvarFile = (File) options.valueOf("clinvar");
        if (!clinvarFile.exists())
        {
            System.out.println("ClinVar pathogenic VCF file not found at " + clinvarFile);
            return;
        }
        else if (clinvarFile.isDirectory())
        {
            System.out.println("ClinVar pathogenic VCF file location is a directory, not a file!");
            return;
        }

        File cgdFile = (File) options.valueOf("cgd");
        if (!cgdFile.exists())
        {
            System.out.println("CGD file not found at " + cgdFile);
            return;
        }
        else if (cgdFile.isDirectory())
        {
            System.out.println("CGD file location is a directory, not a file!");
            return;
        }

        File FDRfile = (File) options.valueOf("fdr");
        if (!FDRfile.exists())
        {
            System.out.println("FDR file not found at " + FDRfile);
            return;
        }
        else if (FDRfile.isDirectory())
        {
            System.out.println("FDR file location is a directory, not a file!");
            return;
        }

        /**
         * Optional
         */
        File labVariants = null;
        if(options.has("lab"))
        {
            labVariants = (File) options.valueOf("lab");
            if (!labVariants.exists())
            {
                System.out.println("VCF file with lab specific variant classifications not found at " + labVariants);
                return;
            }
            else if (labVariants.isDirectory())
            {
                System.out.println("VCF file location with lab specific variant classifications is a directory, not a file!");
                return;
            }
        }

        if(options.has("sv"))
        {
            System.out.println("Structural variation not yet supported!");
            return;
//            File structVar = (File) options.valueOf("sv");
//            if (!structVar.exists())
//            {
//                System.out.println("Structural variation VCF file not found at " + structVar);
//                return;
//            }
//            else if (structVar.isDirectory())
//            {
//                System.out.println("Structural variation VCF file location is a directory, not a file!");
//                return;
//            }
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
                System.out.println("CADD intermediate file not found at" + caddFile.getAbsolutePath());
                return;
            }
            else if (caddFile.isDirectory())
            {
                System.out.println("CADD intermediate file location is a directory, not a file!");
                return;
            }
            else {
                if (!caddFile.getName().endsWith(".tsv"))
                {
                    System.out.println("CADD intermediate file location extension expected to end in *.tsv, do not supply a gzipped file");
                    return;
                }
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
        System.out.println("Starting..");
        new Pipeline().start(inputVcfFile, gavinFile, clinvarFile, cgdFile, caddFile, FDRfile, mode, outputVCFFile, labVariants, verbose);
        System.out.println("..done!");

    }

}
