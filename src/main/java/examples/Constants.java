package examples;

public class Constants {
    public static String prefix = null;

    public static String lubmOntology = Thread.currentThread().getContextClassLoader().getResource("ontologies/LUBM/univ-bench.owl").getPath();
    public static String uobmOntology = Thread.currentThread().getContextClassLoader().getResource("ontologies/UOBM/univ-bench-dl.owl").getPath();
    public static String uniProtOntology = Thread.currentThread().getContextClassLoader().getResource("ontologies/uniprot/core-sat-processed.owl").getPath();
    public static String reactomOntology = Thread.currentThread().getContextClassLoader().getResource("ontologies/reactome/biopax-level3-processed.owl").getPath();
    public static String npdOntology =  Thread.currentThread().getContextClassLoader().getResource("ontologies/npd/npd.owl").getPath();
    public static String chemblOntology = Thread.currentThread().getContextClassLoader().getResource("ontologies/chembl/cco-noDPR.owl").getPath();

    public static String getSystemPrefix() {
        if (prefix != null) {
            return prefix;
        }
        else if (System.getProperty("os.name").contains("Linux"))
            prefix="file:";
        else if (System.getProperty("os.name").contains("Windows"))
            prefix="file:/";
        return prefix;
    }
}
