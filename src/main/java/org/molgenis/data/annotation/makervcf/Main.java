package org.molgenis.data.annotation.makervcf;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang.StringUtils;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by joeri on 6/1/16.
 * <p>
 * High-performance automated variant interpretation
 * <p>
 * Apply GAVIN, knowledge from ClinVar, CGD, variants from your own lab, structural variation,
 * rules of inheritance, and so on to find interesting variants and samples within a VCF file.
 * <p>
 * Output is written to a VCF with all relevant variants where information is contained in the RLV field.
 * We require the input VCF to be annotated with SnpEff, CADD (as much as possible), ExAC, GoNL and 1000G.
 * <p>
 * Tests TODO JvdV: Different ways to discover variants (clinvar, lab list, gavin)
 */
public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String INPUT = "input";
	public static final String OUTPUT = "output";
	public static final String GAVIN = "gavin";
	public static final String REPPATHO = "reppatho";
	public static final String CGD = "cgd";
	public static final String FDR = "fdr";
	public static final String CADD = "cadd";
	public static final String LAB = "lab";
	public static final String MODE = "mode";
	public static final String VERBOSE = "verbose";
	public static final String REPLACE = "replace";
	public static final String HELP = "help";
	public static final String RESTORE = "restore";
	public static final String SPLIT_RLV_FIELD = "separate_fields";
	public static final String KEEP_ALL_VARIANTS = "keep_all_variants";
	public static final String INCLUDE_SAMPLES = "include_samples";
	public static final String DISABLE_PREFIX = "disable_prefix";
	public static final String ADD_SPLITTED_ANN_FIELDS = "add_splitted_ann_fields";

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
		parser.acceptsAll(asList("p", REPPATHO), "VCF file containing reported pathogenic/likely pathogenic variants").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("d", CGD), "CGD file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("f", FDR), "Gene-specific FDR file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("c", CADD), "Input/output CADD missing annotations")
			  .withRequiredArg()
			  .ofType(File.class);
		parser.acceptsAll(asList("l", LAB), "VCF file with custom lab specific variant classifications")
			  .withOptionalArg()
			  .ofType(File.class);
		parser.acceptsAll(asList("m", MODE),
				"Create or use CADD file for missing annotations, either " + Mode.ANALYSIS.toString() + " or "
						+ Mode.CREATEFILEFORCADD.toString()).withRequiredArg().ofType(String.class);
		parser.acceptsAll(asList("v", VERBOSE),
				"Verbally express what is happening underneath the programmatic hood.");
		parser.acceptsAll(asList("r", REPLACE),
				"Enables output RVCF and CADD intermediate file override, replacing a file with the same name as the argument for the -o option");
		parser.acceptsAll(asList("h", HELP), "Prints this help text");
		parser.acceptsAll(asList("e", RESTORE),
				"[not available] Supporting tool. Combine RVCF results with original VCF.")
			  .withOptionalArg()
			  .ofType(File.class);
		parser.acceptsAll(asList("k", KEEP_ALL_VARIANTS), "Do not filter the non relevant variants, return all variants from the input");
		parser.acceptsAll(asList("s", INCLUDE_SAMPLES), "Include samples is output");
		parser.acceptsAll(asList("q", SPLIT_RLV_FIELD), "Create separate INFO fields for every part of the RLV information");
		parser.acceptsAll(asList("y", DISABLE_PREFIX),
				"In case of a splitted RLV field this option will NOT add the '[GENE|ALLELE]' prefix, only use for input with one variant per line.");
		parser.acceptsAll(asList("x", ADD_SPLITTED_ANN_FIELDS),
				"Splits ANN field provided in input and adds splitted values as separate info fields");

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

		if ((options.has(RESTORE) && options.has(INPUT) && options.has(OUTPUT)) || (options.has(INPUT)
				&& options.has(OUTPUT) && options.has(GAVIN) && options.has(REPPATHO) && options.has(CGD)
				&& options.has(FDR) && options.has(CADD) && options.has(MODE)))
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

		/*************
		 "Restore mode" where we add back genotypes in an RVCF
		 */
		if (options.has(RESTORE))
		{
			System.out.println("Restore mode not yet supported!");
			return;
		}

		/*************
		 Regular mode
		 */

		/*
		  Input check
		 */
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

		/*
		  Output and replace check
		 */
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

		/*
		  Check all kinds of files you need
		 */
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

		File repPathoFile = (File) options.valueOf(REPPATHO);
		if (!repPathoFile.exists())
		{
			System.out.println("VCF file (containing reported LP/P variants) not found at " + repPathoFile);
			return;
		}
		else if (repPathoFile.isDirectory())
		{
			System.out.println("VCF file (containing reported LP/P variants) location is a directory, not a file!");
			return;
		}

		File cgdFile = (File) options.valueOf(CGD);
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

		File fdrFile = (File) options.valueOf(FDR);
		if (!fdrFile.exists())
		{
			System.out.println("FDR file not found at " + fdrFile);
			return;
		}
		else if (fdrFile.isDirectory())
		{
			System.out.println("FDR file location is a directory, not a file!");
			return;
		}

		/*
		  Optional
		 */
		File labVariants = null;
		if (options.has(LAB))
		{
			labVariants = (File) options.valueOf(LAB);
			if (!labVariants.exists())
			{
				System.out.println("VCF file with lab specific variant classifications not found at " + labVariants);
				return;
			}
			else if (labVariants.isDirectory())
			{
				System.out.println(
						"VCF file location with lab specific variant classifications is a directory, not a file!");
				return;
			}
		}

		/*
		  Check mode in combination with CADD file and replace
		 */
		String modeString = (String) options.valueOf(MODE);
		if (!isValidEnum(Mode.class, modeString))
		{
			System.out.println("Mode must be one of the following: " + Arrays.toString(Mode.values()));
			return;
		}
		Mode mode = Mode.valueOf(modeString);

		File caddFile = (File) options.valueOf(CADD);
		if (mode == Mode.ANALYSIS)
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
			else
			{
				if (!caddFile.getName().endsWith(".tsv"))
				{
					System.out.println(
							"CADD intermediate file location extension expected to end in *.tsv, do not supply a gzipped file");
					return;
				}
			}
		}
		else if (mode == Mode.CREATEFILEFORCADD && caddFile.exists())
			{
				if (options.has(REPLACE))
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

		/*
		  Verbose
		 */
		if (options.has(VERBOSE))
		{
			setLogLevelToDebug();
		}

		boolean splitRlvField = false;
		if (options.has(SPLIT_RLV_FIELD))
		{
			splitRlvField = true;
		}

		boolean disablePrefix = false;
		if (options.has(DISABLE_PREFIX))
		{
			disablePrefix = true;
		}

		boolean addSplittedAnnFields = false;
		if (options.has(ADD_SPLITTED_ANN_FIELDS))
		{
			addSplittedAnnFields = true;
		}

		boolean keepAllVariants = false;
		if (options.has(KEEP_ALL_VARIANTS))
		{
			keepAllVariants = true;
		}

		boolean includeSamples = false;
		if (options.has(INCLUDE_SAMPLES))
		{
			includeSamples = true;
		}

		/*
		  Everything OK, start pipeline
		 */
		LOG.info("Starting..");
		VcfRecordMapperSettings vcfRecordMapperSettings = VcfRecordMapperSettings.create(includeSamples, splitRlvField,
				addSplittedAnnFields, !disablePrefix);
		Pipeline pipeline = new Pipeline(version, cmdString, vcfRecordMapperSettings, keepAllVariants, mode,
				inputVcfFile, gavinFile, repPathoFile, cgdFile, caddFile, fdrFile, outputVCFFile, labVariants);
		pipeline.start();
		LOG.info("..done!");
	}

	/**
	 * Copied from Apache Commons Lang 3.7
	 */
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
