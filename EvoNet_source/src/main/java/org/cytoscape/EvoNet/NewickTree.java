package org.cytoscape.EvoNet;

import java.util.ArrayList;
public class NewickTree {

    private static int node_uuid = 0;
    ArrayList<Node> nodeList = new ArrayList<>();
    private Node root;
    public boolean has_bootstrap = false;

    static NewickTree readNewickFormat(String newick) {
        return new NewickTree().innerReadNewickFormat(newick);
    }

    private static String[] split(String s) {

        ArrayList<Integer> splitIndices = getIntegers(s);

        int numSplits = splitIndices.size() + 1;
        String[] splits = new String[numSplits];

        if (numSplits == 1) {
            splits[0] = s;
        } else {

            splits[0] = s.substring(0, splitIndices.get(0));

            for (int i = 1; i < splitIndices.size(); i++) {
                splits[i] = s.substring(splitIndices.get(i - 1) + 1, splitIndices.get(i));
            }

            splits[numSplits - 1] = s.substring(splitIndices.get(splitIndices.size() - 1) + 1);
        }
        return splits;
    }

    private static ArrayList<Integer> getIntegers(String s) {
        ArrayList<Integer> splitIndices = new ArrayList<>();

        int rightParenCount = 0;
        int leftParenCount = 0;
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '(':
                    leftParenCount++;
                    break;
                case ')':
                    rightParenCount++;
                    break;
                case ',':
                    if (leftParenCount == rightParenCount) splitIndices.add(i);
                    break;
            }
        }
        return splitIndices;
    }

    private NewickTree innerReadNewickFormat(String newick) {
        this.root = readSubtree(newick.substring(0, newick.length() - 1));
        return this;
    }

    private Node readSubtree(String s) {
        int leftParen = s.indexOf('(');
        int rightParen = s.lastIndexOf(')');

        if (leftParen != -1 && rightParen != -1) {
            String name = s.substring(leftParen+1, rightParen);
            String[] childrenString = split(name);
            Node node = new Node(s.lastIndexOf(")") == (s.length() - 1) ? name : s);
            for (String sub : childrenString) {
                Node child = readSubtree(sub);
                node.children.add(child);
                child.parent = node;
            }
            nodeList.add(node);
            if (node.bootstrap != 0) {
                this.has_bootstrap = true;
            }
            return node;
        } else if (leftParen == rightParen) {
            Node node = new Node(s);
            nodeList.add(node);
            if (node.bootstrap != 0) {
                this.has_bootstrap = true;
            }
            return node;

        } else throw new RuntimeException("Unbalanced brackets in .nwk file");
    }

    public static class Node {
        String name = "";
        double bootstrap = 0;
        double weight = 0;
        boolean realName = false;
        ArrayList<Node> children = new ArrayList<>();
        Node parent;

        Node(String name) {
            double bootstrap = 0; // Node attribute (node:bootstrap)
            double weight    = 0; // Edge attribbute
            String actualNameText;
            //Complex node case with ()'s
            if (name.contains(")")) {
                if (name.lastIndexOf(")") != name.length() - 1) {
                    String arguments = name.substring(name.lastIndexOf(")") + 1);

                    if (arguments.contains(":")) {
                        if (arguments.indexOf(":") != 0) {
                            weight = Double.parseDouble(arguments.substring(0, arguments.indexOf(":")));
                            bootstrap = Double.parseDouble(arguments.substring(arguments.indexOf(":") + 1));
                        } else {
                            bootstrap = Double.parseDouble(arguments.substring(1));
                        }
                    } else {
                        weight = Double.parseDouble(arguments);
                    }
                    actualNameText = name.substring(1, name.lastIndexOf(")") - 1);
                } else {
                    actualNameText = name.substring(1, name.length() - 1);
                }
            } else {
                //Primitive node case
                int colonIndex = name.lastIndexOf(':');
                if (colonIndex == -1){
                    actualNameText = name;
                } else {
                    actualNameText = name.substring(0, colonIndex);
                    bootstrap = Double.parseDouble(name.substring(colonIndex + 1));
                }
            }

                if (actualNameText.indexOf(',') != -1) {
                    this.realName = false;
                    this.name = Integer.toString(node_uuid);
                    node_uuid++;
                } else {
                    this.realName = true;
                    this.name = actualNameText;
                }
                this.bootstrap = bootstrap;
                this.weight = weight;
                System.out.print("Created node: ");
                System.out.println(this.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node other = (Node) o;
            return this.name.equals(other.name);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (children != null && !children.isEmpty()) {
                sb.append("(");
                for (int i = 0; i < children.size() - 1; i++) {
                    sb.append(children.get(i).toString());
                    sb.append(",");
                }
                sb.append(children.get(children.size() - 1).toString());
                sb.append(")");
            }
            if (name != null) sb.append(this.getName());
            return sb.toString();
        }

        String getName() {
            if (realName)
                return name;
            else
                return "";
        }
    }

    @Override
    public String toString() {
        return root.toString() + ";";
    }

    public Node getRoot() {
        return root;
    }

}