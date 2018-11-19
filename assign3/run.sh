#!/bin/bash
ant build -f build_assign_rerank.xml
java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx6000m edu.berkeley.nlp.assignments.rerank.ParsingRerankerTester -path ./data -rerankerType AWESOME -test
java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx6000m edu.berkeley.nlp.assignments.rerank.ParsingRerankerTester -path ./data -rerankerType BASIC -test