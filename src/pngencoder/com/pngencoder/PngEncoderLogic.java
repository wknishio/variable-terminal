package com.pngencoder;

import com.pngencoder.PngEncoderIndexed.IndexedEncoderResult;
import com.pngencoder.PngEncoderScanlineUtil.AbstractPNGLineConsumer;

import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.vash.vate.compatibility.VTCharsets;
import org.vash.vate.compatibility.VTObjects;

class PngEncoderLogic {
    // In hex: 89 50 4E 47 0D 0A 1A 0A
    // This is the "file beginning" aka "header" aka "signature" aka "magicnumber".
    // https://en.wikipedia.org/wiki/Portable_Network_Graphics#File_header
    // http://www.libpng.org/pub/png/book/chapter08.html#png.ch08.div.2
    // All PNGs start this way and it does not include any pixel format info.
    static final byte[] FILE_BEGINNING = {-119, 80, 78, 71, 13, 10, 26, 10};

    // In hex: 00 00 00 00 49 45 4E 44 AE 42 60 82
    // This is the "file ending"
    static final byte[] FILE_ENDING = {0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};

    static final byte IHDR_COLOR_TYPE_GREY = 0;
    static final byte IHDR_COLOR_TYPE_RGB = 2;
    static final byte IHDR_COLOR_TYPE_INDEXED = 3;
    static final byte IHDR_COLOR_TYPE_GREY_ALPHA = 4;
    static final byte IHDR_COLOR_TYPE_RGBA = 6;
    static final byte IHDR_COMPRESSION_METHOD = 0;
    static final byte IHDR_FILTER_METHOD = 0;
    static final byte IHDR_INTERLACE_METHOD = 0;

    // Default values for the gAMA and cHRM chunks when an sRGB chunk is used,
    // as specified at http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html#C.sRGB
    //   "An application that writes the sRGB chunk should also write a gAMA chunk (and perhaps a cHRM chunk)
    //    for compatibility with applications that do not use the sRGB chunk.
    //    In this situation, only the following values may be used:
    //    ..."
    public static final byte[] GAMA_SRGB_VALUE = ByteBuffer.allocate(4).putInt(45455).array();
    public static final byte[] CHRM_SRGB_VALUE = ByteBuffer.allocate(8 * 4)
            .putInt(31270)
            .putInt(32900)
            .putInt(64000)
            .putInt(33000)
            .putInt(30000)
            .putInt(60000)
            .putInt(15000)
            .putInt(6000)
            .array();

    private PngEncoderLogic() {
    }

    
    private interface IDoWithDeflaterStream {
        void encodeImageData(boolean isMultithreaded, OutputStream out) throws IOException;
    }

