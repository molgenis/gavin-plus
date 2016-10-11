package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.data.annotation.reportrvcf.FDR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by joeri on 7/19/16.
 */
public class CombineFDRwithAllGenes {

    public CombineFDRwithAllGenes() throws FileNotFoundException {

        Set<String> allGenes = new HashSet<String>();

        //as produced by GetAllGeneNamesFromVCF
        //columns: no header, 1 gene per line
        File allGenesFile = new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/allGenes.txt");

        //as produced by FDR, as ran on 1000G
        //using: https://molgenis26.gcc.rug.nl/downloads/5gpm/gavin/1000G_FDR/
        //columns: header "Gene Affected  Carrier"
        File FDRfile = new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/FDR_r1.0.tsv");

        //output file: combine FDR results with all genes
        //for some genes, we have found no false results, so their FDR will be 0
        File outputFDR = new File("/Users/joeri/Desktop/GAVIN-APP/1000G_diag_FDR/exomePlus/FDR_allGenes_r1.0.tsv");

        //read all genes
        Scanner allGenesScanner = new Scanner(allGenesFile);
        while(allGenesScanner.hasNextLine())
        {
            allGenes.add(allGenesScanner.nextLine());
        }
        allGenesScanner.close();

        //read FDR genes
        Scanner fdrScanner = new Scanner(FDRfile);
        fdrScanner.nextLine(); //skip header
        Map<String, String> geneToLine = new HashMap<String, String>();
        while(fdrScanner.hasNextLine())
        {
            String line = fdrScanner.nextLine();
            String[] split = line.split("\t", -1);
            geneToLine.put(split[0], line);
        }
        fdrScanner.close();

        System.out.println("read FDR data for " + geneToLine.size() + " genes");

        PrintWriter pw = new PrintWriter(outputFDR);

        pw.println(FDR.HEADER);
        for(String gene : allGenes)
        {
            if(geneToLine.containsKey(gene))
            {
                pw.println(geneToLine.get(gene));
                geneToLine.remove(gene);
            }
            else
            {
                pw.println(gene + "\t" + "0" + "\t" + "0" + "\t" + "0.0" + "\t" + "0.0");
            }
        }

        if(geneToLine.size() > 0)
        {
            System.out.println("weird - check all genes but still some left...");
            for(String key : geneToLine.keySet())
            {
                pw.println(geneToLine.get(key));
                System.out.println("leftover gene: " + key);

            }
        }

        pw.flush();
        pw.close();


    }

    public static void main(String[] args) throws Exception {
        new CombineFDRwithAllGenes();
    }


}
