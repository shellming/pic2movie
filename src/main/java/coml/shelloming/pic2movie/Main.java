package coml.shelloming.pic2movie;

import org.jcodec.api.awt.SequenceEncoder;
import org.jcodec.api.awt.SequenceMuxer;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.Codec;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.common.model.Size;
import org.jcodec.common.tools.MainUtils;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.movtool.streaming.tracks.avc.AVCClipTrack;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ruluo1992 on 2/14/2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {
//        FileChannelWrapper out = null;
//        out = NIOUtils.writableFileChannel("D:\\Downloads\\out.mp4");
//        MP4Muxer muxer = new MP4Muxer(out, Brand.MP4);
//        ByteBuffer _out = ByteBuffer.allocate(1920 * 1080 * 6);
//        H264Encoder encoder = new H264Encoder();
//        Transform transform = ColorUtil.getTransform(ColorSpace.RGB, encoder.getSupportedColorSpaces()[0]);
//
//        int width = 800;
//        int height = 600;
//        Picture toEncode = Picture.create(width, height, encoder.getSupportedColorSpaces()[0]);
////        MuxerTrack outTrack = muxer.addVideoTrack(Codec.H264, new Size(width, height))

//        Mudexer sequenceMuxer = new Mudexer(new File("D:\\Downloads\\out.mp4"));
//        sequenceMuxer.encodeImage(new File("D:\\Downloads\\1.png"));
//        sequenceMuxer.encodeImage(new File("D:\\Downloads\\2.png"));
//        sequenceMuxer.encodeImage(new File("D:\\Downloads\\3.png"));
//        sequenceMuxer.finish();
        CustomSequenceEncoder encoder = new CustomSequenceEncoder(new File("D:\\Downloads\\out.mp4"));
        for (int i = 0; i < 100; i++) {
            encoder.encodeImage(ImageIO.read(new File("D:\\Downloads\\1.png")));
        }
        for (int i = 0; i < 100; i++) {
            encoder.encodeImage(ImageIO.read(new File("D:\\Downloads\\2.png")));
        }
        for (int i = 0; i < 100; i++) {
            encoder.encodeImage(ImageIO.read(new File("D:\\Downloads\\3.png")));
        }
        encoder.finish();
    }
}
