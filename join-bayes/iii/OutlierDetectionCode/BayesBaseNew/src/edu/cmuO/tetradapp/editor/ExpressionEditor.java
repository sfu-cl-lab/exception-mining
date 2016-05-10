package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.NamingProtocol;
import edu.cmu.tetradapp.model.calculator.expression.Equation;
import edu.cmu.tetradapp.model.calculator.expression.ExpressionSignature;
import edu.cmu.tetradapp.model.calculator.parser.ExpressionParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * An editor for expressions.
 *
 * @author Tyler Gibson
 */
class ExpressionEditor extends JPanel {


    /**
     * The variable field.
     */
    private JTextField variable;


    /**
     * The expression field.
     */
    private JTextField expression;


    /**
     * Parser.
     */
    private ExpressionParser parser;


    /**
     * The last field to have focus.
     */
    private JTextField lastFocused;


    /**
     * Focus listeners.
     */
    private List<FocusListener> listeners = new LinkedList<FocusListener>();


    /**
     * States whether the remove box is clicked.
     */
    private boolean remove;

//    /**
//     * The replace positions in the expression editor (used when tokens are added).
//     */
//    private List<Line> replacements = new ArrayList<Line>();


    /**
     * The active selections if there is one.
     */
    private List<Selection> selections = new LinkedList<Selection>();


    /**
     * Normal selections color.
     */
    private static final Color SELECTION = new Color(204, 204, 255);


