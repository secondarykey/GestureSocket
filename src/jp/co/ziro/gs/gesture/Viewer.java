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

import jp.co.ziro.gs.gesture.ni.HandTracker;
import jp.co.ziro.gs.gesture.ni.ImageTracker;
import jp.co.ziro.gs.gesture.ni.Tracker;

import org.OpenNI.Context;

public class Viewer extends Component {
	/**
	 * 画面サイズ
	 */
    private int width;
    private int height;

	private boolean shouldRun = true;
    private BufferedImage bimg;
    private byte[] imgbytes;
	
	private Tracker tracker;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Viewer(Context context) {
		tracker = new ImageTracker(context);

        width  = tracker.getWidth();
        height = tracker.getHeight();
        imgbytes = new byte[width*height*3];
	}

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    @Override
    public void paint(Graphics g) {
    	drawDepth(g);
    	//表示を行う
    	tracker.draw(g);
    }

    /**
     * 深度を取得して再描画を行う
     */
	public void update() {
		//深度部分の算出を行う
		tracker.updateDepth(imgbytes);
    	repaint();
	}

	public boolean isShouldRun() {
		return shouldRun;
	}

	/**
	 * 全体の深度を表示
	 * @param g 描画するグラフィック
	 */
	public void drawDepth(Graphics g) {
        DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);
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
}
