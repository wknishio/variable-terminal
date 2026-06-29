/*
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
package org.vash.vate.io.airlift.compress.zstd;

import static org.vash.vate.io.airlift.compress.zstd.ZstdUtil.checkState;

import org.vash.vate.compatibility.VTObjects;

import static java.lang.Math.min;

class FrameHeader
{
    final long headerSize;
    final int windowSize;
    final long contentSize;
    final long dictionaryId;
    final boolean hasChecksum;

    public FrameHeader(long headerSize, int windowSize, long contentSize, long dictionaryId, boolean hasChecksum)
    {
        checkState(windowSize >= 0 || contentSize >= 0, "Invalid frame header: contentSize or windowSize must be set");
        this.headerSize = headerSize;
        this.windowSize = windowSize;
        this.contentSize = contentSize;
        this.dictionaryId = dictionaryId;
        this.hasChecksum = hasChecksum;
    }

    public int computeRequiredOutputBufferLookBackSize()
    {
        if (contentSize < 0) {
            return windowSize;
        }
        if (windowSize < 0) {
            return (int) (contentSize);
        }
        return (int) (min(windowSize, contentSize));
    }

    
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FrameHeader that = (FrameHeader) o;
        return headerSize == that.headerSize &&
                windowSize == that.windowSize &&
                contentSize == that.contentSize &&
                dictionaryId == that.dictionaryId &&
                hasChecksum == that.hasChecksum;
    }

    
    public int hashCode()
    {
        return VTObjects.hash(headerSize, windowSize, contentSize, dictionaryId, hasChecksum);
    }

    
    public String toString()
    {
        return new StringBuilder("class=" + FrameHeader.class.getSimpleName())
                .append(",headerSize=" + headerSize)
                .append(",windowSize=" + windowSize)
                .append(",contentSize=" + contentSize)
                .append(",dictionaryId=" + dictionaryId)
                .append(",hasChecksum=" + hasChecksum)
                .toString();
    }
}
