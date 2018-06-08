package org.molgenis.data.annotation.splitrlv;

import org.molgenis.data.annotation.makervcf.structs.RVCF;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;


/**
 * Created by joeri on 10/13/16.
 */
public class SplitRlvTool
{

    String rlvSplit =
            "##INFO=<ID=RLV_PRESENT,Number=1,Type=String,Description=\"RLV present\">\n" +
            "##INFO=<ID=RLV,Number=.,Type=String,Description=\"Allele | AlleleFreq | Gene | FDR | Transcript | Phenotype | PhenotypeInheritance | PhenotypeOnset | PhenotypeDetails | PhenotypeGroup | SampleStatus | SamplePhenotype | SampleGenotype | SampleGroup | VariantSignificance | VariantSignificanceSource | VariantSignificanceJustification | VariantCompoundHet | VariantGroup\">\n" +
            "##INFO=<ID=RLV_ALLELE,Number=1,Type=String,Description=\"Allele\">\n" +
            "##INFO=<ID=RLV_ALLELEFREQ,Number=1,Type=String,Description=\"AlleleFreq\">\n" +
            "##INFO=<ID=RLV_GENE,Number=1,Type=String,Description=\"Gene\">\n" +
            "##INFO=<ID=RLV_FDR,Number=1,Type=String,Description=\"FDR\">\n" +
            "##INFO=<ID=RLV_TRANSCRIPT,Number=1,Type=String,Description=\"Transcript\">\n" +
            "##INFO=<ID=RLV_PHENOTYPE,Number=1,Type=String,Description=\"Phenotype\">\n" +
            "##INFO=<ID=RLV_PHENOTYPEINHERITANCE,Number=1,Type=String,Description=\"PhenotypeInheritance\">\n" +
            "##INFO=<ID=RLV_PHENOTYPEONSET,Number=1,Type=String,Description=\"PhenotypeOnset\">\n" +
            "##INFO=<ID=RLV_PHENOTYPEDETAILS,Number=1,Type=String,Description=\"PhenotypeDetails\">\n" +
            "##INFO=<ID=RLV_PHENOTYPEGROUP,Number=1,Type=String,Description=\"PhenotypeGroup\">\n" +
            "##INFO=<ID=RLV_SAMPLESTATUS,Number=1,Type=String,Description=\"SampleStatus\">\n" +
            "##INFO=<ID=RLV_SAMPLEPHENOTYPE,Number=1,Type=String,Description=\"SamplePhenotype\">\n" +
            "##INFO=<ID=RLV_SAMPLEGENOTYPE,Number=1,Type=String,Description=\"SampleGenotype\">\n" +
            "##INFO=<ID=RLV_SAMPLEGROUP,Number=1,Type=String,Description=\"SampleGroup\">\n" +
            "##INFO=<ID=RLV_VARIANTSIGNIFICANCE,Number=1,Type=String,Description=\"VariantSignificance\">\n" +
            "##INFO=<ID=RLV_VARIANTSIGNIFICANCESOURCE,Number=1,Type=String,Description=\"VariantSignificanceSource\">\n" +
            "##INFO=<ID=RLV_VARIANTSIGNIFICANCEJUSTIFICATION,Number=1,Type=String,Description=\"VariantSignificanceJustification\">\n" +
            "##INFO=<ID=RLV_VARIANTCOMPOUNDHET,Number=1,Type=String,Description=\"VariantCompoundHet\">\n" +
            "##INFO=<ID=RLV_VARIANTGROUP,Number=1,Type=String,Description=\"VariantGroup\">";

