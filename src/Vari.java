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
        String k="";
        int t=0;
        for(int i=0;i<kn.size();i++){
            k=kn.get(i);
            t=0;
            for (int j = 0; j < pt.outcomes.size(); j++) {
                k=k+","+pt.name+"="+pt.outcomes.get(j);
                temp.add(k);
                //if(t!=kn.size()-1) {
                    k = kn.get(i);
               // }
            }
        }
        CPT.clear();
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
