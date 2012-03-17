package jp.co.ziro.gs;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.GeneralException;
import org.OpenNI.ImageGenerator;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

import jp.co.ziro.gs.gesture.DepthViewer;
import jp.co.ziro.gs.gesture.ImageViewer;
import jp.co.ziro.gs.gesture.Viewer;
import jp.co.ziro.gs.util.ApplicationUtil;

public class GestureDisplay extends Thread {

	private Viewer<ImageGenerator> imageViewer;
	private Viewer<DepthGenerator> depthViewer;

	private JFrame frame;
    private Context context;

	private boolean shouldRun = true;
	
	private static DepthGenerator gen;
    /**
     * コンストラクタ
     */
	public GestureDisplay() {

		initGesture();

		frame = new JFrame("Gesture");
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	System.exit(0);
            }
        });

		frame.setLayout(new FlowLayout());
		frame.add(depthViewer);
		frame.add(imageViewer);

		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
	}

	/**
	 * ジェスチャー処理の初期化
	 */
	private void initGesture() {
		OutArg<ScriptNode> scriptNode = new OutArg<ScriptNode>();
        try {
			context = Context.createFromXmlFile(ApplicationUtil.get("gesture.config.file.path"), scriptNode);
			//深度データの作成
			imageViewer = new ImageViewer(context);
			//画像データの作成
			depthViewer = new DepthViewer(context);

			gen = depthViewer.getGenerator();
			//TODO ソケットイベントの関連付

			context.startGeneratingAll();

		} catch (GeneralException e1) {
			throw new RuntimeException(e1);
		}
	}

	public boolean isShouldRun() {
		return shouldRun;
	}

	@Override
	public void run() {
		//終了か？
        while( isShouldRun() ) {
            try {
				context.waitAnyUpdateAll();
			} catch (StatusException e) {
				e.printStackTrace();
			}
			frame.repaint();
        }
        frame.dispose();
	}

	public static Point3D convertRealWorldToProjective(Point3D points ) throws StatusException {
		return gen.convertRealWorldToProjective(points);
	}
}
