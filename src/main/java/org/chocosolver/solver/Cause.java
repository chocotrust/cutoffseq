/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.ValueSortedMap;

import java.util.function.Consumer;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public enum Cause implements ICause {
    Null{
        @Override
        public void explain(ExplanationForSignedClause explanation, ValueSortedMap<IntVar> front, Implications implicationGraph, int pivot) {

        }

        @Override
        public void forEachIntVar(Consumer<IntVar> action) {

        }
    }
}
