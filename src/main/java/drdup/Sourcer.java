package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import edu.tarleton.drdup2.nicad.NiCadSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * This class adds the source code to the NiCad's XML file.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Sourcer {

    private static final int CACHE_SIZE = 1024 * 1024 * 1024;
    private static final Map<String, List<String>> CACHE = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("two arguments expected: sourceDir nicadFile");
            return;
        }
        String sourceDir = args[0];
        String inputFile = args[1];
        JAXBContext ctx = JAXBContext.newInstance("edu.tarleton.drdup2.nicad");
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones nicad = (NiCadClones) unmarshaller.unmarshal(new File(inputFile));
        for (NiCadClone clone : nicad.getClones()) {
            for (NiCadSource src : clone.getSources()) {
                String file = src.getFile();
                Integer startline = src.getStartline();
                Integer startcolumn = src.getStartcolumn();
                Integer endline = src.getEndline();
                Integer endcolumn = src.getEndcolumn();
                String code = readFile(sourceDir, file, startline, startcolumn, endline, endcolumn);
                src.setSourceCode(code);
            }
        }
        String outfile = inputFile.replace(".xml", "-source.xml");
        writeTextFile(nicad, outfile);
    }

    static String readFile(String sourceDir, String file, Integer startline, Integer startcolumn, Integer endline, Integer endcolumn) throws IOException {
        List<String> lines = searchCache(sourceDir, file);
        if (lines == null) {
            Path path = Paths.get(sourceDir, file);
            Charset cs = Charset.forName("UTF-8");
            lines = Files.readAllLines(path, cs);
            storeInCache(sourceDir, file, lines);
        }
        List<String> selected = lines.subList(startline - 1, endline);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < selected.size(); i++) {
            String line = selected.get(i);
            if (i == 0 && startcolumn != null && i == selected.size() - 1 && endcolumn != null) {
                line = line.substring(startcolumn - 1, endcolumn);
            } else if (i == 0 && startcolumn != null) {
                line = line.substring(startcolumn - 1);
            } else if (i == selected.size() - 1 && endcolumn != null) {
                line = line.substring(0, endcolumn);
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    static List<String> searchCache(String sourceDir, String file) {
        String s = sourceDir + "/" + file;
        return CACHE.get(s);
    }

    static void storeInCache(String sourceDir, String file, List<String> lines) {
        if (CACHE.size() >= CACHE_SIZE) {
            CACHE.clear();
        }
        String s = sourceDir + "/" + file;
        CACHE.put(s, lines);
    }

    static void writeTextFile(NiCadClones clones, String fileName) throws Exception {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            out.println("<clones>");
            for (NiCadClone clone : clones.getClones()) {
                out.printf("    <clone nlines=\"%d\" similarity=\"%d\">%n", clone.getNlines(), clone.getSimilarity());
                for (NiCadSource src : clone.getSources()) {
                    out.printf("        <source file=\"%s\" startline=\"%d\"", src.getFile(), src.getStartline());
                    if (src.getStartcolumn() != null) {
                        out.printf(" startcolumn=\"%d\"", src.getStartcolumn());
                    }
                    out.printf(" endline=\"%d\"", src.getEndline());
                    if (src.getEndcolumn() != null) {
                        out.printf(" endcolumn=\"%d\"", src.getEndcolumn());
                    }
                    out.printf(">");
                    String code = src.getSourceCode()
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;");
                    code = replaceInvalidXmlChars(code);
                    out.print(code);
                    out.println("</source>");
                }
                out.println("    </clone>");
            }
            out.println("</clones>");
        }
    }

    static String replaceInvalidXmlChars(String str) {
        str = str.replace("\u0000", "\\u0000")
                .replace("\u0001", "\\u0001")
                .replace("\u0002", "\\u0002")
                .replace("\u0003", "\\u0003")
                .replace("\u0004", "\\u0004")
                .replace("\u0005", "\\u0005")
                .replace("\u0006", "\\u0006")
                .replace("\u0007", "\\u0007")
                .replace("\u0008", "\\u0008")
                .replace("\u000B", "\\u000B")
                .replace("\u000C", "\\u000C")
                .replace("\u000E", "\\u000E")
                .replace("\u000F", "\\u000F")
                .replace("\u0010", "\\u0010")
                .replace("\u0011", "\\u0011")
                .replace("\u0012", "\\u0012")
                .replace("\u0013", "\\u0013")
                .replace("\u0014", "\\u0014")
                .replace("\u0015", "\\u0015")
                .replace("\u0016", "\\u0016")
                .replace("\u0017", "\\u0017")
                .replace("\u0018", "\\u0018")
                .replace("\u0019", "\\u0019")
                .replace("\u001A", "\\u001A")
                .replace("\u001B", "\\u001B")
                .replace("\u001C", "\\u001C")
                .replace("\u001D", "\\u001D")
                .replace("\u001E", "\\u001E")
                .replace("\u001F", "\\u001F")
                .replace("\uFFFE", "\\uFFFE")
                .replace("\uFFFF", "\\uFFFF");
        return str;
    }
}
