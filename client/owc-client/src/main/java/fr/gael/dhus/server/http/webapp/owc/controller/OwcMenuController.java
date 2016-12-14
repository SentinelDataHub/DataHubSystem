/*
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.server.http.webapp.owc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;






import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;






import fr.gael.dhus.server.http.webapp.owc.data.MenuData;
import fr.gael.dhus.server.http.webapp.owc.data.MenuSectionsData;


@RestController
public class OwcMenuController {
	private static Log logger = LogFactory.getLog(OwcMenuController.class);

	@RequestMapping(value = "/settings/menu", method = RequestMethod.GET)
	public ResponseEntity<?> getMenu() throws JSONException {
		URL configFile = ClassLoader.getSystemResource("../etc/conf/menu.json");
		if (configFile != null) {
			logger.debug("Loading configuration file " + configFile.getPath());

			try {

				File file = new File(configFile.getPath());
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line = "";
				StringBuffer sb = new StringBuffer();
				while ((line = bufferedReader.readLine()) != null) {
					sb.append(line);
				}
				bufferedReader.close();
				JSONObject menu = new JSONObject(sb.toString());				
				return new ResponseEntity<>(menu.toString(), HttpStatus.OK);
			} catch (IOException e) {

				logger.error(" Cannot load menu configration file content");
				e.printStackTrace();
				return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
						HttpStatus.UNAUTHORIZED);
			}
		} else {
			logger.error(" Cannot get menu configration file ");
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.UNAUTHORIZED);
		}

	}

	@PreAuthorize("isAuthenticated () AND hasRole('ROLE_DATA_MANAGER')")
	@RequestMapping(value = "/settings/menu", method = RequestMethod.PUT)
	public ResponseEntity<?> setMenu(@RequestBody MenuData body)
			throws JSONException {		
		
		URL configFile = ClassLoader.getSystemResource("../etc/conf/menu.json");
		if (configFile != null && body != null) {
			logger.debug("Loading configuration file " + configFile.getPath());

			try {

				PrintWriter fileWriter = new PrintWriter(new FileOutputStream(
						configFile.getPath(), false));
				fileWriter.println("{\"sections\":[");
				for (MenuSectionsData section : body.getSections()) {
					fileWriter.println("{");
					if (section.getTitle() != null
							&& !section.getTitle().isEmpty())
						fileWriter.println("\"title\":\"" + section.getTitle()
								+ "\",");
					if (section.getIcon() != null
							&& !section.getIcon().isEmpty())
						fileWriter.println("\"icon\":\"" + section.getIcon()
								+ "\",");
					if (section.getComponent() != null
							&& !section.getComponent().isEmpty())
						fileWriter.println("\"component\":\""
								+ section.getComponent() + "\",");
					if (section.getWidth() != null
							&& !section.getWidth().isEmpty())
						fileWriter.println("\"width\":\"" + section.getWidth()
								+ "\",");
					if (section.getRemoteUrl() != null
							&& !section.getRemoteUrl().isEmpty())
						fileWriter.println("\"remoteUrl\":\""
								+ section.getRemoteUrl() + "\"");
					else
						fileWriter.println("\"remoteUrl\":\"\"");
					fileWriter.println("},");

				}
				fileWriter.println("]}");
				fileWriter.close();

				return new ResponseEntity<>("{\"code\":\"success\"}", HttpStatus.OK);
			} catch (IOException e) {

				logger.error(" Cannot write menu configration file ");
				e.printStackTrace();
				return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
						HttpStatus.UNAUTHORIZED);
			}			
		} else {
			logger.error(" Cannot get menu configration file ");
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.UNAUTHORIZED);
		}

	}

}
