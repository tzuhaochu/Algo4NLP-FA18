#!/bin/bash
ant build -f build_assign_align.xml
java -cp assign_align.jar:submit.jar -server -Xmx16g edu.berkeley.nlp.assignments.align.AlignmentTester -path ./data -alignerType BASELINE -maxTrain 10000 -data test