package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.Entity;

import java.util.HashMap;

/**
 * Created by joeri on 7/17/16.
 */
public class GenoMatchSamples {
    public HashMap<String, Entity> carriers;
    public HashMap<String, Entity> affected;

    public GenoMatchSamples(HashMap<String, Entity> carriers, HashMap<String, Entity> affected)
    {
        this.carriers = carriers;
        this.affected = affected;
    }

}
