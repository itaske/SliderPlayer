import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ReorderableElementPanel<T extends SelectableElement> extends SelectableElementPanel<T> implements ActionListener {
    private final JButton startButton;
    private final JButton beforeButton;
    private final JButton afterButton;
    private final JButton finishButton;
    private final JButton removeButton;

    public ReorderableElementPanel(JPanel flowPanel, JButton startButton, JButton beforeButton, JButton afterButton,
                                   JButton finishButton, JButton removeButton) {
        super(flowPanel);

        this.startButton = startButton;
        this.beforeButton = beforeButton;
        this.afterButton = afterButton;
        this.finishButton = finishButton;
        this.removeButton = removeButton;

        this.beforeButton.setEnabled(false);
        this.beforeButton.addActionListener(this);
        this.afterButton.setEnabled(false);
        this.afterButton.addActionListener(this);
        this.removeButton.setEnabled(false);
        this.removeButton.addActionListener(this);

        if (startButton != null) {
            this.startButton.setEnabled(false);
            this.startButton.addActionListener(this);
        }
        if (finishButton != null) {
            this.finishButton.setEnabled(false);
            this.finishButton.addActionListener(this);
        }

        this.selectedIndex = -1;
    }

    @Override
    public void onElementSelectedChanged(boolean isSelected, int index) {
        super.onElementSelectedChanged(isSelected, index);

        beforeButton.setEnabled(isSelected);
        afterButton.setEnabled(isSelected);
        removeButton.setEnabled(isSelected);

        if (startButton != null)
            startButton.setEnabled(isSelected);
        if (finishButton != null)
            finishButton.setEnabled(isSelected);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        T element = selectedIndex >= 0 ? elements.remove(selectedIndex) : null;
        if (e.getSource() == startButton)
            selectedIndex = 0;
        if (e.getSource() == beforeButton)
            selectedIndex = Math.max(0, selectedIndex - 1);
        if (e.getSource() == afterButton)
            selectedIndex = Math.min(elements.size(), selectedIndex + 1);
        if (e.getSource() == finishButton)
            selectedIndex = elements.size();
        if (e.getSource() == removeButton) {
            if (elements.isEmpty())
                onElementSelectedChanged(false, -1);
            else {
                int newIndex = Math.min(elements.size() - 1, selectedIndex);
                elements.get(newIndex).setSelected(true);
                onElementSelectedChanged(true, newIndex);
            }
        } else {
            if (element != null)
                elements.add(selectedIndex, element);
        }

        resetFlowPanelWithList();

        for (int i = 0; i < elements.size(); i++) {
            if ((elements.get(i).getBackground() == Color.BLUE) != (i == selectedIndex))
                System.out.println("ERROR");
        }
    }
}
