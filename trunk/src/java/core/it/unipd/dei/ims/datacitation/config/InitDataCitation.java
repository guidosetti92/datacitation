/*
 * Copyright 2015 University of Padua, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.unipd.dei.ims.datacitation.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 * This class loads the required properties
 * 
 * @author <a href="mailto:silvello@dei.unipd.it">Gianmaria Silvello</a>
 * @version 0.1
 * @since 0.1
 * 
 */
public class InitDataCitation {

	private Properties configProp = new Properties();

	private InputStream in;

	/**
	 * Load the properties.
	 * 
	 * @throws IOException
	 */
	public void loadProperties() throws IOException {
		URL url = this.getClass().getResource("/it/unipd/dei/ims/datacitation/config/dataCitation.properties");

		

		this.in = this.getClass().getResourceAsStream("/it/unipd/dei/ims/datacitation/config/dataCitation.properties");
		try {
			configProp.load(this.in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.in.close();
		}

	}

	public void loadProperties(String filename) throws IOException{
		/*
		System.out.println("resources/"+filename);
		this.in = this.getClass().getResourceAsStream("resources/"+filename);
		*/
		
		this.in = new FileInputStream("resources/"+filename);
		try {
			configProp.load(this.in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.in.close();
		}

	}

	public String getProperty(String property) {
		return configProp.getProperty(property);
	}

	public void setProperty(String property, String value) {
		configProp.setProperty(property, value);
	}

	public void saveProperties() throws IOException {

		URL url = this.getClass().getResource("/it/unipd/dei/ims/datacitation/config/dataCitation.properties");

		FileOutputStream fos = new FileOutputStream(url.getPath());

		configProp.store(fos, null);
		
		fos.flush();
		fos.close();
	}

	public void copyProperties(String destFile) throws IOException {
		URL url = this.getClass().getResource("/it/unipd/dei/ims/datacitation/config/dataCitation.properties");

		FileUtils.copyURLToFile(url, new File(destFile));
	}
}
