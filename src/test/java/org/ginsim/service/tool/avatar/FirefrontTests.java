package org.ginsim.service.tool.avatar;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.ginsim.service.tool.avatar.domain.Result;
import org.ginsim.service.tool.avatar.service.FirefrontServiceFacade;

public class FirefrontTests extends TestCase {

	public void testFirefront() throws Exception {
		String dir = "C:\\Users\\Rui\\Documents\\00 Avatar\\Avatar Material\\table-models\\";
		List<String> filenames = Arrays.asList(
			"random_002_v010_k2.avatar");//"random_001_v010_k2.avatar");
			//"random_003_v015_k2.avatar","random_004_v015_k2.avatar",
			//"synthetic_1.avatar","synthetic_2.avatar",
			//"mmc.avatar","mmc-cycD1.avatar",
			//"sp1.avatar","sp2.avatar","sp4.avatar",
		
		List<String> options = Arrays.asList("--alpha=0.00001 --maxDepth=10000 --quiet --plots --input="+dir);

		for(String filename : filenames){
			String[] args = (options.get(0)+filename).split("( --)|=|--");
			System.out.println("ARGS="+args.length+AvatarUtils.toString(args));
			Result result = FirefrontServiceFacade.run(args);
			System.out.println(result.toHTMLString());
			assertNotNull(result);
		}
	}
}
