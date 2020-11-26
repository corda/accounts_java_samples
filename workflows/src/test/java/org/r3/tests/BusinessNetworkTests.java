package org.r3.tests;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName;
import com.r3.corda.lib.accounts.workflows.flows.CordappVersionDetector;
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.r3.flows.AssetForAccountStateSelfIssueFlow;
import org.r3.flows.NodeAccountToNodeAccountFlows;
import org.r3.flows.ShareAccountToParty;


import java.util.List;

public class BusinessNetworkTests {
    private MockNetwork network;
    private StartedMockNode nodeA;
    private StartedMockNode nodeB;

    @Before
    public void setup() {
        network = new MockNetwork(
                new MockNetworkParameters(
                        ImmutableList.of(
                                TestCordapp.findCordapp("org.r3.contracts"),
                                TestCordapp.findCordapp("org.r3.flows"),
                                TestCordapp.findCordapp("com.r3.corda.lib.ci"),
                                TestCordapp.findCordapp("com.r3.corda.lib.accounts.contracts"),
                                TestCordapp.findCordapp("com.r3.corda.lib.accounts.workflows")
                        )
                ));
        nodeA = network.createPartyNode(null);
        nodeB = network.createPartyNode(null);
        network.runNetwork();
        nodeA.startFlow(new CreateAccount("BobAcct"));
        nodeB.startFlow(new CreateAccount("AliceAcct"));
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }



    /**
     * Get the version of the CorDapp, verify it complies with
     * what we expect
     */
    @Test
    public void getCorDappVersion() throws Exception {
        CordappVersionDetector flow = new CordappVersionDetector();
        CordaFuture<String> future = nodeA.startFlow(flow);
        network.runNetwork();
        String result = future.get();
        System.out.println("Result:"+result);
    }

//    @Test
//    public void createBusinessNetwork() throws Exception {
//
//        CreateBusinessNetworkFlow flow = new CreateBusinessNetworkFlow();
//        CordaFuture<SignedTransaction>  future = nodeA.startFlow(flow);
//        network.runNetwork();
//        SignedTransaction st  = future.get();
//        System.out.println("Result:"+st);
//    }

}