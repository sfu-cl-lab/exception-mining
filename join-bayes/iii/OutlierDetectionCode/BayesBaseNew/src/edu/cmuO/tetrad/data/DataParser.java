package edu.cmu.tetrad.data;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.DefaultTetradLoggerConfig;
import edu.cmu.tetrad.util.NamingProtocol;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parses a tabular data file or covariance matrix, with or without data file
 * sectioning, with possibly mixed continuous/discrete variables. With
 * sectioning, a /variables, a /data, and a /knowledge section may be specified.
 * (The /data section is required.) Without sectioning, it is assumed that no
 * variables will be defined in advance and that there will be no knowledge.
 *
 * @author Joseph Ramsey
 */
public final class DataParser {

    /**
     * A set of characters that in any combination makes up a delimiter.
     */
    private DelimiterType delimiterType = DelimiterType.WHITESPACE;

    /**
     * True iff variable names in the data section of the file are listed in the
     * first row.
     */
    private boolean varNamesSupplied = true;

    /**
     * True iff case IDs are provided in the file.
     */
    private boolean idsSupplied = false;

    /**
     * Assuming caseIdsPresent is true, this is null if case IDs are in an
     * unlabeled initial column; otherwise, they are assumed to be in a labeled
     * column by this name.
     */
    private String idLabel = null;

    /**
     * The initial segment of a line that is to be considered a comment line.
     */
    private String commentMarker = "//";

    /**
     * A character that sets off quoted strings.
     */
    private char quoteChar = '"';

    /**
     * In parsing data, missing values will be marked either by this string or
     * by an empty string.
     */
    private String missingValueMarker = "*";

    /**
     * In parsing integral columns, columns with up to this many distinct values
     * will be parsed as discrete; otherwise, continuous.
     */
    private int maxIntegralDiscrete = 10;

    /**
     * Known variable definitions. These will usurp any guessed variable
     * definitions by name.
     */
    private List<Node> knownVariables = new LinkedList<Node>();


    /**
     * The tetrad logger.
     */
    private TetradLogger logger = TetradLogger.getInstance();


    /**
     * Log empty token messages.
     */
    private boolean logEmptyTokens = false;

    /**
     * Constructs a new data parser.
     */
    public DataParser() {
    }

    //============================PUBLIC METHODS========================//


    public void setLogEmptyTokens(boolean log){
        this.logEmptyTokens = log;
    }


    /**
     * Lines beginning with blanks or this marker will be skipped.
     */
    public void setCommentMarker(String commentMarker) {
        if (commentMarker == null) {
            throw new NullPointerException("Cannot be null.");
        }

        this.commentMarker = commentMarker;
    }

    /**
     * This is the delimiter used to parse the data. Default is whitespace.
     */
    public void setDelimiter(DelimiterType delimiterType) {
        if (delimiterType == null) {
            throw new NullPointerException("Cannot be null.");
        }

        this.delimiterType = delimiterType;
    }

