Clinical Concept Extraction
============================
Concept extraction tool in JAVA which extracts information like medical problems, treatments and tests from medical discharge summaries. It is used by Clinical Data Extraction Software suite.

Dependencies
-------------
* BANNER
* MALLET
* dragon-toolkit
* semantic-vectors
* javatuples-1.2
* mysql-connector

Different Parts
---------------
* One package handles the distributional semantics part - sample.randomeindexing
* sample.umls.SqlHandles gets the problem, tests and treatments from SQL database
* sample.i2b2 finally trains the CRF model and outputs it
* sample.i2b2.ThesaurusPipe is a custom pipe implemented which can be used in BANNER
* sample.i2b2.CorpusHandler handles the i2b2 corpus and makes similarity matrix
* sample.i2b2.ConceptTagger calls the BANNER and generates the CRF model
* banner.tagging.CRFTagger contains the features used in CRF

How is it being used in suite?
-------------------------------
1. The generated CRF model is loaded
2. The untagged document should be labeled by some model IOB(preferably), pipe symbol separated
3. properties.txt contains the additional info which is required by the component
4. The model which was loaded takes in the untagged document and outputs the tagged words and then accordingly xml list is made out of the tags where a problem is tagged like &lt;problem&gt;---&lt;/problem&gt;
