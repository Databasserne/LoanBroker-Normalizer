/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.databasserne.loanbroker.normalizer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kasper S. Worm
 */
public class NormalizerTest {

    //Get instance of class to test
    private static Normalizer instance;

    public NormalizerTest() {
    }

    //Getting new instance at start of test class
    @BeforeClass
    public static void setUpClass() {
        instance = new Normalizer();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of messageFormatter method, of class Normalizer. using correct
     * message format
     */
    @Test
    public void testMessageFormatterCorrectFormat() {
        //Bank id
        String id = "BankTEST";
        //Message to test
        String message = "{\"interestRate\":9.350000000000001,\"ssn\":1122334455}";
        //My expected result after i put it in the method
        String expResult = "{\"ssn\":\"112233-4455\",\"interestRate\":9.350000000000001,\"bank\":\"BankTEST\"}";
        //The result of the method
        String result = instance.messageFormatter(id, message);
        //Actual test if the result matches my expected
        assertEquals(expResult, result);
    }

    /**
     * Test of messageFormatter method, of class Normalizer. using wrong message
     * format
     */
    @Test
    public void testMessageFormatterWrongFormat() {
        //Bank id
        String id = "BankTEST";
        //Message to test
        String message = "";
        //My expected result after i put it in the method
        String expResult = "";
        //The result of the method
        String result = instance.messageFormatter(id, message);
        //Actual test if the result matches my expected
        assertEquals(expResult, result);
    }

}
