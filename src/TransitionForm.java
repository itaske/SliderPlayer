import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransitionForm extends JDialog {
    private JRadioButton wipeLeftRadioButton;
    private JRadioButton wipeRightRadioButton;
    private JRadioButton wipeUpRadioButton;
    private JRadioButton wipeDownRadioButton;
    private JRadioButton crossfadeRadioButton;
    private JRadioButton noneDefaultRadioButton;
    private JSpinner transitionLength;
    private JButton confirmButton;
    private JButton cancelButton;
    private JPanel mainPanel;

    private final SlideEffect slideEffect;
    private boolean canceled;

    public TransitionForm(SlideEffect slideEffect) {
        this.slideEffect = slideEffect;

        setContentPane(mainPanel);

        configureWidgets();

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void configureWidgets() {
        List<Integer> spinnerValues = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            spinnerValues.add(i);
        }
        transitionLength.setModel(new SpinnerListModel(spinnerValues));
        transitionLength.setValue(slideEffect.getDuration());

        confirmButton.addActionListener(e -> {
            canceled = false;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            canceled = true;
            dispose();
        });

        switch (slideEffect.getChangeAnimation()) {
            case WipeDown:
                wipeDownRadioButton.setSelected(true);
                break;
            case WipeLeft:
                wipeLeftRadioButton.setSelected(true);
                break;
            case WipeUp:
                wipeUpRadioButton.setSelected(true);
                break;
            case WipeRight:
                wipeRightRadioButton.setSelected(true);
                break;
            case Cross:
                crossfadeRadioButton.setSelected(true);
                break;
            default:
                noneDefaultRadioButton.setSelected(true);
        }
    }

    public SlideEffect getSlideEffect() {
        if (canceled) {
            return slideEffect;
        }
        SlideEffect.ChangeAnimation changeAnimation = SlideEffect.ChangeAnimation.None;
        if (wipeRightRadioButton.isSelected())
            changeAnimation = SlideEffect.ChangeAnimation.WipeRight;
        if (wipeLeftRadioButton.isSelected())
            changeAnimation = SlideEffect.ChangeAnimation.WipeLeft;
        if (wipeUpRadioButton.isSelected())
            changeAnimation = SlideEffect.ChangeAnimation.WipeUp;
        if (wipeDownRadioButton.isSelected())
            changeAnimation = SlideEffect.ChangeAnimation.WipeDown;
        if (crossfadeRadioButton.isSelected())
            changeAnimation = SlideEffect.ChangeAnimation.Cross;
        if (noneDefaultRadioButton.isSelected())
            changeAnimation = SlideEffect.ChangeAnimation.None;
        int duration = Integer.parseInt(transitionLength.getValue().toString());
        return new SlideEffect(changeAnimation, duration);
    }
}
