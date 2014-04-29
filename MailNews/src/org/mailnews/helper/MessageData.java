package org.mailnews.helper;

import java.util.List;

public class MessageData {

	private static volatile List<MessageBean> messages;
	HTMLHelper htmlHelper;
	private String path = "";
	private static boolean checkNewMessages = false;
	private static volatile boolean updateInProgress = false;
	private static volatile long lastUpdate;

	public MessageData(String aPath) {
		path = aPath;
		initMessages();
	}

	private void initMessages() {
		if (updateInProgress) {
			return;
		}
		updateInProgress = true;
		try {
			List<MessageBean> currentMessages = MailHelper.receiveMessage(
					Settings.LOGIN, Settings.PASSWORD, Settings.HOST,
					Settings.PORT, Settings.STORE_PROTOCOL, path,
					false);
			htmlHelper = new HTMLHelper(Settings.FONT_SIZE, Settings.SPACING,
					Settings.FONT_HEADER_SIZE, Settings.HEADER_SPACING,
					Settings.FONT_FAMILY,
					Settings.HEADER_FONT_FAMILY, Settings.TEXT_MARGIN_BOTTOM_TOP,
					Settings.IMG_MARGIN, path, Settings.TEXT_INDENTER);
			for (int i = 0; i < currentMessages.size(); i++) {
				String subject = currentMessages.get(i).getSubject();
				String content = currentMessages.get(i).getContent();
				htmlHelper.setDivHeight(Settings.DIV_HEIGHT);
				htmlHelper.setDivWidth(Settings.DIV_WIDTH);
				int headerH = htmlHelper.getHeaderHeight(subject,
						Settings.HEADER_MARGIN, 0);
				htmlHelper.setDivHeight(Settings.DIV_HEIGHT - headerH
						- Settings.PROGRESS_BAR_HEIGHT);
				htmlHelper.setDivWidth(Settings.DIV_WIDTH
						- Settings.TEXT_MARGIN_LEFT_RIGHT * 2);
				currentMessages.get(i).setContentParts(
						htmlHelper
								.splitContent(content, currentMessages.get(i)
										.getAttachments(),
										currentMessages.get(i).getEncoding()
												.toLowerCase()
												.contains("plain") ? "plain"
												: "mixed"));
			}
			if (checkNewMessages) {
				//messages.addAll(currentMessages);
				messages = currentMessages;
			} else {
				messages = currentMessages;
			}
			messages = MailHelper.filterMessages(messages);
			checkNewMessages = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		lastUpdate = System.currentTimeMillis();
		updateInProgress = false;
	}

	private void checkForUpdate() {
		if (System.currentTimeMillis() - lastUpdate > Settings.LETTERS_REFRESH_TIME * 60000
				&& !updateInProgress) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					initMessages();
				}
			});
			thread.start();
		}
	}

	public List<MessageBean> getMessages() {
		checkForUpdate();
		return messages;
	}
}
