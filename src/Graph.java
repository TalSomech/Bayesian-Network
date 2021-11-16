import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class Graph {
    static HashMap<String, Vari> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    /**loads the graph from the file
     *
     */
    public static void load_graph(String path) {
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
                    }
                    System.out.println("Name : " + elm.getElementsByTagName("NAME").item(0).getTextContent());//prints the name of the variable will be deleted later
                    graph.put(name, vari);
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
                }
                for (String s : table) {
                    _cur_vari.CPT.add(Float.parseFloat(s));
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    /**
     * parse the query and send it to the bayes ball implementation
     * @param query
     * @return
     */
    boolean bayes_ball(String query) {
        String [] qry =query.split("\\|");
        String [] nodes=qry[0].split("-");
        String [] _shades_prep=qry[1].split(",");
        ArrayList<Vari> shades=new ArrayList<>();
        for (String shade : _shades_prep) {
            shade=shade.substring(0,shade.indexOf('='));
            shades.add(graph.get(shade));
        }
        return bayes_ball_imp(graph.get(nodes[0]),graph.get(nodes[1]),shades);
    }

    /**
     * bayes-ball algorithm
     *an implemantation of the bayes ball algorithm which gets a start node, dest node , and an arraylist of evidence
     * goes through the graph while looks for the destination node using the bayes ball rules
     */
    boolean bayes_ball_imp(Vari node, Vari dest, ArrayList<Vari> shades) {
        // Create a queue for BFS
        Queue<Vari> queue = new LinkedList<>();
        // Mark the current node as visited and enqueue it
        Set<Vari> visited = new HashSet<>();
        ArrayList<Vari> came_child=new ArrayList<>();
        //visited.add(node);
        queue.add(node);
        ArrayList<Vari> neis;//adj[s] (as in bfs) but we need to know to put the correct nodes each time
        neis = node.children;//add all the children to the neighbors of the start node
        came_child.add(node);//"came from a child so it can go to its parents"
        while (queue.size() != 0) {
            // Dequeue a vertex from queue and print it
            node = queue.poll();
            System.out.print(node.name + " ");
            if (shades.contains(node)) {
                came_child.addAll(node.parents);//add parents to came child so in time they will be added to the queue
                neis = node.parents;//adding the parents as neighbors of a node
                neis.addAll(node.children);//adding the children because they can also be traversed
            } else {
                if ( came_child.contains(node)) {//add all parents to the neighbors of the
                    neis.addAll(node.parents);
                    came_child.addAll(node.parents);
                } else {
                    neis.addAll(node.children);
                }
            }
            for (Vari _cur : neis) {
                if (neis.contains(dest)) {
                    return true;
                }
                if (!visited.contains(_cur) || (came_child.contains(node)&&node.parents.contains(_cur))){
                    queue.add(_cur);
                }
            }
            neis.clear();
            visited.add(node);
        }
        return false;
    }

    /**
     * this annoying function is used to get a string like "T F T" or "B1 B3 B5" etc.
     * and convert it to an index in the CPT array
     *
     */
    public static void convert(String eq, Vari node) {
        String[] vars = eq.split(" ");
        int idx = (int) (node.outcomes.indexOf(vars[0]) * Math.pow(2, node.parents.size()));//get the index for the first one
        Vari parent;
        for (int i = 1; i < vars.length; i++) {
            parent = node.parents.get(i - 1);
           // idx += Math.pow(2, node.parents.size() - i) * parent.outcomes.indexOf(vars[i]);// this one is only for TF TF TFTT etc , the one below .. im not 100% sure it works
            idx += Math.pow(parent.outcomes.size(), node.parents.size() - i) * parent.outcomes.indexOf(vars[i]);// i get the index by multiplying the number of outcomes of each parent with the index of the value of the variable
        }
    }

    public static void main(String[] args) {

        load_graph("./Data/alarm_net.xml");
    }
}