    /**
     * Creates the editor given the data set being worked on.
     */
    public ExpressionEditor(RectangularDataSet data) {
        this.parser = new ExpressionParser(data.getVariableNames());

        this.variable = new JTextField(5);
        this.expression = new JTextField(25);

//
//        this.variable.addFocusListener(new FocusAdapter() {
//            public void focusGained(FocusEvent evt) {
//                lastFocused = variable;
//                fireGainedFocus();
//            }
//        });
//        this.expression.addFocusListener(new FocusAdapter() {
//            public void focusGained(FocusEvent evt) {
//                lastFocused = expression;
//                fireGainedFocus();
//            }
//        });
        this.variable.addFocusListener(new VariableFocusListener(variable));
        this.expression.addFocusListener(new ExpressionFocusListener(expression));


        Box box = Box.createHorizontalBox();
        box.add(this.variable);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JLabel("="));
        box.add(Box.createHorizontalStrut(5));
        box.add(this.expression);
        JCheckBox checkBox = new JCheckBox();
        checkBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JCheckBox b = (JCheckBox) e.getSource();
                remove = b.isSelected();
            }
        });
        box.add(Box.createHorizontalStrut(2));
        box.add(checkBox);
        box.add(Box.createHorizontalGlue());

        add(box);
    }

    //============================ Public Method ======================================//

    /**
     * Returns the expression.
     *
     * @throws java.text.ParseException - If the values in the editor are not well-formed.
     */
    public Equation getEquation() throws ParseException {
        if (!NamingProtocol.isLegalName(this.variable.getText())) {
            this.variable.setSelectionColor(Color.RED);
            this.variable.select(0, this.variable.getText().length());
            this.variable.grabFocus();
            throw new ParseException(NamingProtocol.getProtocolDescription(), 1);
        }
        String equation = this.variable.getText() + "=" + this.expression.getText();
        try {
            return this.parser.parseEquation(equation);
        } catch (ParseException ex) {
            this.expression.setSelectionColor(Color.RED);
            this.expression.select(ex.getErrorOffset() - 1, this.expression.getText().length());
            this.expression.grabFocus();
            throw ex;
        }
    }

    /**
     * Adds a focus listener that will be notified about the focus events of
     * the fields in the editor.  The listener will only be notified of gain focus
     * events.
     */
    public void addFieldFocusListener(FocusListener listener) {
        this.listeners.add(listener);
    }


    /**
     * Sets the given variable in the variable field.
     *
     * @param var    - The variable to set.
     * @param append - States whether it should append to the field's current value or not.
     */
    public void setVariable(String var, boolean append) {
        if (append) {
            this.variable.setText(this.variable.getText() + var);
        } else {
            this.variable.setText(var);
        }
    }


    /**
     * Sets the given expression fragment in the expression field.
     *
     * @param exp    - Expression value to set.
     * @param append States whether it should append to the field's current value or not.
     */
    public void setExpression(String exp, boolean append) {
        if (exp == null) {
            return;
        }
        if (!this.selections.isEmpty()) {
            String text = this.expression.getText();
            Selection selection = this.selections.remove(0);
            if (caretInSelection(selection)) {
                this.expression.setText(text.substring(0, selection.x) + exp + text.substring(selection.y));
                this.adjustSelections(selection, exp);
                this.highlightNextSelection();
                return;
            }
        }

        if (append) {
            this.expression.setText(this.expression.getText() + exp);
        } else {
            this.expression.setText(exp);
        }
    }


    /**
     * Adds the signature to the expression field.
     */
    public void addExpressionSignature(ExpressionSignature signature) {
        String sig = signature.getSignature();
        String text = this.expression.getText();
        Selection selection = this.selections.isEmpty() ? null : this.selections.remove(0);
        // if empty add the sig with any selections.
        if (selection == null || !caretInSelection(selection)) {
            String newText = text + signature.getSignature();
            this.expression.setText(newText);
            addSelections(signature, newText, false);
            this.highlightNextSelection();
            return;
        }
        // otherwise there is a selections so we want to insert this sig in it.        
        String replacedText = text.substring(0, selection.x) + sig + text.substring(selection.y);
        this.expression.setText(replacedText);
        this.adjustSelections(selection, sig);
        addSelections(signature, replacedText, true);
        this.highlightNextSelection();
    }


    /**
     * Inserts the given symbol into the last focused field, of if there isn't one
     * the expression field.
     *
     * @param symbol
     * @param append States whether it should append to the field's current value or not.
     */
    public void insertLastFocused(String symbol, boolean append) {
        if (this.variable == lastFocused) {
            setVariable(symbol, append);
        } else {
            setExpression(symbol, append);
        }
    }


    public boolean removeSelected() {
        return this.remove;
    }

    //========================== Private Methods ====================================//


    /**
     * States whether the caret is in the current selection, if not false is returned and
     * all the selections are removed (as the user moved the caret around).
     */
    private boolean caretInSelection(Selection sel) {
        int caret = this.expression.getCaretPosition();
        if (caret < sel.x || sel.y < caret) {
            this.selections.clear();
            return false;
        }
        return true;
    }


    /**
     * Adds the selections for the given signature in the given text.
     */
    private void addSelections(ExpressionSignature signature, String newText, boolean addFirst) {
        int offset = 0;
        for (int i = 0; i < signature.getNumberOfArguments(); i++) {
            String arg = signature.getArgument(i);
            int index = newText.indexOf(arg);
            int end = index + arg.length();
            if (0 <= index) {
                if (addFirst) {
                    this.selections.add(i, new Selection(offset + index, offset + end));
                } else {
                    this.selections.add(new Selection(offset + index, offset + end));
                }
            }
            offset = offset + end;
            newText = newText.substring(end);
        }
    }


    private void fireGainedFocus() {
        FocusEvent evt = new FocusEvent(this, FocusEvent.FOCUS_GAINED);
        for (FocusListener l : this.listeners) {
            l.focusGained(evt);
        }
    }


    /**
     * Adjusts any current selections to the fact that the given selections was just
     * replaced by the given string.
     */
    private void adjustSelections(Selection selection, String inserted) {
        int dif = (selection.y - selection.x) - inserted.length();
        for (Selection sel : this.selections) {
            sel.x = sel.x - dif;
            sel.y = sel.y - dif;
        }
    }


    /**
     * Highlights the next selection.
     */
    private void highlightNextSelection() {
        if (!this.selections.isEmpty()) {
            Selection sel = this.selections.get(0);
            this.expression.setSelectionColor(SELECTION);
            this.expression.select(sel.x, sel.y);
            this.expression.grabFocus();
        }
    }

    //========================== Inner class ==============================//


    /**
     * Represents a 1D line.
     */
    private static class Selection {
        private int x;
        private int y;

        public Selection(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    /**
     * Focus listener for the variable field.
     */
    private class VariableFocusListener implements FocusListener {

        private JTextField field;

        public VariableFocusListener(JTextField field) {
            this.field = field;
        }

        @Override
		public void focusGained(FocusEvent e) {
            lastFocused = field;
            fireGainedFocus();
        }

        @Override
		public void focusLost(FocusEvent e) {
            if (field.getText() != null && field.getText().length() != 0
                    && !NamingProtocol.isLegalName(field.getText())) {
                field.setToolTipText(NamingProtocol.getProtocolDescription());
            } else {
                field.setSelectionColor(SELECTION);
                field.setToolTipText(null);
            }
        }
    }


    /**
     * Focus listener for the expression field.
     */
    private class ExpressionFocusListener implements FocusListener {

        private JTextField field;

        public ExpressionFocusListener(JTextField field) {
            this.field = field;
        }

        @Override
		public void focusGained(FocusEvent e) {
            lastFocused = field;
            fireGainedFocus();
        }

        @Override
		public void focusLost(FocusEvent e) {
            if (field.getText() == null || field.getText().length() == 0) {
                return;
            }
            try {
                parser.parseExpression(field.getText());
                field.setSelectionColor(SELECTION);
                field.setToolTipText(null);
            } catch (ParseException e1) {
                field.setToolTipText(e1.getMessage());
            }
        }
    }


}
