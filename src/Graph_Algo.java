import java.util.*;
import java.util.stream.Collectors;

public class Graph_Algo {
    Graph g;
    int doubs, add, counter;

    public Graph_Algo(String graph) {
        g = new Graph(graph);
        add = 0;
        doubs = 0;
    }

    /**
     * this is the first function which send the query to the right algorithm
     * @param qry-a String of the query
     * @return
     */
    public String query(String qry) {
        if (qry.contains("P(")) {
            float t = var_elimation(qry);
            counter++;
            String message = String.format("%.5f,%d,%d", t, this.add, this.doubs);
            this.add = 0;
            this.doubs = 0;
            return message;
        } else {
            if (bayes_ball_query(qry)) {
                return "yes";
            } else {
                return "no";
            }
        }
    }

    /**
     * so this function uses all other function and combines them to the whole variable elimination algorithm
     */
    public Float var_elimation(String qry) {
        ArrayList<ArrayList<String>> _qry_data = parse_query(qry);//data of the string
        String _qry = _qry_data.get(0).get(0);
        Vari _qry_var = g.graph.get(_qry.substring(0, _qry.indexOf('=')));//get the query variable
        HashMap<Vari, String> evi = new HashMap<>();
        int idx;
        ArrayList<Vari> hidden = new ArrayList<>();
        for (String evidence : _qry_data.get(0)) {
            idx = evidence.indexOf('=');//separate between the variable and value of each evidence+the query
            if (idx > -1)
                evi.put(g.graph.get(evidence.substring(0, idx)), evidence.substring(idx + 1));
        }//get an Arraylist of the evidence variable
        for (String var_name : _qry_data.get(1)) {
            hidden.add(g.graph.get(var_name));
        }//get an Arraylist of the hidden variables

        ArrayList<Vari> non_relevant = relevant_hidden(hidden, new ArrayList<>(evi.keySet()), _qry_var);//remove the non relevant variables from the hidden list and save the nonrelevant
        evi.remove(_qry_var);
        ArrayList<LinkedHashMap<String, Float>> _factors_list = new ArrayList<>();//our list of factors
        LinkedHashMap<String, Float> factor;
        factor = local_cpt(evi, _qry_var, non_relevant);
        if (check_found(_qry_var, evi, hidden)) {//checks if the query is possible to be obtained without the whole algorithm
            return factor.get(_qry);
        }
        _factors_list.add(factor);
        /*get the cpt's of all relevant variables*/
        for (Vari _var : evi.keySet()) {
            factor = local_cpt(evi, _var, non_relevant);
            if (factor != null && factor.size() > 1)
                _factors_list.add(factor);
        }
        evi.remove(_qry_var);
        for (Vari hid_var : hidden) {
            factor = local_cpt(evi, hid_var, non_relevant);
            if (factor != null && factor.size() > 1)
                _factors_list.add(factor);
        }
        LinkedHashMap<String, Float> answer;//=join_factors(_factors_list.get(0),_factors_list.get(1),"A");
        PriorityQueue<LinkedHashMap<String, Float>> relevant_factors = create_queue();//creates a priority queue so the factors will be eliminated by the order given by the assignment
        for (Vari hid_var : hidden) {//check which factors to join
            Iterator<LinkedHashMap<String, Float>> factors_it = _factors_list.iterator();
            while (factors_it.hasNext()) {
                LinkedHashMap<String, Float> cur_factor = factors_it.next();
                if (cur_factor.keySet().iterator().next().contains(hid_var.name)) {
                    relevant_factors.add(cur_factor);
                    factors_it.remove();
                }
            }
            /*join all the factors and eliminate them afterwards*/
            while (relevant_factors.size() > 1) {
                //sort_factors(relevant_factors);
                answer = join_factors(relevant_factors.poll(), relevant_factors.poll());
                relevant_factors.add(answer);
            }
                _factors_list.add(eliminate_factor(relevant_factors.poll(), hid_var));
        }
        /*after we eliminated all the hidden variables were left to join the last remaining factors*/
        while (_factors_list.size() > 1) {
            answer = join_factors(_factors_list.remove(0), _factors_list.remove(0));
            _factors_list.add(answer);
        }
        /* normalize the last factor and return the anwser*/
        answer = marginalize(_factors_list.remove(0));
        return answer.get(_qry);
    }

