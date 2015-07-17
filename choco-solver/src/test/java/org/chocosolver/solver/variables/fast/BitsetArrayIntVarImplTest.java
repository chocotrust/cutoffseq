/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.fast;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.impl.BitsetArrayIntVarImpl;
import org.chocosolver.solver.variables.ranges.BitsetRemovals;
import org.chocosolver.solver.variables.ranges.IRemovals;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/02/13
 */
public class BitsetArrayIntVarImplTest {

    BitsetArrayIntVarImpl var;

    public void setUp() throws Exception {
        var = new BitsetArrayIntVarImpl("test", new int[]{-5, 0, 3, 4, 5}, new Solver());
    }

    @Test(groups = "1s")
    public void testRemoveValue() throws Exception {
        setUp();
        Assert.assertFalse(var.removeValue(7, Cause.Null));
        Assert.assertTrue(var.removeValue(0, Cause.Null));
        Assert.assertFalse(var.contains(0));
    }

    @Test(groups = "1s")
    public void testRemoveInterval() throws Exception {
        setUp();
        Assert.assertFalse(var.removeInterval(7, 8, Cause.Null));
        Assert.assertTrue(var.removeInterval(0, 3, Cause.Null));
        Assert.assertFalse(var.contains(0));
        Assert.assertFalse(var.contains(3));
    }

    @Test(groups = "1s")
    public void testUpdateLowerBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateLowerBound(-6, Cause.Null));
        Assert.assertTrue(var.updateLowerBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(-5));

    }

    @Test(groups = "1s")
    public void testUpdateUpperBound() throws Exception {
        setUp();
        Assert.assertFalse(var.updateUpperBound(6, Cause.Null));
        Assert.assertTrue(var.updateUpperBound(0, Cause.Null));
        Assert.assertTrue(var.contains(0));
        Assert.assertFalse(var.contains(5));
    }


    @Test(groups = "1s")
    public void testGetLB() throws Exception {
        setUp();
        Assert.assertEquals(-5, var.getLB());
    }

    @Test(groups = "1s")
    public void testGetUB() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getUB());
    }

    @Test(groups = "1s")
    public void testGetDomainSize() throws Exception {
        setUp();
        Assert.assertEquals(5, var.getDomainSize());
    }

    @Test(groups = "1s")
    public void testNextValue() throws Exception {
        setUp();
        Assert.assertEquals(-5, var.nextValue(-6));
        Assert.assertEquals(0, var.nextValue(-5));
        Assert.assertEquals(Integer.MAX_VALUE, var.nextValue(5));
    }

    @Test(groups = "1s")
    public void testPreviousValue() throws Exception {
        setUp();
        Assert.assertEquals(5, var.previousValue(6));
        Assert.assertEquals(0, var.previousValue(3));
        Assert.assertEquals(Integer.MIN_VALUE, var.previousValue(-5));
    }

    @Test(groups = "1s")
    public void testHasEnumeratedDomain() throws Exception {
        setUp();
        Assert.assertTrue(var.hasEnumeratedDomain());
    }

    @Test(groups = "1s")
    public void testGetTypeAndKind() throws Exception {
        setUp();
        Assert.assertTrue((Variable.INT & var.getTypeAndKind()) != 0);
        Assert.assertTrue((Variable.VAR & var.getTypeAndKind()) != 0);
    }

    @Test(groups = "1s")
    public void testGetValueIterator() throws Exception {
        setUp();
        DisposableValueIterator vit = var.getValueIterator(true);
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(-5, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(0, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(3, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(4, vit.next());
        Assert.assertTrue(vit.hasNext());
        Assert.assertEquals(5, vit.next());
        Assert.assertFalse(vit.hasNext());
        vit.dispose();

        vit = var.getValueIterator(false);
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(5, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(4, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(3, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(0, vit.previous());
        Assert.assertTrue(vit.hasPrevious());
        Assert.assertEquals(-5, vit.previous());
        Assert.assertFalse(vit.hasPrevious());
        vit.dispose();
    }

    @Test(groups = "1s")
    public void testGetRangeIterator() throws Exception {
        setUp();
        DisposableRangeIterator rit = var.getRangeIterator(true);
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(-5, rit.min());
        Assert.assertEquals(-5, rit.max());
        rit.next();
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(0, rit.max());
        rit.next();
        Assert.assertTrue(rit.hasNext());
        Assert.assertEquals(3, rit.min());
        Assert.assertEquals(5, rit.max());
        rit.next();
        Assert.assertFalse(rit.hasNext());

        rit = var.getRangeIterator(false);
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(3, rit.min());
        Assert.assertEquals(5, rit.max());
        rit.previous();
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(0, rit.min());
        Assert.assertEquals(0, rit.max());
        rit.previous();
        Assert.assertTrue(rit.hasPrevious());
        Assert.assertEquals(-5, rit.min());
        Assert.assertEquals(-5, rit.max());
        rit.previous();
        Assert.assertFalse(rit.hasPrevious());
    }

    @Test(groups = "1s")
    public void testRemVals0() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups = "1s")
    public void testRemVals1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        rems.add(-3, -1, 1, 2, 4);
        x.removeValues(rems, Cause.Null);
    }

    @Test(groups = "1s")
    public void testRemVals11() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(2, 5, 8, 9);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertTrue(x.isInstantiatedTo(6));
    }

    @Test(groups = "1s")
    public void testRemVals12() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(2, 5, 6, 8);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(9));
    }

    @Test(groups = "1s")
    public void testRemVals13() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(2, 6, 9);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.getDomainSize() == 2);
    }

    @Test(groups = "1s")
    public void testRemVals14() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(5, 6, 8, 9);
        x.removeValues(rems, Cause.Null);
        Assert.assertTrue(x.isInstantiatedTo(2));
    }

    @Test(groups = "1s", expectedExceptions = ContradictionException.class)
    public void testRemVals15() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{2, 5, 6, 8, 9}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(1, 2, 5, 6, 8, 9);
        x.removeValues(rems, Cause.Null);
    }

    @Test(groups = "1s")
    public void testRemVals21() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        rems.add(-3, -2);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -1);
    }

    @Test(groups = "1s")
    public void testRemVals22() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-4);
        rems.add(-4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getLB(), -3);
    }

    @Test(groups = "1s")
    public void testRemVals3() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        rems.add(1, 2, 3);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 0);
    }

    @Test(groups = "1s")
    public void testRemVals31() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        rems.add(4);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
        Assert.assertEquals(x.getUB(), 3);
    }


    @Test(groups = "1s")
    public void testRemVals41() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, -2, -1, 0, 1, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        rems.add(-1, 0, 1);
        Assert.assertTrue(x.removeValues(rems, Cause.Null));
    }

    @Test(groups = "1s")
    public void testRemVals42() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-3, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-3);
        rems.add(0);
        Assert.assertFalse(x.removeValues(rems, Cause.Null));
    }

    @Test(groups = "1s", expectedExceptions = ContradictionException.class)
    public void testRemVals5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{-1, 0, 1}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(-1);
        rems.add(-1, 0, 1);
        x.removeValues(rems, Cause.Null);
        Assert.fail();
    }

    @Test(groups = "1s")
    public void testRemVals6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 2, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(0, 1, 2);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getLB(), 3);
    }

    @Test(groups = "1s")
    public void testRemVals7() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = new BitsetArrayIntVarImpl("X", new int[]{0, 1, 3}, solver);
        IRemovals rems = new BitsetRemovals();
        rems.setOffset(0);
        rems.add(1, 2, 3);
        x.removeValues(rems, Cause.Null);
        Assert.assertEquals(x.getUB(), 0);
    }

}
