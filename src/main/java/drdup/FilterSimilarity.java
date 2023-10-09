package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class filters the clones with the specified similarity.
 *
 * @author Zdenek Tronicek
 */
public class FilterSimilarity {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("three arguments expected: minSimilarity maxSimilarity inputFile");
            return;
        }
        int minSimilarity = Integer.parseInt(args[0]);
        int maxSimilarity = Integer.parseInt(args[1]);
        String inputFile = args[2];
        List<NiCadClone> cls = new ArrayList<>();
        JAXBContext ctx = JAXBContext.newInstance(NiCadClones.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones nicad = (NiCadClones) unmarshaller.unmarshal(new File(inputFile));
        for (NiCadClone clone : nicad.getClones()) {
            int sim = clone.getSimilarity();
            if (minSimilarity <= sim && sim <= maxSimilarity) {
                cls.add(clone);
            }
        }
        nicad.setClones(cls);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        String outputFile = inputFile.replace(".xml", "-filtered.xml");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            marshaller.marshal(nicad, out);
        }
    }
}
