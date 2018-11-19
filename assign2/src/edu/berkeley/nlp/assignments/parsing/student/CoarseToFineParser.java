package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.UnaryClosure;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

import java.util.ArrayList;
import java.util.List;

public class CoarseToFineParser implements Parser {
    Indexer<String> cIndexer, fIndexer;
    MyLexicon cLexicon, fLexicon;
    MyGrammar cGrammar, fGrammar;
    UnaryClosure cClosure, fClosure;

    public CoarseToFineParser(List<Tree<String>> trees) {
        List<Tree<String>> cTrees = new ArrayList<>();
        for (Tree<String> tree : trees) {
            cTrees.add(CoarseBinarizer.binarize(tree).toTree());
        }
        cGrammar = new MyGrammar(cTrees);
        cLexicon = new MyLexicon(cTrees);
        cIndexer = cGrammar.getLabelIndexer();
        cClosure = new UnaryClosure(cGrammar.getLabelIndexer(), cGrammar.getUnaryRules());

        System.out.println("Coarse:");
        System.out.println(cIndexer.size());
        System.out.println(cGrammar.getUnaryRules().size());
        System.out.println(cGrammar.getBinaryRules().size());

        List<Tree<String>> fTrees = new ArrayList<>();
        for (Tree<String> tree : trees) {
            fTrees.add(FineBinarizer.binarize(tree).toTree());
        }
        fGrammar = new MyGrammar(fTrees);
        fLexicon = new MyLexicon(fTrees);
        fIndexer = fGrammar.getLabelIndexer();
        fClosure = new UnaryClosure(fGrammar.getLabelIndexer(), fGrammar.getUnaryRules());

        System.out.println("Fine:");
        System.out.println(fIndexer.size());
        System.out.println(fGrammar.getUnaryRules().size());
        System.out.println(fGrammar.getBinaryRules().size());
    }

    @Override
    public Tree<String> getBestParse(List<String> list) {
        return null;
    }
}
