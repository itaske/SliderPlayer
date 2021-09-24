import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Opens a slideshow file and parses the information
 */
public class FileLoad {
    private Document doc;   // The document this file holds

    /**
     * Creates a file load object to parse the code from the file
     *
     * @param file The file to be opened and read
     * @throws ParserConfigurationException thrown by DocumentBuilderFactory object.newDocumentBuilder()
     * @throws IOException                  thrown by DocumentBuilder object.parse(File f)
     * @throws SAXException                 thrown by DocumentBuilder object.parse(File f)
     */
    public FileLoad(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(file);
    }

    /**
     * Checks to see if the file opened is a valid slideshow file
     *
     * @return true if the file is valid; false otherwise
     */
    public boolean isValid() {
        Element pres = doc.getDocumentElement();
        if (!(pres.getTagName() == "presentation" && pres.hasAttribute("imageFolder") && pres.hasChildNodes()))
            return false;
        if (pres.getFirstChild().getNodeType() == Node.ELEMENT_NODE) {
            Element sldMst = (Element) pres.getFirstChild();
            if (sldMst.getTagName() != "slideMaster" || !sldMst.hasAttribute("timer"))
                return false;
            NodeList slideList = sldMst.getElementsByTagName("slide");
            for (int i = 0; i < slideList.getLength(); i++) {
                if (slideList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element slide = (Element) slideList.item(i);
                    if (!(slide.hasAttribute("id") && slide.hasAttribute("image") && slide.hasAttribute("effect") && slide.hasAttribute("effectTimer")))
                        return false;
                } else
                    return false;
            }
            NodeList soundList = sldMst.getElementsByTagName("audio");
            for (int i = 0; i < soundList.getLength(); i++) {
                if (soundList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element sound = (Element) soundList.item(i);
                    if (!(sound.hasAttribute("id") && sound.hasAttribute("sound")))
                        return false;
                } else
                    return false;
            }
        } else
            return false;

        return true;
    }

    /**
     * Returns the configuration of the load file
     * @return The SlideConfig object for this load file
     */
    public SlidesConfig getSlidesConfig() {
        String imgFld;
        String[] images;
        SlideEffect[] effects;
        String[] sounds;
        int timer;

        if (!isValid())
            return null;

        Element pres = doc.getDocumentElement();
        imgFld = pres.getAttribute("imageFolder");

        Element sldMst = (Element) pres.getFirstChild();
        timer = Integer.parseInt(sldMst.getAttribute("timer"));

        NodeList slides = sldMst.getElementsByTagName("slide");
        images = new String[slides.getLength()];
        effects = new SlideEffect[slides.getLength()];
        for (int i = 0; i < images.length; i++) {
            Element slide = (Element) slides.item(i);
            images[i] = slide.getAttribute("image");
            String effect = slide.getAttribute("effect");
            if (effect.equals("WipeDown"))
                effects[i] = new SlideEffect(SlideEffect.ChangeAnimation.WipeDown, Integer.parseInt(slide.getAttribute("effectTimer")));
            else if (effect.equals("WipeLeft"))
                effects[i] = new SlideEffect(SlideEffect.ChangeAnimation.WipeLeft, Integer.parseInt(slide.getAttribute("effectTimer")));
            else if (effect.equals("WipeRight"))
                effects[i] = new SlideEffect(SlideEffect.ChangeAnimation.WipeRight, Integer.parseInt(slide.getAttribute("effectTimer")));
            else if (effect.equals("WipeUp"))
                effects[i] = new SlideEffect(SlideEffect.ChangeAnimation.WipeUp, Integer.parseInt(slide.getAttribute("effectTimer")));
            else if (effect.equals("Cross"))
                effects[i] = new SlideEffect(SlideEffect.ChangeAnimation.Cross, Integer.parseInt(slide.getAttribute("effectTimer")));
            else
                effects[i] = new SlideEffect(SlideEffect.ChangeAnimation.None, Integer.parseInt(slide.getAttribute("effectTimer")));
        }

        NodeList audios = sldMst.getElementsByTagName("audio");
        sounds = new String[audios.getLength()];
        for (int i = 0; i < sounds.length; i++) {
            Element audio = (Element) audios.item(i);
            sounds[i] = audio.getAttribute("sound");
        }
        return new SlidesConfig(imgFld, images, effects, sounds, timer == 0, timer);
    }
}