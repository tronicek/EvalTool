package drdup;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import xml.CounterHandler;

/**
 * This class prints the number of clones in the input file.
 *
 * @author Zdenek Tronicek
 */
public class Counter {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("one argument expected: inputFile");
            return;
        }
        String inputFile = args[0];
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        CounterHandler counterHandler = new CounterHandler();
        saxParser.parse(inputFile, counterHandler);
        int size = counterHandler.getCount();
        System.out.printf("the number of clones: %d%n", size);
    }
}
