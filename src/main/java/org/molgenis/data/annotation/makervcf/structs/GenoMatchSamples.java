package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.Entity;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by joeri on 7/17/16.
 */
public class GenoMatchSamples {
    public HashMap<String, Entity> carriers;
    public HashMap<String, Entity> affected;
    public Set<String> parentsWithReferenceCalls;


    public GenoMatchSamples(HashMap<String, Entity> carriers, HashMap<String, Entity> affected, Set<String> parentsWithReferenceCalls)
    {
        this.carriers = carriers;
        this.affected = affected;
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
