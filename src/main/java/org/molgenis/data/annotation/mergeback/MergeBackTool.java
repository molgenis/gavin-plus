package org.molgenis.data.annotation.mergeback;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by joeri on 10/13/16.
 */
public class MergeBackTool
{
    public void start(File inputVcfFile, File rvcfFile, File outputVCFFile) throws Exception {

        HashMap<String, String> chrPosRefAltToRLV = new HashMap<>();

        // put RVCF file in memory
        VcfReader vcfReader = GavinUtils.getVcfReader(rvcfFile);
        Iterator<VcfRecord> vcfIterator = vcfReader.iterator();
        while(vcfIterator.hasNext())
        {
            AnnotatedVcfRecord record = new AnnotatedVcfRecord(vcfIterator.next());
            chrPosRefAltToRLV.put(record.getChromosome() + "_" + record.getPosition() + "_" + VcfRecordUtils.getRef(
					record) + "_" + VcfRecordUtils.getAlt(record), record.getRvcf().stream().map(Object::toString).collect(Collectors.joining(",")));
        }

        System.out.println("chrPosRefAltToRLV = " + chrPosRefAltToRLV);

        // iterate over input and write output
        Scanner inputScanner = null;
        PrintWriter pw = null;
        try
        {
            inputScanner = new Scanner(inputVcfFile);
            pw = new PrintWriter(outputVCFFile);
            String inputLine;

            int nrOfRlvMapped = 0;
            while (inputScanner.hasNextLine())
            {
                inputLine = inputScanner.nextLine();

                if (inputLine.startsWith("##"))
                {
                    pw.println(inputLine);
                    continue;
                }

                if (inputLine.startsWith("#CHROM"))
                {
                    pw.println("##INFO=<ID=" + RVCF.FIELD_NAME + ",Number=.,Type=String,Description=\"" + RVCF.FIELD_NAME
                            + "\">");
                    pw.println(inputLine);
                    continue;
                }

                String[] split = inputLine.split("\t", -1);
                String chr = split[0];
                String pos = split[1];
                String ref = split[3];
                String alt = split[4];
                String key = chr + "_" + pos + "_" + ref + "_" + alt;

                if (chrPosRefAltToRLV.containsKey(key))
                {
                    String rlv = chrPosRefAltToRLV.get(key);

                    StringBuffer sb = new StringBuffer();

                    for (int i = 0; i < split.length; i++)
                    {
                        //INFO is at 7
                        if (i == 7)
                        {
                            if (split[i].equals("."))
                            {
                                //replace "." with the RLV content
                                sb.append("RLV=" + rlv + "\t");
                            }
                            else if (split[i].length() > 1 && split[i].endsWith(";"))
                            {
                                sb.append(split[i] + "RLV=" + rlv + "\t");
                            }
                            else if (split[i].length() > 1)
                            {
                                sb.append(split[i] + ";" + "RLV=" + rlv + "\t");

                            }
                            else
                            {
                                throw new Exception("Bad INFO field: " + split[i]);
                            }
                            continue;
                        }
                        sb.append(split[i] + "\t");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    pw.println(sb.toString());
                    nrOfRlvMapped++;
                }
                else
                {
                    pw.println(inputLine);
                }
            }

            pw.flush();

        if(chrPosRefAltToRLV.size() != nrOfRlvMapped)
        {
            throw new Exception("We have " + chrPosRefAltToRLV.size() + " variants of relevance but only added " + nrOfRlvMapped + " back to original VCF file!");
        }
        else
        {
            System.out.println("We added " + chrPosRefAltToRLV.size() + " variants of relevance back to the original VCF file!");
        }
        }finally
        {
            inputScanner.close();
            pw.close();
        }
    }
}
