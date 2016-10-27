package org.molgenis.data.annotation.splitrlv;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Collectors;


/**
 * Created by joeri on 10/13/16.
 */
public class SplitRlvTool
{

    String rlvSplit = "##INFO=<ID=RLV_ALLELE,Number=1,Type=String,Description=\"Allele\">\n" +
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
        Scanner inputScanner = new Scanner(inputVcfFile);
        PrintWriter pw = new PrintWriter(outputVCFFile);
        String inputLine;

        while(inputScanner.hasNextLine())
        {
            inputLine = inputScanner.nextLine();

            if(inputLine.startsWith("##INFO=<ID=RLV"))
            {
                continue;
            }

            if(inputLine.startsWith("##"))
            {
                pw.println(inputLine);
                continue;
            }

            if(inputLine.startsWith("#CHROM"))
            {
                pw.println(rlvSplit);
                pw.println(inputLine);
                continue;
            }


            String[] split = inputLine.split("\t", -1);

            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < split.length; i++)
            {
                //INFO is at 7
                if(i == 7 && split[7].contains(RVCF.attributeName+"="))
                {
                    boolean rlvFound = false;
                    String[] infoSplit = split[i].split(";", -1);
                    for(int j = 0; j < infoSplit.length; j++)
                    {
                        if(infoSplit[j].startsWith(RVCF.attributeName+"="))
                        {
                            System.out.println("infoSplit["+j+"] = " + infoSplit[j]);
                            rlvFound = true;
                            String[] rlvSplit = infoSplit[j].split(",", -1);
                            if(rlvSplit.length > 1)
                            {
                                throw new Exception("Multiple RLV entries cannot be split! not allowed at line: " + inputLine);
                            }

                            rlvSplit = infoSplit[j].split("\\|", -1);
                            if(rlvSplit.length != RVCF.nrOfFields)
                            {
                                throw new Exception("RLV did not have "+RVCF.nrOfFields+" subfields but "+rlvSplit.length+"! bad data at line: " + inputLine);
                            }

                            sb.append("RLV_ALLELE=" + rlvSplit[0] + ";RLV_ALLELEFREQ=" + rlvSplit[1] + ";RLV_GENE=" + rlvSplit[2] + ";RLV_FDR=" + rlvSplit[3] + ";RLV_TRANSCRIPT=" + rlvSplit[4] +
                                    ";RLV_PHENOTYPE=" + rlvSplit[5] + ";RLV_PHENOTYPEINHERITANCE=" + rlvSplit[6] + ";RLV_PHENOTYPEONSET=" + rlvSplit[7] + ";RLV_PHENOTYPEDETAILS=" + rlvSplit[8] +
                                    ";RLV_PHENOTYPEGROUP=" + rlvSplit[9] + ";RLV_SAMPLESTATUS=" + rlvSplit[10] + ";RLV_SAMPLEPHENOTYPE=" + rlvSplit[11] + ";RLV_SAMPLEGENOTYPE=" + rlvSplit[12] +
                                    ";RLV_SAMPLEGROUP=" + rlvSplit[13] + ";RLV_VARIANTSIGNIFICANCE=" + rlvSplit[14] + ";RLV_VARIANTSIGNIFICANCESOURCE=" + rlvSplit[15] + ";RLV_VARIANTSIGNIFICANCEJUSTIFICATION=" + rlvSplit[16] +
                                    ";RLV_VARIANTCOMPOUNDHET=" + rlvSplit[17] + ";RLV_VARIANTGROUP=" + rlvSplit[18] + ";");
                        }
                        else
                        {
                            sb.append(infoSplit[j]+";");
                        }
                    }

                    if(!rlvFound)
                    {
                        throw new Exception("INFO line contains "+RVCF.attributeName+", but not at the start of a field? problem at line: " + inputLine);
                    }

                    sb.deleteCharAt(sb.length()-1);

                }
                else
                {
                    sb.append(split[i]+"\t");
                }
            }

            sb.deleteCharAt(sb.length()-1);
            pw.println(sb.toString());

        }

        pw.flush();
        pw.close();


    }
}
