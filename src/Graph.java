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
    static HashMap<String, Vari> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    /**
     * loads the graph from the file
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
    }

    /**
     * this function gets each node's CPT with correspondent to the evidence ,in which are local CPT's
     *
     * @param evidence- a hashmap of the evidence nodes and their corresponding values ,example : B,T
     * @param node-     a node on which were trying to get the cpt
     * @return
     */
    public LinkedHashMap<String, Float> local_cpt(HashMap<String, String> evidence, Vari node) {
        for (Map.Entry<String, String> evi : evidence.entrySet()) {
            if (!node.parents.contains(graph.get(evi.getKey()))) ;
            {
                evidence.remove(evi.getKey());
            }
        }

        if (evidence.isEmpty())
            return node.CPT;
        LinkedHashMap<String, Float> factor = new LinkedHashMap<>();
        boolean flag;
        String new_key;
        for (Map.Entry<String, Float> entry : node.CPT.entrySet()) {
            flag = true;
            for (String evi : evidence.keySet()) {
                if (!entry.getKey().contains(evi)) {
                    flag = false;
                    //factor.put(entry.getKey().replace(evi + ",", ""), entry.getValue());
                    break;
                }
            }
            if (flag) {
                new_key = entry.getKey();
                for (String evi : evidence.keySet()) {
                    //new_key=entry.getKey().replace(evi + ",", "");
                    new_key = new_key.replace(evi + ",", "");
                    //factor.put(entry.getKey().replace(evi + ",", ""), entry.getValue());
                }
                factor.put(new_key, entry.getValue());
            }
        }
        return factor;
    }

    /**
     * this function is supposed to join between two tables on the qry evi , it currently doesn't support multiple
     * vari like table1= A B C table 2= ABD ,qry=A
     *
     * @param table1-CPT table (hashmap)
     * @param table2-CPT table(hashmap
     * @param qry-hidden variable
     * @return
     */
    public LinkedHashMap<String, Float> join_factors(LinkedHashMap<String, Float> table1, LinkedHashMap<String, Float> table2, String qry) {
        LinkedHashMap<String, Float> answer = new LinkedHashMap<>();
        List<String> varis_f, varis_s, vari_values = new LinkedList<>();
        Iterator<Map.Entry<String, Float>> f_keys = table1.entrySet().iterator();
        List<Map.Entry<String, Float>> s_keys = table2.entrySet().stream().toList();
        String key, hash;
        Map.Entry<String, Float> key_f, key_s;//an entry object for the first and second table to join
        key_f = f_keys.next();
        varis_f = Arrays.stream(key_f.getKey().split(",")).toList();//these 5 lines are to check which multiple variable
        varis_s = Arrays.stream(s_keys.get(0).getKey().split(",")).toList();//are the same in both tables so ill know to join both
        varis_f.forEach(x -> x = x.split("=")[0]);
        varis_s.forEach(x -> x = x.split("=")[0]);
        List<String> varis = varis_s.stream().filter(varis_f::contains).collect(Collectors.toList());
        //varis.add(qry);
        int idx;
        boolean flag = true;
        String temp;
        while (f_keys.hasNext()) {
            hash = key_f.getKey();//get the string of the entry

            for (String vari : varis) {
                idx = hash.indexOf(vari);
                key = hash.substring(idx, hash.indexOf(',', idx));//get the "A=T" from the substring for each corresponding variable
                vari_values.add(key);
            }
            //key = qry + '=' + node.outcomes.get(i);
            //key_f = f_keys.next();//getting the next entry to check equals
            //idx = hash.indexOf(qry);//get the index of the qry
            // idx=hash.indexOf(vari);
            //key = hash.substring(idx, hash.indexOf(',', idx));//get the "A=T" from the substring
            for (int j = 0; j < s_keys.size(); j++) {
                key_s = s_keys.get(j);//get the next key
                temp = key_s.getKey();
                for (String value : vari_values) {
                    if (!key_s.getKey().contains(value)) {//if it doesent contains the value that means that the current entry does not have equal values on all variables
                        flag = false;
                        temp = "";
                        break;
                        //hash+=key_s.getKey().replace()
                    } else {
                        temp = temp.replace(value, "");// here we get the symmetric difference of table 2 so we can join later on
                    }
                }
                if (flag) {
                    hash += temp;
                    answer.put(hash, key_f.getValue() * key_s.getValue());
                    flag=false;
                }

//                    if (key_s.getKey().contains(key)) {
//                        hash += key_s.getKey().replace(key, "");
//                        answer.put(hash, key_f.getValue() * key_s.getValue());
//                    }
                // hash = key_f.getKey();
                temp = "";
            }
            key_f = f_keys.next();//getting the next entry to check equals

        }
        return answer;
    }


    public void var_eli() {


    }

    /**
     * parse the query and send it to the bayes ball implementation
     */
    boolean bayes_ball(String query) {
        String[] qry = query.split("\\|");
        String[] nodes = qry[0].split("-");
        String[] _shades_prep = qry[1].split(",");
        ArrayList<Vari> shades = new ArrayList<>();
        for (String shade : _shades_prep) {
            shade = shade.substring(0, shade.indexOf('='));
            shades.add(graph.get(shade));
        }
        return bayes_ball_imp(graph.get(nodes[0]), graph.get(nodes[1]), shades);
    }

    ArrayList<ArrayList<String>> parse_query(String s) {
        String[] split = s.split("\\|");
        String query = split[0].substring(2);
        String[] spl_evi_eli = split[1].split("\\)");//split the elimination and the evidence
        String[] evi = spl_evi_eli[0].split(",");
        String[] eli_seq_p = spl_evi_eli[1].split("-"); //elimination sequence
        ArrayList<String> qry_evi = new ArrayList<>();
        ArrayList<String> eli_seq = new ArrayList<>(Arrays.asList(eli_seq_p));
        qry_evi.add(query);
        qry_evi.addAll(Arrays.asList(evi));
        ArrayList<ArrayList<String>> rtrn = new ArrayList<>();
        rtrn.add(qry_evi);
        rtrn.add(eli_seq);
        return rtrn;
    }

    /**
     * bayes-ball algorithm
     * an implemantation of the bayes ball algorithm which gets a start node, dest node , and an arraylist of evidence
     * goes through the graph while looks for the destination node using the bayes ball rules
     */
    boolean bayes_ball_imp(Vari node, Vari dest, ArrayList<Vari> shades) {
        // Create a queue for BFS
        Queue<Vari> queue = new LinkedList<>();
        // Mark the current node as visited and enqueue it
        Set<Vari> visited = new HashSet<>();
        ArrayList<Vari> came_child = new ArrayList<>();
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
            } else {
                if (came_child.contains(node)) {//add all parents to the neighbors of the
                    neis.addAll(node.parents);
                    came_child.addAll(node.parents);
                } else {
                    neis.addAll(node.children);
                }
            }
            for (Vari _cur : neis) {
                if (neis.contains(dest)) {
                    return false;
                }
                if (!visited.contains(_cur) || (came_child.contains(node) && node.parents.contains(_cur))) {
                    queue.add(_cur);
                }
            }
            neis.clear();
            visited.add(node);
        }
        return true;
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
        //Graph g=load_graph("./Data/alarm_net.xml");
       // Graph g=load_graph();
    }
}


