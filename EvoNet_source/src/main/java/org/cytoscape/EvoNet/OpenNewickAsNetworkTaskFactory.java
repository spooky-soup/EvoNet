package org.cytoscape.EvoNet;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class OpenNewickAsNetworkTaskFactory extends AbstractTaskFactory {
    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    public OpenNewickAsNetworkTaskFactory(CyNetworkManager networkManager, CyNetworkFactory networkFactory) {
        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
    }
    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new OpenNewickAsNetworkTask(networkManager, networkFactory));
    }
}
