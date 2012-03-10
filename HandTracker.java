/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package jp.co.ziro.gs.gesture.ni;

import org.OpenNI.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.*;

import jp.co.ziro.gs.SocketType;
import jp.co.ziro.gs.server.ws.WebSocketFactory;
import jp.co.ziro.gs.util.ApplicationUtil;

public class HandTracker extends Tracker {

	private static int gblhistorySize = ApplicationUtil.getInteger("gesture.history");
	private class GestureRecognized implements IObserver<GestureRecognizedEventArgs> {
		@Override
		public void update(IObservable<GestureRecognizedEventArgs> observable,
				GestureRecognizedEventArgs args) {
			try {
				//cmd
				WebSocketFactory.sendMessage(SocketType.READY.message());

				handsGen.StartTracking(args.getEndPosition());
				gestureGen.removeGesture(ApplicationUtil.get("gesture.start"));
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	private class HandCreateEvent implements IObserver<ActiveHandEventArgs> {
		public void update(IObservable<ActiveHandEventArgs> observable,
				ActiveHandEventArgs args) {
			ArrayList<Point3D> newList = new ArrayList<Point3D>();
			newList.add(args.getPosition());
			history.put(new Integer(args.getId()), newList);
		}
	}

	private class HandUpdateEvent implements IObserver<ActiveHandEventArgs> {
		public void update(IObservable<ActiveHandEventArgs> observable,
				ActiveHandEventArgs args) {
			ArrayList<Point3D> historyList = history.get(args.getId());
			historyList.add(args.getPosition());
			while (historyList.size() > gblhistorySize) {
				historyList.remove(0);
			}

			Point3D startPoint = historyList.get(0);
			Point3D endPoint   = historyList.get(historyList.size() - 1);

			SocketType type = SocketType.getType(startPoint,endPoint);
			if (!type.message().isEmpty()) {
				if ( canSend ) {
					WebSocketFactory.sendMessage(type.message());
					sleep();
				}
			}
		}
	}
	private class HandDestroyEvent implements IObserver<InactiveHandEventArgs> {
		public void update(IObservable<InactiveHandEventArgs> observable,
				InactiveHandEventArgs args) {
			history.remove(args.getId());
			if (history.isEmpty()) {
				try {
					WebSocketFactory.sendMessage(SocketType.LOST.message());
					gestureGen.addGesture(ApplicationUtil.get("gesture.start"));
				} catch (StatusException e) {
					e.printStackTrace();
				}
			}
		}
	}
    private static boolean canSend = true;
    public void sleep() {
   		canSend = false;
    	new Timer().start();
	}

	class Timer extends Thread {
        public void run() {
        	try {
				sleep(ApplicationUtil.getInteger("gesture.send.stop"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			canSend = true;
        }
    }


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GestureGenerator gestureGen;

	private HandsGenerator handsGen;
	private HashMap<Integer, ArrayList<Point3D>> history;

	public HandTracker(Context context) {

		super(context);
		try {
			history = new HashMap<Integer, ArrayList<Point3D>>();

			gestureGen = GestureGenerator.create(context);
			handsGen = HandsGenerator.create(context);
			
			gestureGen.addGesture(ApplicationUtil.get("gesture.start"));
			gestureGen.getGestureRecognizedEvent().addObserver(new GestureRecognized());
			
			handsGen.getHandCreateEvent().addObserver(new HandCreateEvent());
			handsGen.getHandUpdateEvent().addObserver(new HandUpdateEvent());
			handsGen.getHandDestroyEvent().addObserver(new HandDestroyEvent());


		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void draw(Graphics g) {

		for (Integer id : history.keySet()) {

			ArrayList<Point3D> points = history.get(id);
			g.setColor(colors[id % colors.length]);

			int[] xPoints = new int[points.size()];
			int[] yPoints = new int[points.size()];

			for (int cnt = 0; cnt < points.size(); ++cnt) {
				Point3D point;
				try {
					point = depthGen.convertRealWorldToProjective(points.get(cnt));
				} catch (StatusException e) {
					throw new RuntimeException("ポイント取得時の例外",e);
				}

				//座標を配列に設定
				xPoints[cnt] = (int)point.getX();
				yPoints[cnt] = (int)point.getY();

				if ( cnt == (points.size()-1) ) {
					//円を描画
					g.fillArc((int)point.getX(),(int)point.getY(),7,7,0,360);
				}
			}

			//履歴分の線を描画する
			g.drawPolyline(xPoints, yPoints, points.size());
		}

	}
}