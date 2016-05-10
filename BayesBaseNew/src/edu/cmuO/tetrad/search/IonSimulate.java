package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * Simulation study using the ION algorithm
 *
 * @author Robert Tillman
 *
 */
public class IonSimulate {

    private static final String path = "/home/rtillman/Desktop/temp/";
    private static final String texFile = path + "simulation.tex";

    public IonSimulate() {

}

    public static void main(String[] args)  {

       int k = 1;

       latexHeader();

        for (int n=5; n < 8; n++) {

        for (int lp=1; lp < 101; lp++) {

        int numNodes = n;
        int degree = numNodes-1;
        int maxEdges = n;
        Graph trueGraph = GraphUtils.createRandomDag(numNodes, 0, maxEdges, 3, 2, 2, false);
        FciSearch tfci = new FciSearch(new IndTestDSep(trueGraph), new Knowledge());
        Pag trueGraphFci = tfci.search();

        // get marginal graphs
        List<Set<Node>> subsets = SearchGraphUtils.powerSet(trueGraph.getNodes());
        // first loop over number of excluded variables
        for (int j = trueGraph.getNodes().size() - 1; j > 2; j--) {
            List<Set<Node>> experiments = new ArrayList<Set<Node>>();
            for (Set<Node> subset : subsets) {
                if (subset.size()==j) {
                    boolean addexp = true;
                    for (Node node : subset) {

                        boolean noadj = true;
                        for (Node node2 : subset) {
                            if (node.equals(node2)) {
                                continue;
                            }
                            if (trueGraph.isAdjacentTo(node,node2)) {
                                noadj = false;
                            }
                        }
                        if (noadj) {
                            addexp = false;
                        }
                    }
                    if (addexp) {
                        experiments.add(subset);
                    }
                }
            }
            Set<Set<Set<Node>>> combinations = new HashSet<Set<Set<Node>>>();
            for (Set<Node> experiment : experiments) {
                for (Set<Node> experiment2 : experiments) {
                    if (experiment.equals(experiment2)) {
                       continue;
                    }
                    HashSet<Set<Node>> combination1 = new HashSet<Set<Node>>();
                    combination1.add(experiment);
                    combination1.add(experiment2);
                    HashSet<Node> chkExp = new HashSet<Node>();
                    chkExp.addAll(experiment);
                    chkExp.addAll(experiment2);
                    if (chkExp.size()==trueGraph.getNodes().size()) {
                        combinations.add(combination1);
                    }
                    for (Set<Node> experiment3 : experiments) {
                        if (experiment3.equals(experiment2) || experiment3.equals(experiment)) {
                            continue;
                        }

                        HashSet<Set<Node>> combination2 = new HashSet<Set<Node>>(combination1);
                        combination2.add(experiment3);
                        chkExp.addAll(experiment3);
                        if (chkExp.size()==trueGraph.getNodes().size()) {
                            combinations.add(combination2);
                        }
                    }
                }
            }

            experiments.clear();
            List<Set<Set<Node>>> clist = new ArrayList<Set<Set<Node>>>(combinations);
            combinations.clear();

            for (int m = 0; m < clist.size(); m++) {

                Set<Set<Node>> combination = clist.get(m);

                // input to ION
                List<Graph> inputPags = new ArrayList<Graph>();
                for (Set<Node> nodes : combination) {
                    List<Node> searchVars = new ArrayList<Node>(nodes);
                    FciSearch fci = new FciSearch(new IndTestDSep(trueGraph), new Knowledge(), searchVars);
                    Pag pag = fci.search();
                    inputPags.add(pag);
                }

                List<Graph> outputPags = new ArrayList<Graph>();
                String stats = "";
                int noninformativeMarginals = 0;
                for (Graph g : inputPags) {
                    Pag pag = (Pag)g;
                    if (pag.getNumEdges() == (pag.getNumNodes()*(pag.getNumNodes()-1))/2 && pag.getUnderLineTriples().size()==0) {
                        List<Edge> pagEdges = pag.getEdges();
                        boolean searchOkay = false;
                        for (Edge edge : pagEdges) {
                            if (!(edge.getEndpoint1()==Endpoint.CIRCLE && edge.getEndpoint2()==Endpoint.CIRCLE)) {
                                searchOkay = true;
                            }
                        }
                        if (!searchOkay) {
                            noninformativeMarginals++;
                        }
                    }
                }
                if (inputPags.size()-noninformativeMarginals>1) {
                    try {
                        IonSearch search = new IonSearch(inputPags);
                        outputPags = search.search();
                        if (outputPags.size()>500) {
                            outputPags.clear();
                        }
                        stats = search.getStats();
                    }
                    catch (Exception e) {
                        stats = e.getMessage();
                    }
                }
                else {
                    stats = "ALL YOUR BASE ARE BELONG TO US";
                }

                if (outputPags.size()==0) {
                    continue;
                }
                
                File dir = new File(path + k);
                dir.mkdir();

                File trueGraphFile = new File(dir.getPath() + "/truegraph.png");
                    writeGraph(trueGraph, trueGraphFile);
                File trueGraphFciFile = new File(dir.getPath() + "/truegraphfci.png");
                    writeGraph(trueGraphFci, trueGraphFciFile);

                List<File> marginalFiles = new ArrayList<File>();
                for (int i=0; i<inputPags.size(); i++) {
                    File file = new File(dir.getPath() + "/marginal" + i + ".png");
                    writeGraph(inputPags.get(i), file);
                    marginalFiles.add(file);
                }

                List<File> ionFiles = new ArrayList<File>();
                for (int i=0; i<outputPags.size(); i++) {
                    File file = new File(dir.getPath() + "/ion" + i + ".png");
                    writeGraph(outputPags.get(i), file);
                    ionFiles.add(file);
                }

                Pag necEdges = necessaryEdges(trueGraph, outputPags);
                File necEdgesFile = new File(dir.getPath() + "/necessaryedges.png");
                writeGraph(necEdges, necEdgesFile);

                latexSection(trueGraphFile, trueGraphFciFile, marginalFiles, ionFiles, stats, trueGraphFci, inputPags, outputPags, necEdgesFile);

                k++;
            }


        }
        }
        }

            latexClose();



    }

