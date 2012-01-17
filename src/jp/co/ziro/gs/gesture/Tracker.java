package jp.co.ziro.gs.gesture;

import java.awt.Color;
import java.awt.Graphics;
import java.nio.ShortBuffer;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.SceneMetaData;
import org.OpenNI.UserGenerator;

public abstract class Tracker {

    protected float histogram[];
	protected DepthGenerator depthGen;
	protected UserGenerator userGen;
    private boolean drawBackground = false;
    protected Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};
    
	public Tracker(Context context) {
        try {
			depthGen = DepthGenerator.create(context);
			userGen = UserGenerator.create(context);
			histogram = new float[10000];
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}

    public void updateDepth(byte[] imgbytes) {
    	//深度を取得
        DepthMetaData depthMD = depthGen.getMetaData();
        ShortBuffer depth = depthMD.getData().createShortBuffer();

        SceneMetaData sceneMD = userGen.getUserPixels(0);
        ShortBuffer scene = sceneMD.getData().createShortBuffer();

        //階層をすべて取得
        calcHist(depth);
        depth.rewind();

        while(depth.remaining() > 0) {

            int pos = depth.position();
            short pixel = depth.get();
            short user = scene.get();
            
    		imgbytes[3*pos] = 0;
    		imgbytes[3*pos+1] = 0;
    		imgbytes[3*pos+2] = 0;                	

            if (drawBackground || pixel != 0) {
            	int colorID = user % (colors.length-1);
            	if (user == 0) {
            		colorID = colors.length-1;
            	}
            	if (pixel != 0) {
            		float histValue = histogram[pixel];
            		imgbytes[3*pos]   = (byte)(histValue*colors[colorID].getRed());
            		imgbytes[3*pos+1] = (byte)(histValue*colors[colorID].getGreen());
            		imgbytes[3*pos+2] = (byte)(histValue*colors[colorID].getBlue());
            	}
            }
        }
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
    public int getWidth() {
        DepthMetaData depthMD = depthGen.getMetaData();
        return depthMD.getFullXRes();
    }
    public int getHeight() {
        DepthMetaData depthMD = depthGen.getMetaData();
        return depthMD.getFullYRes();
    }

    public abstract void draw(Graphics g);
}