    public void start(File inputVcfFile, File outputVCFFile) throws Exception {

		// iterate over input and write output
		try (Scanner inputScanner = new Scanner(inputVcfFile); PrintWriter pw = new PrintWriter(outputVCFFile))
		{
			String inputLine;

			while (inputScanner.hasNextLine())
			{
				inputLine = inputScanner.nextLine();

				if (inputLine.startsWith("##INFO=<ID=RLV"))
				{
					continue;
				}

				if (inputLine.startsWith("##"))
				{
					pw.println(inputLine);
					continue;
				}

				if (inputLine.startsWith("#CHROM"))
				{
					pw.println(rlvSplit);
					pw.println(inputLine);
					continue;
				}

				String[] split = inputLine.split("\t", -1);

				StringBuilder sb = new StringBuilder();

				boolean rlvFound = false;

				for (int i = 0; i < split.length; i++)
				{

					// INFO is at 7
					if (i == 7)
					{
						String[] infoSplit = split[i].split(";", -1);

						if (infoSplit.length == 1 && infoSplit[0].equals("."))
						{
							sb.append("RLV_PRESENT=FALSE");
							if (split.length > 7)
							{
								sb.append("\t");
							}
							continue;
						}

						// find RLV
						for (int j = 0; j < infoSplit.length; j++)
						{
							// match to RLV
							if (infoSplit[j].startsWith(RVCF.FIELD_NAME + "="))
							{
								rlvFound = true;

								infoSplit[j] = infoSplit[j].substring(4);
								String[] rlvSplit = infoSplit[j].split(",", -1);

								String[] multiRlvSplitConcat = new String[RVCF.NR_OF_FIELDS];

								for (String aRlvSplit : rlvSplit)
								{
									String[] multiRlvSplit = aRlvSplit.split("\\|", -1);

									if (multiRlvSplit.length != RVCF.NR_OF_FIELDS)
									{
										throw new Exception("RLV did not have " + RVCF.NR_OF_FIELDS + " subfields but "
												+ multiRlvSplit.length + "! bad data: " + aRlvSplit);
									}

									//TODO JvdV: check if combination unique?
									String alt = multiRlvSplit[0];
									String gene = multiRlvSplit[2];

									for (int r = 0; r < multiRlvSplit.length; r++)
									{
										String value = multiRlvSplit[r].isEmpty() ? "NA" : multiRlvSplit[r];
										String previous =
												multiRlvSplitConcat[r] == null ? "" : multiRlvSplitConcat[r] + ",";
										multiRlvSplitConcat[r] = previous + "[" + alt + "|" + gene + "]" + value;
									}
								}

								sb.append("RLV_PRESENT=TRUE;" + RVCF.FIELD_NAME + "=")
								  .append(infoSplit[j])
								  .append(";RLV_ALLELE=")
								  .append(multiRlvSplitConcat[0])
								  .append(";RLV_ALLELEFREQ=")
								  .append(multiRlvSplitConcat[1])
								  .append(";RLV_GENE=")
								  .append(multiRlvSplitConcat[2])
								  .append(";RLV_FDR=")
								  .append(multiRlvSplitConcat[3])
								  .append(";RLV_TRANSCRIPT=")
								  .append(multiRlvSplitConcat[4])
								  .append(";RLV_PHENOTYPE=")
								  .append(multiRlvSplitConcat[5])
								  .append(";RLV_PHENOTYPEINHERITANCE=")
								  .append(multiRlvSplitConcat[6])
								  .append(";RLV_PHENOTYPEONSET=")
								  .append(multiRlvSplitConcat[7])
								  .append(";RLV_PHENOTYPEDETAILS=")
								  .append(multiRlvSplitConcat[8])
								  .append(";RLV_PHENOTYPEGROUP=")
								  .append(multiRlvSplitConcat[9])
								  .append(";RLV_SAMPLESTATUS=")
								  .append(multiRlvSplitConcat[10])
								  .append(";RLV_SAMPLEPHENOTYPE=")
								  .append(multiRlvSplitConcat[11])
								  .append(";RLV_SAMPLEGENOTYPE=")
								  .append(multiRlvSplitConcat[12])
								  .append(";RLV_SAMPLEGROUP=")
								  .append(multiRlvSplitConcat[13])
								  .append(";RLV_VARIANTSIGNIFICANCE=")
								  .append(multiRlvSplitConcat[14])
								  .append(";RLV_VARIANTSIGNIFICANCESOURCE=")
								  .append(multiRlvSplitConcat[15])
								  .append(";RLV_VARIANTSIGNIFICANCEJUSTIFICATION=")
								  .append(multiRlvSplitConcat[16])
								  .append(";RLV_VARIANTCOMPOUNDHET=")
								  .append(multiRlvSplitConcat[17])
								  .append(";RLV_VARIANTGROUP=")
								  .append(multiRlvSplitConcat[18])
								  .append(";");

							}
							// other info fields
							else if (!infoSplit[j].isEmpty())
							{
								sb.append(infoSplit[j]).append(";");
							}
						}

						if (!rlvFound)
						{
							sb.append("RLV_PRESENT=FALSE;");
						}

						// remove trailing ";" and add tab
						sb.deleteCharAt(sb.length() - 1);
						sb.append("\t");

					}
					else
					{
						sb.append(split[i] + "\t");
					}
				}

				sb.deleteCharAt(sb.length() - 1);
				pw.println(sb.toString());

			}

			pw.flush();
		}


    }
}