    private static void encodeWithCompressorStream(boolean multiThreadedCompressionEnabled, int compressionLevel, int compressionStrategy, BufferedImage bufferedImage, PngEncoderScanlineUtil.EncodingMetaInfo metaInfo,
            OutputStream outputStream,
            IDoWithDeflaterStream action, Deflater deflater) throws IOException {
        int estimatedBytes = metaInfo.rowByteSize * bufferedImage.getHeight();
        final int segmentMaxLengthOriginal = PngEncoderDeflaterOutputStream.getSegmentMaxLengthOriginal(estimatedBytes);
        if (estimatedBytes <= segmentMaxLengthOriginal || !multiThreadedCompressionEnabled) {
            //Deflater deflater = new Deflater(compressionLevel, false);
            deflater.reset();
            deflater.setLevel(compressionLevel);;
            deflater.setStrategy(compressionStrategy);
            DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, deflater);
            action.encodeImageData(false, deflaterOutputStream);
            deflaterOutputStream.finish();
            deflaterOutputStream.flush();
            //deflater.finish();
        } else {
            PngEncoderDeflaterOutputStream deflaterOutputStream = new PngEncoderDeflaterOutputStream(
                    outputStream, compressionLevel, compressionStrategy, segmentMaxLengthOriginal);
            action.encodeImageData(true, deflaterOutputStream);
            deflaterOutputStream.finish();
        }
    }

    static int encode(final BufferedImage bufferedImage, OutputStream outputStream, int compressionLevel, int compressionStrategy,
            boolean multiThreadedCompressionEnabled, PngEncoderSrgbRenderingIntent srgbRenderingIntent,
            PngEncoderPhysicalPixelDimensions physicalPixelDimensions, boolean usePredictor, boolean tryIndexedEncoding, Deflater deflater) throws IOException {
        VTObjects.requireNonNull(bufferedImage, "bufferedImage");
        VTObjects.requireNonNull(outputStream, "outputStream");

        final PngEncoderScanlineUtil.EncodingMetaInfo metaInfo = PngEncoderScanlineUtil.getEncodingMetaInfo(bufferedImage);
        final int width = bufferedImage.getWidth();
        final int height = bufferedImage.getHeight();
        final PngEncoderCountingOutputStream countingOutputStream = new PngEncoderCountingOutputStream(outputStream);

        countingOutputStream.write(FILE_BEGINNING);

        IndexedEncoderResult indexedEncoderResult = null;
        if (bufferedImage.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            indexedEncoderResult = PngEncoderIndexed.encodeImageFromIndexed(bufferedImage, metaInfo);
        } else if (tryIndexedEncoding) {
            indexedEncoderResult = PngEncoderIndexed.encodeImage(bufferedImage, metaInfo);
        }
        
        final CRC32 crc = new CRC32();
        final byte[] ihdr = getIhdrHeader(width, height, metaInfo);
        final byte[] ihdrChunk = asChunk("IHDR", ihdr, crc);
        countingOutputStream.write(ihdrChunk);

        if (srgbRenderingIntent != null && metaInfo.colorProfile == null) {
            outputStream.write(asChunk("sRGB", new byte[]{srgbRenderingIntent.getValue()}, crc));
            outputStream.write(asChunk("gAMA", GAMA_SRGB_VALUE, crc));
            outputStream.write(asChunk("cHRM", CHRM_SRGB_VALUE, crc));
        }

        if (physicalPixelDimensions != null) {
            outputStream.write(asChunk("pHYs", getPhysicalPixelDimensions(physicalPixelDimensions), crc));
        }

        if (metaInfo.colorProfile != null) {
            byte[] iCCP = getICCP(metaInfo.colorProfile);
            outputStream.write(asChunk("iCCP", iCCP, crc));
        }

        PngEncoderIdatChunksOutputStream idatChunksOutputStream = new PngEncoderIdatChunksOutputStream(
                countingOutputStream);
        if (indexedEncoderResult != null) {
            outputStream.write(asChunk("PLTE", indexedEncoderResult.colorTable, crc));
            if (indexedEncoderResult.transparencyTable != null) {
                outputStream.write(asChunk("tRNS", indexedEncoderResult.transparencyTable, crc));
            }
            final byte[] rawIDAT = indexedEncoderResult.rawIDAT;
            encodeWithCompressorStream(multiThreadedCompressionEnabled, compressionLevel, compressionStrategy, bufferedImage, metaInfo, idatChunksOutputStream, new IDoWithDeflaterStream() {
              public void encodeImageData(boolean isMultithreaded, OutputStream out) throws IOException {
                  out.write(rawIDAT);
              }
            }, deflater);
        } else {
            if (usePredictor) {
                encodeWithCompressorStream(multiThreadedCompressionEnabled, compressionLevel, compressionStrategy, bufferedImage, metaInfo, idatChunksOutputStream, new IDoWithDeflaterStream() {
                  public void encodeImageData(boolean isMultithreaded, OutputStream out) throws IOException {
                      if (isMultithreaded) {
                          PngEncoderPredictor.encodeImageMultiThreaded(bufferedImage, metaInfo, out);
                      } else {
                          PngEncoderPredictor.encodeImageSingleThreaded(bufferedImage, metaInfo, out);
                      }
                  }
                }, deflater);
            } else {
                encodeWithCompressorStream(multiThreadedCompressionEnabled, compressionLevel, compressionStrategy, bufferedImage, metaInfo, idatChunksOutputStream, new IDoWithDeflaterStream() {
                  public void encodeImageData(boolean isMultithreaded, final OutputStream out) throws IOException {
                      PngEncoderScanlineUtil.stream(bufferedImage, 0, bufferedImage.getHeight(), new AbstractPNGLineConsumer() {
                          @Override
                          void consume(byte[] currRow, byte[] prevRow) throws IOException {
                              out.write(currRow);
                          }
                      });
                  }
                }, deflater);
            }
        }
        countingOutputStream.write(FILE_ENDING);

        countingOutputStream.flush();

        return countingOutputStream.getCount();
    }

    private static byte[] getICCP(ICC_Profile colorProfile) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Write the profile name
        String name = getProfileName(colorProfile);
        byte[] nameBytes = name.getBytes(VTCharsets.ISO_8859_1);
        int nameByteWriteLen = Math.min(nameBytes.length, 79);
        out.write(nameBytes, 0, nameByteWriteLen);
        out.write(0);

        // ZLib Compression
        out.write(IHDR_COMPRESSION_METHOD);

        // ICC Profile Data
          DeflaterOutputStream compressStream = new DeflaterOutputStream(out);
            compressStream.write(colorProfile.getData());
            compressStream.close();
        
        return out.toByteArray();
    }

    private static String getProfileName(ICC_Profile profile) throws UnsupportedEncodingException {
        byte[] data = profile.getData(ICC_Profile.icSigProfileDescriptionTag);
        if (data == null) {
            return "<noname>";
        }
        int startIdx = 12;
        int endIdx = startIdx;
        while (endIdx < data.length) {
            int c = data[endIdx];
            if (c == 0) {
                break;
            }
            endIdx++;
        }
        if (endIdx != startIdx) {
            return new String(data, startIdx, endIdx - startIdx, VTCharsets.US_ASCII);
        }

        /*
         * Unicode Format ... this is special to parse...
         */
        endIdx = startIdx = 29;
        while (endIdx + 1 < data.length) {
            int c = (data[endIdx] & 0xff) + (data[endIdx] & 0xFF);
            if (c == 0) {
                break;
            }
            endIdx += 2;
        }
        if (endIdx != startIdx) {
            return new String(data, startIdx, endIdx - startIdx, VTCharsets.UTF_16LE);
        }
        return "<unnamed>";
    }

    static byte[] getIhdrHeader(int width, int height, PngEncoderScanlineUtil.EncodingMetaInfo metaInfo) {
        ByteBuffer buffer = ByteBuffer.allocate(13);
        buffer.putInt(width);
        buffer.putInt(height);
        buffer.put((byte) metaInfo.bitsPerChannel);
        switch (metaInfo.colorSpaceType) {
            case Rgb:
                buffer.put(metaInfo.hasAlpha ? IHDR_COLOR_TYPE_RGBA : IHDR_COLOR_TYPE_RGB);
                break;
            case Gray:
                buffer.put(metaInfo.hasAlpha ? IHDR_COLOR_TYPE_GREY_ALPHA : IHDR_COLOR_TYPE_GREY);
                break;
            case Indexed:
                buffer.put(IHDR_COLOR_TYPE_INDEXED);
                break;
        }
        buffer.put(IHDR_COMPRESSION_METHOD);
        buffer.put(IHDR_FILTER_METHOD);
        buffer.put(IHDR_INTERLACE_METHOD);
        return buffer.array();
    }

    static byte[] getPhysicalPixelDimensions(PngEncoderPhysicalPixelDimensions physicalPixelDimensions) {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.putInt(physicalPixelDimensions.getPixelsPerUnitX());
        buffer.putInt(physicalPixelDimensions.getPixelsPerUnitY());
        buffer.put(physicalPixelDimensions.getUnit().getValue());
        return buffer.array();
    }

    static byte[] asChunk(String type, byte[] data, CRC32 crc) throws UnsupportedEncodingException {
        PngEncoderVerificationUtil.verifyChunkType(type);
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length + 12);
        byteBuffer.putInt(data.length);
        ByteBuffer byteBufferForCrc = byteBuffer.slice().asReadOnlyBuffer();
        byteBufferForCrc.limit(4 + data.length);
        byteBuffer.put(type.getBytes(VTCharsets.US_ASCII));
        byteBuffer.put(data);
        byteBuffer.putInt(getCrc32(byteBufferForCrc, crc));
        return byteBuffer.array();
    }

    static int getCrc32(ByteBuffer byteBuffer, CRC32 crc) {
        //CRC32 crc = new CRC32();
        crc.reset();
        int pos = byteBuffer.position();
        int limit = byteBuffer.limit();
        //assert (pos <= limit);
        int rem = limit - pos;
        //crc.update(byteBuffer);
        byte[] b = new byte[rem];
        byteBuffer.get(b);
        crc.update(b, 0, b.length);
        return (int) crc.getValue();
    }
}
