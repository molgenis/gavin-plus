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
		System.out.println("" + "Detect likely relevant clinical variants in a VCF file.\n"
				+ "Your input VCF must be fully annotated with VEP including the CADD plugin.\n"
				+ "Please report any bugs, issues and feature requests at https://github.com/molgenis/gavin-plus\n"
				+ "\n" + "Typical usage: java -jar GAVIN-Plus-" + version
				+ ".jar [inputfile] [outputfile] [helperfiles] [mode/flags]\n" + "Example usage:\n"
				+ "java -Xmx4g -jar GAVIN-Plus-" + version + ".jar \\\n"
				+ "-i patient76.snpeff.exac.gonl.caddsnv.vcf \\\n" + "-o patient76_RVCF.vcf \\\n"
				+ "-g GAVIN_calibrations_r0.5.tsv \\\n"
				+ "\n" + "Details on the various helper files:\n"
				+ "The required helper file for -g can be downloaded from: http://molgenis.org/downloads/gavin at 'data_bundle'.\n"
				+ "The -q option determines if the GAVIN information should be added as separate fields, one merged field, or both.\n"
				+ StringUtils.repeat("-", title.length()) + "\n" + "\n" + "Available options:\n");

		parser.printHelpOn(System.out);

		System.out.println("\n" + StringUtils.repeat("-", title.length()) + "\n");
	}

	public void run(OptionSet options, OptionParser parser, String cmdString) throws Exception
	{
		String version = VersionUtils.getVersion();
		String title = "* MOLGENIS GAVIN+ for genome diagnostics, release " + version + "";
		String titl2 = "* Gene-Aware Variant INterpretation";

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
		RlvMode rlvMode;
		if(StringUtils.isEmpty(rlvModeString)){
			rlvMode = RlvMode.MERGED;
		}else if (!isValidEnum(RlvMode.class, rlvModeString)) {
      System.out.println("Mode must be one of the following: " + Arrays.toString(RlvMode.values()));
      return;
    }else {
			rlvMode = RlvMode.valueOf(rlvModeString);
		}
		boolean keepAllVariants = false;
		if (options.has(KEEP_ALL_VARIANTS))
		{
			keepAllVariants = true;
		}

		run(cmdString, version, inputVcfFile, outputVCFFile, gavinFile, rlvMode, keepAllVariants);
	}

	static void run(String cmdString, String version, File inputVcfFile, File outputVCFFile,
			File gavinFile, RlvMode rlvMode, boolean keepAllVariants) throws Exception {
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
