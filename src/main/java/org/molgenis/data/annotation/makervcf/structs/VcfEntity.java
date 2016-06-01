package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.Entity;

import java.util.Set;

/**
 * Created by joeri on 6/1/16.
 */
public class VcfEntity {
    String chr;
    String pos;
    String ref;
    String[] alts;
    String[] exac_af;
    String[] exac_af_split;

    public VcfEntity(Entity record) throws Exception {
        String chr = record.getString("#CHROM");
        String pos = record.getString("POS");
        String ref = record.getString("REF");
        String altStr = record.getString("ALT");
        String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
        String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? null : record.get("EXAC_AC_HOM").toString();
        String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? null : record.get("EXAC_AC_HET").toString();

        String ann = record.getString("ANN");

        String cadd_STR = record.get("CADD_SCALED") == null ? null : record.get("CADD_SCALED").toString();

        String[] alts = altStr.split(",", -1);


        String[] exac_af_split = new String[alts.length];
        if (exac_af_STR != null)
        {
            exac_af_split = exac_af_STR.split(",", -1);
        }
        String[] exac_ac_hom_split = new String[alts.length];
        if (exac_ac_hom_STR != null)
        {
            exac_ac_hom_split = exac_ac_hom_STR.split(",", -1);
        }

        String[] exac_ac_het_split = new String[alts.length];
        if (exac_ac_het_STR != null)
        {
            exac_ac_het_split = exac_ac_het_STR.split(",", -1);
        }
        String[] cadd_split = new String[alts.length];
        if (cadd_STR != null)
        {
            cadd_split = cadd_STR.split(",", -1);
        }

        Set<String> genes = GavinUtils.getGenesFromAnn(ann);

        if (exac_af_STR != null)
        {
            exac_af_split = exac_af_STR.split(",", -1);
        }
    }


    public String getChr() {
        return chr;
    }

    public String getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String[] getAlts() {
        return alts;
    }

    public String[] getExac_af() {
        return exac_af;
    }

    public double getExac_af(int i)
    {
        double exac_af = (exac_af_split[i] != null && !exac_af_split[i].isEmpty() && !exac_af_split[i].equals(".")) ? Double.parseDouble(exac_af_split[i]) : 0;
        return exac_af;
    }

    public Impact getImpact(int i, String gene)
    {
        return GavinUtils.getImpact(this.a, gene, alt);
    }
}
