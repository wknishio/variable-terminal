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

import org.vash.vate.compatibility.VTArrays;

class HuffmanCompressionTableWorkspace
{
    public final NodeTable nodeTable = new NodeTable((2 * Huffman.MAX_SYMBOL_COUNT - 1)); // number of nodes in binary tree with MAX_SYMBOL_COUNT leaves

    public final short[] entriesPerRank = new short[Huffman.MAX_TABLE_LOG + 1];
    public final short[] valuesPerRank = new short[Huffman.MAX_TABLE_LOG + 1];

    // for setMaxHeight
    public final int[] rankLast = new int[Huffman.MAX_TABLE_LOG + 2];

    public void reset()
    {
        VTArrays.fill(entriesPerRank, (short) 0);
        VTArrays.fill(valuesPerRank, (short) 0);
    }
    
    public void resetFull()
    {
      nodeTable.reset();
      VTArrays.fill(entriesPerRank, (short) 0);
      VTArrays.fill(valuesPerRank, (short) 0);
      VTArrays.fill(rankLast, 0);
    }
}
