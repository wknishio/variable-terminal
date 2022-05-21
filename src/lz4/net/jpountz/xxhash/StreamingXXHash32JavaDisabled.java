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
final class StreamingXXHash32JavaDisabled extends AbstractStreamingXXHash32Java {

	static class Factory implements StreamingXXHash32.Factory {

		public static final StreamingXXHash32.Factory INSTANCE = new Factory();

		public StreamingXXHash32 newStreamingHash(int seed) {
			return new StreamingXXHash32JavaDisabled(seed);
		}

	}

	StreamingXXHash32JavaDisabled(int seed) {
		super(seed);
	}

	public int getValue() {
		return 0;
	}

	public void update(byte[] buf, int off, int len) {

	}
}
