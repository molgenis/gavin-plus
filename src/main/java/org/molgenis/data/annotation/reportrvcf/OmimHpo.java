package org.molgenis.data.annotation.reportrvcf;

import java.io.File;
import java.util.*;

/**
 * Created by joeri on 10/14/16.
 */
public class OmimHpo
{
    public static void main(String[] args) throws Exception {
        //http://compbio.charite.de/jenkins/job/hpo.annotations.monthly/lastSuccessfulBuild/artifact/annotation/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt
        HashMap<String, Set<String>> hpoToGenes = load(new File(args[0]));
        System.out.println("Loaded " + hpoToGenes.size() + " hpo terms");

        HashMap<String, Set<String>> geneToHpo = makeGeneToHpo(hpoToGenes);

    }

    public static HashMap<String, Set<String>> load(File hpoFile) throws Exception
    {
        HashMap<String, Set<String>> hpoToGenes = new HashMap<>();
        Scanner s = new Scanner(hpoFile);

        // skip header
        s.nextLine();

        String line;
        while (s.hasNextLine()) {
            line = s.nextLine();

            // OMIM:614652	COQ2	27235	HP:0000093	Proteinuria
            String[] split = line.split("\t");
            String hpo = split[3]+"|"+split[4];
            String gene = split[1];
            if(hpoToGenes.containsKey(hpo))
            {
                hpoToGenes.get(hpo).add(gene);
            }
            else
            {
                Set<String> geneList = new HashSet<>();
                geneList.add(gene);
                hpoToGenes.put(hpo, geneList);
            }
        }
        return hpoToGenes;
    }

    public static HashMap<String, Set<String>> makeGeneToHpo(HashMap<String, Set<String>> hpoToGenes)
    {
        HashMap<String, Set<String>> res = new HashMap<>();

        for(String hpo : hpoToGenes.keySet())
        {
            for(String gene : hpoToGenes.get(hpo))
            {
                if(res.containsKey(gene))
                {
                    res.get(gene).add(hpo);
                }
                else
                {
                    Set<String> hpoList = new HashSet<>();
                    hpoList.add(hpo);
                    res.put(gene, hpoList);
                }
            }
        }
        System.out.println("[OmimHpo] Converted list of " + hpoToGenes.size() + " hpo terms into list with " + res.size() + " genes");
        return res;
    }

}
