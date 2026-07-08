package org.ginsim.service.tool.avatar;

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.service.AvatarServiceFacade;
import org.ginsim.service.tool.avatar.service.FirefrontServiceFacade;
import org.ginsim.service.tool.avatar.service.MonteCarloServiceFacade;
import org.ginsim.service.tool.avatar.simulation.Simulation;

public class DynamicParamTests extends TestCase {

	public String path = "C:\\Users\\Rui\\Documents\\00 Avatar\\Avatar Material\\table-models\\";

	public void testDynamicAvatar() throws Exception {
		List<String> filenames = Arrays.asList("mmc.avatar", "mmc-cycD1.avatar",
				"random_001_v010_k2.avatar", "random_002_v010_k2.avatar",
				"random_003_v015_k2.avatar", "random_004_v015_k2.avatar",
				"synthetic_1.avatar", "synthetic_2.avatar",
				"sp1.avatar", "sp2.avatar", "sp4.avatar");
		// "simple5.avatar");th-reduced*/

		String avaoption = "--runs=1000 --state=init --max-steps=10000 --tau=4 --min-cycle-size=3 --keep-transients --keep-oracle"
				+ "--approx --max-psize=" + Math.pow(2, 14)
				+ " --min-transient-size=100 --plots --quiet --output-dir=/output --input=" + path;
		String mcoption = "--runs=1000 --maxsteps=1000 --input=" + path;
		String ffoption = "--alpha=0.00001 --quiet --plots --runs=1000 --depth=10000 --output-dir=/output --input="
				+ path;
		for (String filename : filenames) {
			System.out.println("\nFilename = " + filename);
			String[] args = (avaoption + filename).split("( --)|=|--");
			Simulation sim = AvatarServiceFacade.getSimulation(args);
			AvatarParameters p = new AvatarParameters();
			sim.dynamicUpdateValues();
			p.complete(sim);

			args = (ffoption + filename).split("( --)|=|--");
			sim = FirefrontServiceFacade.getSimulation(args);
			sim.dynamicUpdateValues();
			p.complete(sim);

			args = (mcoption + filename).split("( --)|=|--");
			sim = MonteCarloServiceFacade.getSimulation(args);
			sim.dynamicUpdateValues();
			p.complete(sim);
		}
	}
}
