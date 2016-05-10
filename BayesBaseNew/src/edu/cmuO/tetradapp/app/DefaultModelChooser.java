package edu.cmu.tetradapp.app;

import edu.cmu.tetradapp.model.UnlistedSessionModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * THe default model chooser.
 *
 * @author Tyler Gibson
 */
class DefaultModelChooser extends JComponent implements ModelChooser {

    /**
     * Combo box used to allow user to select a model.
     */
    private JComboBox modelClassesBox;


    /**
     * The title of the chooser;
     */
    private String title;


    /**
     * THe name of the node
     */
    private String nodeName;


    /**
     * The id for the node, that this is a chooser for.
     */
    private String id;


    /**
     * Constructs the chooser.
     */
    public DefaultModelChooser() {

    }

    @Override
	public String getTitle() {
        return this.title;
    }

    @Override
	public Class getSelectedModel() {
        ClassWrapper wrapper = (ClassWrapper) modelClassesBox.getSelectedItem();
        return wrapper.getWrappedClass();
    }

    @Override
	public void setTitle(String title) {
        if (title == null) {
            throw new NullPointerException("The given title must not be null");
        }
        this.title = title;
    }

    @Override
	public void setNodeName(String nodeName) {
        if (nodeName == null) {
            throw new NullPointerException("The given node name should not be null");
        }
        this.nodeName = nodeName;
    }

    @Override
	public void setModelConfigs(List<SessionNodeModelConfig> configs) {
        List<ClassWrapper> wrapperList = new LinkedList<ClassWrapper>();

        for (SessionNodeModelConfig config : configs) {
            Class modelClass = config.getModel();
            if (!(UnlistedSessionModel.class.isAssignableFrom(modelClass))) {
                wrapperList.add(new ClassWrapper(modelClass, config.getName()));
            }
        }

        ClassWrapper[] wrappers = wrapperList.toArray(new ClassWrapper[0]);
        this.modelClassesBox = new JComboBox(wrappers);

        this.modelClassesBox.addItemListener(new ItemListener() {
            @Override
			public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedIndex = modelClassesBox.getSelectedIndex();
                    String selectedModelType = modelClassesBox.getItemAt(
                            selectedIndex).toString();
                    Preferences.userRoot().put(id, selectedModelType);
                }
            }
        });

        String storedModelType = Preferences.userRoot().get(this.id, "");
        for (int i = 0; i < modelClassesBox.getItemCount(); i++) {
            String currModelType = modelClassesBox.getItemAt(i).toString();
            if (storedModelType.equals(currModelType)) {
                this.modelClassesBox.setSelectedIndex(i);
            }
        }
    }

    @Override
	public void setNodeId(String id) {
        if (id == null) {
            throw new NullPointerException("The given id must not be null");
        }
        this.id = id;
    }

    @Override
	public void setup() {
        JButton info = new JButton("Help");

        info.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
            	SessionUtils.showPermissibleParentsDialog(getSelectedModel(),
                        modelClassesBox, false, false);
            }
        });

        JLabel l1 = new JLabel("Node name: " + nodeName);
        l1.setForeground(Color.black);

        setLayout(new BorderLayout());

        Box b1 = Box.createVerticalBox();
        Box b2 = Box.createHorizontalBox();

        b2.add(l1);
        b2.add(Box.createHorizontalGlue());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));

        Box b3 = Box.createHorizontalBox();

        b3.add(modelClassesBox);
        Font font = this.modelClassesBox.getFont();

        l1.setFont(font);
        b3.add(Box.createGlue());
        b3.add(info);
        b1.add(b3);
        add(b1, BorderLayout.CENTER);
    }

    //============================= Private Methods ================================//
   

    /**
     * Basic wrapper class.
     */
    private final static class ClassWrapper {
        private final Class wrappedClass;
        private final String name;

        public ClassWrapper(Class wrappedClass, String name) {
            this.wrappedClass = wrappedClass;
            this.name = name;
        }

        public Class getWrappedClass() {
            return this.wrappedClass;
        }

        @Override
		public String toString() {
            return this.name;
        }
    }
}
