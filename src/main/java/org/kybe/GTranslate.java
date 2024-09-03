package org.kybe;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * GTranslate
 *
 * @author kybe236
 */
public class GTranslate extends Plugin {
	
	@Override
	public void onLoad() {
		final GTranslateModule gtranslate = new GTranslateModule();
		RusherHackAPI.getModuleManager().registerFeature(gtranslate);
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("Example plugin unloaded!");
	}
	
}