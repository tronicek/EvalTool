package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class changes the directory in the NiCad's XML file.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class ChangeDir {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("two arguments expected: sourceDir nicadFile");
            return;
        }
        String srcDir = args[0];
        String nicadFile = args[1];
        JAXBContext ctx = JAXBContext.newInstance(NiCadClones.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones nicad = (NiCadClones) unmarshaller.unmarshal(new File(nicadFile));
        for (NiCadClone clone : nicad.getClones()) {
            for (NiCadSource src : clone.getSources()) {
                String file = src.getFile();
                String nfile = file.substring(srcDir.length());
                src.setFile(nfile);
            }
        }
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        String outputFile = nicadFile.replace(".xml", "-dir.xml");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            marshaller.marshal(nicad, out);
        }
    }
}
