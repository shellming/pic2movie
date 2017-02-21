package coml.shelloming.pic2movie;

import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * Created by ruluo1992 on 2/21/2017.
 */
public class HumbleEncoder {
    private final int FPS = 25;

    private int frameNo;
    private MediaPictureConverter converter = null;
    private MediaPicture picture = null;
    // We are going to use 420P as the format because that's what most video formats these days use
    private final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
    private Rational framerate = Rational.make(1, FPS);  // 分子、分母
    private Encoder encoder;
    private Muxer muxer;
    private MediaPacket packet = MediaPacket.make();
    private BufferedImage lastImg;
    public HumbleEncoder(String filename) {
        frameNo = 0;
        muxer = Muxer.make(filename, null, null);
        MuxerFormat format = muxer.getFormat();
        Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
        encoder = Encoder.make(codec);
        encoder.setPixelFormat(pixelformat);
        encoder.setTimeBase(framerate);
        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
        }
    }

    public void encodeImgWithFadeIn(BufferedImage bi, int len) throws IOException, InterruptedException {
        if (lastImg != null) {
            for (int i = 0; i < FPS; i++) {
                BufferedImage img = combine((byte) ((255 / FPS) * i), bi, lastImg);
                encodeImage(img, 1);
            }
        }
        lastImg = bi;
        encodeImage(bi, len);
    }

    public BufferedImage combine(byte alpha, BufferedImage img1, BufferedImage img2) {
        img1 = deepCopy(img1);
        img2 = deepCopy(img2);
        img1 = convertToType(img1, BufferedImage.TYPE_INT_ARGB);
        img2 = convertToType(img2, BufferedImage.TYPE_INT_ARGB);
        byte alpha1 = alpha;
        byte alpha2 = (byte) (255 - alpha1);
        setAlpha(alpha1, img1);
        setAlpha(alpha2, img2);

        Graphics2D g2 = img1.createGraphics();
        g2.drawImage(img1, 0, 0, null);
        g2.drawImage(img2, 0, 0, null);
        g2.dispose();

        return img1;
    }

    public void encodeImage(BufferedImage bi, int len) throws IOException, InterruptedException {
        if (picture == null) {
            picture = MediaPicture.make(
                    bi.getWidth(),
                    bi.getHeight(),
                    pixelformat);
            picture.setTimeBase(framerate);
            encoder.setWidth(bi.getWidth());
            encoder.setHeight(bi.getHeight());
            encoder.open(null, null);
            muxer.addNewStream(encoder);
            muxer.open(null, null);
        }
        final BufferedImage screen = convertToType(bi, BufferedImage.TYPE_3BYTE_BGR);
        if (converter == null) {
            converter = MediaPictureConverterFactory.createConverter(screen, picture);
        }

        for (int i = 0; i < len; i++) {
            /** This is LIKELY not in YUV420P format, so we're going to convert it using some handy utilities. */
            converter.toPicture(picture, screen, frameNo);
            frameNo++;
            do {
                encoder.encode(packet, picture);
                if (packet.isComplete())
                    muxer.write(packet, false);
            } while (packet.isComplete());
        }
    }

    public void setAlpha(byte alpha, BufferedImage bi) {
        alpha %= 0xff;
        for (int cx=0;cx<bi.getWidth();cx++) {
            for (int cy=0;cy<bi.getHeight();cy++) {
                int color = bi.getRGB(cx, cy);
                int mc = (alpha << 24) | 0x00ffffff;
                int newcolor = color & mc;
                bi.setRGB(cx, cy, newcolor);
            }
        }
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void finish() {
        do {
            encoder.encode(packet, null);
            if (packet.isComplete())
                muxer.write(packet,  false);
        } while (packet.isComplete());

        /** Finally, let's clean up after ourselves. */
        muxer.close();
    }

    public BufferedImage convertToType(BufferedImage sourceImage,
                                              int targetType)
    {
        BufferedImage image;

        // if the source image is already the target type, return the source image

        if (sourceImage.getType() == targetType)
            image = sourceImage;

            // otherwise create a new image of the target type and draw the new
            // image

        else
        {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        HumbleEncoder encoder = new HumbleEncoder("D:\\Downloads\\out233.mp4");
        encoder.encodeImgWithFadeIn(ImageIO.read(new File("D:\\Downloads\\11.jpg")), 50);
        encoder.encodeImgWithFadeIn(ImageIO.read(new File("D:\\Downloads\\12.jpg")), 50);
        encoder.encodeImgWithFadeIn(ImageIO.read(new File("D:\\Downloads\\13.jpg")), 50);
        encoder.encodeImgWithFadeIn(ImageIO.read(new File("D:\\Downloads\\14.jpg")), 50);
        encoder.finish();
    }

}
