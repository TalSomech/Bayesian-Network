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
        load_graph(gph);
    }

    /**
     * loads the graph from the file
     * Params: path to the xml of the graph
     */
    public  void load_graph(String path) {
        graph = new HashMap<>();
        try {
            DocumentBuilderFactory dBfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dBfactory.newDocumentBuilder();
            // Fetch XML File
            Document document = builder.parse(new File(path));//getting the file from the path
            document.getDocumentElement().normalize();//normalize the document
            //Get all definitions
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
                    }
                    graph.put(name, vari);
                    vari.init_node();//initiates the node
                }
            }
            /*iterate over all the nodes and create their corresponding family tree and CPT's*/
            for (int i = 0; i < defs.getLength(); i++) {
                Node node = defs.item(i);
                Element elm = (Element) node;
                String _curr = elm.getElementsByTagName("FOR").item(0).getTextContent();//current node
                Vari _cur_vari = graph.get(_curr);
                NodeList givens = elm.getElementsByTagName("GIVEN");
                String[] table = elm.getElementsByTagName("TABLE").item(0).getTextContent().split(" ");
                for (int j = 0; j < givens.getLength(); j++) {//iterate over parents and add them to the arraylist
                    Vari parent = graph.get(givens.item(j).getTextContent());
                    parent.children.add(_cur_vari);
                    _cur_vari.parents.add(parent);
                    _cur_vari.add_st(parent);
                }
                ArrayList<String> keys = new ArrayList<>(_cur_vari.CPT.keySet());
                int j = 0;
                /*this loops over the entire CPT and assign it the table values from the XML table*/
                for (String s : table) {
                    _cur_vari.CPT.put(keys.get(j), Float.parseFloat(s));
                    j++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


