import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;

public class SlidesConfig implements Serializable {
    // Private variables of this class
    private final String slidesFolder;
    private final String[] slidesFileList;
    private final SlideEffect[] slideEffects;
    private final String[] soundFiles;
    private final boolean manualChange;
    private final int slideIntervalSeconds;

    /**
     * Class Constructor. Initializes the private variables.
     * @param slidesFolder  String that represents the directory with photos
     * @param slidesFileList Array of strings that represents the list of files for presentation
     * @param soundFiles String array that represents names of sound filess
     * @param manualChange  Boolean to determine whether user selected automatic or manual change methods
     * @param slideIntervalSeconds Integer to represent the number of seconds between each transition
     */
    public SlidesConfig(String slidesFolder, String[] slidesFileList, SlideEffect[] slideEffects, String[] soundFiles, boolean manualChange, int slideIntervalSeconds) {
        this.slidesFolder = slidesFolder;
        this.manualChange = manualChange;
        this.slideIntervalSeconds = slideIntervalSeconds;

        if (slidesFileList != null)
            this.slidesFileList = slidesFileList.clone();
        else
            this.slidesFileList = null;
        if (slideEffects != null)
            this.slideEffects = slideEffects.clone();
        else
            this.slideEffects = null;
        if (soundFiles != null)
            this.soundFiles = soundFiles.clone();
        else
            this.soundFiles = null;
    }

    /**
     * File save method that will have to be rewritten.
     * @param file .txt file the user is saving
     */
    public void saveToFile(File file) {
        try(PrintWriter writer = new PrintWriter(file)) {
            writer.println(slidesFolder == null ? "" : slidesFolder);
            if (slidesFileList != null) {
                writer.println(slidesFileList.length);
                for (String slideFile : slidesFileList)
                    writer.println(slideFile);
            } else
                writer.println(0);
            if (soundFiles != null) {
                writer.println(soundFiles.length);
                for (String soundFile : soundFiles)
                    writer.println(soundFile);
            } else
                writer.println(0);
            writer.println(manualChange);
            writer.println(slideIntervalSeconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fild load method that will have to be rewritten
     * @param file .xml file that user has saved
     * @return SlidesConfig, the configuration details for the project
     */
    public static SlidesConfig readFromFile(File file) {
        try(Scanner scanner = new Scanner(file)) {
            String slidesFolder = scanner.nextLine().trim();
            if (slidesFolder.isEmpty())
                slidesFolder = null;
            int slides = Integer.parseInt(scanner.nextLine());
            String[] slidesFileList = null;
            if (slides > 0) {
                slidesFileList = new String[slides];
                for (int i = 0; i < slides; i++)
                    slidesFileList[i] = scanner.nextLine();
            }

            int sounds = Integer.parseInt(scanner.nextLine());
            String[] soundFiles = null;
            if (sounds > 0) {
                soundFiles = new String[sounds];
                for (int i = 0; i < slides; i++)
                    soundFiles[i] = scanner.nextLine();
            }
            boolean manualChange = Boolean.parseBoolean(scanner.nextLine());
            int slideIntervalSeconds = Integer.parseInt(scanner.nextLine());
            return new SlidesConfig(slidesFolder, slidesFileList, null, soundFiles, manualChange, slideIntervalSeconds);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Public getter functions for private variables
     * @return private variables, respectively to the function.
     */
    public String getSlidesFolder() { return slidesFolder; }
    public String[] getSlidesFileList() { return slidesFileList; }
    public SlideEffect[] getSlideEffects() { return slideEffects; }
    public String[] getSoundFiles() { return soundFiles;}
    public boolean isManualChange() { return manualChange; }
    public int getSlideIntervalSeconds() { return slideIntervalSeconds; }
}
