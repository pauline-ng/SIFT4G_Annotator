package org.siftdna.genomes;


import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JList7<T> extends JList {

    public List<T> getSelectedValuesList() {
        ArrayList<T> typedSelected = new ArrayList<T>(getSelectedValues().length);
        for (Object selected : getSelectedValues()) {
            try {
                typedSelected.add((T) selected);
            } catch (ClassCastException exc) {
                //pass
            }
        }
        return typedSelected;
    }

}
