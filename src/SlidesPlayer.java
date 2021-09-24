import sun.awt.image.ToolkitImage;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SlidesPlayer extends JFrame {
    private static final int IMAGE_WIDTH = 600;
    private static final int IMAGE_HEIGHT = 400;
    private static final int ANIMATION_STEPS = 20;

    private JPanel mainPanel;
    private JButton backButton;
    private JButton pausePlayButton;
    private JButton forwardButton;
    private JLabel showLabel;
    private JButton backButton1;

    private final List<ImageIcon> imageIcons;
    private final List<ToolkitImage> images;
    private final List<SlideEffect> effects;
    private final SlidesConfig slidesConfig;
    private final SlidesConfigView slidesConfigView;
    private int currentSlide;

    private AutoChangeThread autoChangeThread;
    private WavPlayerThread wavPlayerThread;
    private boolean isPlayed;
    private AnimationIcon animationIcon;

    /**
     * Constructor. Initializes the private variables.
     *
     * @param slidesConfigView
     */
    public SlidesPlayer(SlidesConfigView slidesConfigView) {
        setContentPane(mainPanel);
        setResizable(false);
        imageIcons = new ArrayList<>();
        images = new ArrayList<>();
        effects = new ArrayList<>();

        this.slidesConfigView = slidesConfigView;
        this.slidesConfig = slidesConfigView.getSlidesConfig();
        for (int i = 0; i < slidesConfig.getSlidesFileList().length; i++) {
            String slideFileName = slidesConfig.getSlidesFileList()[i];
            SlideEffect slideEffect = slidesConfig.getSlideEffects()[i];
            Path imagePath = Paths.get(slideFileName);
            try {
                BufferedImage img = ImageIO.read(imagePath.toFile());
                Image dimg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                images.add((ToolkitImage) dimg);
                effects.add(slideEffect);
                imageIcons.add(new ImageIcon(dimg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        configureWidgets();

        currentSlide = 0;
        animationIcon = new AnimationIcon(images.get(0));
        showLabel.setIcon(animationIcon);
        if (slidesConfig.isManualChange()) {
            isPlayed = false;
            pausePlayButton.setEnabled(false);
        } else {
            isPlayed = true;
            pausePlayButton.setText("Pause");
            autoChangeThread = new AutoChangeThread();
            autoChangeThread.start();
        }

        if (slidesConfig.getSoundFiles() != null && slidesConfig.getSoundFiles().length > 0) {
            wavPlayerThread = new WavPlayerThread(slidesConfig.getSoundFiles());
            wavPlayerThread.start();
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (autoChangeThread != null)
                    autoChangeThread.interrupt();
                if (wavPlayerThread != null) {
                    wavPlayerThread.stopped = true;
                    wavPlayerThread.interrupt();
                }
            }
        });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    /**
     * Adds ActionListeners to the components within the frame.
     */
    private void configureWidgets() {
        backButton.addActionListener(e -> {
            if (isPlayed)
                autoChangeThread.interrupt();
            int previousSlide = (currentSlide + imageIcons.size() - 1) % imageIcons.size();
            Thread animation = new Thread(() -> {
                try {
                    applyAnimation(previousSlide);
                    currentSlide = previousSlide;
                    updateScene();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
            animation.start();
            if (isPlayed) {
                autoChangeThread = new AutoChangeThread();
                autoChangeThread.start();
            }
        });

        forwardButton.addActionListener(e -> {
            if (isPlayed)
                autoChangeThread.interrupt();
            int nextSlide = (currentSlide + 1) % imageIcons.size();
            Thread animation = new Thread(() -> {
                try {
                    applyAnimation(nextSlide);
                    currentSlide = nextSlide;
                    updateScene();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
            animation.start();
            if (isPlayed) {
                autoChangeThread = new AutoChangeThread();
                autoChangeThread.start();
            }
        });

        pausePlayButton.addActionListener(e -> {
            if (isPlayed) {
                pausePlayButton.setText("Play");
                if (autoChangeThread != null)
                    autoChangeThread.interrupt();
                if (wavPlayerThread != null) {
                    wavPlayerThread.paused = true;
                }
            } else {
                pausePlayButton.setText("Pause");
                autoChangeThread = new AutoChangeThread();
                autoChangeThread.start();
                if (slidesConfig.getSoundFiles() != null) {
                   if (wavPlayerThread == null){
                       wavPlayerThread = new WavPlayerThread(slidesConfig.getSoundFiles());
                       wavPlayerThread.start();
                   }else{
                       wavPlayerThread.paused = false;
                   }
                }
            }
            isPlayed = !isPlayed;
        });

        backButton1.addActionListener(e -> {
            slidesConfigView.setVisible(true);
            if (autoChangeThread != null)
                autoChangeThread.interrupt();
            if (wavPlayerThread != null) {
                wavPlayerThread.stopped = true;
                wavPlayerThread.interrupt();
            }
            this.setVisible(false);
        });
    }

    /**
     * Updates the frame with new image.
     */
    private void updateScene() {
        animationIcon.setImage(images.get(currentSlide));
        showLabel.revalidate();
        showLabel.repaint();
    }


    /**
     * Algorighm for moving to next image.
     *
     * @param nextSlide integer representing the index of the next image to be shown
     * @throws InterruptedException
     */
    private void applyAnimation(int nextSlide) throws InterruptedException {
        animationIcon.setNextImage(images.get(nextSlide));
        SlideEffect effect = effects.get((effects.size() - 1 + nextSlide)%effects.size());
        animationIcon.setNextAnimation(effect);
        int steps = ANIMATION_STEPS * effect.getDuration();
        for (int i = 1; i <= steps; i++) {
            animationIcon.setAnimationRatio(1.0 * i / (steps));
            updateScene();
            Thread.sleep(50);
        }
        animationIcon.setNextImage(null);
    }

    /**
     * Algorithm for auto-changing the photo
     */
    private class AutoChangeThread extends Thread {
        public AutoChangeThread() {
            super(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000 * slidesConfig.getSlideIntervalSeconds());
                        int nextSlide = (currentSlide + 1) % imageIcons.size();
                        applyAnimation(nextSlide);
                        currentSlide = nextSlide;
                        updateScene();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
        }
    }

    /**
     * Thread class for playing the music
     */
    public class WavPlayerThread extends Thread {
        private boolean stopped, paused;
        private String[] soundFileNames;
        private int lastPosition = 0;
        private long bytesRead = 0;

        /**
         * Constructor. Reads the audio file and plays it.
         */
        public WavPlayerThread(String[] soundFileNames) {
            stopped = false;
            this.soundFileNames = soundFileNames;
        }

        public void run() {
            while (!stopped) {
                for (int i = 0; i < soundFileNames.length && !stopped; i++) {
                    File soundFile = new File(soundFileNames[i]);
                    AudioInputStream audioInputStream = null;
                    try {
                        audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AudioFormat audioFormat = audioInputStream.getFormat();
                    SourceDataLine line = null;
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                    try {
                        line = (SourceDataLine) AudioSystem.getLine(info);
                        line.open(audioFormat);
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    line.start();
                    int nBytesRead = 0;
                    byte[] abData = new byte[128000];
                    try {
                        audioInputStream.skip(bytesRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (!stopped && nBytesRead != -1 && paused == false) {
                        try {
                            try {
                                Thread.sleep(100);
                                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                                bytesRead+=nBytesRead;
                            } catch (InterruptedException ie) {
                                audioInputStream.close();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (nBytesRead >= 0)
                            line.write(abData, 0, nBytesRead);
                    }
                    line.drain();
                    line.close();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public int getLastPosition() {
            return lastPosition;
        }

        public void setLastPosition(int lastPosition) {
            this.lastPosition = lastPosition;
        }

        public long getBytesRead() {
            return bytesRead;
        }

        public void setBytesRead(long bytesRead) {
            this.bytesRead = bytesRead;
        }
    }

    /**
     * Class for animation.
     */
    private static class AnimationIcon extends ImageIcon {
        private ToolkitImage nextImage;
        private double animationRatio;
        private SlideEffect slideEffect;

        /**
         * Public constructor.
         *
         * @param image Call parent class constructor
         */
        public AnimationIcon(Image image) {
            super(image);
        }

        /**
         * Public setter for private variable.
         *
         * @param nextImage Next image to be shown.
         */
        public void setNextImage(ToolkitImage nextImage) {
            this.nextImage = nextImage;
        }

        /**
         * Public setter for private variable.
         *
         * @param animationRatio Ratio of animation (duration?)
         */
        public void setAnimationRatio(double animationRatio) {
            this.animationRatio = animationRatio;
        }

        /**
         * Public setter for private variable.
         *
         * @param slideEffect type of transition.
         */
        public void setNextAnimation(SlideEffect slideEffect) {
            this.slideEffect = slideEffect;
        }

        /**
         * Actual animation algorithm.
         *
         * @param c
         * @param g
         * @param x
         * @param y
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            Graphics2D g2d = (Graphics2D) g.create();
            if (nextImage != null) {
                BufferedImage bufferedImage = nextImage.getBufferedImage();
                BufferedImage sub;
                if (slideEffect.getChangeAnimation() != SlideEffect.ChangeAnimation.Cross &&
                        slideEffect.getChangeAnimation() != SlideEffect.ChangeAnimation.None) {
                    int xx = 0;
                    int yy = 0;
                    int width = 0;
                    int height = 0;
                    switch (slideEffect.getChangeAnimation()) {
                        case WipeLeft:
                            xx = Math.toIntExact(Math.round(IMAGE_WIDTH * (1 - animationRatio)));
                            yy = 0;
                            width = Math.toIntExact(Math.round(IMAGE_WIDTH * animationRatio));
                            height = IMAGE_HEIGHT;
                            break;

                        case WipeRight:
                            xx = 0;
                            yy = 0;
                            width = Math.toIntExact(Math.round(IMAGE_WIDTH * animationRatio));
                            height = IMAGE_HEIGHT;
                            break;
                        case WipeUp:
                            xx = 0;
                            yy = Math.toIntExact(Math.round(IMAGE_HEIGHT * (1 - animationRatio)));
                            width = IMAGE_WIDTH;
                            height = Math.toIntExact(Math.round(IMAGE_HEIGHT * animationRatio));
                            break;
                        case WipeDown:
                            xx = 0;
                            yy = 0;
                            width = IMAGE_WIDTH;
                            height = Math.toIntExact(Math.round(IMAGE_HEIGHT * animationRatio));
                            break;
                    }
                    sub = bufferedImage.getSubimage(xx, yy, width, height);
                    g2d.drawImage(sub, xx, yy, null);
                } else if (slideEffect.getChangeAnimation() == SlideEffect.ChangeAnimation.Cross) {
                    int width = Math.toIntExact(Math.round(IMAGE_WIDTH * animationRatio / 2));
                    int height = Math.toIntExact(Math.round(IMAGE_HEIGHT * animationRatio / 2));
                    BufferedImage topLeft = bufferedImage.getSubimage(0, 0, width, height);
                    g2d.drawImage(topLeft, 0, 0, null);

                    BufferedImage topRight = bufferedImage.getSubimage(IMAGE_WIDTH - width, 0, width, height);
                    g2d.drawImage(topRight, IMAGE_WIDTH - width, 0, null);

                    BufferedImage bottomLeft = bufferedImage.getSubimage(0, IMAGE_HEIGHT - height, width, height);
                    g2d.drawImage(bottomLeft, 0, IMAGE_HEIGHT - height, null);

                    BufferedImage bottomRight = bufferedImage.getSubimage(IMAGE_WIDTH - width, IMAGE_HEIGHT - height, width, height);
                    g2d.drawImage(bottomRight, IMAGE_WIDTH - width, IMAGE_HEIGHT - height, null);
                }
            }
            g2d.dispose();
        }
    }
}
