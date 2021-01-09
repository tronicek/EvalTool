package sourcerercc;

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
 * The convertor from SourcererCC's output format to NiCad's output format.
 * 
 * @author Zdenek Tronicek
 */
public class NiCadConvertor {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("expected arguments: methods_file clones_file source_dir");
            return;
        }
        String methodsFile = args[0];
        String clonesFile = args[1];
        String sourceDir = args[2];
        LineConsumer cons = new LineConsumer(sourceDir);
        Path path = Paths.get(methodsFile);
        Files.lines(path).forEach(cons);
        Map<Long, Method> methods = cons.getMethods();
        CloneConsumer cc = new CloneConsumer(methods);
        Path path2 = Paths.get(clonesFile);
        Files.lines(path2).forEach(cc);
        NiCadClones clones = new NiCadClones(cc.getClones());
        String outfile = clonesFile.replace(".txt", ".xml");
        JAXBContext ctx = JAXBContext.newInstance("edu.tarleton.drdup2.nicad");
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (FileOutputStream out = new FileOutputStream(outfile)) {
            marshaller.marshal(clones, out);
        }
    }

    static class Method {

        long fileId;
        String file;
        long methodId;
        String method;
        int begin;
        int end;

        public Method(long fileId, String file, long methodId, String method, int begin, int end) {
            this.fileId = fileId;
            this.file = file;
            this.methodId = methodId;
            this.method = method;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(methodId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Method) {
                Method that = (Method) obj;
                return methodId == that.methodId;
            }
            return false;
        }
    }

    static class LineConsumer implements Consumer<String> {

        private final String sourceDir;
        private final Map<Long, Method> methods = new HashMap<>();

        public LineConsumer(String sourceDir) {
            this.sourceDir = sourceDir;
        }

        public Map<Long, Method> getMethods() {
            return methods;
        }

        @Override
        public void accept(String line) {
            String[] fields = line.split("[;:]");
            long fileId = Long.parseLong(fields[0]);
            String path = fields[1];
            assert path.startsWith(sourceDir);
            String file = path.substring(sourceDir.length());
            long methodId = Long.parseLong(fields[2]);
            String name = fields[3];
            int begin = Integer.parseInt(fields[4]);
            int end = Integer.parseInt(fields[5]);
            Method m = new Method(fileId, file, methodId, name, begin, end);
            methods.put(methodId, m);
        }
    }

    static class CloneConsumer implements Consumer<String> {

        private final Map<Long, Method> methods;
        private final List<NiCadClone> clones = new ArrayList<>();

        public CloneConsumer(Map<Long, Method> methods) {
            this.methods = methods;
        }

        public List<NiCadClone> getClones() {
            return clones;
        }

        @Override
        public void accept(String line) {
            String[] fields = line.split(",");
            //long fileA = Long.parseLong(fields[0]);
            long methodA = Long.parseLong(fields[1]);
            //long fileB = Long.parseLong(fields[2]);
            long methodB = Long.parseLong(fields[3]);
            Method mA = methods.get(methodA);
            Method mB = methods.get(methodB);
            int lines = Math.min(mA.end - mA.begin + 1, mB.end - mB.begin + 1);
            List<NiCadSource> sources = new ArrayList<>();
            sources.add(new NiCadSource(mA.file, mA.begin, null, mA.end, null));
            sources.add(new NiCadSource(mB.file, mB.begin, null, mB.end, null));
            NiCadClone cl = new NiCadClone(lines, 100, sources);
            clones.add(cl);
        }
    }
}
