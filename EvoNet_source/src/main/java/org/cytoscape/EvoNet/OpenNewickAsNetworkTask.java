package org.cytoscape.EvoNet;

import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class OpenNewickAsNetworkTask extends AbstractTask {
    @Tunable(description = "Newick file", params = "fileCategory=unspecified;input=true")
    public File f;

    CyNetworkManager networkManager;
    CyNetworkFactory networkFactory;
    CyNetwork newNetwork;

    HashMap<NewickTree.Node, Long> nodes_suid = new HashMap<>();

    public OpenNewickAsNetworkTask(CyNetworkManager networkManager, CyNetworkFactory networkFactory) {
        this.networkFactory = networkFactory;
        this.networkManager = networkManager;
        newNetwork = this.networkFactory.createNetwork();
    }


    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        ArrayList<NewickTree> trees = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            trees.add(NewickTree.readNewickFormat(line));
        }
        reader.close();
        CyTable node_table = newNetwork.getDefaultNodeTable();
        CyTable edge_table = newNetwork.getDefaultEdgeTable();
        node_table.createColumn("is_terminal", Boolean.class, false);
        edge_table.createColumn("weight", Double.class, false);
        if (trees.get(0).has_bootstrap) {
            node_table.createColumn("bootstrap", Double.class, false);
        }
        for (NewickTree tree : trees) {
            for (NewickTree.Node n : tree.nodeList) {
                CyNode cyNode = addNode(n);
                nodes_suid.put(n, cyNode.getSUID());
                CyRow row = newNetwork.getRow(cyNode);
                if (!n.children.isEmpty()) {
                    addEdges(n, newNetwork);
                    row.set("is_terminal", Boolean.FALSE);
                } else {
                    row.set("is_terminal", Boolean.TRUE);
                }
                if (tree.has_bootstrap) {
                    row.set("bootstrap", n.bootstrap);
                }
                taskMonitor.setStatusMessage("adding node" + n);
            }
        }
        networkManager.addNetwork(newNetwork);
    }

    public CyNode addNode(NewickTree.Node node) {
        CyNode cyNode = newNetwork.addNode();
        CyRow row = newNetwork.getRow(cyNode);
        row.set("name", node.name);
        return cyNode;
    }

    public void addEdges(NewickTree.Node node, CyNetwork network) {
        CyNode parent = newNetwork.getNode(nodes_suid.get(node));
        for (NewickTree.Node c : node.children) {
            CyNode child = newNetwork.getNode(nodes_suid.get(c));
            CyEdge edge = newNetwork.addEdge(parent, child, true);
            CyRow edge_row = network.getRow(edge);
            edge_row.set("weight", c.weight);
        }
    }
}
