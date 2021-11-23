import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Graph {
    public  HashMap<String, Vari> graph;

    public Graph(String gph) {
        graph = load_graph(gph);
    }

    /**
     * loads the graph from the file
     */
    public  HashMap<String ,Vari> load_graph(String path) {
        graph = new HashMap<>();
        try {
            DocumentBuilderFactory dBfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dBfactory.newDocumentBuilder();
            // Fetch XML File
            Document document = builder.parse(new File(path));//getting the file from the path
            document.getDocumentElement().normalize();//normalize the documennt
            //Get all students
            NodeList defs = document.getElementsByTagName("DEFINITION");//getting the table (for...given...table)
            NodeList vars = document.getElementsByTagName("VARIABLE");//list of the variables
            for (int i = 0; i < vars.getLength(); i++) {
                Node node = vars.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elm = (Element) node;
                    String name = elm.getElementsByTagName("NAME").item(0).getTextContent();//name of the variable
                    NodeList outcomes = elm.getElementsByTagName("OUTCOME");//outcomes of the variable
                    Vari vari = new Vari(name);
                    for (int j = 0; j < outcomes.getLength(); j++) {
                        vari.outcomes.add(outcomes.item(j).getTextContent());
                        //k.put(name,name+'='+outcomes.item(j).getTextContent());
                    }
                    System.out.println("Name : " + elm.getElementsByTagName("NAME").item(0).getTextContent());//prints the name of the variable will be deleted later
                    graph.put(name, vari);
                    vari.init_node();//initiates the node
                }
            }
            for (int i = 0; i < defs.getLength(); i++) {
                Node node = defs.item(i);
                Element elm = (Element) node;
                String _curr = elm.getElementsByTagName("FOR").item(0).getTextContent();//current node
                System.out.println(_curr);
                Vari _cur_vari = graph.get(_curr);
                NodeList givens = elm.getElementsByTagName("GIVEN");
                String[] table = elm.getElementsByTagName("TABLE").item(0).getTextContent().split(" ");
                for (int j = 0; j < givens.getLength(); j++) {
                    Vari parent = graph.get(givens.item(j).getTextContent());
                    parent.children.add(_cur_vari);
                    _cur_vari.parents.add(parent);
                    _cur_vari.add_st(parent);
                }
                ArrayList<String> keys = new ArrayList<>(_cur_vari.CPT.keySet());
                int j = 0;
                for (String s : table) {
                    _cur_vari.CPT.put(keys.get(j), Float.parseFloat(s));
                    j++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }


    /**
     * this annoying function is used to get a string like "T F T" or "B1 B3 B5" etc.
     * and convert it to an index in the CPT array
     */
    public static void convert(String eq, Vari node) {
        String[] vars = eq.split(" ");
        int idx = (int) (node.outcomes.indexOf(vars[0]) * Math.pow(node.outcomes.size(), node.parents.size()));//get the index for the first one
        Vari parent;
        for (int i = 1; i < vars.length; i++) {
            parent = node.parents.get(i - 1);
            // idx += Math.pow(2, node.parents.size() - i) * parent.outcomes.indexOf(vars[i]);// this one is only for TF TF TFTT etc , the one below .. im not 100% sure it works
            idx += Math.pow(parent.outcomes.size(), node.parents.size() - i) * parent.outcomes.indexOf(vars[i]);// i get the index by multiplying the number of outcomes of each parent with the index of the value of the variable
        }
    }

    public static void main(String[] args) {
        Graph_Algo g_a=new Graph_Algo("./Data/alarm_net.xml");
        System.out.println(g_a.bayes_ball_query("B-E|J=T"));
        //Graph g;
        //load_graph("./Data/alarm_net.xml");

        // Graph g=load_graph();
    }
}


