/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *  http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *-----------------------------------------------------------------------------*/
package com.stabilit.scm.common.util;

import java.util.TimerTask;

/**
 * The Interface ITimerRun.
 * 
 * @author JTraber
 */
public interface ITimerRun {

	/**
	 * Timeout. Runs when time is out.
	 */
	public void timeout();

	/**
	 * Gets the timer task.
	 * 
	 * @return the timer task
	 */
	public abstract TimerTask getTimerTask();

	/**
	 * Sets the timer task.
	 * 
	 * @param timerTask
	 *            the new timer task
	 */
	public abstract void setTimerTask(TimerTask timerTask);

	/**
	 * Gets the timeout.
	 * 
	 * @return the timeout
	 */
	public abstract int getTimeout();
}
