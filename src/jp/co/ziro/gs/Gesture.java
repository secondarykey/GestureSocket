package jp.co.ziro.gs;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.OpenNI.Context;
import org.OpenNI.GeneralException;
import org.OpenNI.OutArg;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

import jp.co.ziro.gs.gesture.Viewer;
import jp.co.ziro.gs.util.ApplicationUtil;

public class Gesture extends Thread {

	private Viewer viewer;
	private JFrame frame;
    private Context context;

	public Gesture() {

		initGesture();

		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
	}

	private void initView() {
		frame = new JFrame("Gesture");
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	System.exit(0);
            }
        });
		viewer = new Viewer(context);
		frame.add("Center", viewer);
	}

	private void initGesture() {
		OutArg<ScriptNode> scriptNode = new OutArg<ScriptNode>();
        try {
			context = Context.createFromXmlFile(ApplicationUtil.get("gesture.config.file.path"), scriptNode);
		} catch (GeneralException e1) {
			e1.printStackTrace();
		}
		initView();
		try {
			context.startGeneratingAll();
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
        while(viewer.isShouldRun()) {
            try {
				context.waitAnyUpdateAll();
			} catch (StatusException e) {
				e.printStackTrace();
			}
			//更新処理
        	viewer.update();
        }
        frame.dispose();
	}
}
