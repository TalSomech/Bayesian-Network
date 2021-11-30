import java.awt.geom.FlatteningPathIterator;
import java.util.*;

public class Vari {
    String name;
    //ArrayList<Vari> parents;
    //String parents_indx;
    //HashMap<String,Vari> parents;
    ArrayList<Vari> parents;
    //LinkedHashMap<String,Float> cps;
    //ArrayList<Vari> pts;
    LinkedHashMap<String,Float> CPT;

    ArrayList<String> outcomes;
    ArrayList<Vari> children;
    //ArrayList<Float> CPT;
    //ashMap<String, Float> cps;

    public Vari() {

    }
    public void init_node(){
        for (String out:
             outcomes) {
            CPT.put(name+"="+out,(float)-1);
        }
    }

    public void add_st(Vari pt){
        ArrayList<String> kn = new ArrayList<>(CPT.keySet());
        ArrayList<String> temp=new ArrayList<>();
        String k,st;
        int idx;
            for (int j = 0; j < kn.size(); j+=outcomes.size()) {
                for(int i=0;i<pt.outcomes.size();i++){
                for (int l = 0; l < outcomes.size(); l++) {
                    k = name + '=' + outcomes.get(l) ;
                    st = kn.get(j);
                    idx=st.indexOf(',');
                    if(st.contains(",")) {
                        st=st.substring(idx);
                        k += st + ',' + pt.name + "=" + pt.outcomes.get(i);
                    }
                    else{
                        k+=',' + pt.name + "=" + pt.outcomes.get(i);
                    }

                    temp.add(k);
                }
            }
        }
        CPT.clear();
        int gd=0;
        for (String s : temp) {
            CPT.put(s, (float) -1.0);
        }
    }

    public Vari(String name) {
        //parents_indx="";
        this.name = name;
        outcomes = new ArrayList<>();
        children = new ArrayList<>();
        parents = new ArrayList<>();
       // CPT = new ArrayList<>();
        CPT=new LinkedHashMap<>();
        //table=new ArrayList<>();
    }

//    public int[] get_table(String[] evidence) {
//        String[] split;
//        int[] table;
//        String t;
//        for (String evi : evidence) {
//            split = evi.split("=");
//
//            String[][] ot = new String[parents.size()][];
//            ArrayList<ArrayList<String>> outs = new ArrayList<>();
//            outs.add(0, outcomes);
//            //ot[1]=outcomes.toArray(new String[0]);
//            for (int i = 1; i < parents.size(); i++) {
//                if (!parents.get(i).name.equals(split[0])) {
//                    outs.add(i, parents.get(i).outcomes);
//                }
//            }
//            int t= parents.indexOf();
//        }
//        return null;
//    }

}
