package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by joeri on 6/13/16.
 */
public class RelevantVariant
{
    VcfEntity variant;
    List<Relevance> relevance;

    public RelevantVariant(VcfEntity variant, List<Relevance> relevance)
    {
        this.variant = variant;
        this.relevance = relevance;
    }

    public VcfEntity getVariant() {
        return variant;
    }

    public List<Relevance> getRelevance() {
        return relevance;
    }

    public String toStringShort(){
        return "RelevantVariant{"+variant.getChr()+" " +variant.getPos()+ " " + variant.getRef()+ " " + variant.getAltString() + '}';
    }

    @Override
    public String toString() {
        return "RelevantVariant{" +
                "variant=" + variant +
                '}';
    }

}
