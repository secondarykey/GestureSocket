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
import jp.co.ziro.gs.gesture.Tracker;
import jp.co.ziro.gs.server.ws.WebSocketFactory;

public class HandTracker extends Tracker {

	class MyGestureRecognized implements IObserver<GestureRecognizedEventArgs> {
		@Override
		public void update(IObservable<GestureRecognizedEventArgs> observable,
				GestureRecognizedEventArgs args) {
			try {
				handsGen.StartTracking(args.getEndPosition());
				gestureGen.removeGesture("Click");
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	class MyHandCreateEvent implements IObserver<ActiveHandEventArgs> {
		public void update(IObservable<ActiveHandEventArgs> observable, ActiveHandEventArgs args) {
			ArrayList<Point3D> newList = new ArrayList<Point3D>();
			newList.add(args.getPosition());
			history.put(new Integer(args.getId()), newList);
		}
	}
	class MyHandUpdateEvent implements IObserver<ActiveHandEventArgs> {
		public void update(IObservable<ActiveHandEventArgs> observable,
				ActiveHandEventArgs args) {
			ArrayList<Point3D> historyList = history.get(args.getId());
			historyList.add(args.getPosition());
			while (historyList.size() > historySize) {
				historyList.remove(0);
			}
			
			Point3D startPoint = historyList.get(0);
			Point3D endPoint   = historyList.get(historyList.size()-1);

			float modX = endPoint.getX() - startPoint.getX();
			float modY = endPoint.getY() - startPoint.getY();
			float modZ = endPoint.getZ() - startPoint.getZ();
			SocketType type = SocketType.getType(modX,modY,modZ);
			if ( !type.message().isEmpty() ) {
				WebSocketFactory.sendMessage(type.message());
			}
		}
	}
	private int historySize = 10;
	class MyHandDestroyEvent implements IObserver<InactiveHandEventArgs> {
		public void update(IObservable<InactiveHandEventArgs> observable,
				InactiveHandEventArgs args) {
			history.remove(args.getId());
			if (history.isEmpty()) {
				try {
					gestureGen.addGesture("Click");
				} catch (StatusException e) {
					e.printStackTrace();
				}
			}
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
        	gestureGen = GestureGenerator.create(context);
            gestureGen.addGesture("Click");
            gestureGen.getGestureRecognizedEvent().addObserver(new MyGestureRecognized());
    
            handsGen = HandsGenerator.create(context);
            handsGen.getHandCreateEvent().addObserver(new MyHandCreateEvent());
            handsGen.getHandUpdateEvent().addObserver(new MyHandUpdateEvent());
            handsGen.getHandDestroyEvent().addObserver(new MyHandDestroyEvent());

            history = new HashMap<Integer, ArrayList<Point3D>>(); 
            
        } catch (GeneralException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void draw(Graphics g) {

        for (Integer id : history.keySet()) {
        	try
        	{
        	ArrayList<Point3D> points = history.get(id);
        	g.setColor(colors[id%colors.length]);
        	int[] xPoints = new int[points.size()];
        	int[] yPoints = new int[points.size()];
        	for (int i = 0; i < points.size(); ++i)
        	{
        		Point3D proj = depthGen.convertRealWorldToProjective(points.get(i));
        		xPoints[i] = (int)proj.getX();
        		yPoints[i] = (int)proj.getY();
        	}
            g.drawPolyline(xPoints, yPoints, points.size());
    		Point3D proj = depthGen.convertRealWorldToProjective(points.get(points.size()-1));
            g.drawArc((int)proj.getX(), (int)proj.getY(), 5, 5, 0, 360);
        	} catch (StatusException e)
        	{
        		e.printStackTrace();
        	}
        }
        
    }
}