package org.kybe;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

/**
 * GTranslateModule
 *
 * @author kybe236
 */
public class GTranslateModule extends ToggleableModule {

	public enum TranslateLang {
		am, ar, eu, bn, bg, ca, chr, hr, cs, da, nl, en, et, fil, fi, fr, de, el, gu, iw, hi, hu, is, id, it, ja, kn, ko, lv, lt, ms, ml, mr, no, pl, ro, ru, sr, sk, sl, es, sw, sv, ta, te, th, tr, ur, uk, vi, cy, cn
	}

	public enum ResponseType {
		newMsg,
		sameMsg,
		replaceMsg
	}


	private final BooleanSetting translateReceiving = new BooleanSetting("Translate Receiving messages", true);
	private final EnumSetting<TranslateLang> receiveLang = new EnumSetting<>("Your lang", "The language to translate to", TranslateLang.de);
	private final EnumSetting<ResponseType> receiveMsg = new EnumSetting<>("Repsonse form", "the way the translated msg is shown", ResponseType.newMsg);
	private final BooleanSetting translateSending = new BooleanSetting("Translate Sending messages", true);
	private final EnumSetting<TranslateLang> sendLang = new EnumSetting<>("Sending Lang", "The language an message sending should be", TranslateLang.en);

	public GTranslateModule() {
		super("GTranslateModule", "Translates using google translate", ModuleCategory.CLIENT);

		translateSending.addSubSettings(
				sendLang
		);

		translateReceiving.addSubSettings(
				receiveLang,
				receiveMsg
		);

		this.registerSettings(
				translateReceiving,
				translateSending
		);
	}

	boolean secondcancel = false;
	@Subscribe
	public void onPacket(EventPacket.Send e) {
		if (e.getPacket() instanceof ServerboundChatPacket p) {
			if (!this.translateSending.getValue() || secondcancel) return;
			try {
				e.setCancelled(true);
				new Thread(() -> {
					String text = "";
					try {
						text = translate(p.message(), sendLang.getValue().name());
					} catch (Exception ex) {
						this.getLogger().error(ex.getMessage());
						this.getLogger().error(ex.getStackTrace().toString());
					}
					secondcancel = true;
					mc.getConnection().sendChat(text);
					secondcancel = false;
				}).start();
			} catch (Exception err) {
				this.getLogger().error(err.getMessage());
				this.getLogger().error(err.getStackTrace().toString());
			}
		}
	}

	boolean cancel = false;
	@Subscribe
	public void onMsgAdd(EventAddChat e) {
		if (e == null || !translateReceiving.getValue() || cancel) return;

		if (e.getChatComponent().toString().contains("[rusherhack]")) {
			this.getLogger().debug("ignored rusherhack");
			return;
		}
		if (e.getChatComponent().toString().contains("[TRANSLATOR]: ")) {
			this.getLogger().debug("ignored translate");
			return;
		}

		String msg = e.getChatComponent().getString();
		if (msg.isBlank()) return;
		e.setCancelled(true);
		new Thread(() -> {
			try {
				this.getLogger().debug("translating");
				String result = translate(e.getChatComponent().getString(), receiveLang.getValue().name());
				final MutableComponent prefix = Component.literal("[TRANSLATOR]: ").withStyle(ChatFormatting.RED);
				final MutableComponent text = Component.literal(translate(e.getChatComponent().getString(), this.receiveLang.getValue().name())).withStyle(ChatFormatting.WHITE);
				if (this.receiveMsg.getValue() == ResponseType.newMsg) {
					cancel = true;
					mc.gui.getChat().addMessage(text);
					cancel = false;
					mc.gui.getChat().addMessage(Component.empty().append(prefix).append(text));
				} else if (this.receiveMsg.getValue() == ResponseType.sameMsg) {
					mc.gui.getChat().addMessage(e.getChatComponent().copy().append(" ").append(prefix).append(text));
				} else if (this.receiveMsg.getValue() == ResponseType.replaceMsg) {
					cancel = true;
					mc.gui.getChat().addMessage(text);
					cancel = false;
				}
			} catch (UnsupportedEncodingException | MalformedURLException ex) {
				throw new RuntimeException(ex);
			}
		}).start();
	}

	public String translate(String inp, String to) throws UnsupportedEncodingException, MalformedURLException {
		StringBuilder response = new StringBuilder();

		URL url = new URL(String.format("https://translate.google.com/m?sl=auto&tl=%s&hl=en&q=%s", to, URLEncoder.encode(inp.trim(), "UTF-8")));
		try {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null)
					response.append(line + "\n");
			}
		} catch (IOException ignored) {
			this.getLogger().debug(ignored.getMessage() + ignored.getStackTrace());
		}

		Matcher matcher = Pattern.compile("class=\"result-container\">([^<]*)<\\/div>", Pattern.MULTILINE).matcher(response);
		matcher.find();
		String match = matcher.group(1);
		if (match == null || match.isEmpty()) {
			this.getLogger().debug("Translation failed");
			return "Translation failed";
		}
		return unescapeHtml4(match);
	}
}
