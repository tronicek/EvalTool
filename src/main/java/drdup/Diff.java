package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class takes two NiCad's XML file and subtracts them. The difference is
 * written to a file.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Diff {

    private static final int TOLERANCE = 2;

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("three arguments expected: inputFileA inputFileB outputFile");
            return;
        }
        String inputFileA = args[0];
        String inputFileB = args[1];
        String outputFile = args[2];
        JAXBContext ctx = JAXBContext.newInstance(NiCadClones.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones inputA = (NiCadClones) unmarshaller.unmarshal(new File(inputFileA));
        NiCadClones inputB = (NiCadClones) unmarshaller.unmarshal(new File(inputFileB));
        List<NiCadClone> clones = subtract(inputA, inputB);
        NiCadClones cloneSet = new NiCadClones(clones);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            marshaller.marshal(cloneSet, out);
        }
    }

    static List<NiCadClone> subtract(NiCadClones clones1, NiCadClones clones2) {
        Map<String, List<NiCadClone>> fileMap = createMap(clones2.getClones());
        List<NiCadClone> diff = new ArrayList<>();
        for (NiCadClone clone : clones1.getClones()) {
            boolean found = false;
            List<NiCadSource> srcs = clone.getSources();
            NiCadSource src = srcs.get(0);
            List<NiCadClone> cands = fileMap.get(src.getFile());
            if (cands != null) {
                for (NiCadClone cand : cands) {
                    if (subset(clone, cand)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                diff.add(clone);
            }
        }
        return diff;
    }

    private static Map<String, List<NiCadClone>> createMap(List<NiCadClone> clones) {
        Map<String, List<NiCadClone>> map = new HashMap<>();
        for (NiCadClone clone : clones) {
            for (NiCadSource src : clone.getSources()) {
                String file = src.getFile();
                List<NiCadClone> cls = map.get(file);
                if (cls == null) {
                    cls = new ArrayList<>();
                    map.put(file, cls);
                }
                cls.add(clone);
            }
        }
        return map;
    }

    private static boolean subset(NiCadClone clone1, NiCadClone clone2) {
        for (NiCadSource src1 : clone1.getSources()) {
            boolean found = false;
            for (NiCadSource src2 : clone2.getSources()) {
                if (isIn(src1, src2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIn(NiCadSource src1, NiCadSource src2) {
        String file1 = src1.getFile();
        String file2 = src2.getFile();
        int c = file1.compareTo(file2);
        if (c != 0) {
            return false;
        }
        long startline1 = src1.getStartline();
        long startline2 = src2.getStartline();
        long endline1 = src1.getEndline();
        long endline2 = src2.getEndline();
        if (startline1 >= startline2 && endline1 <= endline2) {
            return true;
        }
        long d = Math.abs(startline1 - startline2);
        if (d > TOLERANCE) {
            return false;
        }
        d = Math.abs(endline1 - endline2);
        return d <= TOLERANCE;
    }
}