    /**
     * this function is used to marginalize the last factor remaining
     */
    public LinkedHashMap<String, Float> marginalize(LinkedHashMap<String, Float> table) {
        Float sum = (float) 0.0;
        this.add--;
        for (Float value : table.values()) {
            sum += value;
            this.add++;
        }
        for (Map.Entry<String, Float> entry : table.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
        return table;
    }

    /**
     * this function just creates and returns a priority queue which ordered by the terms given in the assignment
     */
    public PriorityQueue<LinkedHashMap<String, Float>> create_queue() {
        return new PriorityQueue<>((Comparator) (o1, o2) -> {
            LinkedHashMap<String, Float> a = (LinkedHashMap<String, Float>) o1;
            LinkedHashMap<String, Float> b = (LinkedHashMap<String, Float>) o2;
            if (a.size() < b.size())
                return -1;
            else {

                if (a.size() == b.size()) {
                    String[] a_split = a.keySet().iterator().next().split(",");
                    String[] b_split = b.keySet().iterator().next().split(",");
                    String a_varis = "";
                    String b_varis = "";
                    for (String s : a_split) {
                        a_varis += s.split("=")[0];
                    }
                    for (String s : b_split) {
                        b_varis += s.split("=")[0];

                    }
                    return a_varis.compareTo(b_varis);
                }
            }
            return 1;
        });
    }

    /**
     * this function is used to find if the query at a possibility to return a value without the entire algorithm
     * it iterates over all the parents of the query node and checks if all it's parents are in the evidence
     *
     */
    public boolean check_found(Vari _qry, HashMap<Vari, String> evi, ArrayList<Vari> hidden) {
        int counter = 0;
        for (Vari pt : _qry.parents) {
            if (evi.containsKey(pt.name))
                counter++;
            if (hidden.contains(pt)) {
                return false;
            }
        }
        return counter == evi.size();
    }

    /**
     * this function gets each node's CPT with correspondent to the evidence ,in which are local CPT's
     *
     * @param evidence- a hashmap of the evidence nodes and their corresponding values ,example : B,T
     * @param node-     a node on which were trying to get the cpt
     */
    public LinkedHashMap<String, Float> local_cpt(HashMap<Vari, String> evidence, Vari node, ArrayList<Vari> non_relevant) {
        HashMap<Vari, String> relevant_evi = new HashMap<>(evidence);
        for (Map.Entry<Vari, String> evi : evidence.entrySet()) {// since we are getting all the evidence we are interested only in the parents
            if (!node.parents.contains(evi.getKey()) && evi.getKey() != node) {
                relevant_evi.remove(evi.getKey());
            }
        }
        String check = node.CPT.keySet().iterator().next();
        for (Vari non_rel : non_relevant) {// we need to find if this CPT contains a non relevant node , if so , dont add it to the factor list
            if (check.contains(non_rel.name)) {
                return null;
            }
        }
        if (relevant_evi.isEmpty())//if the evidence is empty it means that this CPT only column is the node itself
            return node.CPT;
        LinkedHashMap<String, Float> factor = new LinkedHashMap<>();
        String new_key;//new key without the relevant evidence
        String valued;//the evidence+value
        boolean flag;//this flag is used to see if the entry ("row") contains the entire evidence and query
        for (Map.Entry<String, Float> entry : node.CPT.entrySet()) {//iterate over all of the CPT entries
            flag = true;
            new_key = entry.getKey();
            for (Map.Entry<Vari, String> evi : relevant_evi.entrySet()) {//get only the row which corresponding to all the evidence
                valued = evi.getKey().name + '=' + evi.getValue();
                if (entry.getKey().contains(valued)) {
                    new_key = getString(new_key, valued);
                } else {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                factor.put(new_key, entry.getValue());
            }
        }
        return factor;
    }

    /**
     * just a syntax function used to get a key without a ','
     */
    private String getString(String new_key, String valued) {
        if (new_key.contains(',' + valued + ',')) {
            new_key = new_key.replace(valued + ',', "");
        } else {
            if ((new_key.contains(valued + ',')))
                new_key = new_key.replace(valued + ',', "");
            else {
                if ((new_key.contains(',' + valued)))
                    new_key = new_key.replace(',' + valued, "");
                else {
                    new_key = new_key.replace(valued, "");
                }
            }
        }
        return new_key;
    }

    /**
     * this function is supposed to join between two tables on the qry evi , it currently doesn't support multiple
     * vari like table1= A B C table 2= ABD ,qry=A
     *
     * @param table1-CPT table (hashmap)
     * @param table2-CPT table(hashmap
     */
    public LinkedHashMap<String, Float> join_factors(LinkedHashMap<String, Float> table1, LinkedHashMap<String, Float> table2) {
        LinkedHashMap<String, Float> answer = new LinkedHashMap<>();
        List<String> varis_f, varis = new ArrayList<>();//variables for first and second table
        List<Map.Entry<String, Float>> f_keys = new ArrayList<>(table1.entrySet());//keys of all the rows in the first table
        String t = table2.keySet().iterator().next();//random row from table 2
        ArrayList<String> vari_values = new ArrayList<>();
        String key, hash;
        int idx;
        varis_f = Arrays.stream(f_keys.get(0).getKey().split(",")).collect(Collectors.toList());//these 5 lines are to check which multiple variable are the same in both tables
        for (String st : varis_f) {
            idx = st.indexOf('=');
            if (t.contains(st.substring(0, idx + 1)))
                varis.add(st.substring(0, idx));
        }
        boolean flag = true;//this flag is used to check the equality between each 2 rows
        String temp;//a String to recieve the node and value of corresponding variables in the row
        for (Map.Entry<String, Float> key_f : f_keys) {
            hash = key_f.getKey() + ',';//get the string of the entry
            for (String vari : varis) {
                idx = hash.indexOf(vari);
                if (hash.contains(",")) {
                    key = hash.substring(idx, hash.indexOf(',', idx));
                }//get the "A=T" from the substring for each corresponding variable
                else {
                    key = hash;
                }
                vari_values.add(key);
            }
            for (Map.Entry<String, Float> s_key : new ArrayList<>(table2.entrySet())) {
                temp = s_key.getKey();
                for (String value : vari_values) {
                    if (!temp.contains(value)) {//if it doesent contains the value that means that the current entry does not have equal values on all variables
                        flag = false;
                        temp = "";
                        break;
                    } else {
                        temp = getString(temp, value);
                    }
                }
                if (flag) {
                    hash += temp;
                    if (hash.charAt(hash.length() - 1) == ',')
                        hash = hash.substring(0, hash.length() - 1);
                    answer.put(hash, key_f.getValue() * s_key.getValue());
                    hash = key_f.getKey() + ',';
                    this.doubs++;
                }
                flag = true;
            }
            vari_values.clear();
        }
        return answer;
    }

    /**
     * parse the query and send it to the bayes ball implementation
     */
    boolean bayes_ball_query(String query) {
        String[] qry = query.split("\\|");
        String[] nodes = qry[0].split("-");
        ArrayList<Vari> shades = new ArrayList<>();
        if (qry.length > 1) {
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
    boolean bayes_ball_imp(Vari node, Vari dest, List<Vari> shades) {
        // Create a queue for BFS
        Queue<Vari> queue = new LinkedList<>();
        // Mark the current node as visited and enqueue it
        Set<Vari> visited = new HashSet<>();
        ArrayList<Vari> came_child = new ArrayList<>();
        queue.add(node);
        ArrayList<Vari> neis = new ArrayList<>();//adj[s] (as in bfs) but we need to know to put the correct nodes each time
        came_child.add(node);//"came from a child so it can go to its parents"
        while (queue.size() != 0) {
            // Dequeue a vertex from queue and print it
            node = queue.poll();
            if (shades.contains(node)) {
                if (came_child.contains(node)) {
                    continue;
                }
                came_child.addAll(node.parents);//add parents to came child so in time they will be added to the queue
                neis.addAll(node.parents);//adding the parents as neighbors of a node
            } else {
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
     *
     */
    public LinkedHashMap<String, Float> eliminate_factor(LinkedHashMap<String, Float> table, Vari hidden) {
        LinkedHashMap<String, Float> answer = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Float>> keys = table.entrySet().iterator();
        Map.Entry<String, Float> it = keys.next();
        String key;
        List<String> outs = hidden.outcomes;
        List<String> visited = new ArrayList<>();
        Float _prob = (float) 0;
        while (visited.size() != table.size()) {//Iterate over the table until we get to the first time we see a different outcome
            key = it.getKey();//get the key of current entry
            if (visited.contains(key)) {
                it = keys.next();
                continue;
            }
            visited.add(key);
            _prob += it.getValue();//getting the value
            for (int j = 1; j < outs.size(); j++) {//Iterate over all the outcomes of the hidden node so we can eliminate it
                key = key.replace(hidden.name + '=' + outs.get(j - 1), hidden.name + '=' + outs.get(j));//change the outcome
                visited.add(key);
                _prob += table.get(key);//add the table value
                this.add++;
            }
            if (key.contains(hidden.name + '=' + outs.get(outs.size() - 1) + ',')) {
                key = key.replace(hidden.name + '=' + outs.get(outs.size() - 1) + ',', "");
            } else {
                key = key.replace(hidden.name + '=' + outs.get(outs.size() - 1), "");
            }
            if (key.charAt(key.length() - 1) == ',')
                key = key.substring(0, key.length() - 1);
            answer.put(key, _prob);
            _prob = (float) 0;
            it = keys.next();
        }
        return answer;
    }

    /**
     * this function receives a string which is a VE query and parse it
     * so it will return the query,evidence and the elimination sequence
     *
     * @param s-string on which we parse the query
     * @return the first index includes the qry and the evidence , second index contains the elimination sequence
     */
    ArrayList<ArrayList<String>> parse_query(String s) {
        String[] split = s.split("\\|");
        String query = split[0].substring(2);
        String[] spl_evi_eli = split[1].split("\\)");//split the elimination and the evidence
        String[] evi = spl_evi_eli[0].split(",");
        String[] eli_seq_p = spl_evi_eli[1].split("-"); //elimination sequence
        eli_seq_p[0] = eli_seq_p[0].substring(1);//in the first character there is a space
        ArrayList<String> qry_evi = new ArrayList<>();
        ArrayList<String> eli_seq = new ArrayList<>(Arrays.asList(eli_seq_p));
        ArrayList<ArrayList<String>> rtrn = new ArrayList<>();
        qry_evi.add(query);
        qry_evi.addAll(Arrays.asList(evi));
        rtrn.add(qry_evi);
        rtrn.add(eli_seq);
        return rtrn;
    }

    /**
     * this function gets an arraylist of the hidden variables and gets the relevant(query+evidence) variables
     * and removes any non-relevant variables
     * @param hidden- hidden nodes
     * @param relevant -relevant nodes
     * @return- array list of only relevant nodes
     */
    public ArrayList<Vari> relevant_hidden(ArrayList<Vari> hidden, List<Vari> relevant, Vari query) {
        ArrayList<Vari> non_relevant = new ArrayList<>();
        Iterator<Vari> it = hidden.iterator();
        Vari node;
        while (it.hasNext()) {
            node = it.next();
            if (!find_ancestor(node, relevant)) {// Iterate over
                non_relevant.add(node);
                it.remove();
            } else {
                if (bayes_ball_imp(node, query, relevant)) {//pretty self-explanatory
                    non_relevant.add(node);
                    it.remove();
                }
            }
        }
        return non_relevant;
    }

    /**
     * this is a recursive function which iterates over the family tree of a node
     * and see if it is an ancestor of a relevant node
     */
    public boolean find_ancestor(Vari hidden, List<Vari> relevant) {
        boolean flag = false;
        if (hidden.children.stream().anyMatch(relevant::contains))
            return true;
        else {
            for (Vari child : hidden.children) {
                flag = find_ancestor(child, relevant);
                if (flag)
                    break;
            }
        }
        return flag;
    }

}
