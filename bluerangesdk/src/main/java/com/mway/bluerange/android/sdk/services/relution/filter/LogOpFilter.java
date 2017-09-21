/**
 * LogOpFilter.java
 * gofer-filter-api-domain
 *
 * Created by Undefined on 5.3.2013
 * Copyright (c)
 * 2013
 * M-Way Solutions GmbH. All rights reserved.
 * http://www.mwaysolutions.com
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.mway.bluerange.android.sdk.services.relution.filter;

import org.json.JSONArray;
import org.json.JSONObject;

public class LogOpFilter extends Filter {

	public static enum Operation { AND, OR, NAND, NOR }

	private Operation operation;
	private Filter[] filters;

	public LogOpFilter(final Operation operation, final Filter... filters) {
		this.operation = operation;
		this.filters = filters;
	}

	@Override
	public JSONObject toJson() {
		JSONObject filter = new JSONObject();
		try {

			filter.put("type", "logOp");
			filter.put("operation", operation.toString());

			JSONArray jsonFilters = new JSONArray();
			for(int i=0; i<filters.length; i++){
				jsonFilters.put(filters[i].toJson());
			}

			filter.put("filters", jsonFilters);

		} catch (Exception e) {

		}
		return filter;
	}
}