    private static void writeGraph(Graph graph, File file) {
        GraphUtils.arrangeInCircle(graph, 120, 120, 75);
        GraphWorkbench graphWb = new GraphWorkbench(graph);
        graphWb.setSize(new Dimension(240,240));
        Dimension size = graphWb.getSize();
        BufferedImage image = new BufferedImage(size.height, size.width, Image.SCALE_FAST);
        Graphics graphics = image.getGraphics();
        graphWb.paint(graphics);

        try {
            ImageIO.write(image, "png", file);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    private static void latexHeader() {
        try {
            FileWriter outFile = new FileWriter(texFile);
            PrintWriter out = new PrintWriter(outFile);
            out.println("\\documentclass[letterpaper,10pt]{article}\\usepackage{latexsym}\\usepackage{graphicx}\\title{ION algorithm simulation study}\\author{}\\begin{document}\\maketitle");
            out.println("");
            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void latexClose() {
        try {
            FileWriter outFile = new FileWriter(texFile,true);
            PrintWriter out = new PrintWriter(outFile);
            out.println("\\end{document}");
            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void latexSection(File trueGraph, File trueGraphFciFile, List<File> marginalFiles, List<File> ionFiles, String stats, Pag trueGraphFci, List<Graph> inputPags, List<Graph> outputPags, File necessaryEdges) {
        try {
            FileWriter outFile = new FileWriter(texFile, true);
            PrintWriter out = new PrintWriter(outFile);
            out.println("\\section{}");
            out.println("True Graph and FCI output");
            out.println("\\begin{center}\\includegraphics[scale=.45]{" + trueGraph.getPath() + "}");
            if (trueGraphFci.getUnderLineTriples().size()!=0) {
                out.println("\\\\ \\includegraphics[scale=.45]{" + trueGraphFciFile.getPath() + "} \\\\");
                for (Triple nodes : trueGraphFci.getUnderLineTriples()) {
                    out.print("\\{" + nodes.getX() + "," + nodes.getY() + "," + nodes.getZ() + "\\} ");
                }
                out.println("\\end{center}");
            }
            else {
                out.println("\\includegraphics[scale=.45]{" + trueGraphFciFile.getPath() + "}\\end{center}");
            }
            out.print("Marginal Graphs (");
            out.print(marginalFiles.size());
            out.println(")");
            out.println("\\begin{center}");
            for (int j = 0; j < marginalFiles.size(); j++) {
                if (((Pag)inputPags.get(j)).getUnderLineTriples().size()!=0) {
                    out.println("\\\\ \\includegraphics[scale=.45]{" + marginalFiles.get(j).getPath() + "}  \\\\");
                    for (Triple nodes : ((Pag)inputPags.get(j)).getUnderLineTriples()) {
                        out.print("\\{" + nodes.getX() + "," + nodes.getY() + "," + nodes.getZ() + "\\} ");
                    }
                    out.println("\\\\");
                }
                else {
                    out.println("\\includegraphics[scale=.45]{" + marginalFiles.get(j).getPath() + "}");
                }
            }
            out.println("\\end{center}");
            out.print("Graphs in ION output (");
            out.print(ionFiles.size());
            out.println(")");
            out.println("\\begin{center}");
            for (int j = 0; j < ionFiles.size(); j++) {
                if (((Pag)outputPags.get(j)).getUnderLineTriples().size()!=0) {
                    out.println("\\\\ \\includegraphics[scale=.45]{" + ionFiles.get(j).getPath() + "} \\\\");
                    for (Triple nodes : ((Pag)outputPags.get(j)).getUnderLineTriples()) {
                        out.print("\\{" + nodes.getX() + "," + nodes.getY() + "," + nodes.getZ() + "\\} ");
                    }
                    out.println("\\\\");
                }
                else {
                    out.println("\\includegraphics[scale=.45]{" + ionFiles.get(j).getPath() + "}");
                }
            }
            out.println("\\end{center}");
            out.print("Pag of necessary edges");
            out.println("\\begin{center}");
            out.println("\\includegraphics[scale=.45]{" + necessaryEdges + "}");
            out.println("\\end{center}");
            out.println(stats);
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Pag necessaryEdges(Graph trueGraph, List<Graph> pags) {
        Set<Edge> necEdges = new HashSet<Edge>();
        for (Graph pag : pags) {
            if (necEdges.isEmpty()) {
                necEdges.addAll(pag.getEdges());
                continue;
            }
            List<Edge> remEdges = new ArrayList<Edge>();
            Set<Edge> newEdges = new HashSet<Edge>(pag.getEdges());
            for (Edge edge : necEdges) {
                if (!newEdges.contains(edge)) {
                    remEdges.add(edge);
                }
            }
            for (Edge edge : remEdges) {
                necEdges.remove(edge);
            }
            if (necEdges.isEmpty()) {
                break;
            }
        }
        Pag necPag = new Pag(trueGraph.getNodes());
        for (Edge edge : necEdges) {
            necPag.addEdge(edge);
        }
        return necPag;
    }

    private class PowerSet<E> implements Iterable<Set<E>>
    {
        Collection<E> all;

        public PowerSet(Collection<E> all)
        {
            this.all = all;
        }

        /***
         * @return      an iterator over elements of type Collection<E> which enumerates
         *              the PowerSet of the collection used in the constructor
         */

        @Override
		public Iterator<Set<E>> iterator()
        {
            return new PowerSetIterator<E>(this);
        }

        class PowerSetIterator<InE> implements Iterator<Set<InE>>
        {
            PowerSet<InE> powerSet;
            List<InE> canonicalOrder = new ArrayList<InE>();
            List<InE> mask = new ArrayList<InE>();
            boolean hasNext = true;

            PowerSetIterator(PowerSet<InE> powerSet) {

                this.powerSet = powerSet;
                canonicalOrder.addAll(powerSet.all);
            }

            @Override
			public void remove()
            {
                throw new UnsupportedOperationException();
            }

            private boolean allOnes()
            {
                for( InE bit : mask) {
                    if( bit == null ) {
                        return false;
                    }
                }
                return true;
            }

            private void increment()
            {
                int i=0;
                while( true ) {
                    if( i < mask.size() ) {
                        InE bit = mask.get(i);
                        if( bit == null ) {
                            mask.set(i, canonicalOrder.get(i));
                            return;
                        }
                        else {
                            mask.set(i, null);
                            i++;
                        }
                    }
                    else {
                        mask.add(canonicalOrder.get(i));
                        return;
                   }
                }
            }

            @Override
			public boolean hasNext()
            {
                return hasNext;
            }

            @Override
			public Set<InE> next()
            {

                Set<InE> result = new HashSet<InE>();
                result.addAll(mask);
                result.remove(null);

                hasNext = mask.size() < powerSet.all.size() || ! allOnes();

                if( hasNext ) {
                    increment();
                }

                return result;

            }

        }
    }
}
