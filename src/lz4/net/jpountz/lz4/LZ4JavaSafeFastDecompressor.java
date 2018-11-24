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

package net.jpountz.lz4;

import static net.jpountz.lz4.LZ4Constants.*;

import java.nio.ByteBuffer;

import net.jpountz.util.ByteBufferUtils;
import net.jpountz.util.SafeUtils;

/**
 * Decompressor.
 */
final class LZ4JavaSafeFastDecompressor extends LZ4FastDecompressor {

	public static final LZ4FastDecompressor INSTANCE = new LZ4JavaSafeFastDecompressor();

	public int decompress(byte[] src, final int srcOff, byte[] dest, final int destOff, int destLen) {

		SafeUtils.checkRange(src, srcOff);
		SafeUtils.checkRange(dest, destOff, destLen);

		if (destLen == 0) {
			if (SafeUtils.readByte(src, srcOff) != 0) {
				throw new LZ4Exception("Malformed input at " + srcOff);
			}
			return 1;
		}

		final int destEnd = destOff + destLen;

		int sOff = srcOff;
		int dOff = destOff;

		while (true) {
			final int token = SafeUtils.readByte(src, sOff) & 0xFF;
			++sOff;

			// literals
			int literalLen = token >>> ML_BITS;
			if (literalLen == RUN_MASK) {
				byte len = (byte) 0xFF;
				while ((len = SafeUtils.readByte(src, sOff++)) == (byte) 0xFF) {
					literalLen += 0xFF;
				}
				literalLen += len & 0xFF;
			}

			final int literalCopyEnd = dOff + literalLen;

			if (literalCopyEnd > destEnd - COPY_LENGTH) {
				if (literalCopyEnd != destEnd) {
					throw new LZ4Exception("Malformed input at " + sOff);

				} else {
					LZ4SafeUtils.safeArraycopy(src, sOff, dest, dOff, literalLen);
					sOff += literalLen;
					dOff = literalCopyEnd;
					break; // EOF
				}
			}

			LZ4SafeUtils.wildArraycopy(src, sOff, dest, dOff, literalLen);
			sOff += literalLen;
			dOff = literalCopyEnd;

			// matchs
			final int matchDec = SafeUtils.readShortLE(src, sOff);
			sOff += 2;
			int matchOff = dOff - matchDec;

			if (matchOff < destOff) {
				throw new LZ4Exception("Malformed input at " + sOff);
			}

			int matchLen = token & ML_MASK;
			if (matchLen == ML_MASK) {
				byte len = (byte) 0xFF;
				while ((len = SafeUtils.readByte(src, sOff++)) == (byte) 0xFF) {
					matchLen += 0xFF;
				}
				matchLen += len & 0xFF;
			}
			matchLen += MIN_MATCH;

			final int matchCopyEnd = dOff + matchLen;

			if (matchCopyEnd > destEnd - COPY_LENGTH) {
				if (matchCopyEnd > destEnd) {
					throw new LZ4Exception("Malformed input at " + sOff);
				}
				LZ4SafeUtils.safeIncrementalCopy(dest, matchOff, dOff, matchLen);
			} else {
				LZ4SafeUtils.wildIncrementalCopy(dest, matchOff, dOff, matchCopyEnd);
			}
			dOff = matchCopyEnd;
		}

		return sOff - srcOff;

	}

	public int decompress(ByteBuffer src, final int srcOff, ByteBuffer dest, final int destOff, int destLen) {

		if (src.hasArray() && dest.hasArray()) {
			return decompress(src.array(), srcOff + src.arrayOffset(), dest.array(), destOff + dest.arrayOffset(),
					destLen);
		}
		src = ByteBufferUtils.inNativeByteOrder(src);
		dest = ByteBufferUtils.inNativeByteOrder(dest);

		ByteBufferUtils.checkRange(src, srcOff);
		ByteBufferUtils.checkRange(dest, destOff, destLen);

		if (destLen == 0) {
			if (ByteBufferUtils.readByte(src, srcOff) != 0) {
				throw new LZ4Exception("Malformed input at " + srcOff);
			}
			return 1;
		}

		final int destEnd = destOff + destLen;

		int sOff = srcOff;
		int dOff = destOff;

		while (true) {
			final int token = ByteBufferUtils.readByte(src, sOff) & 0xFF;
			++sOff;

			// literals
			int literalLen = token >>> ML_BITS;
			if (literalLen == RUN_MASK) {
				byte len = (byte) 0xFF;
				while ((len = ByteBufferUtils.readByte(src, sOff++)) == (byte) 0xFF) {
					literalLen += 0xFF;
				}
				literalLen += len & 0xFF;
			}

			final int literalCopyEnd = dOff + literalLen;

			if (literalCopyEnd > destEnd - COPY_LENGTH) {
				if (literalCopyEnd != destEnd) {
					throw new LZ4Exception("Malformed input at " + sOff);

				} else {
					LZ4ByteBufferUtils.safeArraycopy(src, sOff, dest, dOff, literalLen);
					sOff += literalLen;
					dOff = literalCopyEnd;
					break; // EOF
				}
			}

			LZ4ByteBufferUtils.wildArraycopy(src, sOff, dest, dOff, literalLen);
			sOff += literalLen;
			dOff = literalCopyEnd;

			// matchs
			final int matchDec = ByteBufferUtils.readShortLE(src, sOff);
			sOff += 2;
			int matchOff = dOff - matchDec;

			if (matchOff < destOff) {
				throw new LZ4Exception("Malformed input at " + sOff);
			}

			int matchLen = token & ML_MASK;
			if (matchLen == ML_MASK) {
				byte len = (byte) 0xFF;
				while ((len = ByteBufferUtils.readByte(src, sOff++)) == (byte) 0xFF) {
					matchLen += 0xFF;
				}
				matchLen += len & 0xFF;
			}
			matchLen += MIN_MATCH;

			final int matchCopyEnd = dOff + matchLen;

			if (matchCopyEnd > destEnd - COPY_LENGTH) {
				if (matchCopyEnd > destEnd) {
					throw new LZ4Exception("Malformed input at " + sOff);
				}
				LZ4ByteBufferUtils.safeIncrementalCopy(dest, matchOff, dOff, matchLen);
			} else {
				LZ4ByteBufferUtils.wildIncrementalCopy(dest, matchOff, dOff, matchCopyEnd);
			}
			dOff = matchCopyEnd;
		}

		return sOff - srcOff;

	}

}
