package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.ling.Tree;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TagEnricher {

    private static HashSet<String> prep =
            Stream.of(
                    "in", "of", "with", "at", "from", "into", "during", "including", "against",
                    "amoung", "throughout", "despite", "towards", "upon", "concerning", "to", "for",
                    "on", "by", "about", "like", "through", "over", "between", "without", "under",
                    "within", "along", "following", "across", "behind", "beyond", "plus", "except",
                    "up", "around", "down", "above", "near", "out"
            ).collect(Collectors.toCollection(HashSet::new));
    private static HashSet<String> subconj =
            Stream.of(
                    "after", "once", "until", "although", "when", "as", "before", "because", "so",
                    "unless", "why", "while", "than", "whereas", "where", "wherever", "whether",
                    "while", "until", "though", "since", "that", "if", "but"
            ).collect(Collectors.toCollection(HashSet::new));

    public static void enrich(Tree<String> tree) {
        if (tree.isPreTerminal()) {
            String word = tree.getChildren().get(0).getLabel();
            if (tree.getLabel() == "IN") {
                if(prep.contains(word)) tree.setLabel("PREP");
                else if(subconj.contains(word)) tree.setLabel("SUBCONJ");
                else ;
            }else if(tree.getLabel() == "RB"){

            }else if(tree.getLabel() == "UN"){

            }
            return;
        }
        for (Tree<String> child : tree.getChildren()) {
            enrich(child);
        }
    }
}
