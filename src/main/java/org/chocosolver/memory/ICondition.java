/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;



/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 24/09/2014
 */
public interface ICondition  {

    /** False condition, never satisfied */
    ICondition FALSE = () -> false;

    boolean satisfied();

    /**
     * Set the environment of this
     * @param environment a backtrackable environment
     */
    default void set(IEnvironment environment) {
        // nothing to do by default
    }
}