    /**
     * Text between matched ones of these will treated as quoted text.
     */
    public void setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
    }

    /**
     * Will read variable names from the first row if this is true; otherwise,
     * will make make up variables in the series X1, x2, ... Xn.
     */
    public void setVarNamesSupplied(boolean varNamesSupplied) {
        this.varNamesSupplied = varNamesSupplied;
    }

    /**
     * If true, a column of ID's is supplied; otherwise, not.
     */
    public void setIdsSupplied(boolean caseIdsPresent) {
        this.idsSupplied = caseIdsPresent;
    }

    /**
     * If null, ID's are in an unlabeled first column; otherwise, they are in
     * the column with the given label.
     */
    public void setIdLabel(String caseIdsLabel) {
        this.idLabel = caseIdsLabel;
    }

    /**
     * Tokens that are blank or equal to this value will be counted as missing
     * values.
     */
    public void setMissingValueMarker(String missingValueMarker) {
        if (missingValueMarker == null) {
            throw new NullPointerException("Cannot be null.");
        }

        this.missingValueMarker = missingValueMarker;
    }

    /**
     * Integral columns with up to this number of discrete values will be
     * treated as discrete.
     */
    public void setMaxIntegralDiscrete(int maxIntegralDiscrete) {
        if (maxIntegralDiscrete < 0) {
            throw new IllegalArgumentException(
                    "Must be >= 0: " + maxIntegralDiscrete);
        }

        this.maxIntegralDiscrete = maxIntegralDiscrete;
    }

    /**
     * The known variables for a given name will usurp guess the variable by
     * that name.
     */
    public void setKnownVariables(List<Node> knownVariables) {
        if (knownVariables == null) {
            throw new NullPointerException();
        }

        this.knownVariables = knownVariables;
    }

    /**
     * Parses the given files for a tabular data set, returning a
     * RectangularDataSet if successful.
     *
     * @throws IOException if the file cannot be read.
     */
    public RectangularDataSet parseTabular(File file) throws IOException {
        return parseTabular(loadChars(file));
    }

    /**
     * Parses the given character array for a tabular data set, returning a
     * RectangularDataSet if successful. Log messages are written to the
     * LogUtils log; to view them, add System.out to that.
     */
    //@zqian May 2nd
    public RectangularDataSet parseTabular(char[] chars) {

        // Wrap a liniezer around the chars.
        CharArrayReader reader = new CharArrayReader(chars);
        Lineizer lineizer = new Lineizer(reader, commentMarker);

        if (!lineizer.hasMoreLines()) {
            throw new IllegalArgumentException("Data source is empty.");
        }
        this.logger.setTetradLoggerConfig(createConfig());

        this.logger.info("\nDATA LOADING PARAMETERS:");
        this.logger.info("File type = TABULAR");
        this.logger.info("Comment marker = " + commentMarker);
        this.logger.info("Delimiter chars = " + delimiterType);
        this.logger.info("Quote char = " + quoteChar);
        this.logger.info("Var names first row = " + varNamesSupplied);
        this.logger.info("IDs supplied = " + idsSupplied);
        this.logger.info("ID label = " + idLabel);
        this.logger.info("Missing value marker = " + missingValueMarker);
        this.logger.info("Max discrete = " + maxIntegralDiscrete);
        this.logger.info("--------------------");

        // The delimiter comes from the delimiter type.
        Pattern delimiter = delimiterType.getPattern();

        // Read in variable definitions.
        String line = lineizer.nextLine();
        boolean variablesSectionIncluded = false;

        if (line.startsWith("/variables")) {
            variablesSectionIncluded = true;

            // Read lines one at a time. The part to the left of ":" is
            // the variable name. Divide the part to the right of ":" using
            // commas. Each token should be of the form n=name. n should
            // start with 0 and increment by 1. Build a variable with this
            // information and store it on the knownVariables list. (If
            // there's already a variable by that name in the list, throw
            // an exception.)
            LINES:
            while (lineizer.hasMoreLines()) {
                line = lineizer.nextLine();

                if (line.startsWith("/data")) {
                    break;
                }

                RegexTokenizer tokenizer = new RegexTokenizer(line,
                        DelimiterType.COLON.getPattern(), quoteChar);
                String name = tokenizer.nextToken().trim();

                if ("".equals(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": Expected a variable name, got an empty token.");
                }

                // Skip any definitions for known variables.
                for (Node node : knownVariables) {
                    if (name.equals(node.getName())) {
                        continue LINES;
                    }
                }

                String values = tokenizer.nextToken();

                if ("".equals(values.trim())) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": Empty variable specification for variable " + name + ".");
                } else if ("Continuous".equalsIgnoreCase(values.trim())) {
                    ContinuousVariable variable = new ContinuousVariable(name);
                    knownVariables.add(variable);
                } else {
                    List<String> categories = new LinkedList<String>();
                    tokenizer = new RegexTokenizer(values,
                            delimiterType.getPattern(), quoteChar);

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken().trim();

                        if ("".equals(token)) {
                            throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                                    + ": Expected a category name, got an empty token, " +
                                    "for variable " + name + ".");
                        }

                        if (categories.contains(token)) {
                            throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                                    + ": Duplicate category (" + token + ") for variable "
                                    + name + ".");
                        }

                        categories.add(token);
                    }

                    DiscreteVariable variable = new DiscreteVariable(name, categories);
                    variable.setAccommodateNewCategories(false);
                    knownVariables.add(variable);
                }
            }
        }

        if (variablesSectionIncluded && !line.startsWith("/data")) {
            throw new IllegalArgumentException(
                    "If a /variables section is included, a /data section must follow.");
        }

        // Construct list of variable names.
        String dataFirstLine = line;

        if (line.startsWith("/data")) {
            dataFirstLine = lineizer.nextLine();
        }

        List<String> varNames;

        if (varNamesSupplied) {
            varNames = new ArrayList<String>();
            RegexTokenizer tokenizer =
                    new RegexTokenizer(dataFirstLine, delimiter, quoteChar);

            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();

                if ("".equals(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": Expected variable name, got empty token: " + line);
                }

                if (varNames.contains(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": Duplicate variable name (" + name + ").");
                }

                varNames.add(name);
            }

            dataFirstLine = null;
        } else {
            varNames = new LinkedList<String>();
            RegexTokenizer tokenizer =
                    new RegexTokenizer(dataFirstLine, delimiter, quoteChar);

            if (idsSupplied && idLabel == null) {
                if (tokenizer.hasMoreTokens()) {

                    // Eat first token, which is supposed to be a case ID.
                    tokenizer.nextToken();
                }
            }

            int i = 0;

            while (tokenizer.hasMoreTokens()) {
                tokenizer.nextToken();
                varNames.add("X" + (++i));
            }
        }

        // Adjust variable names for id, returning the index of id.
        int idIndex = adjustForId(varNames, lineizer);

        // Scan for variable types.
        DataSetDescription description = scanForDescription(varNames, lineizer,
                delimiter, dataFirstLine, idIndex,variablesSectionIncluded);

        // Close the reader and re-open for a second pass.
        reader.close();
        CharArrayReader reader2 = new CharArrayReader(chars);
        lineizer = new Lineizer(reader2, commentMarker);
        String line2;

        // Skip through /variables.
        if (variablesSectionIncluded) {
            while (lineizer.hasMoreLines()) {
                line2 = lineizer.nextLine();

                if (line2.startsWith("/data")) {
                    break;
                }
            }
        }

        line2 = lineizer.nextLine();

        if (line2.startsWith("/data")) {
            line2 = lineizer.nextLine();
        }

        // Note that now line2 is either the first line of the file or the
        // first line after the /data. Either way it's either the variable line
        // or the first line of the data itself.
        dataFirstLine = line2;

        if (varNamesSupplied) {
            line2 = lineizer.nextLine();
            dataFirstLine = line2;
        }

        // Read in the data.
        RectangularDataSet dataSet = new ColtDataSet(description.getNumRows(),
                description.getVariables());

        int row = -1;

        while (lineizer.hasMoreLines()) {
            if (dataFirstLine == null) {
                line2 = lineizer.nextLine();
            } else {
                line2 = dataFirstLine;
                dataFirstLine = null;
            }

            if (line2.startsWith("/knowledge")) {
                break;
            }

            ++row;

           /* RegexTokenizer tokenizer1 = new RegexTokenizer(line2, delimiter, quoteChar);*/ 
            //@zqian May 2nd
            RegexTokenizer tokenizer1 = new RegexTokenizer(line2, description.getDelimiter(),
                    quoteChar);
            if (description.isMultColumnIncluded() && tokenizer1.hasMoreTokens()) {
                String token = tokenizer1.nextToken().trim();
                int multiplier = Integer.parseInt(token);
                dataSet.setMultiplier(row, multiplier);
            }
            //@zqian May 2nd
            
            
            int col = -1;

            while (tokenizer1.hasMoreTokens()) {
                String token = tokenizer1.nextToken().trim();
                setValue(dataSet, row, ++col, token);
            }
        }

        // Copy ids into the data set and remove the id column.
        if (idIndex != -1) {
            DiscreteVariable idVar =
                    (DiscreteVariable) dataSet.getVariable(idIndex);

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                int index = dataSet.getInt(i, idIndex);

                if (index == -99) {
                    continue;
                }

                String id = idVar.getCategories().get(index);
                dataSet.setCaseId(i, id);
            }

            dataSet.removeColumn(idVar);
        }

        Knowledge knowledge = parseKnowledge(lineizer, delimiterType.getPattern());

        if (knowledge != null) {
            dataSet.setKnowledge(knowledge);
        }

        this.logger.info("\nData set loaded!");
        this.logger.reset();
        return dataSet;
    }

    /**
     * Parses the given files for a tabular data set, returning a
     * RectangularDataSet if successful.
     *
     * @throws IOException if the file cannot be read.
     */
    public CovarianceMatrix parseCovariance(File file) throws IOException {
        return parseCovariance(loadChars(file));
    }

    /**
     * Reads in a covariance matrix. The format is as follows. </p>
     * <pre>
     * /covariance
     * 100
     * X1   X2   X3   X4
     * 1.4
     * 3.2  2.3
     * 2.5  3.2  5.3
     * 3.2  2.5  3.2  4.2
     * </pre>
     * <pre>
     * CovarianceMatrix dataSet = DataLoader.loadCovMatrix(
     *                           new FileReader(file), " \t", "//");
     * </pre>
     * The initial "/covariance" is optional.
     */
    public CovarianceMatrix parseCovariance(char[] chars) {
        this.logger.setTetradLoggerConfig(createConfig());

        this.logger.info("\nDATA LOADING PARAMETERS:");
        this.logger.info("File type = COVARIANCE");
        this.logger.info("Comment marker = " + commentMarker);
        this.logger.info("Delimiter type = " + delimiterType);
        this.logger.info("Quote char = " + quoteChar);
//        LogUtils.getInstance().info("Var names first row = " + varNamesSupplied);
//        LogUtils.getInstance().info("IDs supplied = " + idsSupplied);
//        LogUtils.getInstance().info("ID label = " + idLabel);
        this.logger.info("Missing value marker = " + missingValueMarker);
//        LogUtils.getInstance().info("Max discrete = " + maxIntegralDiscrete);
        this.logger.info("--------------------");

        CharArrayReader reader = new CharArrayReader(chars);
        Lineizer lineizer = new Lineizer(reader, commentMarker);

        // Skip "/Covariance" if it is there.
        String line = lineizer.nextLine();

        if ("/Covariance".equalsIgnoreCase(line.trim())) {
            line = lineizer.nextLine();
        }

        // Read br sample size.

        System.out.println("line = " + line);

        RegexTokenizer st = new RegexTokenizer(line, delimiterType.getPattern(), quoteChar);
        String token = st.nextToken();

        int n;

        try {
            n = Integer.parseInt(token);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Expected a sample size here, got \"" + token + "\".");
        }

        if (st.hasMoreTokens() && !"".equals(st.nextToken())) {
            throw new IllegalArgumentException(
                    "Line from file has more tokens than expected: \"" + st.nextToken() + "\"");
        }

        // Read br variable names and set up DataSet.
        line = lineizer.nextLine();
        st = new RegexTokenizer(line, delimiterType.getPattern(), quoteChar);

        List<String> vars = new ArrayList<String>();

        while (st.hasMoreTokens()) {
            String _token = st.nextToken();

            if ("".equals(_token)) {
                TetradLogger.getInstance().log("emptyToken", "Parsed an empty token for a variable name--ignoring.");
                continue;
            }

            vars.add(_token);
        }

        String[] varNames = vars.toArray(new String[vars.size()]);

        this.logger.info("Variables:");

        for (String varName : varNames) {
            this.logger.info(varName + " --> Continuous");
        }

        // Read br covariances.
        DoubleMatrix2D c = new DenseDoubleMatrix2D(vars.size(), vars.size());

        for (int i = 0; i < vars.size(); i++) {
            st = new RegexTokenizer(lineizer.nextLine(), delimiterType.getPattern(), quoteChar);

            for (int j = 0; j <= i; j++) {
                if (!st.hasMoreTokens()) {
                    throw new IllegalArgumentException("Expecting " + (i + 1) +
                            " numbers on line " + (i + 1) +
                            " of the covariance " + "matrix input.");
                }

                String literal = st.nextToken();

                if ("".equals(literal)) {
                    TetradLogger.getInstance().log("emptyToken", "Parsed an empty token for a " +
                            "covariance value--ignoring.");
                    continue;
                }

                double r = Double.parseDouble(literal);

                c.setQuick(i, j, r);
                c.setQuick(j, i, r);
            }
        }

        Knowledge knowledge = parseKnowledge(lineizer, delimiterType.getPattern());

        CovarianceMatrix covarianceMatrix =
                new CovarianceMatrix(varNames, c, n);

        if (knowledge != null) {
            covarianceMatrix.setKnowledge(knowledge);
        }

        this.logger.info("\nData set loaded!");
        this.logger.reset();
        return covarianceMatrix;
    }

    /**
     * Loads knowledge from a file. Assumes knowledge is the only thing in
     * the file. No jokes please. :)
     */
    public Knowledge parseKnowledge(File file) throws IOException {
        return parseKnowledge(loadChars(file));
    }

    /**
     * Parses knowledge from the char array, assuming that's all there is in
     * the char array.
     */
    public Knowledge parseKnowledge(char[] chars) {
        CharArrayReader reader = new CharArrayReader(chars);
        Lineizer lineizer = new Lineizer(reader, commentMarker);
        this.logger.setTetradLoggerConfig(createConfig());
        Knowledge knowledge =  parseKnowledge(lineizer, delimiterType.getPattern());
        this.logger.reset();
        return knowledge;
    }

    //============================PRIVATE METHODS========================//

    /**
     * Creates the logger config, for data parsing.
     */
    private TetradLoggerConfig createConfig(){
        TetradLoggerConfig config = new DefaultTetradLoggerConfig("info", "emptyToken");
        config.setEventActive("info", true);
        config.setEventActive("emptyToken", this.logEmptyTokens);
        return config;
    }


    private int adjustForId(List<String> varNames, Lineizer lineizer) {
        int idIndex = -1;

        if (idsSupplied) {
            if (idLabel == null) {
                idIndex = 0;
                varNames.add(0, "");
            }
            else {
                idIndex = varNames.indexOf(idLabel);

                if (idIndex == -1) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": The given ID column label (" + idLabel + ") was not among " +
                            "the list of variables.");
                }
            }
        }
        return idIndex;
    }

    private void setValue(RectangularDataSet dataSet, int row, int col,
            String s) {
        if (s == null || s.equals("") || s.trim().equals(missingValueMarker)) {
            return;
        }

        if (col >= dataSet.getNumColumns()) {
            return;
        }

        Node node = dataSet.getVariable(col);

        if (node instanceof ContinuousVariable) {
            try {
                double value = Double.parseDouble(s);
                dataSet.setDouble(row, col, value);
            }
            catch (NumberFormatException e) {
                dataSet.setDouble(row, col, Double.NaN);
            }
        }
        else if (node instanceof DiscreteVariable) {
            DiscreteVariable var = (DiscreteVariable) node;
            int value = var.getCategories().indexOf(s.trim());

            if (value == -1) {
                dataSet.setInt(row, col, -99);
            }
            else {
                dataSet.setInt(row, col, value);
            }
        }
    }

    /**
     * Reads a knowledge file in tetrad2 format (almost--only does temporal
     * tiers currently). Format is:
     * <pre>
     * /knowledge
     * addtemporal
     * 0 x1 x2
     * 1 x3 x4
     * 4 x5
     * </pre>
     */
    private Knowledge parseKnowledge(Lineizer lineizer, Pattern delimiter) {
        Knowledge knowledge = new Knowledge();
        if (!lineizer.hasMoreLines()) {
            return null;
        }

        String line = lineizer.nextLine();
        String firstLine = line;

        if (line.startsWith("/knowledge")) {
            line = lineizer.nextLine();
            firstLine = line;
        }

        this.logger.info("\nLoading knowledge.");

        SECTIONS:
        while (lineizer.hasMoreLines()) {
            if (firstLine == null) {
                line = lineizer.nextLine();
            } else {
                line = firstLine;
            }

            // "addtemp" is the original in Tetrad 2.
            if ("addtemporal".equalsIgnoreCase(line.trim())) {
                while (lineizer.hasMoreLines()) {
                    line = lineizer.nextLine();

                    if (line.startsWith("forbiddirect")) {
                        firstLine = line;
                        continue SECTIONS;
                    }

                    if (line.startsWith("requiredirect")) {
                        firstLine = line;
                        continue SECTIONS;
                    }

                    int tier = -1;

                    RegexTokenizer st = new RegexTokenizer(line, delimiter, quoteChar);
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        boolean forbiddenWithin = false;
                        if (token.endsWith("*")) {
                            forbiddenWithin = true;
                            token = token.substring(0, token.length() - 1);
                        }

                        tier = Integer.parseInt(token);
                        if (tier < 1) {
                            throw new IllegalArgumentException(
                                    lineizer.getLineNumber() + ": Tiers must be 1, 2...");
                        }
                        if (forbiddenWithin) {
                            knowledge.setTierForbiddenWithin(tier - 1, true);
                        }
                    }

                    while (st.hasMoreTokens()) {
                        String name = substitutePeriodsForSpaces(st.nextToken());
                        knowledge.addToTier(tier - 1, name);

                        this.logger.info("Adding to tier " + (tier - 1) + " " + name);
                    }
                }
            } else if ("forbiddirect".equalsIgnoreCase(line.trim())) {
                while (lineizer.hasMoreLines()) {
                    line = lineizer.nextLine();

                    if (line.startsWith("addtemporal")) {
                        firstLine = line;
                        continue SECTIONS;
                    }

                    if (line.startsWith("requiredirect")) {
                        firstLine = line;
                        continue SECTIONS;
                    }

                    RegexTokenizer st = new RegexTokenizer(line, delimiter, quoteChar);
                    String from = null, to = null;

                    if (st.hasMoreTokens()) {
                        from = st.nextToken();
                    }

                    if (st.hasMoreTokens()) {
                        to = st.nextToken();
                    }

                    if (st.hasMoreTokens()) {
                        throw new IllegalArgumentException("Line " + lineizer.getLineNumber() +
                                ": Lines contains more than two elements.");
                    }

                    if (from == null || to == null) {
                        throw new IllegalArgumentException("Line " + lineizer.getLineNumber() +
                                ": Line contains fewer than two elements.");
                    }

                    knowledge.setEdgeForbidden(from, to, true);
                }
            } else if ("requiredirect".equalsIgnoreCase(line.trim())) {
                while (lineizer.hasMoreLines()) {
                    line = lineizer.nextLine();

                    if (line.startsWith("forbiddirect")) {
                        firstLine = line;
                        continue SECTIONS;
                    }

                    if (line.startsWith("addtemporal")) {
                        firstLine = line;
                        continue SECTIONS;
                    }

                    RegexTokenizer st = new RegexTokenizer(line, delimiter, quoteChar);
                    String from = null, to = null;

                    if (st.hasMoreTokens()) {
                        from = st.nextToken();
                    }

                    if (st.hasMoreTokens()) {
                        to = st.nextToken();
                    }

                    if (st.hasMoreTokens()) {
                        throw new IllegalArgumentException("Line " + lineizer.getLineNumber() +
                                ": Lines contains more than two elements.");
                    }

                    if (from == null || to == null) {
                        throw new IllegalArgumentException("Line " + lineizer.getLineNumber() +
                                ": Line contains fewer than two elements.");
                    }

                    knowledge.setEdgeRequired(from, to, true);
                }
            } else {
                throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                        + ": Expecting 'addtemporal', 'forbiddirect' or 'requiredirect'.");
            }
        }

        return knowledge;
    }

    private static String substitutePeriodsForSpaces(String s) {
        return s.replaceAll(" ", ".");
    }

   /*@zqian May 2nd
    *  private static class DataSetDescription {
        private List<Node> variables;
        private int numRows; 
        
        
        public DataSetDescription(List<Node> variables, int numRows) {
            this.variables = variables;
            this.numRows = numRows;
        }

        public List<Node> getVariables() {
            return variables;
        }

        public int getNumRows() {
            return numRows;
        }
    }
    */
    
    //@zqian May 2nd
    private static class DataSetDescription {
        private List<Node> variables;
        private int numRows;
        private int idIndex;
        private boolean variablesSectionIncluded;
        private Pattern delimiter;
        private boolean multColumnIncluded;

        public DataSetDescription(List<Node> variables, int numRows, int idIndex,
                                  boolean variablesSectionIncluded, Pattern delimiter,
                                  boolean multColumnIncluded) {
            this.variables = variables;
            this.numRows = numRows;
            this.idIndex = idIndex;
            this.variablesSectionIncluded = variablesSectionIncluded;
            this.delimiter = delimiter;
            this.multColumnIncluded = multColumnIncluded;
        }

        public List<Node> getVariables() {
            return variables;
        }

        public int getNumRows() {
            return numRows;
        }

        public int getIdIndex() {
            return idIndex;
        }

        public boolean isVariablesSectionIncluded() {
            return variablesSectionIncluded;
        }

        public Pattern getDelimiter() {
            return delimiter;
        }

        public boolean isMultColumnIncluded() {
            return multColumnIncluded;
        }
    }

    /**@zqian May 2nd
     * Scans the file for variable definitions and number of cases.
     *
     * @param varNames  Names of variables, if known. Otherwise, if null,
     *                  variables in the series X1, X2, ..., Xn will be made up,
     *                  one for each token in the first row.
     * @param lineizer  Parses lines, skipping comments.
     * @param delimiter Delimiter to tokenize tokens in each row.
     * @param firstLine Non-null if a non-variable first line had to be
*                  lineized
     * @param idIndex   The index of the ID column.
     * @param variableSectionIncluded
     */
    private DataSetDescription scanForDescription(List<String> varNames,
                                                  Lineizer lineizer, Pattern delimiter,
                                                  String firstLine, int idIndex,
                                                  boolean variableSectionIncluded) {

        // Scan file, collecting up the set of range values for each variables.
        List<Set<String>> dataStrings = new ArrayList<Set<String>>();

        for (int i = 0; i < varNames.size(); i++) {
            dataStrings.add(new HashSet<String>(varNames.size()));
        }

        int row = -1;

        while (lineizer.hasMoreLines()) {
            String line;

            if (firstLine == null) {
                line = lineizer.nextLine();
            } else {
                line = firstLine;
                firstLine = null;
            }

            if (line.startsWith("/knowledge")) {
                break;
            }

            ++row;

            RegexTokenizer tokenizer =
                    new RegexTokenizer(line, delimiter, quoteChar);

            int col = -1;

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                ++col;

                if (col >= dataStrings.size()) {
                    continue;
                }

                if ("".equals(token) || missingValueMarker.equals(token)) {
                    continue;
                }

                dataStrings.get(col).add(token);
            }

            if (col < varNames.size() - 1) {
                this.logger.log("info", "Line " + lineizer.getLineNumber()
                                + ": Too few tokens; expected " + varNames.size() +
                                " tokens but got " + (col + 1) + " tokens.");
            }

            if (col > varNames.size() - 1) {
                this.logger.log("info", "Line " + lineizer.getLineNumber()
                                + ": Too many tokens; expected " + varNames.size() +
                                " tokens but got " + (col + 1) + " tokens.");
            }
        }

        this.logger.log("info", "\nNumber of data rows = " + (row + 1));
        int numRows = row + 1;

        // Convert these range values into variable definitions.
        List<Node> variables = new ArrayList<Node>();

        VARNAMES:
        for (int i = 0; i < varNames.size(); i++) {
            Set<String> strings = dataStrings.get(i);

            // Use known variables if they exist for the corresponding name.
            for (Node variable : knownVariables) {
                if (variable.getName().equals(varNames.get(i))) {
                    variables.add(variable);
                    continue VARNAMES;
                }
            }

            if (isDouble(strings) && !isIntegral(strings) && i != idIndex) {
                variables.add(new ContinuousVariable(varNames.get(i)));
            } else if (isIntegral(strings) && tooManyDiscreteValues(strings) &&
                    i != idIndex) {
                String name = varNames.get(i);

                if (name.contains(" ")) {
                    name = name.replaceAll(" ", "_");
                    varNames.set(i, name);
                }

                if (!NamingProtocol.isLegalName(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": This cannot be used as a variable name: " + name + ".");
                }

                variables.add(new ContinuousVariable(name));
            } else {
                List<String> categories = new LinkedList<String>(strings);
                categories.remove(null);
                categories.remove("");
                categories.remove(missingValueMarker);

                Collections.sort(categories, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
//                        try {
//                            int i1 = Integer.parseInt(o1);
//                            int i2 = Integer.parseInt(o2);
//                            return i1 - i2;
//                            return i2 < i1 ? -1 : i2 == i1 ? 0 : 1;
//                        }
//                        catch (NumberFormatException e) {
//                            return o1.compareTo(o2);
//                        }
                    }
                });

                String name = varNames.get(i);

                if (name.contains(" ")) {
                    name = name.replaceAll(" ", "_");
                    varNames.set(i, name);
                }

                if (!NamingProtocol.isLegalName(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": This cannot be used as a variable name: " + name + ".");
                }

                variables.add(new DiscreteVariable(name, categories));
            }
        }

        boolean multColumnIncluded = false;

        if (variables.get(0).getName().equals("MULT")) {
            multColumnIncluded = true;
            variables.remove(0);
            varNames.remove(0);
        }

        // Print out a report of the variable definitions guessed at (or
        // read in through the /variables section or specified as known
        // variables.
        for (int i = 0; i < varNames.size(); i++) {
            if (i == idIndex) {
                continue;
            }

            Node node = variables.get(i);

            if (node instanceof ContinuousVariable) {
                this.logger.log("info", node + " --> Continuous");
            } else if (node instanceof DiscreteVariable) {
                StringBuilder buf = new StringBuilder();
                buf.append(node).append(" --> <");
                List<String> categories =
                        ((DiscreteVariable) node).getCategories();

                for (int j = 0; j < categories.size(); j++) {
                    buf.append(categories.get(j));

                    if (j < categories.size() - 1) {
                        buf.append(", ");
                    }
                }

                buf.append(">");
                this.logger.log("info", buf.toString());
            }
        }

        return new DataSetDescription(variables, numRows, idIndex, variableSectionIncluded,
                delimiter, multColumnIncluded);
    }

    /*@zqian May 2nd
    /**
     * Scans the file for variable definitions and number of cases.
     *
     * @param varNames  Names of variables, if known. Otherwise, if null,
     *                  variables in the series X1, X2, ..., Xn will be made up,
     *                  one for each token in the first row.
     * @param lineizer  Parses lines, skipping comments.
     * @param delimiter Delimiter to tokenize tokens in each row.
     * @param firstLine Non-null if a non-variable first line had to be
     *                  lineized
     * @param idIndex   The index of the ID column.
     */
   
  /*  private DataSetDescription scanForDescription(List<String> varNames,
            Lineizer lineizer, Pattern delimiter, String firstLine, int idIndex) {

        // Scan file, collecting up the set of range values for each variables.
        List<Set<String>> dataStrings = new ArrayList<Set<String>>();

        for (int i = 0; i < varNames.size(); i++) {
            dataStrings.add(new HashSet<String>(varNames.size()));
        }

        int row = -1;

        while (lineizer.hasMoreLines()) {
            String line;

            if (firstLine == null) {
                line = lineizer.nextLine();
            } else {
                line = firstLine;
                firstLine = null;
            }

            if (line.startsWith("/knowledge")) {
                break;
            }

            ++row;

            RegexTokenizer tokenizer =
                    new RegexTokenizer(line, delimiter, quoteChar);

            int col = -1;

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                ++col;

                if (col >= dataStrings.size()) {
                    continue;
                }

                if ("".equals(token) || missingValueMarker.equals(token)) {
                    continue;
                }

                dataStrings.get(col).add(token);
            }

            if (col < varNames.size() - 1) {
                this.logger.info("Line " + lineizer.getLineNumber()
                        + ": Too few tokens; expected " + varNames.size() +
                        " tokens but got " + (col + 1) + " tokens.");
            }

            if (col > varNames.size() - 1) {
                this.logger.info("Line " + lineizer.getLineNumber()
                        + ": Too many tokens; expected " + varNames.size() +
                        " tokens but got " + (col + 1) + " tokens.");
            }
        }

        this.logger.info("\nNumber of data rows = " + (row + 1));
        int numRows = row + 1;

        // Convert these range values into variable definitions.
        List<Node> variables = new ArrayList<Node>();

        VARNAMES:
        for (int i = 0; i < varNames.size(); i++) {
            Set<String> strings = dataStrings.get(i);

            // Use known variables if they exist for the corresponding name.
            for (Node variable : knownVariables) {
                if (variable.getName().equals(varNames.get(i))) {
                    variables.add(variable);
                    continue VARNAMES;
                }
            }

            if (isDouble(strings) && !isIntegral(strings) && i != idIndex) {
                variables.add(new ContinuousVariable(varNames.get(i)));
            } else if (isIntegral(strings) && tooManyDiscreteValues(strings) &&
                    i != idIndex) {
                String name = varNames.get(i);

                if (!NamingProtocol.isLegalName(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": This cannot be used as a variable name: " + name + ".");
                }

                variables.add(new ContinuousVariable(name));
            } else {
                List<String> categories = new LinkedList<String>(strings);
                categories.remove(null);
                categories.remove("");
                categories.remove(missingValueMarker);

                Collections.sort(categories, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
//                        try {
//                            int i1 = Integer.parseInt(o1);
//                            int i2 = Integer.parseInt(o2);
//                            return i1 - i2;
//                            return i2 < i1 ? -1 : i2 == i1 ? 0 : 1;
//                        }
//                        catch (NumberFormatException e) {
//                            return o1.compareTo(o2);
//                        }
                    }
                });

                String name = varNames.get(i);

                if (!NamingProtocol.isLegalName(name)) {
                    throw new IllegalArgumentException("Line " + lineizer.getLineNumber()
                            + ": This cannot be used as a variable name: " + name + ".");
                }

                variables.add(new DiscreteVariable(name, categories));
            }
        }

        boolean multColumnIncluded = false;

        if (variables.get(0).getName().equals("MULT")) {
            multColumnIncluded = true;
            variables.remove(0);
            varNames.remove(0);
        }

        // Print out a report of the variable definitions guessed at (or
        // read in through the /variables section or specified as known
        // variables.
        for (int i = 0; i < varNames.size(); i++) {
            if (i == idIndex) {
                continue;
            }

            Node node = variables.get(i);

            if (node instanceof ContinuousVariable) {
                this.logger.info(node + " --> Continuous");
            } else if (node instanceof DiscreteVariable) {
                StringBuffer buf = new StringBuffer();
                buf.append(node).append(" --> <");
                List<String> categories =
                        ((DiscreteVariable) node).getCategories();

                for (int j = 0; j < categories.size(); j++) {
                    buf.append(categories.get(j));

                    if (j < categories.size() - 1) {
                        buf.append(", ");
                    }
                }

                buf.append(">");
                this.logger.info(buf.toString());
            }
        }

        return new DataSetDescription(variables, numRows);
    }
*/
   
    
    
    private boolean tooManyDiscreteValues(Set<String> strings) {
        return strings.size() > maxIntegralDiscrete;
    }

    private static boolean isIntegral(Set<String> strings) {
        for (String s : strings) {
            try {
                Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private static boolean isDouble(Set<String> strings) {
        for (String s : strings) {
            try {
                Double.parseDouble(s);
            }
            catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
 
    /**
     * Loads text from the given file in the form of a char[] array.
     */
   
    private static char[] loadChars(File file) throws IOException {
        FileReader reader = new FileReader(file);
        CharArrayWriter writer = new CharArrayWriter();
        int c;

        while ((c = reader.read()) != -1) {
            writer.write(c);
        }

        return writer.toCharArray();
    }
}
