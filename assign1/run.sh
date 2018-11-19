#!/bin/bash
ant -f build_assign1.xml
java -cp assign1.jar:assign1-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.assign1.LanguageModelTester -path ./data -lmType TRIGRAM
