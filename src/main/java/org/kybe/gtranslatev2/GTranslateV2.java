package org.kybe.gtranslatev2;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.Module;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.plugin.Plugin;

public class GTranslateV2 extends Plugin {
	public static GTranslateV2 INSTANCE;
	
	@Override
	public void onLoad() {
		INSTANCE = this;

		ToggleableModule gTranslateV2Module = new GTranslateV2Module();
		RusherHackAPI.getModuleManager().registerFeature(gTranslateV2Module);
	}
	
	@Override
	public void onUnload() {
	}
	
}