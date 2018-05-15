package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.calibratecadd.support.LoadCADDWebserviceOutput;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.utils.FixVcfAlleleNotation;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by joeri on 6/1/16.
 */
public class HandleMissingCaddScores {

    public enum Mode { ANALYSIS, CREATEFILEFORCADD }
    private Mode mode;
    private PrintWriter pw;
    private HashMap<String, Double> caddScores;

    public HandleMissingCaddScores(Mode mode, File caddFile) throws Exception
    {
        this.mode = mode;

        //either print missing cadd scores to this file, or read from file to get them, depending on mode
        if(mode.equals(Mode.CREATEFILEFORCADD))
        {
            this.pw = new PrintWriter(caddFile);
        }
        else if(mode.equals(Mode.ANALYSIS))
        {
            this.caddScores = LoadCADDWebserviceOutput.load(caddFile);
        }
        else
        {
            throw new Exception("Mode unknown: " + mode);
        }
    }


    public Double dealWithCaddScores(VcfEntity record, int altIndex) throws Exception {
        if(record.getCaddPhredScores(altIndex) == null)
        {
            if(mode.equals(Mode.CREATEFILEFORCADD))
            {
                String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), record.getAlt(altIndex), "\t");
                this.pw.println(record.getChr() + "\t" + record.getPos() + "\t" + "." + "\t" + trimmedRefAlt);
                this.pw.flush();
                return null;
            }
            else if(mode.equals(Mode.ANALYSIS))
            {
                String key = record.getChr() + "_" + record.getPos() + "_" + record.getRef() + "_" + record.getAlt(altIndex);
                if(this.caddScores.containsKey(key))
                {
                    return this.caddScores.get(key);
                }
                else
                {
                    String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), record.getAlt(altIndex), "_");
                    key = record.getChr() + "_" + record.getPos() + "_" + trimmedRefAlt;
                    if(this.caddScores.containsKey(key))
                    {
                        return this.caddScores.get(key);
                    }
                    else
                    {
                        System.out.println("[HandleMissingCaddScores] WARNING: CADD score missing for " + record.getChr() + " " + record.getPos() + " " + record.getRef() + " " + record.getAlt(altIndex) + " ! (even when using trimmed key '"+key+"')");
                        return null;
                    }
                }
            }
            else
            {
                throw new Exception("Mode unknown: " + mode);
            }
        }
        else
        {
            return record.getCaddPhredScores(altIndex);
        }
    }
}
