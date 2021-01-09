package deckard;

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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * The convertor from Deckard's output format to NiCad's output format.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class NiCadConvertor {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("expected arguments: method_file deckard_file similarity source_dir");
            return;
        }
        String methodFile = args[0];
        String deckardFile = args[1];
        int similarity = Integer.parseInt(args[2]);
        String sourceDir = args[3];
        LineConsumer cons = new LineConsumer(similarity, sourceDir);
        Path path = Paths.get(deckardFile);
        Files.lines(path).forEach(cons);
        List<NiCadClone> cc = cons.getClones();
        List<NiCadClone> cc2 = cc.stream()
                .filter(new MethodFilter(methodFile))
                .flatMap(new SeparateMapper())
                .collect(Collectors.toList());
        NiCadClones clones = new NiCadClones(cc2);
        String outfile = deckardFile.replace(".txt", ".xml");
        JAXBContext ctx = JAXBContext.newInstance("edu.tarleton.drdup2.nicad");
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (FileOutputStream out = new FileOutputStream(outfile)) {
            marshaller.marshal(clones, out);
        }
    }

    static class SeparateMapper implements Function<NiCadClone, Stream<NiCadClone>> {

        @Override
        public Stream<NiCadClone> apply(NiCadClone clone) {
            List<NiCadClone> cc = new ArrayList<>();
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
                        cc.add(cl);
                    }
                }
            }
            return cc.stream();
        }
    }

    static class MethodFilter implements Predicate<NiCadClone> {

        private final List<NiCadSource> methods;

        MethodFilter(String methodFile) throws Exception {
            methods = readMethods(methodFile);
        }

        @Override
        public boolean test(NiCadClone clone) {
            List<NiCadSource> sources = new ArrayList<>();
            for (NiCadSource src : clone.getSources()) {
                if (methods.contains(src)) {
                    sources.add(src);
                }
            }
            clone.setSources(sources);
            return sources.size() > 1;
        }

    }

    static List<NiCadSource> readMethods(String methodFile) throws Exception {
        List<NiCadSource> ss = new ArrayList<>();
        Path path = Paths.get(methodFile);
        for (String s : Files.readAllLines(path)) {
            String[] t = s.split("\\|");
            Integer sline = Integer.valueOf(t[3]);
            Integer eline = Integer.valueOf(t[4]);
            NiCadSource src = new NiCadSource(t[0], sline, null, eline, null);
            ss.add(src);
        }
        return ss;
    }

    static class LineConsumer implements Consumer<String> {

        private final int similarity;
        private final String sourceDir;
        private final List<NiCadClone> clones = new ArrayList<>();
        private List<NiCadSource> sources = new ArrayList<>();
        private int nlines;

        public LineConsumer(int similarity, String sourceDir) {
            this.similarity = similarity;
            this.sourceDir = sourceDir;
        }

        public List<NiCadClone> getClones() {
            return clones;
        }

        @Override
        public void accept(String s) {
            if (s.isEmpty()) {
                if (sources.isEmpty()) {
                    return;
                }
                NiCadClone clone = new NiCadClone(nlines, similarity, sources);
                clones.add(clone);
                sources = new ArrayList<>();
                nlines = 0;
                return;
            }
            if (s.startsWith("0")) {
                String[] p = s.trim().split("\\s+");
                //String dist = p[1];
                String path = p[3];
                assert path.startsWith(sourceDir);
                String file = path.substring(sourceDir.length());
                String[] pp = p[4].split(":");
                Integer start = Integer.valueOf(pp[1]);
                nlines = Integer.parseInt(pp[2]);
                Integer end = start + nlines - 1;
                NiCadSource src = new NiCadSource(file, start, null, end, null);
                sources.add(src);
            }
        }
    }
}
