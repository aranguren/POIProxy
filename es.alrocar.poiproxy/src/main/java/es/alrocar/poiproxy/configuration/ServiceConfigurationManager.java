/* POIProxy. A proxy service to retrieve POIs from public services
 *
 * Copyright (C) 2011 Alberto Romeu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * For more information, contact:
 *
 *   Prodevelop, S.L.
 *   Pza. Don Juan de Villarrasa, 14 - 5
 *   46001 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   aromeu@prodevelop.es
 *   http://www.prodevelop.es
 *   
 *   2011.
 *   author Alberto Romeu aromeu@prodevelop.es  
 *   
 */

package es.alrocar.poiproxy.configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import es.alrocar.jpe.parser.configuration.DescribeServiceParser;
import es.prodevelop.gvsig.mini.utiles.Constants;

/**
 * This class is used to register into the library al the json documents
 * available that describe available services to request and parse responses
 * 
 * It takes all the services at
 * {@link ServiceConfigurationManager#CONFIGURATION_DIR} parses the json
 * documents through {@link DescribeServiceParser} and registers each file as a
 * {@link DescribeService} taking as the id the name of the json file
 * 
 * So adding a new service to the library implies write its json document
 * describing its {@link RequestType} and {@link FeatureType} and putting it
 * into the {@link #CONFIGURATION_DIR}
 * 
 * 
 * @author albertoromeu
 * 
 */
public class ServiceConfigurationManager {

	public static String CONFIGURATION_DIR = "/var/lib/services";

	private HashMap<String, String> registeredConfigurations = new HashMap<String, String>();
	private HashMap<String, DescribeService> parsedConfigurations = new HashMap<String, DescribeService>();
	private DescribeServiceParser parser = new DescribeServiceParser();

	/**
	 * A map of ids of the registered services
	 * 
	 * @return
	 */
	public HashMap<String, String> getRegisteredConfigurations() {
		return registeredConfigurations;
	}

	/**
	 * The constructor.
	 * 
	 * Internally calls {@link #loadConfiguration()}
	 */
	public ServiceConfigurationManager() {
		this.loadConfiguration();
	}

	/**
	 * Iterates the files at {@link #CONFIGURATION_DIR} and calls
	 * {@link #registerServiceConfiguration(String, String)} for each file found
	 * setting as the id of the service the file name
	 */
	public void loadConfiguration() {
		System.out.println("CONFIGURATION_DIR: " + CONFIGURATION_DIR);
		File f = new File(CONFIGURATION_DIR);

		if (f.isDirectory()) {
			String[] files = f.list();
			for (String s : files) {
				if (s.endsWith(".json")) {
					System.out.println("Registering: " + s.toLowerCase());
					this.registerServiceConfiguration(s.split(".json")[0], s);
				}
			}
		}
	}

	/**
	 * Registers a new service into the library
	 * 
	 * @param id
	 *            The id of the service
	 * @param configFile
	 *            The content of the json document describing the service
	 */
	public void registerServiceConfiguration(String id, String configFile) {
		this.registeredConfigurations.put(id, configFile);
	}

	/**
	 * Returns a {@link DescribeService} given an id. If the service has not
	 * been used previously, this method parses the json document describing the
	 * service
	 * 
	 * @param id
	 *            The id of the service. Usually the name of the json document
	 * @return The {@link DescribeService}
	 */
	public DescribeService getServiceConfiguration(String id) {
		// Buscar los servicios registrados
		DescribeService service = this.parsedConfigurations.get(id);

		if (service == null) {
			String res = this.getServiceAsJSON(id);
			service = parser.parse(res);
			this.parsedConfigurations.put(id, service);
		}

		return service;
	}

