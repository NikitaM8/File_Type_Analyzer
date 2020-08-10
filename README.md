# File_Type_Analyser
This program work with file in chosen directory and output type of each file according to pattern which contain inside of file. 
It accept 2 parameters: 1 - directory of files, 2 - file with patterns, sorted with priority order.
Implementation based on strategy pattern, so we can simply extend this program with another analyse strategy.
We can use strategy of analyse (naive, knutt-morris-pratt, rabin-karp). 
Analyse of files goes in multithreading mode.

Example:

input
java Main test_files patterns.db

output
doc_0.doc: MS Office Word 2003
doc_1.pptx: MS Office PowerPoint 2007+
doc_2.pdf: PDF document
file.pem: PEM certificate
