package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class filters out the clones that refer to an invalid file.
 *
 * @author Zdenek Tronicek
 */
public class FilterInvalidFile {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("two arguments expected: sourceDir nicadFile");
            return;
        }
        String sourceDir = args[0];
        String inputFile = args[1];
        List<NiCadClone> cls = new ArrayList<>();
        JAXBContext ctx = JAXBContext.newInstance(NiCadClones.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones nicad = (NiCadClones) unmarshaller.unmarshal(new File(inputFile));
        for (NiCadClone clone : nicad.getClones()) {
            boolean ok = true;
            for (NiCadSource src : clone.getSources()) {
                String file = src.getFile();
                Path p = Paths.get(sourceDir, file);
                if (!Files.isRegularFile(p)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
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
