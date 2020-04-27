package com.servicemesh.agility.api.service;

import java.util.List;

import com.servicemesh.agility.api.LaunchItemDeployment;
import com.servicemesh.agility.api.Task;
import com.servicemesh.agility.api.Template;
import com.servicemesh.agility.api.Topology;
import com.servicemesh.agility.api.TopologyDeployment;
import com.servicemesh.agility.api.TopologyStats;

public interface ITopology<T>
{

    public TopologyStats stats(int id) throws Exception;

    /**
     * Returns the templates associated to the specified topology.
     *
     * @param topology
     * @throws Exception
     */
    public List<Template> getTemplates(Topology topology) throws Exception;

    /**
     * Request to delete a topology.
     *
     * @param parentTask
     *            Parent task of the delete request
     * @param topology
     *            Topology to be deleted
     * @param isParentContext
     *            If true, aspect context of the parent is used
     * @return The task specifying the delete request
     * @throws Exception
     */
    public Task delete(Task parentTask, Topology topology, boolean isParentContext) throws Exception;

    /**
     * Finalizes the delete request of a Topology. To be used only when all templates associated to a topology have been deleted.
     *
     * @param parentTask
     *            Parent task of the delete request
     * @param topology
     *            Topology to be deleted
     * @param isParentContext
     *            If true, aspect context of the parent is used
     * @throws Exception
     */
    public void finalizeDelete(Task parentTask, Topology topology, boolean isParentContext) throws Exception;

    /**
     * For a given topology object, the associated LaunchItemDeployment will be returned. If none exists, null will be returned.
     *
     * @param topology
     *            - topology object from which to get the LaunchItemDeploymnet
     * @throws Exception
     */
    public LaunchItemDeployment getLaunchItemDeployment(Topology topology) throws Exception;

    /**
     * For a given topology object,all the associated Deployment will be returned. If none exists, null will be returned.
     *
     * @param topology
     *            - topology object from which to get the LaunchItemDeploymnet
     * @throws Exception
     */
    public TopologyDeployment getTopologyDeployment(Topology topology) throws Exception;

}
