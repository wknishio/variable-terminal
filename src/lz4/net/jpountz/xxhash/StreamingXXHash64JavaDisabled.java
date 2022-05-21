/*******************************************************************************
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
 *******************************************************************************/
// Auto-generated: DO NOT EDIT

package net.jpountz.xxhash;

/**
 * Streaming xxhash.
 */
final class StreamingXXHash64JavaDisabled extends AbstractStreamingXXHash64Java {

	static class Factory implements StreamingXXHash64.Factory {

		public static final StreamingXXHash64.Factory INSTANCE = new Factory();

		public StreamingXXHash64 newStreamingHash(long seed) {
			return new StreamingXXHash64JavaDisabled(seed);
		}

	}

	StreamingXXHash64JavaDisabled(long seed) {
		super(seed);
	}

	public long getValue() {
		return 0;
	}

	public void update(byte[] buf, int off, int len) {
		return;
	}

}
