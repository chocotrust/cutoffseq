/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.parser.flatzinc.ast.constraints;

import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

import java.util.List;

import static org.chocosolver.solver.constraints.PropagatorPriority.BINARY;
import static org.chocosolver.solver.constraints.PropagatorPriority.TERNARY;
import static org.chocosolver.util.ESat.TRUE;

/**
 * (a = b) &#8660; r
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 26/01/11
 */
public class IntEqReifBuilder implements IBuilder {

    @Override
    public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        IntVar a = exps.get(0).intVarValue(model);
        IntVar b = exps.get(1).intVarValue(model);
        final BoolVar r = exps.get(2).boolVarValue(model);
        // this constraint is not poster, hence not returned, because it is reified
        if (((FznSettings) model.getSettings()).enableClause()
                && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
            model.addClausesBoolIsEqVar((BoolVar) a, (BoolVar) b, r);
        } else {
            if (((FznSettings) model.getSettings()).adhocReification()) {
                if (a.isInstantiated() || b.isInstantiated()) {
                    IntVar x;
                    int c;
                    if (a.isInstantiated()) {
                        x = b;
                        c = a.getValue();
                    } else {
                        x = a;
                        c = b.getValue();
                    }
                    final IntVar var = x;
                    final int cste = c;
                    new Constraint("reif(a=cste,r)", new Propagator<IntVar>(new IntVar[]{x, r}, BINARY, false) {
                        @Override
                        public void propagate(int evtmask) throws ContradictionException {
                            if (r.getLB() == 1) {
                                var.instantiateTo(cste, this);
                                setPassive();
                            } else {
                                if (r.getUB() == 0) {
                                    if (var.removeValue(cste, this) || !var.contains(cste)) {
                                        setPassive();
                                    }
                                } else {
                                    if (var.isInstantiatedTo(cste)) {
                                        r.setToTrue(this);
                                        setPassive();
                                    } else if (!var.contains(cste)) {
                                        r.setToFalse(this);
                                        setPassive();
                                    }
                                }
                            }
                        }

                        @Override
                        public ESat isEntailed() {
//                            throw new UnsupportedOperationException("isEntailed not implemented ");
                            return TRUE;
                        }

                        @Override
                        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                            boolean nrules = ruleStore.addPropagatorActivationRule(this);
                            if (var == vars[1]) {
                                if (vars[1].isInstantiatedTo(1)) {
                                    nrules |= ruleStore.addFullDomainRule(vars[0]);
                                } else {
                                    nrules |= ruleStore.addRemovalRule(vars[0], cste);
                                }
                            } else {
                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                            }
                            return nrules;
                        }
                    }).post();
                } else {
                    new Constraint("reif(a=b,r)", new Propagator<IntVar>(new IntVar[]{a, b, r}, TERNARY, false) {
                        @Override
                        public void propagate(int evtmask) throws ContradictionException {
                            if (r.getLB() == 1) {
                                if (vars[0].isInstantiated()) {
                                    setPassive();
                                    vars[1].instantiateTo(vars[0].getValue(), this);
                                } else if (vars[1].isInstantiated()) {
                                    setPassive();
                                    vars[0].instantiateTo(vars[1].getValue(), this);
                                }
                            } else if (r.getUB() == 0) {
                                if (vars[0].isInstantiated()) {
                                    if (vars[1].removeValue(vars[0].getValue(), this)) {
                                        setPassive();
                                    }
                                } else if (vars[1].isInstantiated()) {
                                    if (vars[0].removeValue(vars[1].getValue(), this)) {
                                        setPassive();
                                    }
                                }
                            } else {
                                if (vars[0].isInstantiated()) {
                                    if (vars[1].isInstantiated()) {
                                        if (vars[0].getValue() == vars[1].getValue()) {
                                            r.setToTrue(this);
                                        } else {
                                            r.setToFalse(this);
                                        }
                                        setPassive();
                                    } else {
                                        if (!vars[1].contains(vars[0].getValue())) {
                                            setPassive();
                                            r.setToFalse(this);
                                        }
                                    }
                                } else {
                                    if (vars[1].isInstantiated()) {
                                        if (!vars[0].contains(vars[1].getValue())) {
                                            setPassive();
                                            r.setToFalse(this);
                                        }
                                    } else {
                                        if (vars[0].getLB() > vars[1].getUB()
                                                || vars[1].getLB() > vars[0].getUB()) {
                                            setPassive();
                                            r.setToFalse(this);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public ESat isEntailed() {
                            return TRUE;//throw new UnsupportedOperationException("isEntailed not implemented ");
                        }

                        @Override
                        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                            boolean nrules = ruleStore.addPropagatorActivationRule(this);
                            if (var == vars[2]) {
                                if (vars[2].isInstantiatedTo(1)) {
                                    nrules |= ruleStore.addFullDomainRule(vars[0]);
                                    nrules |= ruleStore.addFullDomainRule(vars[1]);
                                } else {
                                    if (vars[0].isInstantiated()) {
                                        nrules |= ruleStore.addRemovalRule(vars[1], vars[0].getValue());
                                    } else {
                                        nrules |= ruleStore.addFullDomainRule(vars[1]);
                                    }
                                    if (vars[1].isInstantiated()) {
                                        nrules |= ruleStore.addRemovalRule(vars[0], vars[1].getValue());
                                    } else {
                                        nrules |= ruleStore.addFullDomainRule(vars[0]);
                                    }
                                }
                            } else {
                                if (var == vars[0]) {
                                    nrules |= ruleStore.addFullDomainRule(vars[1]);
                                } else if (var == vars[1]) {
                                    nrules |= ruleStore.addFullDomainRule(vars[0]);
                                }
                                nrules |= ruleStore.addFullDomainRule(vars[2]);
                            }
                            return nrules;
                        }
                    }).post();
                }
            } else {
                model.arithm(a, "=", b).reifyWith(r);
            }
        }
    }
}
