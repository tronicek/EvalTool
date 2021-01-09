package drdup;

import edu.tarleton.drdup2.nicad.NiCadClone;
import edu.tarleton.drdup2.nicad.NiCadClones;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import static drdup.Diff.subtract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class DiffTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private int test(String dir) throws Exception {
        Path pathA = Paths.get("src/test/input", dir, "A.xml");
        Path pathB = Paths.get("src/test/input", dir, "B.xml");
        JAXBContext ctx = JAXBContext.newInstance(NiCadClones.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        NiCadClones inputA = (NiCadClones) unmarshaller.unmarshal(pathA.toFile());
        NiCadClones inputB = (NiCadClones) unmarshaller.unmarshal(pathB.toFile());
        List<NiCadClone> clones = subtract(inputA, inputB);
        return clones.size();
    }

    @Test
    public void test1() throws Exception {
        int size = test("1");
        assertEquals(0, size);
    }

    @Test
    public void test2() throws Exception {
        int size = test("2");
        assertEquals(1, size);
    }

    @Test
    public void test3() throws Exception {
        int size = test("3");
        assertEquals(1, size);
    }

    @Test
    public void test4() throws Exception {
        int size = test("4");
        assertEquals(1, size);
    }

    @Test
    public void test5() throws Exception {
        int size = test("5");
        assertEquals(1, size);
    }

    @Test
    public void test6() throws Exception {
        int size = test("6");
        assertEquals(1, size);
    }

    @Test
    public void test7() throws Exception {
        int size = test("7");
        assertEquals(2, size);
    }

    @Test
    public void test8() throws Exception {
        int size = test("8");
        assertEquals(2, size);
    }

    @Test
    public void test9() throws Exception {
        int size = test("9");
        assertEquals(0, size);
    }
}
