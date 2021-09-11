///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0

// JBang deps and picocli
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

// The imports that do the job
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@Command(name = "collatz", mixinStandardHelpOptions = true, version = "collatz 0.1",
        description = "collatz made with jbang")
class collatz implements Callable<Integer> {

    public static final String OUTPUT_CSV_RELATIONS = "RELATIONS.csv";
    public static final String OUTPUT_CSV_NODES = "NODES.csv";
    public static final String OUTPUT_GRAPHML = "collatz_";

    public static final int computeNext(int i) {
        int out = i;

        if (Math.abs(i) % 2 == 1) {
            out = 3 * i + 1;
            //System.out.println("Nombre impair détecté");
            //System.out.println("OUT : " + out);
        } else {
            out = i / 2;
            //System.out.println("Nombre pair détecté");
            //System.out.println("OUT : " + out);
        }
        return out;
    }

    public static final LinkedList<Integer> buildSequenceFromSeed(Integer i) {
        LinkedList<Integer> out = new LinkedList<Integer>();
        Integer current = i;
        out.add(current);
        //System.out.print(current);
        if (i.equals(0)) {
            out.add(current);
            //System.out.print("->" + current);
            return out;
        }

        while (true) {
            out.add(current);
            current = collatz.computeNext(current);
            //System.out.print("->" + current);
            if (out.indexOf(current) > 0) {
                //System.out.println("\nBreak. Loop detected on <" + current + ">");
                out.add(current);
                break;
            }
        }
        return out;
    }

    public static final void appendToCsv(int seed) throws IOException {

        FileWriter fw = new FileWriter(OUTPUT_CSV_RELATIONS, true);

        LinkedList<Integer> lList = collatz.buildSequenceFromSeed(seed);
        //System.out.println("########################################");
        for (int i = 1; i < lList.size() - 1; i++) {
            //System.out.println(lList.get(i) + "->" + lList.get(i + 1));
            fw.write(lList.get(i) + "," + lList.get(i + 1) + "\n");//appends the string to the file
        }
        fw.flush();
        fw.close();
    }

    public static final void feedCsv(int min, int max) throws IOException {
        // write nodes
        FileWriter nodes = new FileWriter(OUTPUT_CSV_NODES, false);
        nodes.write("id\n");

        FileWriter rels = new FileWriter(OUTPUT_CSV_RELATIONS, false);
        rels.write("source,target\n");
        rels.flush();
        rels.close();

        for (int i = min; i < max; i++) {
            appendToCsv(i);
            nodes.write(i + "\n");
        }
        nodes.flush();
        nodes.close();
    }

    public static final void dumpCsvs(int min, int max) throws IOException {
        FileWriter nodes = new FileWriter(OUTPUT_CSV_NODES, false);
        FileWriter relations = new FileWriter(OUTPUT_CSV_RELATIONS, false);
        // put headers
        nodes.write("id\n");
        relations.write("source,target\n");

        Set<Integer> uniqueNodes = new HashSet<Integer>();
        // write edges
        for (int i = min; i < max; i++) {
            LinkedList<Integer> lList = collatz.buildSequenceFromSeed(i);
            for (int j = 1; j < lList.size() - 1; j++) {
                //System.out.println(lList.get(j) + "->" + lList.get(j + 1));
                uniqueNodes.add(lList.get(j));
                uniqueNodes.add(lList.get(j + 1));

                //gml.write(lList.get(j) + "," + lList.get(j+1) + "\n");
                relations.write(lList.get(j) + "," + lList.get(j + 1) + "\n");
            }
            relations.flush();
        }

        // write nodes
        for (Integer i : uniqueNodes) {
            nodes.write(i + "\n");
        }
        nodes.flush();

        relations.close();
        nodes.close();

    }

    public static final void dumpGraphml(int min, int max) throws IOException {
        String outputFilename = OUTPUT_GRAPHML + min + "_" + max + ".graphml";
        FileWriter gml = new FileWriter(outputFilename, false);

        // put header
        gml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gml.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n"
                + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "    xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
                + "     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n"
                + "  <graph id=\"G\" edgedefault=\"directed\">");
        gml.flush();

        Set<Integer> uniqueNodes = new HashSet<Integer>();
        int nbNodes = 0;
        int nbRelations = 0;
        int fiberMaxLength = 0;
    
        int lowerBound = 0;
        int upperBound = 0;
        
        // write edges
        for (int i = min; i < max; i++) {
            LinkedList<Integer> lList = collatz.buildSequenceFromSeed(i);
            if( fiberMaxLength < lList.size()){
                fiberMaxLength = lList.size();
            }
            for (int j = 1; j < lList.size() - 1; j++) {
                //System.out.println(lList.get(j) + "->" + lList.get(j + 1));
                uniqueNodes.add(lList.get(j));
                uniqueNodes.add(lList.get(j + 1));

                //gml.write(lList.get(j) + "," + lList.get(j+1) + "\n");
                gml.write("<edge source=\"" + lList.get(j) + "\" target=\"" + lList.get(j + 1) + "\"/>\n");
                nbRelations++;
            }
            gml.flush();
        }
        // write nodes
        for (Integer i : uniqueNodes) {
            gml.write("<node id=\"" + i + "\"/>\n");
            nbNodes++;
            if(i < lowerBound){
                lowerBound = i;
            }
            if (i > upperBound){
                upperBound = i;
            }
        }
        gml.flush();

        // close the graph
        gml.write("</graph>\n"
                + "</graphml>");
        gml.flush();
        gml.close();
        System.out.println("- Initial lower seed : " + min);
        System.out.println("- Initial upper seed : " + max);
        System.out.println("- Upper bound : " + upperBound);
        System.out.println("- Lower bound : " + lowerBound);
        System.out.println("- Nb. nodes : " + nbNodes);
        System.out.println("- Nb. relations : " + nbRelations);
        System.out.println("- Fiber max length: " + fiberMaxLength);
    }

    //@Parameters(index = "0", description = "Lower bound", defaultValue = "-1000")
    @CommandLine.Option(
            names = {"-l", "--lower-bound"},
            description = "The lower bound of numbers",
            required = true)
    private int lowerBound;

    //@Parameters(index = "1", description = "Upper bound", defaultValue = "1000")
    @CommandLine.Option(
            names = {"-u", "--upper-bound"},
            description = "The upper bound of numbers",
            required = true)
    private int upperBound;

    public static void main(String... args) {
        int exitCode = new CommandLine(new collatz()).execute(args);
        System.exit(exitCode);
    }

    private void printlnAnsi(String msg) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string(msg));
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        System.out.println("=================== JCollatz ================");
        System.out.println("About to generate file for following paramaters :\n");

        System.out.println("- Lower bound : " + lowerBound);
        System.out.println("- Upper bound : " + upperBound);
        //System.out.println(lowerBound + upperBound);
        System.out.println("\n-> Generating graphml...\n");
        collatz.dumpGraphml(lowerBound, upperBound);
        
        printlnAnsi("@|green -> graphml generated.|@");
        //
        System.out.println("\n-> Generating csvs...");
        collatz.dumpCsvs(lowerBound, upperBound);
        printlnAnsi("@|green Csvs generated.|@");
        printlnAnsi("@|green \nEnjoy playing with files.|@");
        return 0;
    }
}
