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
package io.airlift.compress.zstd;

import io.airlift.compress.hadoop.HadoopInputStream;
import io.airlift.compress.hadoop.HadoopOutputStream;
import io.airlift.compress.hadoop.HadoopStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static java.util.Collections.singletonList;

public class ZstdHadoopStreams
        implements HadoopStreams
{
    
    public String getDefaultFileExtension()
    {
        return ".zst";
    }

    
    public List<String> getHadoopCodecName()
    {
        return singletonList("org.apache.hadoop.io.compress.ZStandardCodec");
    }

    
    public HadoopInputStream createInputStream(InputStream in)
            throws IOException
    {
        return new ZstdHadoopInputStream(in);
    }

    
    public HadoopOutputStream createOutputStream(OutputStream out)
            throws IOException
    {
        return new ZstdHadoopOutputStream(out);
    }
}