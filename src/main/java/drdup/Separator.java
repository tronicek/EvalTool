package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class separates clones in a class to clone pairs.
 *
 * @author Zdenek Tronicek
 */
public class Separator {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("one argument expected: inputFile");
            return;
        }
        String inputFile = args[0];
        JAXBContext ctx = JAXBContext.newInstance(NiCadClones.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones drdup = (NiCadClones) unmarshaller.unmarshal(new File(inputFile));
        List<NiCadClone> cc = separate(drdup.getClones());
        cc.sort(new NiCadCloneComparator());
        drdup.setClones(cc);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        String outputFile = inputFile.replace(".xml", "-separated.xml");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            marshaller.marshal(drdup, out);
        }
    }

    static List<NiCadClone> separate(List<NiCadClone> clones) throws Exception {
        List<NiCadClone> cc = new ArrayList<>();
        for (NiCadClone clone : clones) {
            List<NiCadSource> ss = clone.getSources();
            if (ss.size() <= 2) {
                cc.add(clone);
            } else {
                for (int i = 0; i < ss.size(); i++) {
                    for (int j = i + 1; j < ss.size(); j++) {
                        List<NiCadSource> ss2 = new ArrayList<>();
                        ss2.add(ss.get(i));
                        ss2.add(ss.get(j));
                        NiCadClone cl = new NiCadClone(clone.getNlines(), clone.getSimilarity(), ss2);
                        cl.setDistance(clone.getDistance());
                        cc.add(cl);
                    }
                }
            }
        }
        return cc;
    }
}
