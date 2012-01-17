package jp.co.ziro.gs.gesture.ni;

import java.awt.Color;
import java.awt.Graphics;

import java.util.HashMap;

import jp.co.ziro.gs.gesture.Tracker;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthMetaData;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;

public class SkeletonTracker extends Tracker {

    //骨格を表示するかのフラグ
    private boolean drawSkeleton = true;
    private boolean printID = true;
    private boolean printState = true;

	private static final long serialVersionUID = 1L;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;
    private String calibPose = null;

    private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

    public SkeletonTracker(Context context) {
    	super(context);
        try {
			init(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private void init(Context context) throws Exception {
        skeletonCap = userGen.getSkeletonCapability();
        poseDetectionCap = userGen.getPoseDetectionCapability();
        //新しいユーザが入った場合
        userGen.getNewUserEvent().addObserver(new IObserver<UserEventArgs>(){
			@Override
			public void update(IObservable<UserEventArgs> observable,UserEventArgs args) {
				try {
					if (skeletonCap.needPoseForCalibration()) {
						poseDetectionCap.startPoseDetection(calibPose, args.getId());
					} else {
						skeletonCap.requestSkeletonCalibration(args.getId(), true);
					}
				} catch (StatusException e) {
					e.printStackTrace();
				}
			}
    	});
        
        //ユーザを失った場合
        userGen.getLostUserEvent().addObserver(new IObserver<UserEventArgs>(){
        	@Override
        	public void update(IObservable<UserEventArgs> observable,
        			UserEventArgs args) {
        		System.out.println("Lost user " + args.getId());
        		joints.remove(args.getId());
        	}
        });

        skeletonCap.getCalibrationCompleteEvent().addObserver(new IObserver<CalibrationProgressEventArgs>(){
			@Override
			public void update(IObservable<CalibrationProgressEventArgs> observable,
					CalibrationProgressEventArgs args) {
				System.out.println("Calibraion complete: " + args.getStatus());
				try {
					if (args.getStatus() == CalibrationProgressStatus.OK) {

						System.out.println("starting tracking "  +args.getUser());
						skeletonCap.startTracking(args.getUser());
			            joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
			            
					} else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT) {
						if (skeletonCap.needPoseForCalibration()) {
							poseDetectionCap.startPoseDetection(calibPose, args.getUser());
						} else {
							skeletonCap.requestSkeletonCalibration(args.getUser(), true);
						}
					}
				} catch (StatusException e) {
					e.printStackTrace();
				}
			}
    	});
 
        poseDetectionCap.getPoseDetectedEvent().addObserver(new IObserver<PoseDetectionEventArgs>() {
			@Override
			public void update(IObservable<PoseDetectionEventArgs> observable,
					PoseDetectionEventArgs args) {
				System.out.println("Pose " + args.getPose() + " detected for " + args.getUser());
				try {
					poseDetectionCap.stopPoseDetection(args.getUser());
					skeletonCap.requestSkeletonCalibration(args.getUser(), true);
				} catch (StatusException e) {
					e.printStackTrace();
				}
			}
        });

        calibPose = skeletonCap.getSkeletonCalibrationPose();
        joints = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();
        
        skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);
    }

    
    public int getWidth() {
        DepthMetaData depthMD = depthGen.getMetaData();
        return depthMD.getFullXRes();
    }
    public int getHeight() {
        DepthMetaData depthMD = depthGen.getMetaData();
        return depthMD.getFullYRes();
    }

    @Override
    public void draw(Graphics g) {
        try {
			int[] users = userGen.getUsers();

			//存在するユーザ数回繰り返す
			for (int i = 0; i < users.length; ++i) {
		    	Color c = colors[users[i]%colors.length];
		    	c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());

		    	g.setColor(c);
				if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i])) {
					drawSkeleton(g, users[i]);
				}

				if (printID) {
					Point3D com = depthGen.convertRealWorldToProjective(userGen.getUserCoM(users[i]));
					String label = null;
					if (!printState)
					{
						label = new String(""+users[i]);
					}
					else if (skeletonCap.isSkeletonTracking(users[i]))
					{
						// Tracking
						label = new String(users[i] + " - Tracking");
					}
					else if (skeletonCap.isSkeletonCalibrating(users[i]))
					{
						// Calibrating
						label = new String(users[i] + " - Calibrating");
					}
					else
					{
						// Nothing
						label = new String(users[i] + " - Looking for pose (" + calibPose + ")");
					}

					g.drawString(label, (int)com.getX(), (int)com.getY());
				}
			}
		} catch (StatusException e) {
			e.printStackTrace();
		}
    }
    
    
    private void drawSkeleton(Graphics g, int user) throws StatusException
    {
    	getJoints(user);
    	HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

    	drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

    	drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
    	drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

    	drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
    	drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
    	drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

    	drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
    	drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
    	drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

    	drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
    	drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
    	drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);

    	drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
    	drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);

    	drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
    	drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);

    }
    
    private void getJoints(int user) throws StatusException
    {
    	getJoint(user, SkeletonJoint.HEAD);
    	getJoint(user, SkeletonJoint.NECK);
    	
    	getJoint(user, SkeletonJoint.LEFT_SHOULDER);
    	getJoint(user, SkeletonJoint.LEFT_ELBOW);
    	getJoint(user, SkeletonJoint.LEFT_HAND);

    	getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
    	getJoint(user, SkeletonJoint.RIGHT_ELBOW);
    	getJoint(user, SkeletonJoint.RIGHT_HAND);

    	getJoint(user, SkeletonJoint.TORSO);

    	getJoint(user, SkeletonJoint.LEFT_HIP);
        getJoint(user, SkeletonJoint.LEFT_KNEE);
        getJoint(user, SkeletonJoint.LEFT_FOOT);

    	getJoint(user, SkeletonJoint.RIGHT_HIP);
        getJoint(user, SkeletonJoint.RIGHT_KNEE);
        getJoint(user, SkeletonJoint.RIGHT_FOOT);

    }

    private void drawLine(Graphics g, HashMap<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2) {
		Point3D pos1 = jointHash.get(joint1).getPosition();
		Point3D pos2 = jointHash.get(joint2).getPosition();
		if (jointHash.get(joint1).getConfidence() == 0 || jointHash.get(joint2).getConfidence() == 0) {
			return;
		}

		g.drawLine((int)pos1.getX(), (int)pos1.getY(), (int)pos2.getX(), (int)pos2.getY());
    }
    
    private void getJoint(int user, SkeletonJoint joint) throws StatusException {
        SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
		if (pos.getPosition().getZ() != 0) {
			joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
		} else {
			joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
		}
    }
}
