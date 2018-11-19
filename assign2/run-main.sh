#!/bin/bash
java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path ./data -parserType GENERATIVE -maxTestLength 20