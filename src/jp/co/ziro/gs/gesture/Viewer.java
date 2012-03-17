package jp.co.ziro.gs.gesture;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.OpenNI.Context;
import org.OpenNI.GeneralException;
import org.OpenNI.Generator;

public abstract class Viewer<T extends Generator> extends Component {

	/**
	 * 画面サイズ
	 */
    private int width;
    private int height;

    private BufferedImage bimg;

    protected T gen;
    protected byte[] imgBytes;
    private HandTracker tracker;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Viewer(Context context) {

		tracker = new HandTracker(context);
		try {
			gen = createGenerator(context);
		} catch (GeneralException e) {
			e.printStackTrace();
		}

		width  = getWidth();
		height = getHeight();
        imgBytes = new byte[width*height*3];
	}
	
    protected abstract T createGenerator(Context context) throws GeneralException;
	@Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    @Override
    public void paint(Graphics g) {
    	if ( createImage(g) ) {
    		tracker.draw(g);
    		paintImage(g);
    	}
    }

    /**
     * 作成した描画を取得
     * @param g
     */
    private void paintImage(Graphics g) {
        DataBufferByte dataBuffer = new DataBufferByte(imgBytes, width*height*3);
        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null); 
        ColorModel colorModel = new ComponentColorModel(
        		ColorSpace.getInstance(ColorSpace.CS_sRGB), 
        		new int[]{8, 8, 8}, 
        		false, 
        		false, 
        		ComponentColorModel.OPAQUE, 
        		DataBuffer.TYPE_BYTE);

        bimg = new BufferedImage(colorModel, raster, false, null);
        g.drawImage(bimg, 0, 0, null);
    }

	
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract boolean createImage(Graphics g);

	public T getGenerator() {
		return gen;
	}
}
