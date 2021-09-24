import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SlidesConfigView extends JFrame {
    // Extensions for image and sound files.
    private static final String[] slidesExtensions = new String[]{"jpg", "jpeg"};
    private static final String[] soundExtensions = new String[]{"wav", "aiff"};

    // Directories for image folder and sound file.
    private String folderPathTextField;
    private String soundPathTextField;

    // Components of the Forms
    private JPanel mainPanel;
    private JButton finishSlideButton;
    private JButton rightSlideButton;
    private JButton leftSlideButton;
    private JButton startSlideButton;
    private JPanel presentationPanel;
    private JPanel imgPanel;
    private JPanel audioPanel;
    private JPanel slidesFlowPanel;
    private JPanel musicPanel;
    private JRadioButton manualChangeRadioButton;
    private JRadioButton autoChangeRadioButton;
    private JSpinner intervalInSecondsSpinner;
    private JButton playButton;
    private JPanel thumbnailFlowPanel;
    private JButton addSlideButton;
    private JButton removeSlideButton;
    private JButton fxButton;
    private JPanel wavFlowPanel;
    private JButton leftWavButton;
    private JButton removeWavButton;
    private JButton rightWavButton;
    private JMenuBar menuBar;
    private JFileChooser slidesFolderChooser;
    private JFileChooser soundFileChooser;

    private WavComponent wavComponent;
    private ThumbnailComponent thumbnailComponent;
    private SlideComponent slideComponent;

    /**
     * Class constructor. Initializes the variables used within the class.
     */
    public SlidesConfigView()
    {
        // Initialize the variables to the default values.
        folderPathTextField = "";
        soundPathTextField = "";

        // Initialization of the JFrame
        setContentPane(mainPanel);

        configureWidgets();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setJMenuBar(menuBar);
        setVisible(true);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Returns a newly instantiated <code>SlidesConfig</code> class that contains path to the image directory, path to the sound
     * files, selection of manual or automatic change, and if automatic is selected, the interval of change.
     *
     * @return  The new instantiated <code>SlidesConfig</code> class that contains the information about the current project.
     */
    public SlidesConfig getSlidesConfig()
    {
        String slidesFolder = folderPathTextField;
        if (slidesFolder.trim().isEmpty()) {
            slidesFolder = null;
        }
        String[] soundFiles = wavComponent.getElements().stream().map(SelectableElement::getData).toArray(String[]::new);
        boolean manualChange = manualChangeRadioButton.isSelected();
        int slideIntervalSeconds = (Integer)intervalInSecondsSpinner.getValue();

        String[] slidesFileList = slideComponent.getElements().stream().map(SelectableElement::getData).toArray(String[]::new);
        SlideEffect[] slidesEffectList = slideComponent.getElements().stream().map(ImageWithEffectThumbnail::getSlideEffect).toArray(SlideEffect[]::new);
        return new SlidesConfig(slidesFolder, slidesFileList, slidesEffectList, soundFiles, manualChange, slideIntervalSeconds);
    }

    private void loadSlidesConfig(SlidesConfig slidesConfig) {
        if (slidesConfig.isManualChange()) {
            manualChangeRadioButton.setSelected(true);
            intervalInSecondsSpinner.setEnabled(false);
        }
        else {
            autoChangeRadioButton.setSelected(true);
            intervalInSecondsSpinner.setEnabled(true);
        }
        intervalInSecondsSpinner.setValue(slidesConfig.getSlideIntervalSeconds());

        String[] soundFiles = slidesConfig.getSoundFiles();
        File[] files = new File[soundFiles.length];

        for (int i = 0; i < soundFiles.length; i++)
        {
            files[i] = new File(soundFiles[i]);
        }
        wavComponent.processSelectedPaths(Arrays.stream(files).map(f -> Paths.get(f.getPath())).collect(Collectors.toList()));
        //soundPathTextField = (soundFiles == null ? "" : soundFiles);

        String slidesFolder = slidesConfig.getSlidesFolder();
        if (slidesFolder == null) {
            folderPathTextField = "";
        }
        else {
            folderPathTextField = slidesFolder;
        }
        File folder = new File(slidesFolder);
        processSelectedFolder(folder);
        folderPathTextField = slidesFolder;
        slideComponent.processSelectedPaths(Arrays.stream(slidesConfig.getSlidesFileList()).map(Paths::get).collect(Collectors.toList()));
        SlideEffect[] effects = slidesConfig.getSlideEffects();
        for (int i = 0; i < effects.length; i++)
            slideComponent.setElementEffect(effects[i], i);
    }

    /**
     * Initializes the <code>JFileChooser slidesFolderChooser</code>.
     */
    private void setSlidesFolderChooser() {
        slidesFolderChooser = new JFileChooser();
        slidesFolderChooser.setCurrentDirectory(new File("."));
        slidesFolderChooser.setDialogTitle("Choose slides folder");
        slidesFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        slidesFolderChooser.setMultiSelectionEnabled(false);
    }

    /**
     * Initializes the <code>JFileChooser setSoundFileChooser</code>.
     */
    private void setSoundFileChooser() {
        soundFileChooser = new JFileChooser();
        soundFileChooser.setCurrentDirectory(new File("."));
        soundFileChooser.setDialogTitle("Choose sound file");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Sound .WAV/.AIFF files", soundExtensions);
        soundFileChooser.setFileFilter(filter);
        soundFileChooser.setMultiSelectionEnabled(true);
    }

    /**
     * Initializes the JButtons, JFileChoosers, JRadioButtons, and JMenuBar.
     */
    public void configureWidgets() {
        // Set JMenuChoosers for image directory and sounds.
        setSlidesFolderChooser();
        setSoundFileChooser();

        // Initialize spinner
        List<Integer> spinnerValues = new ArrayList<>();
        for (int i = 1; i<=60; i++)
            spinnerValues.add(i);
        intervalInSecondsSpinner.setModel(new SpinnerListModel(spinnerValues));

        // Add Action Listeners to RadioButtons
        manualChangeRadioButton.addActionListener(e -> {
            if (manualChangeRadioButton.isSelected())
                intervalInSecondsSpinner.setEnabled(false);
        });

        autoChangeRadioButton.addActionListener(e -> {
            if (autoChangeRadioButton.isSelected())
                intervalInSecondsSpinner.setEnabled(true);
        });

        // Initially have manual button chosen
        manualChangeRadioButton.setSelected(true);

        // Add action listeners to buttons
        addSlideButton.addActionListener(e -> {
            // Add image to be in the slideshow
            thumbnailComponent.applyToSelected(imageThumbnail -> slideComponent.add(imageThumbnail.getData()));
        });

        playButton.addActionListener(e -> {
            if(getSlidesConfig().getSlidesFileList().length > 0){
                // Make the current frame invisible
                setVisible(false);
                // Open up SlidesPlayer (the Run-Time slideshow player)
                new SlidesPlayer(this);

            }else{
                JOptionPane.showMessageDialog(null, "No Images Present");
            }
        });

        // Configure JMenuBar
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveMenuItem = new JMenuItem("Save ...");
        saveMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text file", "txt"));
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setDialogTitle("Save slides config file");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    FileSave fs = new FileSave();
                    fs.addFile(getSlidesConfig());
                    fs.printFile(fileToSave.getAbsolutePath());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JMenuItem loadMenuItem = new JMenuItem("Load ...");
        loadMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setDialogTitle("Open slides config file");
            fileChooser.setMultiSelectionEnabled(false);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    FileLoad fl = new FileLoad(fileChooser.getSelectedFile());
                    SlidesConfig slidesConfig = fl.getSlidesConfig();
                    if (slidesConfig != null)
                        loadSlidesConfig(slidesConfig);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JMenuItem openFolderItem = new JMenuItem("Open image folder...");
        openFolderItem.addActionListener(e -> {
            slidesFolderChooser.setAcceptAllFileFilterUsed(false);
            if (slidesFolderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File folderPath = slidesFolderChooser.getSelectedFile();
                processSelectedFolder(folderPath);
                folderPathTextField = folderPath.toString();
            }
        });

        JMenuItem musicFileItem = new JMenuItem("Select audio...");
        musicFileItem.addActionListener(e -> {
            soundFileChooser.setAcceptAllFileFilterUsed(false);
            if (soundFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] selected = soundFileChooser.getSelectedFiles();
                wavComponent.processSelectedPaths(Arrays.stream(selected).map(f -> Paths.get(f.getPath())).collect(Collectors.toList()));
            }
        });

        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(openFolderItem);
        fileMenu.add(musicFileItem);
        menuBar.add(fileMenu);

        thumbnailComponent = new ThumbnailComponent(thumbnailFlowPanel);
        wavFlowPanel.setLayout(new BoxLayout(wavFlowPanel, BoxLayout.Y_AXIS));
        wavComponent = new WavComponent(wavFlowPanel, null, leftWavButton, rightWavButton, null, removeWavButton);
        slideComponent = new SlideComponent(slidesFlowPanel, startSlideButton, leftSlideButton, rightSlideButton, finishSlideButton, removeSlideButton, fxButton);
        musicPanel.setLayout(new BoxLayout(musicPanel, BoxLayout.Y_AXIS));

        Runnable musicTracksUpdater = () -> {
            musicPanel.removeAll();
            int counter = 1;
            for (WavThumbnail wavThumbnail : wavComponent.getElements()) {
                int slideLength = 10;
                if (autoChangeRadioButton.isSelected())
                    slideLength = Integer.valueOf(intervalInSecondsSpinner.getValue().toString());
                musicPanel.add(new TrackPanel(MessageFormat.format("Track {0}", counter), wavThumbnail, slideLength));
                counter++;
            }
            musicPanel.revalidate();
            musicPanel.repaint();
        };

        wavComponent.setApplyOnUpdate(musicTracksUpdater);
        autoChangeRadioButton.addActionListener(e -> musicTracksUpdater.run());
        intervalInSecondsSpinner.addChangeListener(e -> musicTracksUpdater.run());
    }

    /**
     * Given the File object that represents the directory of images, process the images.
     *
     * @param folderPath  File object representing the directory of images.
     */
    private void processSelectedFolder(File folderPath) {
        // Create a filter for file extensions
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Slide JPEG files", slidesExtensions);

        // ?????????????
        List<String> slidesNamesList = Arrays.stream(folderPath.list(
                (dir, name) -> filter.accept(
                        Paths.get(dir.getAbsolutePath(), name).toFile()
                ))).filter(s->s.endsWith(".jpg") || s.endsWith(".jpeg")).collect(Collectors.toList());

        String[] slidesNames = new String[slidesNamesList.size()];
        for(int i=0; i<slidesNamesList.size(); i++){
            slidesNames[i] = slidesNamesList.get(i);
        }

        // If the array is not null,
        if (slidesNames != null) {

            if(slidesNames.length <= 0)
                JOptionPane.showMessageDialog(null, "Folder does not contain images");

            // Set the directory string
            folderPathTextField = folderPath.toString();

            // And process the images within the directory.
            thumbnailComponent.processSelectedPaths(
                    Arrays.stream(slidesNames).map(
                            name -> Paths.get(folderPath.getAbsolutePath(), name)).collect(Collectors.toList())
            );
        }
    }

    public static class ImageThumbnail extends SelectableElement {
        private ImageIcon icon;

        public ImageThumbnail(int index, String data, SelectableElementPanel<? extends SelectableElement> container) {
            super(index, data, container);

            try {
                // Read the image,
                BufferedImage img = ImageIO.read(new File(data));
                // Scale it,
                this.icon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            } catch (IOException e) {
                e.printStackTrace();
            }
            add(new JLabel(icon));
        }
    }

    public static class ThumbnailComponent extends SelectableElementPanel<ImageThumbnail> {
        public ThumbnailComponent(JPanel flowPanel) {
            super(flowPanel);
            flowPanel.setPreferredSize(new Dimension(100, 1000));
        }
    }

    public static class WavThumbnail extends SelectableElement {
        private static ImageIcon wavIcon;

        static {
            try {
                BufferedImage img = ImageIO.read(new File("wav.png"));
                wavIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private final int wavLength;

        public WavThumbnail(int index, String data, SelectableElementPanel<? extends SelectableElement> container) {
            super(index, data, container);
            setMaximumSize(new Dimension(400, 70));
            File fl = new File(data);

            int wavLength = 10;
            try {
                System.out.println(fl.getPath());
                JLabel label = new JLabel(fl.getCanonicalPath(), wavIcon, SwingConstants.LEFT);
                label.setFont(new Font("Serif", Font.PLAIN, 12));
                add(label);
                File file = new File(data);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                AudioFormat format = audioInputStream.getFormat();
                long frames = audioInputStream.getFrameLength();
                double durationInSeconds = (frames + 0.0) / format.getFrameRate();
                wavLength = Math.toIntExact(Math.round(durationInSeconds));
            } catch (UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
            }
            this.wavLength = wavLength;
        }

        public int getWavLength() {
            return wavLength;
        }
    }

    public static class WavComponent extends ReorderableElementPanel<WavThumbnail> {
        public WavComponent(JPanel flowPanel, JButton startButton, JButton beforeButton, JButton afterButton, JButton finishButton, JButton removeButton) {
            super(flowPanel, startButton, beforeButton, afterButton, finishButton, removeButton);
        }
    }

    public static class ImageWithEffectThumbnail extends ImageThumbnail {
        private SlideEffect slideEffect;

        public ImageWithEffectThumbnail(int index, String data, SelectableElementPanel<? extends SelectableElement> container) {
            super(index, data, container);
            slideEffect = new SlideEffect(SlideEffect.ChangeAnimation.None, 1);
        }

        public SlideEffect getSlideEffect() {
            return slideEffect;
        }
    }

    public static class SlideComponent extends ReorderableElementPanel<ImageWithEffectThumbnail> {
        private JButton fxButton;

        public SlideComponent(JPanel flowPanel, JButton startButton, JButton beforeButton, JButton afterButton, JButton finishButton, JButton removeButton,
                              JButton fxButton) {
            super(flowPanel, startButton, beforeButton, afterButton, finishButton, removeButton);
            this.fxButton = fxButton;
            this.fxButton.setEnabled(false);
            fxButton.addActionListener(e -> {
                if (selectedIndex >= 0) {
                    ImageWithEffectThumbnail element = elements.get(selectedIndex);
                    TransitionForm transitionForm = new TransitionForm(element.slideEffect);
                    transitionForm.setModal(true);
                    transitionForm.setVisible(true);
                    element.slideEffect = transitionForm.getSlideEffect();
                }
            });
        }

        public void setElementEffect(SlideEffect effect, int index) { elements.get(index).slideEffect = effect; }

        @Override
        public void onElementSelectedChanged(boolean isSelected, int index) {
            super.onElementSelectedChanged(isSelected, index);

            if (fxButton != null)
                fxButton.setEnabled(isSelected);
        }
    }

    public static class TrackPanel extends JPanel{
        public TrackPanel(String shortName, WavThumbnail wavThumbnail, int slideLength) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(0);
            JLabel trackNameLabel = new JLabel(shortName);
            trackNameLabel.setPreferredSize(new Dimension(70, 20));
            trackNameLabel.setMaximumSize(new Dimension(70, 20));
            int trackLength = wavThumbnail.wavLength;
            int panelLength = Math.toIntExact(Math.round((1.0 * trackLength / slideLength) * 63));
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(panelLength, 20));
            panel.setBackground(Color.GREEN);
            add(trackNameLabel);
            add(panel);
            int trackLengthMins = trackLength / 60;
            int trackLengthSecs = trackLength % 60;
            JLabel trackLengthLabel = new JLabel(MessageFormat.format("{0}:{1}",trackLengthMins, String.format("%2d", trackLengthSecs)));
            trackLengthLabel.setPreferredSize(new Dimension(50, 20));
            trackLengthLabel.setMaximumSize(new Dimension(50, 20));
            add(trackLengthLabel);
            setMaximumSize(new Dimension(140 + panelLength, 30));
            setAlignmentX( Component.LEFT_ALIGNMENT );
        }
    }
}
