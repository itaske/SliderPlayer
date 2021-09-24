import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

/**
 * A Class to save the slideshow as an xml file
 */
public class FileSave {
    private Document doc;
    private Element pres;
    private Element sldMstr;

    /**
     * Constructs a FileSave object
     */
    public FileSave() throws ParserConfigurationException {
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBld = docFac.newDocumentBuilder();
        doc = docBld.newDocument();
        pres = doc.createElement("presentation");
        doc.appendChild(pres);
        sldMstr = doc.createElement("slideMaster");
        pres.appendChild(sldMstr);
    }

    /**
     * Uses a SlideConfig object to fill out the save file
     * @param sc The configuration of the slideshow save file
     */
    public void addFile(SlidesConfig sc) {
        Attr imgFld = doc.createAttribute("imageFolder");
        imgFld.setValue(sc.getSlidesFolder());
        pres.setAttributeNode(imgFld);

        Attr tmr = doc.createAttribute("timer");
        tmr.setValue(Integer.toString(sc.getSlideIntervalSeconds()));
        sldMstr.setAttributeNode(tmr);

        String[] slides = sc.getSlidesFileList();
        SlideEffect[] effects = sc.getSlideEffects();
        for (int i = 0; i < slides.length; i++) {
            Element sld = doc.createElement("slide");
            Attr idn = doc.createAttribute("id");
            Attr img = doc.createAttribute("image");
            Attr eff = doc.createAttribute("effect");
            Attr effTim = doc.createAttribute("effectTimer");
            idn.setValue(Integer.toString(i));
            img.setValue(slides[i]);
            eff.setValue(effects[i].getChangeAnimation().toString());
            effTim.setValue(Integer.toString(effects[i].getDuration()));
            sld.setAttributeNode(idn);
            sld.setAttributeNode(img);
            sld.setAttributeNode(eff);
            sld.setAttributeNode(effTim);
            sldMstr.appendChild(sld);
        }

        String[] sounds = sc.getSoundFiles();
        for (int i = 0; i < sounds.length; i++) {
            Element audio = doc.createElement("audio");
            Attr id = doc.createAttribute("id");
            Attr snd = doc.createAttribute("sound");
            id.setValue(Integer.toString(i));
            snd.setValue(sounds[i]);
            audio.setAttributeNode(id);
            audio.setAttributeNode(snd);
            sldMstr.appendChild(audio);
        }
    }

    /**
     * Prints the file as an xml document
     * @return true if successful and false otherwise
     */
    public boolean printFile(String filename) {
        try {
            if (!filename.substring(filename.length() - 4).toLowerCase().equals(".xml"))
                filename = filename + ".xml";
            TransformerFactory transFac = TransformerFactory.newInstance();
            Transformer trans = transFac.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename));

            trans.transform(source, result);
        } catch (TransformerException te) {
            te.printStackTrace();
            return false;
        }
        return true;
    }
}
