package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * 
 * PController from prior lab to follow the obstacle once
 * interrupted from simple navigation
 * 
 * @author Oliver Clark
 * @author Michael Vaquier
 */
public class PController implements UltrasonicController {

	/* Constants */
	private static final int MOTOR_SPEED = 140;
	private static final int FILTER_OUT = 16;
	private final int ERROR_PROPORTIONALITY = 35;
	private final int CORNER_SPEED_DIFFERENCE = 45;
	private static final int SPIN_SPEED = 150;

	private final int bandCenter;
	private final int bandWidth;
	private int wheelSpeedDifference;
	private int distance;
	private int filterControl;
	private boolean spinning = false;
	private boolean corner = false;

	public PController(int bandCenter, int bandWidth) {
		this.bandCenter = bandCenter;
		this.bandWidth = bandWidth;
		this.filterControl = 0;

		
	}

	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).
		//
		if (distance >= 100 && filterControl < FILTER_OUT) {
			// bad value increment the filter value
			// set distance to bandCenter to go straight
			filterControl++;
			this.distance = bandCenter;
		} else if (distance >= 100) {
			// We have repeated large values, so there must actually be nothing
			this.distance = distance;
		} else {
			// distance went below 100: reset filter
			filterControl = 0;
			this.distance = distance;
		}

		corner = false;
		if (this.distance >= 100) {
			wheelSpeedDifference = CORNER_SPEED_DIFFERENCE;
			NavigationLab.leftMotor.setSpeed(100);
			NavigationLab.rightMotor.setSpeed(250);
			NavigationLab.rightMotor.forward();
			NavigationLab.leftMotor.forward();
			corner = true;
		} else {
			wheelSpeedDifference = Math.abs(ERROR_PROPORTIONALITY * (bandCenter - distance) / 10);
			if (wheelSpeedDifference > 90)
				wheelSpeedDifference = 90;
		}

		if (!corner) {
			if (Math.abs(this.distance - bandCenter) < bandWidth) { // Sweet
																	// spot
				NavigationLab.leftMotor.setSpeed(MOTOR_SPEED);
				NavigationLab.rightMotor.setSpeed(MOTOR_SPEED);
				NavigationLab.rightMotor.forward();
				NavigationLab.leftMotor.forward();
			} else if (this.distance < bandCenter) { // Too close
				if (this.distance < 8) {
					NavigationLab.leftMotor.setSpeed(200);
					NavigationLab.rightMotor.setSpeed(200);
					NavigationLab.rightMotor.backward();
					NavigationLab.leftMotor.backward();
					spinning = false;
				} else if (this.distance < 17) { // if the robot is way too
													// close, it spins
					NavigationLab.leftMotor.setSpeed(SPIN_SPEED);
					NavigationLab.rightMotor.setSpeed(SPIN_SPEED);
					NavigationLab.rightMotor.backward();
					NavigationLab.leftMotor.forward();
					spinning = true;
				} else {
					NavigationLab.leftMotor.setSpeed(MOTOR_SPEED + wheelSpeedDifference);
					NavigationLab.rightMotor.setSpeed(MOTOR_SPEED - wheelSpeedDifference);
					NavigationLab.rightMotor.forward();
					NavigationLab.leftMotor.forward();
					spinning = false;
				}
			} else { // Too far
				NavigationLab.leftMotor.setSpeed(MOTOR_SPEED - wheelSpeedDifference);
				NavigationLab.rightMotor.setSpeed(MOTOR_SPEED + wheelSpeedDifference);
				NavigationLab.rightMotor.forward();
				NavigationLab.leftMotor.forward();
				spinning = false;
			}
		}

	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
