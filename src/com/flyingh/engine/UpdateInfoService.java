package com.flyingh.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Xml;

import com.flyingh.moguard.R;
import com.flyingh.vo.UpdateInfo;

public class UpdateInfoService {
	private Context context;

	public UpdateInfoService(Context context) {
		this.context = context;
	}

	public UpdateInfo getUpdateInfo() throws IOException, XmlPullParserException {
		return parse(getInputStream());
	}

	private UpdateInfo parse(InputStream is) throws XmlPullParserException, IOException {
		UpdateInfo updateInfo = new UpdateInfo();
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if ("version".equals(parser.getName())) {
					updateInfo.setVersion(parser.nextText());
				} else if ("description".equals(parser.getName())) {
					updateInfo.setDescription(parser.nextText());
				} else if ("url".equals(parser.getName())) {
					updateInfo.setUrl(parser.nextText());
				}
				break;

			default:
				break;
			}
			eventType = parser.next();
		}
		return updateInfo;
	}

	private InputStream getInputStream() throws IOException {
		String updateInfoUrl = context.getResources().getString(R.string.update_info_url);
		return new URL(updateInfoUrl).openStream();
	}

}
