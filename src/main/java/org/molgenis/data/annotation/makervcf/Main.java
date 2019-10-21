package org.molgenis.data.annotation.makervcf;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String INPUT = "input";
	public static final String OUTPUT = "output";
	public static final String GAVIN = "gavin";
	public static final String VERBOSE = "verbose";
	public static final String REPLACE = "replace";
	public static final String HELP = "help";
  public static final String RLV_FIELD_MODE = "separate_fields";
	public static final String KEEP_ALL_VARIANTS = "keep_all_variants";

  public enum RlvMode {
    MERGED, SPLITTED, BOTH
  }

	public static void main(String[] args) throws Exception
	{
		OptionParser parser = createOptionParser();
		OptionSet options = parser.parse(args);
		new Main().run(options, parser, Arrays.toString(args));
	}

	private static OptionParser createOptionParser()
	{
		OptionParser parser = new OptionParser();

		parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("o", OUTPUT), "Output RVCF file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("g", GAVIN), "GAVIN calibration file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("v", VERBOSE),
				"Verbally express what is happening underneath the programmatic hood.");
		parser.acceptsAll(asList("r", REPLACE),
				"Enables output RVCF and CADD intermediate file override, replacing a file with the same name as the argument for the -o option");
		parser.acceptsAll(asList("h", HELP), "Prints this help text");
		parser.acceptsAll(asList("k", KEEP_ALL_VARIANTS), "Do not filter the non relevant variants, return all variants from the input");
    parser.acceptsAll(asList("q", RLV_FIELD_MODE),
        "The format mode of the RLV field: MERGED, SPLITTED, BOTH").withRequiredArg()
        .ofType(String.class);

		return parser;
	}

	private void printHelp(String version, String title, OptionParser parser) throws IOException
	{
		System.out.println("" + "Detect likely relevant clinical variants and matching samples in a VCF file.\n"
				+ "Your input VCF must be fully annotated with SnpEff, ExAC frequencies and CADD scores, and optionally frequencies from GoNL and 1000G.\n"
				+ "This can be done with MOLGENIS CmdlineAnnotator, available at https://github.com/molgenis/molgenis/releases/download/v1.21.1/CmdLineAnnotator-1.21.1.jar\n"
				+ "Please report any bugs, issues and feature requests at https://github.com/molgenis/gavin-plus\n"
				+ "\n" + "Typical usage: java -jar GAVIN-Plus-" + version
				+ ".jar [inputfile] [outputfile] [helperfiles] [mode/flags]\n" + "Example usage:\n"
				+ "java -Xmx4g -jar GAVIN-Plus-" + version + ".jar \\\n"
				+ "-i patient76.snpeff.exac.gonl.caddsnv.vcf \\\n" + "-o patient76_RVCF.vcf \\\n"
				+ "-g GAVIN_calibrations_r0.5.tsv \\\n" + "-p clinvar.vkgl.patho.26june2018.vcf.gz \\\n"
				+ "-d CGD_26jun2018.txt.gz \\\n" + "-f FDR_allGenes_r1.2.tsv \\\n" + "-c fromCadd.tsv \\\n"
				+ "-m ANALYSIS \n" + "\n" + "Dealing with CADD intermediate files:\n"
				+ "You first want to generate a intermediate file with any missing CADD annotations using '-d toCadd.tsv -m CREATEFILEFORCADD'\n"
				+ "After which, you want to score the variants in toCadd.tsv with the web service at http://cadd.gs.washington.edu/score\n"
				+ "The resulting scored file should be unpacked and then used for analysis with '-d fromCadd.tsv -m ANALYSIS'\n"
				+ "\n" + "Details on the various helper files:\n"
				+ "The required helper files for -g, -c, -d and -f can be downloaded from: http://molgenis.org/downloads/gavin at 'data_bundle'.\n"
				+ "The -a file is either produced by the analysis (using -m CREATEFILEFORCADD) or used as an existing file (using -m ANALYSIS).\n"
				+ "The -l is a user-supplied VCF of interpreted variants. Use 'CLSF=LP' or 'CLSF=P' as info field to denote (likely) pathogenic variants.\n"
        + "The -q option determines if the GAVIN information should be added as separate fields, one merged field, or both.\n"
				+ "\n" + "Using pedigree data for filtering:\n"
				+ "Please use the standard PEDIGREE notation in your VCF header, e.g. '##PEDIGREE=<Child=p01,Mother=p02,Father=p03>'. Trios and duos are allowed.\n"
				+ "Parents are assumed unaffected, children affected. Using complex family trees, grandparents and siblings is not yet supported.\n"
				+ "\n" + "Some other notes:\n"
				+ "Phased genotypes are used to remove obvious false compound heterozygous hits. These are demoted to heterozygous multihit.\n"
				+ "If GoNL annotations are provided, variants above 5% MAF are removed as presumed false positives (in addition to ExAC >5%).\n"
				+ "The gene FDR values are based on 2,504 individuals from The 1000 Genomes project and may be used as a general indication of significance -\n"
				+ "however - high FDR values may be caused by either faulty detection OR false positives from the low-coverage sequencing data.\n"
				+ StringUtils.repeat("-", title.length()) + "\n" + "\n" + "Available options:\n");

		parser.printHelpOn(System.out);

		System.out.println("\n" + StringUtils.repeat("-", title.length()) + "\n");
	}

	public void run(OptionSet options, OptionParser parser, String cmdString) throws Exception
	{
		String version = VersionUtils.getVersion();
		String title = "* MOLGENIS GAVIN+ for genome diagnostics, release " + version + "";
		String titl2 = "* Gene-Aware Variant INterpretation Plus";

		int len = Math.max(title.length(), titl2.length());
		String appTitle =
				"\n" + StringUtils.repeat("*", len) + "\n" + title + "\n" + titl2 + "\n" + StringUtils.repeat("*", len)
						+ "\n";

		System.out.println(appTitle);

		if ((options.has(INPUT) && options.has(OUTPUT)) || (options.has(INPUT)
				&& options.has(OUTPUT) && options.has(GAVIN)))
		{
			System.out.println("Arguments OK.");
		}
		else if (options.has(HELP))
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

		File inputVcfFile = (File) options.valueOf(INPUT);
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

		File outputVCFFile = (File) options.valueOf(OUTPUT);
		if (outputVCFFile.exists())
		{
			if (options.has(REPLACE))
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

		File gavinFile = (File) options.valueOf(GAVIN);
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

		if (options.has(VERBOSE))
		{
			setLogLevelToDebug();
		}

    String rlvModeString = (String) options.valueOf(RLV_FIELD_MODE);
    if (!isValidEnum(RlvMode.class, rlvModeString)) {
      System.out.println("Mode must be one of the following: " + Arrays.toString(RlvMode.values()));
      return;
    }
    RlvMode rlvMode = RlvMode.valueOf(rlvModeString);

		boolean keepAllVariants = false;
		if (options.has(KEEP_ALL_VARIANTS))
		{
			keepAllVariants = true;
		}

		LOG.info("Starting..");
    VcfRecordMapperSettings vcfRecordMapperSettings = VcfRecordMapperSettings.create(rlvMode);
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, keepAllVariants);
		Iterator<GavinRecord> gavinResults = discover.findRelevantVariants();

		//write Entities output VCF file
		new WriteToRVCF().writeRVCF(gavinResults, outputVCFFile, inputVcfFile, version, cmdString, true,
				vcfRecordMapperSettings);
		LOG.info("..done!");
	}

	private static <E extends Enum<E>> boolean isValidEnum(final Class<E> enumClass, final String enumName)
	{
		if (enumName == null)
		{
			return false;
		}
		try
		{
			Enum.valueOf(enumClass, enumName);
			return true;
		}
		catch (final IllegalArgumentException ex)
		{
			return false;
		}
	}

	private static void setLogLevelToDebug()
	{
		org.slf4j.Logger logger = LoggerFactory.getLogger("org.molgenis");
		if (!(logger instanceof ch.qos.logback.classic.Logger))
		{
			throw new RuntimeException("Root logger is not a Logback logger");
		}
		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
		logbackLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
	}
}
