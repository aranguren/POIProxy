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

package es.alrocar.poiproxy.servlet;

import java.util.ArrayList;
import java.util.HashMap;

import org.restlet.resource.ServerResource;

import es.alrocar.poiproxy.configuration.Param;

public abstract class BaseServerResource extends ServerResource {	

	public ArrayList<Param> extractParams(HashMap<String, String> params) {
		final ArrayList<String> optionalParams = this.getOptionalParamsNames();

		ArrayList<Param> extractedParams = new ArrayList<Param>();

		String p;
		Param optParam;
		for (String paramName : optionalParams) {
			p = params.get(paramName);

			if (p != null) {
				optParam = new Param(paramName, p);
				extractedParams.add(optParam);
			}
		}

		return extractedParams;
	}

	public abstract ArrayList<String> getOptionalParamsNames();
}
