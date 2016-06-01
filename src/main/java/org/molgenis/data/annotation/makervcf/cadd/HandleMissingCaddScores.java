package org.molgenis.data.annotation.makervcf.cadd;

import org.apache.commons.lang3.EnumUtils;
import org.molgenis.calibratecadd.support.LoadCADDWebserviceOutput;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by joeri on 6/1/16.
 */
public class HandleMissingCaddScores {

    private enum Mode { ANALYSIS, CREATEFILEFORCADD}
    private Mode mode;

    public HandleMissingCaddScores(String mode) throws Exception {
        if(!EnumUtils.isValidEnum(Mode.class, mode))
        {
            throw new Exception("Not a valid mode: '"+mode+"'. Please use one of: " + Mode.values().toString());
        }
    }

    public void init(String mode, File caddFile) throws FileNotFoundException {
        //either print missing cadd scores to this file, or read from file to get them, depending on mode
        PrintWriter pw = null;
        if(mode.equals(Mode.CREATEFILEFORCADD))
        {
            pw = new PrintWriter(caddFile);
        }
        HashMap<String, Double> caddScores = null;
        if(mode.equals(Mode.ANALYSIS))
        {
            caddScores = LoadCADDWebserviceOutput.load(caddFile);
        }
    }

    public void dealWithMissingCaddScore(Double cadd)
    {
        if(cadd == null)
        {
            if(mode.equals(Mode.CREATEFILEFORCADD))
            {
                String trimmedRefAlt = LoadCADDWebserviceOutput.trimRefAlt(ref, alt, "\t");
                pw.println(chr + "\t" + pos + "\t" + "." + "\t" + trimmedRefAlt);
            }
            else if(mode.equals(Mode.ANALYSIS))
            {
                String key = chr + "_" + pos + "_" + ref + "_" + alt;
                if(caddScores.containsKey(key))
                {
                    cadd = caddScores.get(key);
                }
                else
                {
                    String trimmedRefAlt = LoadCADDWebserviceOutput.trimRefAlt(ref, alt, "_");
                    key = chr + "_" + pos + "_" + trimmedRefAlt;
                    if(caddScores.containsKey(key))
                    {
                        cadd = caddScores.get(key);
                    }
                    else
                    {
                        System.out.println("WARNING: CADD score missing for " + chr + " " + pos + " " + ref + " " + alt + " ! (even when using trimmed key '"+key+"')");
                    }

                }
            }
        }
    }
}
