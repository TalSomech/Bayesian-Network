import java.awt.geom.FlatteningPathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Vari {
    String name;
    ArrayList<Vari> parents;
    ArrayList<String> outcomes;
    ArrayList<Vari> children;
    ArrayList<Float> CPT;
    boolean bayes;
    public Vari(){

    }

    public Vari(String name) {
        bayes=false;
        this.name = name;
        outcomes=new ArrayList<>();
        children=new ArrayList<>();
        parents=new ArrayList<>();
        CPT=new ArrayList<>();
        //table=new ArrayList<>();
    }

}
