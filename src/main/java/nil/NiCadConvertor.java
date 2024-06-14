package nil;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * The convertor from NIL's output format to NiCad's output format.
 * 
 * @author Zdenek Tronicek
 */
public class NiCadConvertor {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("expected arguments: clones_file similarity");
            return;
        }
        String clonesFile = args[0];
        int similarity = Integer.parseInt(args[1]);
        Path path = Paths.get(clonesFile);
        CloneConsumer cc = new CloneConsumer(similarity);
        Files.lines(path).forEach(cc);
        NiCadClones clones = new NiCadClones(cc.getClones());
        String outfile = clonesFile.replace(".csv", ".xml");
        JAXBContext ctx = JAXBContext.newInstance("edu.tarleton.drdup2.nicad");
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (FileOutputStream out = new FileOutputStream(outfile)) {
            marshaller.marshal(clones, out);
        }
    }

    static class CloneConsumer implements Consumer<String> {

        private final int similarity;
        private final List<NiCadClone> clones = new ArrayList<>();

        public CloneConsumer(int similarity) {
            this.similarity = similarity;
        }

        public List<NiCadClone> getClones() {
            return clones;
        }

        @Override
        public void accept(String line) {
            String[] fields = line.split(",");
            String fileA = fields[0];
            int startA = Integer.parseInt(fields[1]);
            int endA = Integer.parseInt(fields[2]);
            String fileB = fields[3];
            int startB = Integer.parseInt(fields[4]);
            int endB = Integer.parseInt(fields[5]);
            int lines = Math.min(endA - startA + 1, endB - startB + 1);
            List<NiCadSource> sources = new ArrayList<>();
            sources.add(new NiCadSource(fileA, startA, null, endA, null));
            sources.add(new NiCadSource(fileB, startB, null, endB, null));
            NiCadClone cl = new NiCadClone(lines, similarity, sources);
            clones.add(cl);
        }
    }
}
