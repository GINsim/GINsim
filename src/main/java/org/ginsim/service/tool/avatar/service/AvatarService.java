package org.ginsim.service.tool.avatar.service;

import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.ginsim.service.tool.avatar.domain.Result;
import org.kohsuke.MetaInfServices;

/**
 * Provider of Avatar services in GINsim
 * @author Pedro T. Monteiro
 * @author Rui Henriques
 */
@MetaInfServices(Service.class)
@ServiceStatus(EStatus.RELEASED)
public class AvatarService implements Service {	
	/**
	 * Runs a simulation from an input set of arguments
	 * @param args the arguments for the simulation
	 * @param algorithm the selected type of simulation
	 * @return the discovered attractors, their reachability, and remaining contextual information from the simulation
	 * @throws Exception the exception
	 */
	public static Result run(String[] args, EnumAlgorithm algorithm) throws Exception {
		switch(algorithm){
			case AVATAR : return AvatarServiceFacade.run(args);
			case FIREFRONT : return FirefrontServiceFacade.run(args);
			default : return MonteCarloServiceFacade.run(args);
		}
	}
}
