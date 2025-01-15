package com.ilsian.rmweb;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ilsian.tomcat.TemplateInteraction;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.WebLib;

public class SettingsAgent implements TemplateInteraction {

	@Override
	public void handleAction(String action, UserInfo user,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Global.CONDITION_MODS = WebLib.getBoolParam(request, "condition_mods", Global.CONDITION_MODS);
		Global.USE_COMBAT_TRACKER = WebLib.getBoolParam(request, "combat_tracker", Global.USE_COMBAT_TRACKER);
		Global.USE_AFFIRMATIVE_TRACKER = WebLib.getBoolParam(request, "affirmative_tracker", Global.USE_AFFIRMATIVE_TRACKER);
		Global.ENTITY_LINKS = WebLib.getStringParam(request, "entity_links", Global.ENTITY_LINKS);
		Global.save();
		response.sendRedirect(response.encodeRedirectURL("/gui"));		
	}

	@Override
	public HashMap buildTemplateData() {
		final HashMap data = new HashMap();
		data.put("condition_mods", Global.CONDITION_MODS);
		data.put("combat_tracker", Global.USE_COMBAT_TRACKER);
		data.put("affirmative_tracker", Global.USE_AFFIRMATIVE_TRACKER);
		data.put("entity_links", Global.ENTITY_LINKS);
		return data;
	}

}
