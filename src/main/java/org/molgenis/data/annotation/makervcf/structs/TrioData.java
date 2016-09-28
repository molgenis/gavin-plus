package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.data.vcf.datastructures.Trio;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by joeri on 9/28/16.
 */
public class TrioData {

    HashMap<String, Trio> trios;
    Set<String> parents;

    public TrioData(HashMap<String, Trio> trios, Set<String> parents) {
        this.parents = parents;
        this.trios = trios;
    }

    public HashMap<String, Trio> getTrios() {
        return trios;
    }

    public Set<String> getParents() {
        return parents;
    }
}
