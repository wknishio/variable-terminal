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
package vate.io.airlift.compress.zstd;

import java.io.IOException;
import java.io.OutputStream;

import vate.io.airlift.compress.hadoop.HadoopOutputStream;

import static org.vash.vate.compatibility.VTObjects.requireNonNull;

public class ZstdHadoopOutputStream
        extends HadoopOutputStream
{
    private final OutputStream out;
    //private boolean initialized;
    private ZstdOutputStream zstdOutputStream;
    private boolean small;

    public ZstdHadoopOutputStream(OutputStream out, boolean small)
    {
        this.out = requireNonNull(out, "out is null");
        this.small = small;
    }

    @Override
    public void write(int b)
            throws IOException
    {
        openStreamIfNecessary();
        zstdOutputStream.write(b);
    }

    @Override
    public void write(byte[] buffer, int offset, int length)
            throws IOException
    {
        openStreamIfNecessary();
        zstdOutputStream.write(buffer, offset, length);
    }

    @Override
    public void finish()
            throws IOException
    {
        if (zstdOutputStream != null) {
          zstdOutputStream.flush();
            //zstdOutputStream.finishWithoutClosingSource();
            //zstdOutputStream = null;
        }
    }

    @Override
    public void flush()
            throws IOException
    {
      zstdOutputStream.flush();
    }

    @Override
    public void close()
            throws IOException
    {
        out.close();
    }

    private void openStreamIfNecessary()
            throws IOException
    {
        if (zstdOutputStream == null) {
            //initialized = true;
            zstdOutputStream = new ZstdOutputStream(out, small);
        }
    }
    
    public void resetState()
    {
      if (zstdOutputStream != null)
      {
        zstdOutputStream.resetState();
      }
    }
}
