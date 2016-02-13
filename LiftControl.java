/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * Created on 28.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test;

	
/**
 * @author martin
 *
 * JOP can also say 'Hello World'
 */
public class LiftControl {
	
	static boolean[] in = {false, false, false, false, false, false, false, false, false, false};
	static boolean[] out = {false, false, false, false, false, false, false, false, false, false};
	static int[] analog = {0, 0, 0};
	static boolean[] led = {false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	final static int GO_LOAD = 8;
	final static int GO_TOP = 6;
	final static int GO_BOTTOM = 7;
	final static int GO_UP = 4;
	final static int GO_DOWN = 5;
	final static int SENS_IMPULS = 0;
	final static int SENS_TOP = 1;
	final static int SENS_BOTTOM = 2;
	final static int SENS_LOAD = 3;
	
	final static int MOTOR_ON = 0;
	final static int MOTOR_UP = 1;
	
	static int [] levelPos;
	static int one_level;
	
	/**
	 * Is the counter valid for level positioning?
	 */
	static boolean cntValid;
	/**
	 * Position absolut or relativ.
	 */
	static int cnt;
	/**
	 * Last stoped level (1..13) if position is absolute else 0.
	 */
	static int level;
	
	/**
	 * load position in level, 0 means we don't know
	 */
	static int loadLevel;
	/**
	 * we're going TOP or BOTTOM, but stop at load position.
	 */
	static boolean loadPending;
	/**
	 * we're waiting for the load sensor to go.
	 */
	static boolean loadSensor;
	
	final static int CMD_NONE = 0;
	final static int CMD_TOP = 1;
	final static int CMD_BOTTOM = 2;
	final static int CMD_UP = 3;
	final static int CMD_DOWN = 4;
//	final static int CMD_LOAD = 3;
	/**
	 * Wait for motor stop and continue impuls counting.
	 * Go to CMD_NONE after the wait time (IMP_CONT).
	 * All commands should end here. 
	 */
	final static int CMD_WAIT = 99;
	
	/**
	 * cmd keeps the value of the command until the command is finished.
	 * It is only updated by the switches if it's current value is CMD_NONE.
	 */
	static int cmd;
	
	final static int PERIOD = 10;		// in ms
	
	/**
	 * Wait time till motor will start after dircetion.
	 */
	final static int MOTOR_WAIT_MS = 500;
	final static int MOTOR_WAIT_CNT = MOTOR_WAIT_MS/PERIOD;
	
	static int timMotor;
	
	/**
	 * Additional time where pulse are still counted after motor stop.
	 * This time also delayes new commands for a clean direction switch.
	 */
	final static int IMP_CONT_MS = 500;
	final static int IMP_CONT_CNT = IMP_CONT_MS/PERIOD;
	
	static int timImp;
	
	/**
	 * Remember last direction for impuls count after motor off;
	 */
	static boolean directionUp;
	
	/**
	 * Last value of impuls sensor.
	 */
	static boolean lastImp;
	
	/**
	 * compensate running motor after stop.
	 */
	final static int OFFSET = 1;
	
	/**
	 * Call super class with period in ms.
	 */
	public LiftControl() {
		//super(PERIOD);
		cntValid = false;
		cnt = 0;
		cmd = CMD_NONE;
		timMotor = 0;
		timImp = 0;
		directionUp = true;
		lastImp = false;
		loadLevel = 0;
		loadPending = false;
		loadSensor = false;

		
		int[] tmp = {
			0,
			58,
			115,
			173,
			230,
			288,
			346,
			403,
			461,
			518,
			576,
			634,
			691,
			749,
			806,
			864
		};
		for (int i=0; i<10; ++i) in[i]	= false;	
		for (int i=0; i<4; ++i) out[i]	= false;	
		for (int i=0; i<3; ++i) analog[i]	= 0;	
		for (int i=0; i<14; ++i) led[i]	= false;	
		levelPos = tmp;
// for simpler debugging
// for (int i=0; i<levelPos.length; ++i) levelPos[i] /= 10;
		one_level = levelPos[1];
	}
	
	static int endCnt;

	static int dbgCnt;
	
//	public static TalIo io;
		
	public static void main(String[] args) {

		if (cmd==CMD_NONE) {
			if (loadPending) {
				if (in[SENS_BOTTOM]) {
					cmd = CMD_TOP;		
				}
			} else if (in[GO_UP]) {
				if (!in[SENS_TOP] && level!=levelPos.length) {
					cmd = CMD_UP;
				}
			} else if (in[GO_DOWN]) {
				if (!in[SENS_BOTTOM] && level!=1) {
					cmd = CMD_DOWN;
				}
			} else if (in[GO_LOAD]) {
				if (loadLevel!=0 && level<loadLevel) {
					cmd = CMD_TOP;
				} else {
					cmd = CMD_BOTTOM;
				}
				loadPending = true;
				loadSensor = false;
			} else if (in[GO_TOP]) {
				if (!in[SENS_TOP]) {
					cmd = CMD_TOP;
				}
			} else if (in[GO_BOTTOM]) {
				if (!in[SENS_BOTTOM]) {
					cmd = CMD_BOTTOM;
				}
			}
			if (cmd!=CMD_NONE) {
				timMotor = MOTOR_WAIT_CNT;
			}
	
		} else {
		
		        boolean val = in[SENS_IMPULS];
			boolean motor = out[MOTOR_ON];
			boolean reset = in[SENS_BOTTOM];
			if (val && !lastImp) {
				if (motor || timImp>0) {
					if (directionUp) {
						++cnt;
					} else {
						--cnt;
					}
				}
			}
			if (reset) {
				cnt = 0;
				cntValid = true;
			} 
			lastImp = val;
			if (timImp>0) {
				--timImp;
				if (timImp==0 && cmd!=CMD_NONE) {
					cmd = CMD_NONE;
				}
			}
			


			if (timMotor > 0) {
				--timMotor;
				directionUp = (cmd==CMD_UP || cmd==CMD_TOP);
				out[MOTOR_UP] =  directionUp;
				if (!cntValid) {
					cnt = 0;		// use relative counter
					if (cmd==CMD_UP) {
						endCnt = one_level; 
					} else {
						endCnt = -one_level;
					}
				} else {
					endCnt = cnt;
					int newLevel = -99;
					if (cmd==CMD_UP) {
						newLevel = level+1;
					} else if (cmd==CMD_DOWN){
						newLevel = level-1;
					}
					--newLevel;	// level is one based
					if (newLevel>=0 && newLevel<levelPos.length) {
						endCnt = levelPos[newLevel]; 
					}
				}
			} else {
				boolean run ;
				if (cmd == CMD_UP) {
					if (cnt < endCnt - OFFSET && !in[SENS_TOP])
						run = true;
				} else if (cmd == CMD_DOWN) {
					if (cnt > endCnt + OFFSET && !in[SENS_BOTTOM])
					run = true;
				} else if (cmd == CMD_TOP) {
					if (loadPending && in[SENS_LOAD]) {
					// we are at load position
					loadLevel = level;
					loadPending = false;
					run = false;
				}
				if (!in[SENS_TOP])
					run = true;
				// for shure if load sensor does not work
				loadPending = false;
				} else if (cmd == CMD_BOTTOM) {
					if (loadPending) {
						if (loadSensor) {
						if (!in[SENS_LOAD]) {
							loadSensor = false;
							// we are at load position
							loadPending = false;
							loadLevel = level;
							run = false;
						}		
						}
						loadSensor = in[SENS_LOAD];
					}
					if (!in[SENS_BOTTOM])
						run = true;
				}		
				run = false;	
				if (out[MOTOR_ON] && !run) {
					// motor stopped:
					cmd = CMD_WAIT;
					timImp = IMP_CONT_CNT;
				}
				out[MOTOR_ON] = run; 
			}
		
		}


	
		int middle = one_level>>2;
		if (cntValid) {
			for (level=1; level<levelPos.length; ++level) { // @WCA loop=14
				if (cnt < levelPos[level]-middle) {
					break;
				}
			}
		} else {
			level = 0;
		}
		for (int i=0; i<led.length; ++i) { // @WCA loop=14
			led[i] = (i == level-1);
		}


		led[13] = (dbgCnt&0x80) != 0;
		++dbgCnt;
		if ((dbgCnt&0x3f) == 0) {
//			dbg();
		}
	}
}

