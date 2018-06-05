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

        Scanner inputScanner = null;
        PrintWriter pw = null;
        // iterate over input and write output
        try
        {
            inputScanner = new Scanner(inputVcfFile);
            pw = new PrintWriter(outputVCFFile);
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

                StringBuffer sb = new StringBuffer();

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

                                String[] multiRlvSplitConcat = new String[RVCF.nrOfFields];

                                for (int k = 0; k < rlvSplit.length; k++)
                                {
                                    String[] multiRlvSplit = rlvSplit[k].split("\\|", -1);

                                    if (multiRlvSplit.length != RVCF.nrOfFields)
                                    {
                                        throw new Exception("RLV did not have " + RVCF.nrOfFields + " subfields but " + multiRlvSplit.length + "! bad data: " + rlvSplit[k]);
                                    }

                                    //TODO JvdV: check if combination unique?
                                    String alt = multiRlvSplit[0];
                                    String gene = multiRlvSplit[2];

                                    for (int r = 0; r < multiRlvSplit.length; r++)
                                    {
                                        String value = multiRlvSplit[r].isEmpty() ? "NA" : multiRlvSplit[r];
                                        String previous = multiRlvSplitConcat[r] == null ? "" : multiRlvSplitConcat[r] + ",";
                                        multiRlvSplitConcat[r] = previous + "[" + alt + "|" + gene + "]" + value;
                                    }
                                }

                                sb.append("RLV_PRESENT=TRUE;" + RVCF.FIELD_NAME + "=" + infoSplit[j] + ";RLV_ALLELE="
                                        + multiRlvSplitConcat[0] + ";RLV_ALLELEFREQ=" + multiRlvSplitConcat[1] + ";RLV_GENE=" + multiRlvSplitConcat[2] + ";RLV_FDR=" + multiRlvSplitConcat[3]
                                        + ";RLV_TRANSCRIPT=" + multiRlvSplitConcat[4] + ";RLV_PHENOTYPE=" + multiRlvSplitConcat[5] + ";RLV_PHENOTYPEINHERITANCE=" + multiRlvSplitConcat[6]
                                        + ";RLV_PHENOTYPEONSET=" + multiRlvSplitConcat[7] + ";RLV_PHENOTYPEDETAILS=" + multiRlvSplitConcat[8] + ";RLV_PHENOTYPEGROUP=" + multiRlvSplitConcat[9]
                                        + ";RLV_SAMPLESTATUS=" + multiRlvSplitConcat[10] + ";RLV_SAMPLEPHENOTYPE=" + multiRlvSplitConcat[11] + ";RLV_SAMPLEGENOTYPE=" + multiRlvSplitConcat[12]
                                        + ";RLV_SAMPLEGROUP=" + multiRlvSplitConcat[13] + ";RLV_VARIANTSIGNIFICANCE=" + multiRlvSplitConcat[14] + ";RLV_VARIANTSIGNIFICANCESOURCE="
                                        + multiRlvSplitConcat[15] + ";RLV_VARIANTSIGNIFICANCEJUSTIFICATION=" + multiRlvSplitConcat[16] + ";RLV_VARIANTCOMPOUNDHET=" + multiRlvSplitConcat[17]
                                        + ";RLV_VARIANTGROUP=" + multiRlvSplitConcat[18] + ";");

                            }
                            // other info fields
                            else if (!infoSplit[j].isEmpty())
                            {
                                sb.append(infoSplit[j] + ";");
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
        }finally
        {
            inputScanner.close();
            pw.close();
        }


    }
}
