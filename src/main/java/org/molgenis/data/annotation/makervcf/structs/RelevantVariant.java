package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.cgd.CGDEntry;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;


import java.util.*;

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

    /**
     * Helper function to group all genes of relevance
     * @return
     */
    public Set<String> getRelevantGenes()
    {
        HashSet res = new HashSet<>();
        for(Relevance rlv : this.relevance)
        {
            res.add(rlv.getGene());
        }
        return res;
    }

    public VcfEntity getVariant() {
        return variant;
    }

    public List<Relevance> getRelevance() {
        return relevance;
    }

    public String toStringShort(){
        return variant.getChr()+ " " +variant.getPos()+ " " + variant.getRef()+ " " + variant.getAltString();
    }

    @Override
    public String toString() {
        return "RelevantVariant{" +
                "variant=" + variant +
                '}';
    }

}
