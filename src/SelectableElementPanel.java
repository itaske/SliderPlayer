import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class SelectableElementPanel<T extends SelectableElement> {
    protected final JPanel flowPanel;
    protected final List<T> elements;

    protected int selectedIndex;
    protected Runnable applyOnUpdate;

    public SelectableElementPanel(JPanel flowPanel) {
        this.flowPanel = flowPanel;
        this.elements = new ArrayList<>();
        this.selectedIndex = -1;
    }

    public List<T> getElements() {
        return elements;
    }

    public void add(String data) {
        T element = (T) SelectableElement.createElement(elements.size(), data, this);
        elements.add(element);
        resetFlowPanelWithList();
    }

    public void processSelectedPaths(Collection<Path> paths) {
        setValues(paths.stream().map(Path::toString).collect(Collectors.toList()));
    }

    public void applyToSelected(Consumer<T> consumer) {
        if (selectedIndex >= 0)
            consumer.accept(elements.get(selectedIndex));
    }

    public void setApplyOnUpdate(Runnable runnable) {
        this.applyOnUpdate = runnable;
    }

    public void onElementSelectedChanged(boolean isSelected, int index) {
        if (selectedIndex >= 0 && elements.size() > selectedIndex && selectedIndex != index) {
            T oldSelected = elements.get(selectedIndex);
            oldSelected.setSelected(false);
        }

        selectedIndex = isSelected ? index : -1;
    }

    protected void resetFlowPanelWithList() {
        flowPanel.removeAll();
        for(int i = 0; i<elements.size(); i++) {
            T element = elements.get(i);
            element.setIndex(i);
            flowPanel.add(element);
        }
        flowPanel.repaint();
        flowPanel.updateUI();

        if (applyOnUpdate != null)
            applyOnUpdate.run();
    }

    private void setValues(List<String> data) {
        elements.clear();
        for (int i = 0; i < data.size(); i++) {
            T element = (T) SelectableElement.createElement(elements.size(), data.get(i), this);
            elements.add(element);
        }
        resetFlowPanelWithList();
    }
}
