package org.cytoscape.EvoNet;

import org.cytoscape.model.*;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
    public CyActivator() {super();}

    public void start(BundleContext bc) throws Exception {
        CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
        CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);

        OpenNewickAsNetworkTaskFactory openFileTaskFactory = new OpenNewickAsNetworkTaskFactory(networkManager, networkFactory);

        Properties props = new Properties();
        props.setProperty("preferredMenu","Apps");
        props.setProperty("title","Create network from nwk tree");
        registerService(bc, openFileTaskFactory, TaskFactory.class, props);
    }
}
