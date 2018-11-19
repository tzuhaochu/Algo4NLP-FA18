#!/bin/bash
ant -f build_assign1.xml
java -cp assign_parsing.jar:submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path ./data -parserType GENERATIVE