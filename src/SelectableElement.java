import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SelectableElement extends JPanel implements MouseListener {
    private final String data;
    private final SelectableElementPanel<? extends SelectableElement> container;
    private boolean selected;
    private int index;

    public SelectableElement(int index, String data, SelectableElementPanel<? extends SelectableElement> container) {
        this.data = data;
        this.container = container;

        this.selected = false;
        this.index = index;

        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        addMouseListener(this);
        updateView();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        selected = !selected;
        container.onElementSelectedChanged(selected, index);
        updateView();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public String getData() {
        return data;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateView();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void updateView() {
        setBackground(selected ? Color.blue : Color.white);
        repaint();
    }

    public static <T extends SelectableElement> SelectableElement createElement(int index, String data, SelectableElementPanel<T> container ) {
        if (container.getClass() == SlidesConfigView.ThumbnailComponent.class)
            return new SlidesConfigView.ImageThumbnail(index, data, container);
        if (container.getClass() == SlidesConfigView.WavComponent.class)
            return new SlidesConfigView.WavThumbnail(index, data, container);
        if (container.getClass() == SlidesConfigView.SlideComponent.class)
            return new SlidesConfigView.ImageWithEffectThumbnail(index, data, container);
        throw new IllegalStateException();
    }
}
