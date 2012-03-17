package jp.co.ziro.gs.gesture;

import java.awt.Color;
import java.awt.Graphics;
import java.nio.ShortBuffer;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;

public class DepthViewer extends Viewer<DepthGenerator> {

    protected float histogram[];
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DepthViewer(Context context) {
		super(context);
		histogram = new float[10000];
	}

	@Override
    public boolean createImage(Graphics g) {

    	//深度を取得
        DepthMetaData depthMD = gen.getMetaData();
        ShortBuffer depth = depthMD.getData().createShortBuffer();

        //階層をすべて取得
        calcHist(depth);
        depth.rewind();

        while(depth.remaining() > 0) {

            int pos = depth.position();
            short pixel = depth.get();
    
    		imgBytes[3*pos] = 0;
    		imgBytes[3*pos+1] = 0;
    		imgBytes[3*pos+2] = 0;                	

           	if (pixel != 0) {
           		float histValue = histogram[pixel];
           		//RGBを指定
           		imgBytes[3*pos]   = (byte)(histValue*Color.WHITE.getRed());
           		imgBytes[3*pos+1] = (byte)(histValue*Color.WHITE.getGreen());
           		imgBytes[3*pos+2] = (byte)(histValue*Color.WHITE.getBlue());
           	}
       }
        return true;
    }

    private void calcHist(ShortBuffer depth) {
        // reset
        for (int i = 0; i < histogram.length; ++i) {
            histogram[i] = 0;
        }
        depth.rewind();
        int points = 0;
        while(depth.remaining() > 0) {
            short depthVal = depth.get();
            if (depthVal != 0) {
                histogram[depthVal]++;
                points++;
            }
        }

        for (int i = 1; i < histogram.length; i++) {
            histogram[i] += histogram[i-1];
        }

        if (points > 0) {
            for (int i = 1; i < histogram.length; i++) {
                histogram[i] = 1.0f - (histogram[i] / (float)points);
            }
        }
    }

	@Override
    public int getWidth() {
        DepthMetaData depthMD = gen.getMetaData();
        return depthMD.getFullXRes();
    }
	@Override
    public int getHeight() {
        DepthMetaData depthMD = gen.getMetaData();
        return depthMD.getFullYRes();
    }

	@Override
	protected DepthGenerator createGenerator(Context context) throws GeneralException {
		return DepthGenerator.create(context);
	}
}
