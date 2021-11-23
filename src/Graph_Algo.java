import java.util.*;
import java.util.stream.Collectors;

public class Graph_Algo {
    Graph g;
    public Graph_Algo(String graph){
        g=new Graph(graph);
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
            if (!node.parents.contains(g.graph.get(evi.getKey()))) {
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
                    flag = false;
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
    /**
     * parse the query and send it to the bayes ball implementation
     */
    boolean  bayes_ball_query(String query) {
        String[] qry = query.split("\\|");
        String[] nodes = qry[0].split("-");
        ArrayList<Vari> shades = new ArrayList<>();
        if(qry.length>1) {
            String[] _shades_prep = qry[1].split(",");
            for (String shade : _shades_prep) {
                shade = shade.substring(0, shade.indexOf('='));
                shades.add(g.graph.get(shade));
            }
        }
        return bayes_ball_imp(g.graph.get(nodes[0]), g.graph.get(nodes[1]), shades);
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
        ArrayList<Vari> neis=new ArrayList<>();//adj[s] (as in bfs) but we need to know to put the correct nodes each time
       // neis = node.children;//add all the children to the neighbors of the start node
        came_child.add(node);//"came from a child so it can go to its parents"
        while (queue.size() != 0) {
            // Dequeue a vertex from queue and print it
            node = queue.poll();
            System.out.print(node.name + " ");
            if (shades.contains(node)) {
                if(came_child.contains(node)){
                    continue;
                }
                came_child.addAll(node.parents);//add parents to came child so in time they will be added to the queue
                neis = node.parents;//adding the parents as neighbors of a node
            }
            else {
                if (came_child.contains(node)) {//add all parents to the neighbors of the
                    neis.addAll(node.parents);
                    neis.addAll(node.children);
                    came_child.addAll(node.parents);
                } else {
                    neis.addAll(node.children);
                }
            }
            for (Vari _cur : neis) {
                if (neis.contains(dest)) {
                    return false;
                }
                if (!visited.contains(_cur) || (came_child.contains(_cur) && node.parents.contains(_cur))) {
                    queue.add(_cur);
                }
            }
            neis.clear();
            visited.add(node);
        }
        return true;
    }

    /**
     * This function eliminates the hidden variable from a factor after we joined all of it's corresponding factors
     * @param table
     * @param hidden
     * @return
     */
    public LinkedHashMap<String,Float> eliminate_factor(LinkedHashMap<String,Float> table,Vari hidden){
        LinkedHashMap<String,Float> answer=new LinkedHashMap<>();
        Iterator<Map.Entry<String, Float>> keys = table.entrySet().iterator();
        Map.Entry<String, Float> it=keys.next();
        String key;
        List<String> outs=hidden.outcomes;
        List<String> visited=new ArrayList<>();
        Float _prob=(float)0;
        while(visited.size()!=table.size()){//!it.getKey().contains(hidden.name+'='+outs.get(1))){//Iterate over the table until we get to the first time we see a different outcome
            key=it.getKey();//get the key of current entry
            if(visited.contains(key))
                continue;
            _prob+=it.getValue();//getting the value
            for (int j = 1; j < outs.size(); j++) {//Iterate over all the outcomes of the hidden node so we can eliminate it
                key=key.replace(hidden.name+'='+outs.get(j-1),hidden.name+'='+outs.get(j) );//change the outcome
                visited.add(key);
                _prob+=table.get(key);//add the table value
            }
            key=key.replace(hidden.name+'='+outs.get(outs.size()-1),"");
            answer.put(key,_prob);
            _prob=(float)0;
            it=keys.next();
        }
        return answer;
    }
    /**
     * this function receives a string which is a VE query and parse it
     * so it will return the query,evidence and the elimination sequence
     * @param s-string on which we parse the query
     * @return the first index includes the qry and the evidence , second index contains the elimination sequence
     */
    ArrayList<ArrayList<String>> parse_query(String s) {
        String[] split = s.split("\\|");
        String query = split[0].substring(2);
        String[] spl_evi_eli = split[1].split("\\)");//split the elimination and the evidence
        String[] evi = spl_evi_eli[0].split(",");
        String[] eli_seq_p = spl_evi_eli[1].split("-"); //elimination sequence
        ArrayList<String> qry_evi = new ArrayList<>();
        ArrayList<String> eli_seq = new ArrayList<>(Arrays.asList(eli_seq_p));
        ArrayList<ArrayList<String>> rtrn = new ArrayList<>();
        qry_evi.add(query);
        qry_evi.addAll(Arrays.asList(evi));
        rtrn.add(qry_evi);
        rtrn.add(eli_seq);
        return rtrn;
    }


}
