# Overview

Hydrowl was developed between 2012 and 2016 as part of the [SCORE Marie Curie Funded Research Project](https://cordis.europa.eu/project/id/303914). Hydrowl is a prototypical tool for answering SPARQL queries over RDF Knowledge Graphs stored in triples-stores (rdf4j, Stardog, GraphDB, Jena) and described using expressive OWL 2 DL schemas (Ontology-based SPARQL QA). It actually attempts to be a set of techniques/framework that combines the best from existing state-of-the-art systems (scalability of RDF-triple stores and expressiveness of OWL 2 DL reasoners) to achieve both '''scalable''' and '''expressive''' SPARQL question answerings. There are two approaches to achieve this:

 1) Repairing: A rewriting system is used at a pre-processing step to extract/materialise from the input Ontology Schema (TBox) all those schema axioms R that an RDF triple-store would miss. Then, it loads to the system the input schema, the extracted set of axioms R and the RDF graph. The idea is that the extra axioms would help the system compute more answers than it would otherwise do.

2) Hybrid: Techniques are used at run-time to decide if a given user SPARQL query Q can be answers correctly by a triple-store. If the answer is "yes" then the query is evaluated over this system; otherwise a fully-fledged OWL 2 DL system needs to be employed. Even in this case special techniques are used to speed-up evaluation. 

In principle, both approaches can be applied to any kind of system that supports any fragment of OWL 2 DL. Moreover, all techniques supported by Hydrowl interact with the various reasoners through (in most cases standard) interfaces that can be implemented for new systems in order to integrate them in the architecture and benefit from the approaches. 

The existing distribution uses an old version of GraphDB (known as OWLim) as an RDF triple-store while it can use HermiT and HermiT-BGP for OWL 2 DL reasoners. The system also uses the query rewriting system Rapid for extracting query bases and repairs.

For technical details about the techniques used by Hydrowl and the theory behind the interesting reader is referred to the following papers:

* Giorgos Stoilos. Ontology-based data access using Rewriting, OWL 2 RL reasoners, and Repairing. In Proceedings of the European Semantic Web Conference 2014. [Link](https://link.springer.com/chapter/10.1007/978-3-319-07443-6_22)

* Giorgos Stoilos and Giorgos Stamou. Hybrid Query Answering Over OWL Ontologies. In proceedings of the 21st European Conference on AI (ECAI 2014). [Link](http://ebooks.iospress.nl/volumearticle/37049)

* Giorgos Stoilos. Hydrowl: A Hybrid Query Answering System for OWL 2 DL Ontologies. In proceeding of the 8th International Conference On Web Reasoning And Rule Systems (RR), 2014. [Link]((https://link.springer.com/chapter/10.1007/978-3-319-11113-1_20)

# INSTALLATION

Maven is used for some dependencies, however, some systems are not available in maven repository hence the necessary jar files are located under the /lib directory. The current distribution is using a fairly old version of GraphDB (called SwiftOWLIM) that was available under LGPL. All necessary libs are under the respective folders and need to be added to the build path.

# Usage

Under the folder /examples, there are several java classes that contain examples about how to use Hydrowl. Some pre-computed repairs, query bases, and more for popular ontologies also exist.
