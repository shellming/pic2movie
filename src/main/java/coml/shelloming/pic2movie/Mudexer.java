package coml.shelloming.pic2movie;

import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by ruluo1992 on 2/14/2017.
 */
public class Mudexer {
    private SeekableByteChannel ch;
    private FramesMP4MuxerTrack outTrack;
    private int frameNo;
    private MP4Muxer muxer;
    private Size size;

    public Mudexer(File out) throws IOException {
        this.ch = NIOUtils.writableFileChannel(out);

        // Muxer that will store the encoded frames
        muxer = new MP4Muxer(ch, Brand.MP4);

        // Add video track to muxer
        outTrack = muxer.addTrack(TrackType.VIDEO, 1);
    }

    public void encodeImage(File png) throws IOException {
        if (size == null) {
            BufferedImage read = ImageIO.read(png);
            size = new Size(read.getWidth(), read.getHeight());
        }
        // Add packet to video track
        outTrack.addFrame(new MP4Packet(NIOUtils.fetchFrom(png), frameNo, 1, 2, frameNo, true, null, frameNo, 0));

        frameNo++;
    }

    public void finish() throws IOException {
        // Push saved SPS/PPS to a special storage in MP4
        outTrack.addSampleEntry(MP4Muxer.videoSampleEntry("png ", size, "JCodec"));

        // Write MP4 header and finalize recording
        muxer.writeHeader();
        NIOUtils.closeQuietly(ch);
    }
}
