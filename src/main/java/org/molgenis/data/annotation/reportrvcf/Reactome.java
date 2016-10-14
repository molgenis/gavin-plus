package org.molgenis.data.annotation.reportrvcf;

import net.didion.jwnl.data.Exc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by joeri on 10/14/16.
 *
 * Works on: http://www.reactome.org/download/current/ReactomePathways.gmt.zip  (unzipped)
 *
 * Example rows:
 *
 * AKT phosphorylates targets in the nucleus	R-HSA-198693	Reactome Pathway	AKT1	AKT2	AKT3	CREB1	FOXO1	FOXO3	FOXO4	NR4A1	RPS6KB2
 * AKT-mediated inactivation of FOXO1A	R-HSA-211163	Reactome Pathway	AKT1	AKT2	AKT3	FOXO1
 * ALKBH2 mediated reversal of alkylation damage	R-HSA-112122	Reactome Pathway	ALKBH2
 * ALKBH3 mediated reversal of alkylation damage	R-HSA-112126	Reactome Pathway	ALKBH3	ASCC1	ASCC2	ASCC3
 *
 * No header.
 *
 */
public class Reactome
{

    public static void main(String[] args) throws Exception {

        HashMap<String, Set<String>> pathwayNameToGeneNames = load(new File(args[0]));
        System.out.println("Loaded " + pathwayNameToGeneNames.size() + " pathways");

        HashMap<String, Set<String>> geneToPathways = makeGeneToPathwaysMap(pathwayNameToGeneNames);

    }

    public static HashMap<String, Set<String>> load(File reactomeFile) throws Exception
    {
        HashMap<String, Set<String>> pathwayNameToGeneNames = new HashMap<>();
        Scanner s = new Scanner(reactomeFile);
        String line;
        while(s.hasNextLine())
        {
            line = s.nextLine();
            String[] split = line.split("\t");

            // add ID to allow multiple with the same name, e.g.
            // RNA Pol II CTD phosphorylation and interaction with CE	R-HSA-77075
            // RNA Pol II CTD phosphorylation and interaction with CE	R-HSA-167160
            String pathwayName = split[0]+"|"+split[1];

            //sanity check
            if(pathwayNameToGeneNames.containsKey(pathwayName))
            {
                throw new Exception("Pathway twice in Reactome: " + pathwayName);
            }

            if(split.length < 4)
            {
                throw new Exception("Bad data, less than 4 elements: " + pathwayName);
            }
            Set<String> genes = new HashSet<>();
            for(int i = 3; i < split.length; i++)
            {
                genes.add(split[i]);
            }
            //System.out.println("Pathway " + pathwayName + " has genes " + genes.toString());
            pathwayNameToGeneNames.put(pathwayName, genes);
        }


        //built-in check, adjust for future versions of the data
        if(pathwayNameToGeneNames.get("ALKBH2 mediated reversal of alkylation damage|R-HSA-112122").size() != 1)
        {
            throw new Exception("Expected pathway for ALKBH2 not present");
        }
        if(!pathwayNameToGeneNames.get("ALKBH2 mediated reversal of alkylation damage|R-HSA-112122").iterator().next().equals("ALKBH2"))
        {
            throw new Exception("Expected gene for ALKBH2 pathway not present");
        }

        return pathwayNameToGeneNames;
    }

    public static HashMap<String, Set<String>> makeGeneToPathwaysMap(HashMap<String, Set<String>> pathwayNameToGeneNames)
    {
        HashMap<String, Set<String>> res = new HashMap<>();

        for(String pathway : pathwayNameToGeneNames.keySet())
        {
            for(String gene : pathwayNameToGeneNames.get(pathway))
            {
                if(res.containsKey(gene))
                {
                    res.get(gene).add(pathway);
                }
                else
                {
                    Set<String> pwList = new HashSet<>();
                    pwList.add(pathway);
                    res.put(gene, pwList);
                }
            }
        }
        System.out.println("[Reactome] Converted list of " + pathwayNameToGeneNames.size() + " pathways into list with " + res.size() + " genes");
        return res;
    }

}
