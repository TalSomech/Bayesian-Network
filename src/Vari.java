import java.awt.geom.FlatteningPathIterator;
import java.util.*;

public class Vari {
    String name;
    ArrayList<Vari> parents;
    LinkedHashMap<String, Float> CPT;
    ArrayList<String> outcomes;
    ArrayList<Vari> children;
    public Vari() {

    }

    public void init_node() {
        for (String out :
                outcomes) {
            CPT.put(name + "=" + out, (float) -1);
        }
    }

    /**
     * this function is used to receive a new parent and add's its values to the CPT so all the CPT's will be ordered
     * just like in class
     *
     * @param pt- parent node
     */
    public void add_st(Vari pt) {
        ArrayList<String> cpt_copy = new ArrayList<>(CPT.keySet());//a deep copy of the cpt because we don't want to change it
        ArrayList<String> temp = new ArrayList<>();
        String k, st;
        int idx;
        /*this iterates over the current CPT so we can add the parent onto the current CPT*/
        for (int j = 0; j < cpt_copy.size(); j += outcomes.size()) {
            /*this for loop is used to iterate over all the outcomes of the parents*/
            for (int i = 0; i < pt.outcomes.size(); i++) {
                /*this iterates over this node's outcomes*/
                for (int l = 0; l < outcomes.size(); l++) {
                    /*these 6-7 lines are just for syntax but it takes the current row from this node's CPT and just sort of sticks a new variable to it*/
                    k = name + '=' + outcomes.get(l);
                    st = cpt_copy.get(j);
                    idx = st.indexOf(',');
                    if (st.contains(",")) {
                        st = st.substring(idx);
                        k += st + ',' + pt.name + "=" + pt.outcomes.get(i);
                    } else {
                        k += ',' + pt.name + "=" + pt.outcomes.get(i);
                    }
                    temp.add(k);
                }
            }
        }
        CPT.clear();
        for (String s : temp) {
            CPT.put(s, (float) -1.0);
        }
    }

    public Vari(String name) {
        this.name = name;
        outcomes = new ArrayList<>();
        children = new ArrayList<>();
        parents = new ArrayList<>();
        CPT = new LinkedHashMap<>();
    }
}
