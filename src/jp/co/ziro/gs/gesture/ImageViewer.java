package jp.co.ziro.gs.gesture;

import java.awt.Graphics;
import java.nio.ByteBuffer;

import org.OpenNI.Context;
import org.OpenNI.GeneralException;
import org.OpenNI.ImageGenerator;
import org.OpenNI.ImageMap;
import org.OpenNI.ImageMetaData;

public class ImageViewer extends Viewer<ImageGenerator> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ImageViewer(Context context) {
		super(context);
	}

	@Override
	public boolean createImage(Graphics g) {
		ImageMetaData imageMD = gen.getMetaData();
		ImageMap imageMap = imageMD.getData();
		ByteBuffer image = imageMap.createByteBuffer();
		while ( image.remaining() > 0 ) {
			int pos = image.position();
			byte pixel = image.get();
    		imgBytes[pos] = pixel;
		}
		return true;
	}
	
	@Override
    public int getWidth() {
    	ImageMetaData depthMD = gen.getMetaData();
        return depthMD.getFullXRes();
    }

	@Override
    public int getHeight() {
		ImageMetaData depthMD = gen.getMetaData();
        return depthMD.getFullYRes();
    }
	@Override
	protected ImageGenerator createGenerator(Context context) throws GeneralException {
		return ImageGenerator.create(context);
	}
}
