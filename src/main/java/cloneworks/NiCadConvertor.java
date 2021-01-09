package cloneworks;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * The convertor from CloneWorks' output format to NiCad's output format.
 * 
 * @author Zdenek Tronicek
 */
public class NiCadConvertor {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("expected arguments: methods_file clones_file source_dir");
            return;
        }
        String files = args[0];
        String clonesFile = args[1];
        String sourceDir = args[2];
        LineConsumer cons = new LineConsumer(sourceDir);
        Path path = Paths.get(files);
        Files.lines(path).forEach(cons);
        Map<Long, InputFile> ifiles = cons.getMethods();
        CloneConsumer cc = new CloneConsumer(ifiles);
        Path path2 = Paths.get(clonesFile);
        Files.lines(path2).forEach(cc);
        NiCadClones clones = new NiCadClones(cc.getClones());
        String outfile = clonesFile.replace(".clones", ".xml");
        JAXBContext ctx = JAXBContext.newInstance("edu.tarleton.drdup2.nicad");
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (FileOutputStream out = new FileOutputStream(outfile)) {
            marshaller.marshal(clones, out);
        }
    }

    static class InputFile {

        long fileId;
        String file;

        public InputFile(long fileId, String file) {
            this.fileId = fileId;
            this.file = file;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(fileId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InputFile) {
                InputFile that = (InputFile) obj;
                return fileId == that.fileId;
            }
            return false;
        }
    }

    static class LineConsumer implements Consumer<String> {

        private final String sourceDir;
        private final Map<Long, InputFile> map = new HashMap<>();

        public LineConsumer(String sourceDir) {
            this.sourceDir = sourceDir;
        }

        public Map<Long, InputFile> getMethods() {
            return map;
        }

        @Override
        public void accept(String line) {
            if (line.startsWith("#")) {
                return;
            }
            String[] fields = line.split("\\s+");
            long fileId = Long.parseLong(fields[0]);
            String path = fields[1];
            assert path.startsWith(sourceDir);
            String file = path.substring(sourceDir.length());
            InputFile f = new InputFile(fileId, file);
            map.put(fileId, f);
        }
    }

    static class CloneConsumer implements Consumer<String> {

        private final Map<Long, InputFile> map;
        private final List<NiCadClone> clones = new ArrayList<>();

        public CloneConsumer(Map<Long, InputFile> map) {
            this.map = map;
        }

        public List<NiCadClone> getClones() {
            return clones;
        }

        @Override
        public void accept(String line) {
            if (line.startsWith("#")) {
                return;
            }
            String[] fields = line.split(",");
            long fileA = Long.parseLong(fields[0]);
            int beginA = Integer.parseInt(fields[1]);
            int endA = Integer.parseInt(fields[2]);
            long fileB = Long.parseLong(fields[3]);
            int beginB = Integer.parseInt(fields[4]);
            int endB = Integer.parseInt(fields[5]);
            int lines = Math.min(endA - beginA + 1, endB - beginB + 1);
            List<NiCadSource> sources = new ArrayList<>();
            InputFile if1 = map.get(fileA);
            InputFile if2 = map.get(fileB);
            sources.add(new NiCadSource(if1.file, beginA, null, endA, null));
            sources.add(new NiCadSource(if2.file, beginB, null, endB, null));
            NiCadClone cl = new NiCadClone(lines, 100, sources);
            clones.add(cl);
        }
    }
}
