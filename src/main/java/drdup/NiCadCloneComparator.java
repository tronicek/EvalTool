package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.util.Comparator;
import java.util.List;

/**
 * NiCadClone comparator.
 * 
 * @author Zdenek Tronicek
 */
public class NiCadCloneComparator implements Comparator<NiCadClone> {

    @Override
    public int compare(NiCadClone c1, NiCadClone c2) {
        List<NiCadSource> ss1 = c1.getSources();
        List<NiCadSource> ss2 = c2.getSources();
        String s1 = ss1.get(0).getSourceCode();
        String s2 = ss2.get(0).getSourceCode();
        int c = s1.compareTo(s2);
        if (c != 0) {
            return c;
        }
        return -1;
    }
}
