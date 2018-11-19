#!/bin/bash
java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx300m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path ./data -parserType GENERATIVE -sanityCheck