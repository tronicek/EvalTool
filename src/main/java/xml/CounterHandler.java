package xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CounterHandler extends DefaultHandler {

    private int count;

    @Override
    public void startElement(String uri, String lName, String qName, Attributes attr) throws SAXException {
        switch (qName) {
            case "clone":
                count++;
        }
    }

    public int getCount() {
        return count;
    }
}
