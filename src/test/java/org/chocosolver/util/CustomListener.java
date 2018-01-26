/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 20/08/2014
 */
public class CustomListener extends TestListenerAdapter {
    private int m_count = 0;

    @Override
    public void onTestFailure(ITestResult tr) {
        log(tr, "FAILURE");
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        log(tr, "SKIP");
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        log(tr, "SUCCESS");
    }

    private void log(ITestResult tr, String RESULT) {
        System.out.print(String.format("\t%s.%s %s (%dms)\n", tr.getTestClass().getName(), tr.getName(), RESULT, tr.getEndMillis() - tr.getStartMillis()));
        if (++m_count % 40 == 0) {
            System.out.println("");
        }
    }

}
