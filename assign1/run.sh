#!/bin/bash
ant -f build_assign_lm.xml
java -cp assign_lm.jar:submit.jar -server -mx2000m edu.berkeley.nlp.assignments.assign1.LanguageModelTester -path ./data -lmType TRIGRAM