	/**
	 * Returns the content of the json document describing a service given its
	 * id
	 * 
	 * @param id
	 *            The id of the service. Usually the name of the json document
	 * @return The content of the json document
	 */
	public String getServiceAsJSON(String id) {
		String path = this.registeredConfigurations.get(id);

		if (path == null) {
			return null;
		}

		// cargar fichero de disco obtener el json y parsearlo
		File f = new File(CONFIGURATION_DIR + File.separator + path);
		FileInputStream fis = null;
		InputStream in = null;
		OutputStream out = null;
		String res = null;
		try {
			fis = new FileInputStream(f);
			in = new BufferedInputStream(fis, Constants.IO_BUFFER_SIZE);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, Constants.IO_BUFFER_SIZE);
			byte[] b = new byte[8 * 1024];
			int read;
			int total = 0;
			while ((read = in.read(b)) != -1) {
				total += read;
				out.write(b, 0, read);
			}
			out.flush();
			res = new String(dataStream.toByteArray());

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			Constants.closeStream(fis);
			Constants.closeStream(in);
			Constants.closeStream(out);
		}

		return res;
	}

	/**
	 * not used at the moment
	 * 
	 * @param url
	 * @return
	 */
	public DescribeService getRemoteConfiguration(String url) {
		// Descargar json de la url y parsearlo
		return null;
	}

	/**
	 * list resources available from the classpath
	 * 
	 * @author stoughto
	 * 
	 */
	public static class ResourceList {
		/**
		 * for all elements of java.class.path get a Collection of resources
		 * Pattern pattern = Pattern.compile(".*"); gets all resources
		 * 
		 * @param pattern
		 *            the pattern to match
		 * @return the resources in the order they are found
		 */
		public static Collection<String> getResources(Pattern pattern) {
			ArrayList<String> retval = new ArrayList<String>();
			String classPath = System.getProperty("java.class.path", ".");
			String[] classPathElements = classPath.split(":");
			for (String element : classPathElements) {
				retval.addAll(getResources(element, pattern));
			}
			return retval;
		}

		private static Collection<String> getResources(String element,
				Pattern pattern) {
			ArrayList<String> retval = new ArrayList<String>();
			File file = new File(element);
			if (file.isDirectory()) {
				retval.addAll(getResourcesFromDirectory(file, pattern));
			} else {
				// retval.addAll(getResourcesFromJarFile(file, pattern));
			}
			return retval;
		}

		private static Collection<String> getResourcesFromJarFile(File file,
				Pattern pattern) {
			ArrayList<String> retval = new ArrayList<String>();
			ZipFile zf;
			try {
				zf = new ZipFile(file);
			} catch (ZipException e) {
				throw new Error(e);
			} catch (IOException e) {
				throw new Error(e);
			}
			Enumeration e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				String fileName = ze.getName();
				boolean accept = pattern.matcher(fileName).matches();
				if (accept) {
					retval.add(fileName);
				}
			}
			try {
				zf.close();
			} catch (IOException e1) {
				throw new Error(e1);
			}
			return retval;
		}

		private static Collection<String> getResourcesFromDirectory(
				File directory, Pattern pattern) {
			ArrayList<String> retval = new ArrayList<String>();
			File[] fileList = directory.listFiles();
			for (File file : fileList) {
				if (file.isDirectory()) {
					retval.addAll(getResourcesFromDirectory(file, pattern));
				} else {
					try {
						String fileName = file.getCanonicalPath();
						boolean accept = pattern.matcher(fileName).matches();
						if (accept) {
							retval.add(fileName);
						}
					} catch (IOException e) {
						throw new Error(e);
					}
				}
			}
			return retval;
		}

		/**
		 * list the resources that match args[0]
		 * 
		 * @param args
		 *            args[0] is the pattern to match, or list all resources if
		 *            there are no args
		 */
		public static void main(String[] args) {
			Pattern pattern;
			if (args.length < 1) {
				pattern = Pattern.compile(".json");
			} else {
				pattern = Pattern.compile(args[0]);
			}
			Collection<String> list = ResourceList.getResources(pattern);
			for (String name : list) {
				System.out.println(name);
			}

		}
	}

}
