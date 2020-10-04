1) BRIEF DESCRIPTION

Hydrowl is a prototypical tool for answering conjunctive queries over data whose schema has been described with the use of OWL 2 DL ontologies. In other words Hydrowl is a system for ontology-based query answering. Hydrowl actually first attempts to be a framework for query answering rather than a specific system. Its idea is to combine different systems of different expressive power and (obviously) different performance guarantees to achieve both expressiveness as well as efficiency. Hydrowl uses two (not necessarily mutually exclusive) approaches:

i) Repairing: A rewriting system is used at a pre-processing step to extract/materialise from the input ontology (TBox) all those axioms R that an incomplete system, like an OWL 2 RL system, would miss. Then, it loads to the system the input ontology T, the extracted set of axioms R and the dataset A. The idea is that the extra axioms R would help the system compute more answers than it would otherwise do.

ii) Hybrid: Special techniques are used at run-time to decide if a given user query Q can be answers correctly by a scalable system (like e.g., an OWL 2 RL system). If yes then the query is evaluated over this system; otherwise a fully-fledged OWL 2 DL system needs to be employed. Even in this case special techniques are used to speed-up evaluation. 

In principle, both approaches can be applied to any kind of system that supports any fragment of OWL 2 DL. For example, in case ii) we can first check if some OWL 2 QL system can answer the given user query; if not then check if some OWL 2 RL system can, and so forth. If all options fail then only then fall-back to a complete system.

All techniques supported by Hydrowl interact with the various reasoners through (in most cases standard) interfaces. Hence, we feel that anyone can easily implement these interfaces using their system of choice and take advantage of the methods already implemented in Hydrowl. For example, Hydrowl only uses the getInstances method (slightly extended) of OWL API to interact with HermiT. Hence, it can be easily extended to also interact with Pellet. 

The existing distribution uses an a scalable incomplete system OWLim, as a complete one there are implementations of the interfaces for HermiT and HermiT-BGP. The system also uses the query rewriting system Rapid for extracting query bases and repairs.

For technical details about the techniques used by Hydrowl and the theory behind the interesting reader is referred to the following papers:

* Giorgos Stoilos. Ontology-based data access using Rewriting, OWL 2 RL reasoners, and Repairing. In Proceedings of the European Semantic Web Conference 2014.

* Giorgos Stoilos and Giorgos Stamou. Hybrid Query Answering Over OWL Ontologies. Submitted to European Conference on AI (ECAI 2014).

* Giorgos Stoilos. Hydrowl: A Hybrid Query Answering System for OWL 2 DL Ontologies. Submitted to RR 2014.

2) INSTALLATION

Most jar files required to use Hydrowl are already located under the /lib directory. So you need to ensure that java sees them in order to run Hydrowl. 

Hydrowl is also using SwiftOWLIM. SwiftOWLIM uses various other jars that can be located under the folder SwiftOWLIM (in particular it uses openrdf-model.jar, openrdf-util.jar, owlim-2.9.1.jar, rio.jar, sesame.jar, and trree-2.9.1.jar). Java needs to be able to see these jars as well in order for Hydrowl to work. Please see the separate SwiftOWLIM folder for licenses regarding these jars.

3) USING Hydrowl

Under the folder /examples in Hydrowl, there are several java classes that contain examples about how to use Hydrowl.
