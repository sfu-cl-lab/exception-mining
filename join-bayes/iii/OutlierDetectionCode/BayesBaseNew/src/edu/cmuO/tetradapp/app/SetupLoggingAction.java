package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Prompts and allows them to config general logging features.
 *
 * @author Tyler Gibson
 * @version $Revision: 1 $ $Date: Mar 14, 2007 3:40:22 AM $
 */
class SetupLoggingAction extends AbstractAction {



    public SetupLoggingAction() {
        super("Setup logging ...");
    }


    //========================= Public Methods =================================//


    @Override
	public void actionPerformed(ActionEvent e) {
        JComponent comp = buildSetupLoggingComponent();
        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), comp,
                "File Logging Setup", JOptionPane.PLAIN_MESSAGE);
    }



    /**
     * The component used to config logging.
     */
    public static JComponent buildSetupLoggingComponent() {



        // build yes/no combo box.
        JComboBox activateCombo = new JComboBox(new String[]{"No", "Yes"});
        activateCombo.setMaximumSize(activateCombo.getPreferredSize());
        activateCombo.setSelectedItem(TetradLogger.getInstance().isLogging() ? "Yes" : "No");
        activateCombo.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox) e.getSource();
                TetradLogger.getInstance().setLogging("Yes".equals(combo.getSelectedItem()));
            }
        });

        String saveLocation = TetradLogger.getInstance().getLoggingDirectory();
        final JTextField saveField = new JTextField(saveLocation);
        saveField.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JTextField field = (JTextField) e.getSource();
                try {
                    TetradLogger.getInstance().setLoggingDirectory(field.getText());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), ex.getMessage());
                }
            }
        });

        JButton chooseButton = new JButton(" ... ");
        chooseButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                String saveLocation = TetradLogger.getInstance().getLoggingDirectory();

                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(new File(saveLocation));

                int ret = chooser.showOpenDialog(JOptionUtils.centeringComp());

                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    Preferences.userRoot().put("loggingDirectory", selectedFile.getAbsolutePath());
                    saveField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        chooseButton.setBorder(new EtchedBorder());
        JTextField prefixField = new JTextField(TetradLogger.getInstance().getLoggingFilePrefix());

        prefixField.addCaretListener(new CaretListener() {
            @Override
			public void caretUpdate(CaretEvent e) {
                JTextField field = (JTextField) e.getSource();
                String text = field.getText();

                if (!text.matches("[a-zA-Z_]*")) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "Spaces, numbers, and special characters (" +
                                    "except underlines) in filenames will be " +
                                    "ignored. You might want to delete them.",
                            "Friendly Detail Message",
                            JOptionPane.WARNING_MESSAGE);
                }

                TetradLogger.getInstance().setLoggingFilePrefix(text);
            }
        });

        JCheckBox automatic = new JCheckBox("Automatically display log output.");
        Boolean b = TetradLogger.getInstance().isAutomaticLogDisplayEnabled();
        automatic.setSelected(b != null && b);
        automatic.setHorizontalTextPosition(SwingConstants.LEFT);
        automatic.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox)e.getSource();
                TetradLogger.getInstance().setAutomaticLogDisplayEnabled(box.isSelected());
            }
        });
        Box automaticBox = Box.createHorizontalBox();
        automaticBox.add(automatic);
        automaticBox.add(Box.createHorizontalGlue());

        // Do layout.
        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Activate Logging: "));
        b2.add(Box.createHorizontalStrut(5));
        b2.add(activateCombo);
        b2.add(Box.createHorizontalGlue());

        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));

        b1.add(createLogToBox());
        b1.add(Box.createVerticalStrut(5));
        b1.add(automaticBox);
        b1.add(Box.createVerticalStrut(10));

        Box b4 = Box.createHorizontalBox();
        b4.add(new JLabel("Output Directory:"));
        b4.add(Box.createHorizontalGlue());
        b1.add(b4);

        Box b5 = Box.createHorizontalBox();
        b5.add(saveField);
        b5.add(chooseButton);
        b1.add(b5);
        b1.add(Box.createVerticalStrut(5));

        Box b6 = Box.createHorizontalBox();
        b6.add(new JLabel("File Prefix:"));
        b6.add(Box.createHorizontalGlue());
        b1.add(b6);

        Box b7 = Box.createHorizontalBox();
        b7.add(prefixField);
        b1.add(b7);
        b1.add(Box.createVerticalStrut(5));

        Box b8 = Box.createHorizontalBox();
        b8.add(new JLabel("<html>" +
                "Output will be written to sequentially numbered files, using the<br>" +
                "given file prefix, in the given directory, with one output file<br>" +
                "generated per search." + "</html>"));
        b1.add(b8);

        return b1;
    }

    //========================= Private Methods =================================//


    /**
     * Builds the output selection boxes.
     */
    private static Box createLogToBox() {
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Log output to: "));
        box.add(Box.createHorizontalStrut(5));

        JCheckBox fileCheckBox = new JCheckBox(" File ");
        fileCheckBox.setSelected(TetradLogger.getInstance().isFileLoggingEnabled());
        fileCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
        fileCheckBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                TetradLogger.getInstance().setFileLoggingEnabled(box.isSelected());
            }
        });

        JCheckBox textareaCheckBox = new JCheckBox(" Log Display ");
        textareaCheckBox.setSelected(TetradLogger.getInstance().isDisplayLogEnabled());
        textareaCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
        textareaCheckBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                TetradLogger.getInstance().setDisplayLogEnabled(box.isSelected());
            }
        });

        box.add(fileCheckBox);
        box.add(textareaCheckBox);
        box.add(Box.createHorizontalGlue());

        return box;
    }


}
