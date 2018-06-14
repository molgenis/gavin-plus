package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.vcf.datastructures.Sample;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by joeri on 7/17/16.
 */
public class GenoMatchSamples {
    public HashMap<String, Sample> carriers;
    public HashMap<String, Sample> affected;
    public Set<String> parentsWithReferenceCalls;


    public GenoMatchSamples(HashMap<String, Sample> carriers, HashMap<String, Sample> affected)
    {
        this.carriers = carriers;
        this.affected = affected;
    }

    public void setParentsWithReferenceCalls(Set<String> parentsWithReferenceCalls)
    {
        this.parentsWithReferenceCalls = parentsWithReferenceCalls;
    }

    @Override
    public String toString() {
        return "GenoMatchSamples{" +
                "carriers=" + carriers +
                ", affected=" + affected +
                ", parentsWithReferenceCalls=" + parentsWithReferenceCalls +
                '}';
    }
}
