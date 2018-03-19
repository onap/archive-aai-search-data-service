/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.sa.searchdbabstraction.entity;

import java.util.Arrays;

public class SuggestHits {

	private String totalHits;
	private SuggestHit[] hits;

	public String getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(String totalHits) {
		this.totalHits = totalHits;
	}

	public SuggestHit[] getHits() {
		return hits;
	}

	public void setHits(SuggestHit[] hits) {
		this.hits = hits;
	}

	@Override
	public String toString() {
		return "SuggestHit [totalHits=" + totalHits + ", hits=" + Arrays.toString(hits) + "]";
	}
}